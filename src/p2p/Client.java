package p2p;

import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.DataInputStream;

// ClientSocket defines a single socket connection a client uses to connect 
// to the server, the output and input streams associated with it
class Client implements Runnable,ClientInterface {

	// localPeer defines the peer on which the client is running.
	Peer localPeer;

	// neighbor defines the peer info for the neighbor.
	PeerInfo neighbor;

	// handshakeReceived is set once the handshake is received
	// by the peer.
	boolean handshakeReceived;

	// isChoked is set if the neighbor has been choked
	// by the peer.
	boolean isChoked;

	// socket defines the socket used to connect to the neighbor.
	Socket socket;

	// outStream defines the output stream for the socket.
	DataOutputStream outStream;

	LinkedBlockingQueue<Message> writeMessageQueue;

	// inStream defines the input stream for the socket.
	DataInputStream inStream;

	// Logger defines the client logger.
	Logger logger;

	public Client(Peer peer, PeerInfo neighborPeerInfo, Logger p2pLogger){
		localPeer = peer;
		neighbor = neighborPeerInfo;
		logger = p2pLogger;
		handshakeReceived = false;
		isChoked = false;
		writeMessageQueue = new LinkedBlockingQueue<Message>();
		try {
			socket = new Socket(neighborPeerInfo.hostName, neighborPeerInfo.port);
			inStream = new DataInputStream(socket.getInputStream());
			outStream = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Client(Peer peer, PeerInfo neighborPeerInfo, Logger p2pLogger, Socket sock, DataInputStream in, DataOutputStream out) {
		localPeer = peer;
		neighbor = neighborPeerInfo;
		logger = p2pLogger;
		socket = sock;
		writeMessageQueue = new LinkedBlockingQueue<Message>();
		handshakeReceived = true;
		isChoked = false;
		inStream = in;
		outStream = out;
	}

	@Override
	public void run() {
		try {
			performHandshake();
			sendBitField();

			// spawn new thread to write messages.
			new Thread(new Runnable() {
				@Override
				public void run() {
					writeMessages();
				}
			}).start();
			
			while(true) {
				handleActualMessage();
			}

		} catch (EOFException e) {
			writeMessageQueue.add(new Message(null, null));
			System.out.println("Terminating connection with Peer " + neighbor.peerID);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// writeMessages is responsible for writing data
	// on the output stream by polling the 
	// writeMessageQueue for new messages and acting on it.
	private void writeMessages() {
		while(true) {
			try {
				Message msg = writeMessageQueue.take();
				if (msg.type == null)
					return;
				
				sendActualMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// sendActualMessage writes the message on the output stream.
	private void sendActualMessage(Message msg) throws IOException {
		int totalLength = 1;
		if (msg.body != null) {
			totalLength += msg.body.length;
		}

		ByteBuffer buffer = ByteBuffer.allocate(totalLength+4);
		buffer.putInt(totalLength);
		buffer.put((byte) msg.type.ordinal());
		if (msg.body != null) {
			buffer.put(msg.body);
		}

		outStream.write(buffer.array());
	}

	// sendHave sends a 'HAVE' message to the neighbor.
	public void sendHave(int pieceIndex)  {
		ByteBuffer bb = ByteBuffer.allocate(4); 
		bb.putInt(pieceIndex);
		writeMessageQueue.add(new Message(MessageType.HAVE, bb.array()));
	}

	// handleActualMessage reads the input stream for a message and
	// allows for the specific message type handler to act on it.
	private void handleActualMessage() throws IOException {
		Integer mLength = inStream.readInt();
		MessageType mType = MessageType.getType(Byte.toUnsignedInt(inStream.readByte()));
		switch (mType) {
		case CHOKE:
			// Do nothing
			break;
		case UNCHOKE:
			handleUnchoke();
			break;
		case INTERESTED:
			handleInterested();
			break;
		case NOT_INTERESTED:
			handleNotInterested();
			break;
		case HAVE:
			handleHave();
			break;
		case BITFIELD:
			handleBitField(mLength - 1);
			break;
		case REQUEST:
			handleRequest();
			break;
		case PIECE:
			handlePiece(mLength - 1);
			break;
		}
	}

	// handleUnchoke acts on the UNCHOKE message.
	private void handleUnchoke() throws IOException {
		requestPiece();
	}

	// handleInterested acts on the INTERESTED message.
	private void handleInterested() {
		logger.info("Peer " + localPeer.getID() + " received the `INTERESTED` message from Peer " + neighbor.peerID);
		localPeer.addInterestedPeer(neighbor.peerID);
	}

	// handleNotInterested acts on the NOT INTERESTED message.
	private void handleNotInterested() {
		logger.info("Peer " + localPeer.getID() + " received the `NOT INTERESTED` message from Peer " + neighbor.peerID);
	}

	// handleHave acts on the HAVE message.
	private void handleHave() throws IOException {
		logger.info("Peer " + localPeer.getID() + " received the `HAVE` message from Peer " + neighbor.peerID);
		Integer pieceIndex = inStream.readInt();
		localPeer.updateNeighborPieceIndex(neighbor.peerID, pieceIndex);
		if (!localPeer.hasPiece(pieceIndex)) {
			writeMessageQueue.add(new Message(MessageType.INTERESTED, null));
		}
	}

	// handleBitField acts on the BITFIELD message.
	private void handleBitField(int mLength) throws IOException {
		byte[] mBitField = new byte[mLength]; 
		inStream.readFully(mBitField);
		localPeer.setNeighborBitField(neighbor.peerID, mBitField);
		if (localPeer.getPieceRequestIndex(neighbor.peerID) != -1) {
			writeMessageQueue.add(new Message(MessageType.INTERESTED, null));
			requestPiece();
		}
	}

	// handleRequest acts on the REQUEST message.
	private void handleRequest() throws IOException {
		Integer pieceIndex = inStream.readInt();
		// drop message if the neighbor is choked
		// at the moment.
		if (isChoked) {
			return;
		}
		System.out.println("Received Request for Piece Index: " + pieceIndex);
		byte[] piece = localPeer.getPiece(pieceIndex);
		if (piece != null) {
			ByteBuffer bb = ByteBuffer.allocate(4 + piece.length); 
			bb.putInt(pieceIndex);
			bb.put(piece);
			writeMessageQueue.add(new Message(MessageType.PIECE, bb.array()));
		}
	}

	// handlePiece acts on the PIECE message.
	private void handlePiece(int mLength) throws IOException {
		Integer pieceIndex = inStream.readInt();
		byte[] data = new byte[mLength-4];
		inStream.readFully(data);
		System.out.println("Received Piece with piece index: " + pieceIndex);
		localPeer.addPiece(pieceIndex, data);
		logger.info("Peer " + localPeer.getID() + " has downloaded the piece " + pieceIndex +" from Peer " + neighbor.peerID);
		requestPiece();
	}

	// requestPiece sends a request to the neighbor for a piece
	// that it's missing and the neighbor has.
	private void requestPiece() throws IOException {
		int pieceIndex = localPeer.getPieceRequestIndex(neighbor.peerID);
		System.out.println("Sending Request for Piece Index: " + pieceIndex);
		if (pieceIndex == -1) {
			return;
		}
		ByteBuffer bb = ByteBuffer.allocate(4); 
		bb.putInt(pieceIndex);
		writeMessageQueue.add(new Message(MessageType.REQUEST, bb.array()));
	}

	// performHandshake performs a handshake with a
	// neighbor.
	public void performHandshake() {
		sendHandshake();
		
		if (!handshakeReceived) {
			receiveHandshake();
		}	
	}

	// sendHandshake sends the handshake request to a neighbor.
	private void sendHandshake() {
		try {
			outStream.writeUTF(new HandshakeMessage(localPeer.getID()).getString());
			logger.info("Peer " + localPeer.getID() + " makes a connection to Peer " + neighbor.peerID);
		} catch (IOException e) {
			logger.info(e.toString());
		}
	}

	// receiveHandshake receives and handles the handshake
	// request from the neighbor.
	private void receiveHandshake() {
		String neighborPeerID = "";
		while(!handshakeReceived) {
			try {
				String message = (String) inStream.readUTF();
				neighborPeerID = message.substring(28, 32);

				if (message.equalsIgnoreCase(new HandshakeMessage(neighborPeerID).getString())) {
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("Peer " + localPeer.getID() + " is connected from Peer " + neighborPeerID);
		handshakeReceived = true;
		localPeer.addClient(this);
	}

	// sendBitField sends the bitfield of the local peer
	// to the neighbor.
	public void sendBitField() throws IOException {
		if (localPeer.hasFile()) {
			sendActualMessage(new Message(MessageType.BITFIELD, localPeer.getBitField()));
		}
	}
}