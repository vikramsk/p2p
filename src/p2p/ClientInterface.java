package p2p;

import java.io.IOException;

interface ClientInterface {

	// performHandshake is the call made from one 
	// peer to another in order to establish a 
	// persistent connection for the transfer.
	void performHandshake();

	// sendBitField is used to notify the neighboring
	// peer of its bitfield.
	void sendBitField() throws IOException;
}
