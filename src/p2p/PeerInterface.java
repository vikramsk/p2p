package p2p;

// PeerInterface defines the behavior that needs
// to be exposed/performed by a Peer in the p2p 
// network.
public interface PeerInterface {
	
	// PerformHandshake is the call made from one 
	// peer to another in order to establish a 
	// persistent connection for the transfer.
	void PerformHandshake(Peer peer, HandshakeMessage msg);
	
	// SendMessage sends the payload message to the peer.
	void SendMessage(Peer peer, PayloadMessage msg);
}
