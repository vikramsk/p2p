package p2p;

// PayloadMessage defines the message type
// exchanged between the peers when data is
// being transferred between the peers.
class PayloadMessage {

	// messageLenth defines the size of the 
	// payload message.
	int messageLength;

	// messageType defines the type of the payload
	// message.
	MessageType messageType;

	// payload defines the payload data being transferred
	// via the message.
	byte[] payload;

	PayloadMessage(MessageType mType, byte[] payload) {
		this.messageLength = payload.length;
		this.messageType = mType;
		this.payload = payload;
	}
}
