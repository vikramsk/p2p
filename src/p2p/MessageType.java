package p2p;

enum MessageType {
	CHOKE,
	UNCHOKE,
	INTERESTED,
	NOT_INTERESTED,
	HAVE,
	BITFIELD,
	REQUEST,
	PIECE;

	// getType does a reverse lookup on the MessageType.
	public static MessageType getType(int typeIndex) {
		return MessageType.values()[typeIndex];
	}
}
