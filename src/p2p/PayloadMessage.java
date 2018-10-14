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

// MessageType defines the different kinds of
// payloads.
enum MessageType {
	CHOKE("choke", 0),
	UNCHOKE("unchoke", 1),
	INTERESTED("interested", 2),
	NOT_INTERESTED("notInterested", 3),
	HAVE("have", 4),
	BITFIELD("bitField", 5),
	REQUEST("request", 6),
	PIECE("piece", 7);
	
	private final String type;
	private final byte value;
	
	private MessageType(String type, int val) {
		this.type = type;
		this.value = (byte)val;
	}

	public String getType() {
		return type;
	}

	public byte getValue() {
		return value;
	}
}
