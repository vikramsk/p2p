package p2p;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInputStream;

// ClientSocket defines a single socket connection a client uses to connect 
// to the server, the output and input streams associated with it
class Client implements Runnable,ClientInterface {

	// localPeer defines the peer on which the client is running.
	Peer localPeer;

	// neighbor defines the peer info for the neighbor.
	PeerInfo neighbor;

	// handshakeComplete is set once the handshake is complete.
	boolean handshakeComplete;

	// socket defines the socket used to connect to the neighbor.
	Socket socket;

	// outStream defines the output stream for the socket.
	DataOutputStream outStream;

	// inStream defines the input stream for the socket.
	DataInputStream inStream;

	// Logger defines the client logger.
	Logger logger;

	public Client(Peer peer, PeerInfo neighborPeerInfo, Logger p2pLogger){
		localPeer = peer;
		neighbor = neighborPeerInfo;
		logger = p2pLogger;
		handshakeComplete = false;
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
		handshakeComplete = false;
		inStream = in;
		outStream = out;
	}

	@Override
	public void run() {
		performHandshake();
		sendBitField();
		while(true) {
			handleActualMessage();
		}
	}

	private void handleActualMessage() {
		try {
			Integer mLength = inStream.readInt();
			MessageType mType = MessageType.getType(inStream.readByte());
			switch (mType) {
			case CHOKE:
				//handleChoke();
				break;
			case UNCHOKE:
				break;
			case INTERESTED:
				break;
			case NOT_INTERESTED:
				break;
			case HAVE:
				break;
			case BITFIELD:
				break;
			case REQUEST:
				break;
			case PIECE:
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void performHandshake() {
		sendHandshake();
		receiveHandshake();
	}

	private void sendHandshake() {
		try {
			outStream.writeUTF(new HandshakeMessage(localPeer.getID()).getString());
			logger.info("Peer " + localPeer.getID() + " makes a connection to Peer " + neighbor.peerID);
			System.out.println("Peer " + localPeer.getID() + " makes a connection to Peer " + neighbor.peerID);
		} catch (IOException e) {
			logger.info(e.toString());
		}
	}

	private void receiveHandshake() {
		String neighborPeerID = "";
		while(!handshakeComplete) {
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
		System.out.println("Peer " + localPeer.getID() + " is connected from Peer " + neighborPeerID);
		handshakeComplete = true;
		localPeer.addClient(this);
	}

	@Override
	public void sendBitField() {

	}

}