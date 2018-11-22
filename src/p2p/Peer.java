package p2p;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
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

	// Logger defines the peer logger.
	private Logger logger;

	// neighbors contains all the sockets of other peers this peer is connected to
	private List<Client> neighbors;

	// server listens for incoming connection requests.
	private Thread server;

	// fileHandler holds the file handler responsible
	// for managing operations on the file.
	private FileHandler fileHandler;

	// Peer initializes a peer with the required info.
	public Peer(PeerInfo peerInfo) {
		info = peerInfo;
		prefNeighborLimit = Configs.Common.NumberOfPreferredNeighbors;
		unchokeInterval = Configs.Common.UnchokingInterval;
		logger = P2PLog.GetLogger(peerInfo.peerID);
		neighbors = Collections.synchronizedList(new ArrayList<Client>());
		neighborBitFields = new ConcurrentHashMap<String, BitSet>();
		
		int totalPieces = (int)Math.ceil((double)Configs.Common.FileSize / Configs.Common.PieceSize);
		bitFieldSet = new BitSet(totalPieces);
		if (peerInfo.hasFile) {
			bitFieldSet.set(0, totalPieces-1);
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
		neighbors.add(client);
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
	}

	// updateNeighborBitField updates the bitfield info for a neighboring peer.
	// This operation is thread safe.
	public void updateNeighborPieceIndex(String peerID, Integer pieceIndex) {
		BitSet nBitSet = neighborBitFields.get(peerID);
		if (nBitSet == null) {
			nBitSet = new BitSet(bitFieldSet.size());
		}
		nBitSet.set(pieceIndex);
		neighborBitFields.put(peerID, nBitSet);
	}

	// setNeighborBitField sets the bitField for the given peer id in
	// the neighbor bit field map.
	public void setNeighborBitField(String peerID, byte[] bitField) {
		BitSet bitSet = getBitSet(bitField);
		neighborBitFields.put(peerID, bitSet);
		System.out.println(bitSet);
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
			for (int i=0; i<bitFieldSet.size(); i++) {
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

	public int getPieceRequestIndex(String peerID) {
		bitFieldLock.readLock().lock();
		try {
			for (int i=0; i < bitFieldSet.size(); i++) {
				if (!bitFieldSet.get(i) && neighborBitFields.get(peerID).get(i))
					return i;
			}
		} finally {
			bitFieldLock.readLock().unlock();
		}

		return -1;
	}

	public void addPiece(Integer pieceIndex, byte[] data) throws IOException {
		bitFieldLock.writeLock().lock();
		try {
			if (!bitFieldSet.get(pieceIndex)) {
				fileHandler.addPiece(pieceIndex, data);
				bitFieldSet.set(pieceIndex);
			}
		} finally {
			bitFieldLock.writeLock().unlock();
		}

		for (int i = 0; i < neighbors.size(); i++) {
			neighbors.get(i).sendHave(pieceIndex);
		}
	}

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

	public void addInterestedPeer(String peerID) {

	}
}
