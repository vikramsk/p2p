package p2p;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

// Peer defines a single peer in the
// network and the properties associated
// with it.
public class Peer {

	// info defines the information for the peer.
	private PeerInfo info;

	// prefNeighborLimit defines the limit for the
	// number of concurrent connections on which
	// the peer will upload a message.
	int prefNeighborLimit; 

	// unchokeInterval defines the frequency (in seconds),
	// for a peer to reselect its preferred neighbors.
	int unchokeInterval;

	// bitFieldLock protect the bitFieldSet.
	private final ReadWriteLock bitFieldLock = new ReentrantReadWriteLock();

	// bitFieldSet tracks 
	BitSet bitFieldSet;

	// neighborBitFields defines the mapping between a peer
	// and its bitfield status.
	ConcurrentHashMap<String, BitSet> neighborBitFields;

	// preferredNeighbors stores the list of preferred
	// neighbors for a given peer at a given point in time.
	ArrayList<Client> preferredNeighbors;

	// optimisticallyUnchokedNeighbor defines the peer
	// that is optimistically unchoked at a given point in time.
	Client optimisticallyUnchokedNeighbor;

	// totalPieces defines the total number of pieces
	// of the file that's being transferred.
	private int totalPieces;

	// Logger defines the peer logger.
	private Logger logger;

	// neighbors contains all the sockets of other peers this peer is connected to
	private ConcurrentHashMap<String, Client> neighbors;

	// server listens for incoming connection requests.
	private Thread server;

	// fileHandler holds the file handler responsible
	// for managing operations on the file.
	private FileHandler fileHandler;

	// taskTimer is used to run the timed tasks such as 
	// the preferred neighbor calculation, optimistically
	// unchoked neighbor calculation and the graceful
	// termination checker.
	Timer taskTimer;

	// Peer initializes a peer with the required info.
	public Peer(PeerInfo peerInfo) {
		info = peerInfo;
		prefNeighborLimit = Configs.Common.NumberOfPreferredNeighbors;
		unchokeInterval = Configs.Common.UnchokingInterval;
		logger = P2PLog.GetLogger(peerInfo.peerID);
		neighbors = new ConcurrentHashMap<String, Client>();
		neighborBitFields = new ConcurrentHashMap<String, BitSet>();
		preferredNeighbors = new ArrayList<Client>();

		totalPieces = (int)Math.ceil((double)Configs.Common.FileSize / Configs.Common.PieceSize);
		bitFieldSet = new BitSet(totalPieces);
		if (peerInfo.hasFile) {
			bitFieldSet.set(0, totalPieces);
		}

		fileHandler = new FileHandler(Configs.Common.FileName, peerInfo.hasFile, Configs.Common.PieceSize);
	}

	// getPort returns the port number on which the peer
	// is running.
	public Integer getPort() {
		return info.port;
	}

	// getID returns the ID of the peer.
	public String getID() {
		return info.peerID;
	}

	// hasFile returns true if the peer has the file.
	public boolean hasFile() {
		return info.hasFile;
	}

	// addClient adds the client to the list of neighbors maintained by the peer.
	public void addClient(Client client) {
		neighborBitFields.put(client.getID(), new BitSet(bitFieldSet.length()));
		neighbors.put(client.getID(), client);
	}

	// start does the following:
	// 1. performs a handshake with the list of input peers.
	// 2. starts a server to listen for incoming connections.
	public void start(ArrayList<PeerInfo> peers) {
		for(int i = 0; i< peers.size(); i++) {
			Client client = new Client(this, peers.get(i), logger);
			Thread clientThread = new Thread(client);
			clientThread.start();
		}
		server = new Server(this, logger);
		server.start();

		taskTimer = new Timer(true);
		taskTimer.schedule(completionCheckTask(), 10000, 5000);
		taskTimer.schedule(preferredNeighborTask(), 0, Configs.Common.UnchokingInterval * 1000);
		taskTimer.schedule(optimisticallyUnchokedNeighborTask(), 0, Configs.Common.OptimisticUnchokingInterval * 1000);

	}

	// completionCheckTask runs the completion checker to 
	// determine if all the neighbors have received the file.
	// If this condition is met, the peer shuts itself down.
	private TimerTask completionCheckTask() {
		return new TimerTask() {
			public void run() {
				ArrayList<Client> completedPeers = getCompletedPeers();
				if (completedPeers.size() == neighbors.size()) {
					shutdown();
					taskTimer.cancel();
				}
			}
		};
	}

