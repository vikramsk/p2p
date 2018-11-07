package p2p;

import java.util.ArrayList;
import java.util.Iterator;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

// Peer defines a single peer in the
// network and the properties associated
// with it.
public class Peer {

	// id defines the peer ID which is
	// set on initialization. The peer retains
	// and uses this ID while its a part of the
	// p2p network.
	String id;
	
	// prefNeighborLimit defines the limit for the
	// number of concurrent connections on which
	// the peer will upload a message.
	int prefNeighborLimit; 

	// unchokeInterval defines the frequency (in seconds),
	// for a peer to reselect its preferred neighbors.
	int unchokeInterval;
	
	// bitField represents the pieces of the file that the
	// peer has. Each bit represents one piece. The extra
	// bits at the end are padded to zero if required.
	byte[] bitField;

    // connToPeers contains the list of peers it's supposed
	// to establish a connection with on launch.
    ArrayList<PeerInfo> connToPeers;

    // address represents the peer's IP address
    String address;

    // serverPort represents the server port number on which 
    // the peer is listening
    String serverPort;

    P2PLog logger;
    HandshakeMessage handShake;
    DataOutputStream out;

    // clientSockets contains all the sockets/servers of other peers this peer is connected to
    ArrayList<ClientSocket> clientSockets = new ArrayList<ClientSocket>();

    // attacker checks if the received handshake is from a valid peer
    Boolean attacker = true;

	// Peer initializes a peer with the required info.
	public Peer(String id, ArrayList<PeerInfo> connToPeers, String address, String serverPort) {
		this.id = id;
        this.connToPeers = connToPeers;
        this.address = address;
        this.serverPort = serverPort;
		this.prefNeighborLimit = Configs.Common.NumberOfPreferredNeighbors;
		this.unchokeInterval = Configs.Common.UnchokingInterval;
        this.logger = new P2PLog(id);
	}

    // peer establishes TCP connection with all the peers in connToPeers
    // and sends handshake message to them
    public void establishTCPConnection(){
        DataOutputStream out = null;
        DataInputStream in = null;
        Socket socket;

        // Start peer's server in a separate thread to listen for connections
        Server server = new Server(this, serverPort, connToPeers);
        Thread listener = new Thread(server);
        listener.start();

        // peer/client connects to the other servers 
        for(int i = 0; i<connToPeers.size(); i++){
            PeerInfo peerInfo = connToPeers.get(i);
            System.out.println("\n\nTrying to connect to peer " + peerInfo.peerID);
            try{
                socket = new Socket(peerInfo.hostName, Integer.parseInt(peerInfo.port));
                out = new DataOutputStream(socket.getOutputStream());
                out.flush();
                in = new DataInputStream(socket.getInputStream());
                synchronized(clientSockets){
                    clientSockets.add(new ClientSocket(peerInfo.peerID, socket, in, out));
                }
                System.out.println("Connected to peer " + peerInfo.peerID);
                logger.log("Peer " + id + " makes a connection to Peer " + peerInfo.peerID);
                System.out.println("Sending handShake to " + peerInfo.peerID);
                handShake(new HandshakeMessage(id), out);
                System.out.println("handShake sent");
            } catch(IOException e){
                e.printStackTrace();
            }
        }

    }

    // 
    public void handShake(HandshakeMessage handShake, DataOutputStream out) throws IOException{
        out.writeUTF(handShake.getString());
    }

    // Response handshake verifies whether the handshake is from 
    // an authorized peer and sends a response handshake.
    public void reshandShake(String peerId){
        String eachPeerLine;
        String address = null;
        String serverPort = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        Socket socket;
        try{
            BufferedReader buffer = new BufferedReader(new FileReader("p2p/PeerInfo.cfg"));
            while ((eachPeerLine = buffer.readLine()) != null) {
                String[] tokens = eachPeerLine.split("\\s+");
                if(tokens[0].equals(peerId)){
                    address = tokens[1];
                    serverPort = tokens[2];
                    attacker = false;
                    System.out.print("Verified the peer. ");
                    break;
                }
            }
            if(!attacker){
                System.out.println("Sending response handshake.");
                // System.out.println("In response handshake. Trying to connect to peer " + peerId);
                socket = new Socket(address, Integer.parseInt(serverPort));
                out = new DataOutputStream(socket.getOutputStream());
                out.flush();
                in = new DataInputStream(socket.getInputStream());
                synchronized(clientSockets){
                    clientSockets.add(new ClientSocket(peerId, socket, in, out));
                }
                // System.out.println("In response handshake. Connected to peer " + peerId);
                handShake(new HandshakeMessage(id), out);
                System.out.println("Response handshake sent.");
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
