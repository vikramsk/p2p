package p2p;

import java.util.ArrayList;

// Peer defines a single peer in the
// network and the properties associated
// with it.
public class Peer {

	// id defines the peer ID which is
	// set on initialization. The peer retains
	// and uses this ID while its a part of the
	// p2p network.
	String id;
	
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
	
	// Peer initializes a peer with the required info.
	// connPeers contains the list of peers it's supposed
	// to establish a connection with on launch.
	public Peer(String id, ArrayList<PeerInfo> connPeers) {
		this.id = id;
		this.prefNeighborLimit = Configs.Common.NumberOfPreferredNeighbors;
		this.unchokeInterval = Configs.Common.UnchokingInterval;
	}
}
