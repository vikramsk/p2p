package p2p;

class Message {
	
	// The type of the message.
	MessageType type;
	
	// The contents of the message. 
	byte[] body;
	
	Message(MessageType mType, byte[] mBody) {
		type = mType;
		body = mBody;
	}
}