	// preferredNeighborTask is used to recalculate the 
	// set of preferred neighbors for the peer.
	private TimerTask preferredNeighborTask() {
		return new TimerTask() {
			public void run() {
				if (neighbors.size() > 0) {
					determinePreferredNeighbors();
				}
			}
		};
	}

	// optimisticallyUnchokedNeighborTask is used to recalculate
	// the optimistically unchoked neighbor for the peer.
	private TimerTask optimisticallyUnchokedNeighborTask() {
		return new TimerTask() {
			public void run() {
				if (neighbors.size() > 0) {
					determineOptimisticallyUnchokedNeighbor();
				}
			}
		};
	}

	// determinePreferredNeighbors calculates and sets the
	// preferred neighbors based on the download rate from 
	// the peers.
	private void determinePreferredNeighbors() {
		// get eligible peers by removing the completed peers from
		// the list of all peers.
		ArrayList<Client> eligiblePeers = getPeersEligibleForUpload();
		if (optimisticallyUnchokedNeighbor != null) {
			eligiblePeers.remove(optimisticallyUnchokedNeighbor);
		}

		if (eligiblePeers.isEmpty())
			return;
		
		// sort the peers in decreasing order of the download rate.
		Collections.sort(eligiblePeers, new DownloadRateComparator());

		// pick the first k elements from the list where k
		// is the number of preferred neighbors.
		ArrayList<Client> newPreferredClients = 
				new ArrayList<Client>(eligiblePeers.subList(0, 
						Math.min(eligiblePeers.size(), Configs.Common.NumberOfPreferredNeighbors)));
		
		// unchoke all neighbors that have been picked in this round
		// and aren't currently choked.
		for (int i = 0; i < newPreferredClients.size(); i++) {
			Client pc = newPreferredClients.get(i);
			if (!preferredNeighbors.contains(pc) && pc != optimisticallyUnchokedNeighbor) {
				newPreferredClients.get(i).unchokeNeighbor();
			}
		}

		// choke all neighbors that are not in the new list but are unchoked.
		for (int i = 0; i < preferredNeighbors.size(); i++) {
			if (!newPreferredClients.contains(preferredNeighbors.get(i))) {
				preferredNeighbors.get(i).chokeNeighbor();
			}
		}

		// update preferred neighbors.
		preferredNeighbors = newPreferredClients;
		logger.info("Peer " + getID() + " has the preferred neighbors " + getIDList(preferredNeighbors));
	}

	private String getIDList(ArrayList<Client> clients) {
		String result = clients.get(0).getID();
		for (int i = 1; i < clients.size(); i++) {
			result += ", " + clients.get(i).getID();
		}
		return result;
	}
	
	// determineOptimisticallyUnchokedNeighbor calculates and sets the
	// optimistically unchoked neighbor for the peer via random
	// selection.
	private void determineOptimisticallyUnchokedNeighbor() {
		ArrayList<Client> eligiblePeers = getPeersEligibleForUpload();
		eligiblePeers.removeAll(preferredNeighbors);
		eligiblePeers.remove(optimisticallyUnchokedNeighbor);

		if (eligiblePeers.size() == 0) {
			return;
		}

		if (optimisticallyUnchokedNeighbor != null) {
			optimisticallyUnchokedNeighbor.chokeNeighbor();
		}

		Random rand = new Random();
		optimisticallyUnchokedNeighbor = eligiblePeers.get(rand.nextInt(eligiblePeers.size()));
		optimisticallyUnchokedNeighbor.unchokeNeighbor();
		
		logger.info("Peer " + getID() + " has the optimistically unchoked neighbor " + optimisticallyUnchokedNeighbor.getID());
	}

	// getPeersEligibleForUpload returns the list of peers that
	// are eligible for receiving a piece.
	private ArrayList<Client> getPeersEligibleForUpload() {
		ArrayList<Client> completedPeers = getCompletedPeers();
		ArrayList<Client> candidatePeers = new ArrayList<Client>(neighbors.values());
		candidatePeers.removeAll(completedPeers);
		return candidatePeers;
	}

	// getCompletedPeers returns list of peers that have
	// completed the file download in the p2p network.
	private ArrayList<Client> getCompletedPeers() {
		ArrayList<Client> completedPeers = new ArrayList<Client>();
		Iterator<Entry<String, BitSet>> neighborIterator = neighborBitFields.entrySet().iterator();
		while (neighborIterator.hasNext()) {
			Map.Entry<String, BitSet> neighbor = (Map.Entry<String, BitSet>)neighborIterator.next();
			if (neighbor.getValue().nextClearBit(0) < 0 || neighbor.getValue().nextClearBit(0) >= totalPieces) {
				completedPeers.add(neighbors.get(neighbor.getKey()));
			}
		}
		return completedPeers;
	}

