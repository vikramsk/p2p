package p2p;

// PeerInfo holds the information needed
// to establish a connection with a peer.
class PeerInfo {
	
	// PeerID defines the ID of the peer.
	String peerID;
	
	// Hostname is the name of the host on 
	// which the peer is running.
	String hostName;
	
	// Port defines the port number on which
	// the peer is listening/accepting connections.
	String port;

    // PeerInfo intializes the peerInfo with required peerInfo
    public PeerInfo(String PeerID, String Hostname, String Port){
        this.peerID = PeerID;
        this.hostName = Hostname;
        this.port = Port;
    }
}