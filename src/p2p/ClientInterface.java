package p2p;

interface ClientInterface {

	// performHandshake is the call made from one 
	// peer to another in order to establish a 
	// persistent connection for the transfer.
	void performHandshake();


	void sendBitField();
}