	// updateNeighborBitField updates the bitfield info for a neighboring peer.
	// This operation is thread safe.
	public void updateNeighborPieceIndex(String peerID, Integer pieceIndex) {
		BitSet nBitSet = neighborBitFields.get(peerID);
		nBitSet.set(pieceIndex);
		neighborBitFields.put(peerID, nBitSet);
	}

	// setNeighborBitField sets the bitField for the given peer id in
	// the neighbor bit field map.
	public void setNeighborBitField(String peerID, byte[] bitField) {
		BitSet bitSet = getBitSet(bitField);
		neighborBitFields.put(peerID, bitSet);
	}

	// hasPiece returns true if the peer has a given piece.
	public boolean hasPiece(Integer pieceIndex) {
		bitFieldLock.readLock().lock();
		try {
			return bitFieldSet.get(pieceIndex);
		} finally {
			bitFieldLock.readLock().unlock();
		}
	}

	// getBitField returns the bit field for the peer.
	public byte[] getBitField() {
		bitFieldLock.readLock().lock();
		byte[] bytes = new byte[(bitFieldSet.size() + 7) / 8];
		try {
			for (int i = 0; i<bitFieldSet.size(); i++) {
				if (bitFieldSet.get(i)) {
					bytes[bytes.length-i/8-1] |= 1<<(i%8);
				}
			}
		} finally {
			bitFieldLock.readLock().unlock();
		}	
		return bytes;
	}

	// Returns a bitset containing the values in bytes.
	private BitSet getBitSet(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	// getPieceRequestIndex returns the ID of the piece that
	// needs to be requested from a given peer.
	public int getPieceRequestIndex(String peerID) {
		bitFieldLock.readLock().lock();
		try {
			for (int i = 0; i < totalPieces; i++) {
				if (!bitFieldSet.get(i) && neighborBitFields.get(peerID).get(i))
					return i;
			}
		} finally {
			bitFieldLock.readLock().unlock();
		}

		return -1;
	}

	// addPiece saves a given piece for the local peer.
	public void addPiece(Integer pieceIndex, byte[] data) throws IOException {
		bitFieldLock.writeLock().lock();
		try {
			if (!bitFieldSet.get(pieceIndex)) {
				fileHandler.addPiece(pieceIndex, data);
				bitFieldSet.set(pieceIndex);
				if (bitFieldSet.nextClearBit(0) >= totalPieces) {
					logger.info("Peer " + getID() + " has downloaded the complete file.");
				}
			}
		} finally {
			bitFieldLock.writeLock().unlock();
		}

		Iterator<Entry<String, Client>> neighborIterator = neighbors.entrySet().iterator();
		while(neighborIterator.hasNext()) {
			Map.Entry<String, Client> pair = (Map.Entry<String, Client>)neighborIterator.next();
			pair.getValue().sendHave(pieceIndex);
		}
	}

	// getPiece returns the requested piece from the local peer.
	public byte[] getPiece(Integer pieceIndex) throws IOException {
		byte[] piece = null;
		bitFieldLock.readLock().lock();
		try {
			if (bitFieldSet.get(pieceIndex)) {
				piece = fileHandler.getPiece(pieceIndex);
			}
		} finally {
			bitFieldLock.readLock().unlock();
		}
		return piece;
	}

	// shutdown shuts the peer down gracefully.
	private void shutdown() {
		Iterator<Entry<String, Client>> neighborIterator = neighbors.entrySet().iterator();
		while(neighborIterator.hasNext()) {
			Map.Entry<String, Client> pair = (Map.Entry<String, Client>)neighborIterator.next();
			pair.getValue().shutdown();
		}
		System.exit(0);
	}

	public void addInterestedPeer(String peerID) {
	}
}

// DownloadRateComparator sorts the clients in descending
// order of their download rates.
class DownloadRateComparator implements Comparator<Client> {
	@Override
	public int compare(Client c1, Client c2) {
		if (c1.getDownloadRate() < c2.getDownloadRate()) {
			return 1;
		} else if (c1.getDownloadRate() == c2.getDownloadRate()) {
			return 0;
		} 
		return -1;
	}

}