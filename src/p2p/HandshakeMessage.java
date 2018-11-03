package p2p;

// HandshakeMessage defines the message type
// used for a peer to establish a connection
// with another peer.
class HandshakeMessage {

	// headerString for the HandshakeMessage is constant at 
	// this point.
	static String headerString = "P2PFILESHARINGPROJ";
	
	// header represents the header in the handshake message.
	String header;
	
	// zeroBits represents the zero bits in the handshake message.
	byte[] zeroBits;
	
	// peerID is the String representation of the peerID.
	String peerID;
	
	HandshakeMessage(String peerID) {
		this.header = headerString;
		this.zeroBits = new byte[10];
		this.peerID = peerID;
	}

    String getString(){
        return (headerString + new String(zeroBits) + peerID);
    }
}