package p2p;

// PeerInterface defines the behavior that needs
// to be exposed/performed by a Peer in the p2p 
// network.
public interface PeerInterface {
	// SendMessage sends the payload message to the peer.
	void SendMessage(Peer peer, PayloadMessage msg);
}
