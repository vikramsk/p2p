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

	public static MessageType getType(byte typeIndex) {
		return MessageType.values()[typeIndex];
	}
}
