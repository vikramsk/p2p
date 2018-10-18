package p2p;

// PeerInfo holds the information needed
// to establish a connection with a peer.
class PeerInfo {
	
	// PeerID defines the ID of the peer.
	String PeerID;
	
	// Hostname is the name of the host on 
	// which the peer is running.
	String Hostname;
	
	// Port defines the port number on which
	// the peer is listening/accepting connections.
	String Port;

    public PeerInfo(String PeerID, String Hostname, String Port){
        this.PeerID = PeerID;
        this.Hostname = Hostname;
        this.Port = Port;
    }
}