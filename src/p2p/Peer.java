package p2p;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

	// bitField represents the pieces of the file that the
	// peer has. Each bit represents one piece. The extra
	// bits at the end are padded to zero if required.
	byte[] bitField;

	// Logger defines the peer logger.
	private Logger logger;

	// neighbors contains all the sockets of other peers this peer is connected to
	private List<Client> neighbors;

	// server listens for incoming connection requests.
	private Thread server;

	// Peer initializes a peer with the required info.
	public Peer(PeerInfo peerInfo) {
		info = peerInfo;
		prefNeighborLimit = Configs.Common.NumberOfPreferredNeighbors;
		unchokeInterval = Configs.Common.UnchokingInterval;
		logger = P2PLog.GetLogger(peerInfo.peerID);
		neighbors = Collections.synchronizedList(new ArrayList<Client>());
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
}
