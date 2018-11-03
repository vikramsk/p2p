package p2p;

import java.util.ArrayList;
import java.util.Iterator;
import java.net.Socket;
import java.io.IOException;

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

    // Initialize output stream to write to the socket
    DataOutputStream out = null;

    // Initialize input stream to read from the socket
    DataInputStream in = null;

    // 
    ArrayList<Socket> clientSocket = new ArrayList<Socket>;
    // Socket[] clientSocketForward;

    Boolean attacker = true;

	// Peer initializes a peer with the required info.
	public Peer(String id, ArrayList<PeerInfo> connToPeers, String address, String serverPort) {
		this.id = id;
        this.connToPeers = connToPeers;
        this.address = address;
        this.serverPort = serverPort;
		this.prefNeighborLimit = Configs.Common.NumberOfPreferredNeighbors;
		this.unchokeInterval = Configs.Common.UnchokingInterval;
	}

    // peer establishes TCP connection with all the peers in connToPeers
    public void establishTCPConnection(){
        
        // Start peer's server in a separate thread to listen for connections
        Server server = new Server(this, serverPort, connToPeers);
        Thread listener = new Thread(server);
        listener.start();

        // peer/client connects to the other servers 
        for(int i = 0; i<connToPeers.size(); i++){
            PeerInfo peerInfo = connToPeers.get(i);
            System.out.println("Trying to connect to peer " + peerInfo.peerID);
            try{
                synchronized(clientSocket){
                    clientSocket.add(new Socket(peerInfo.hostName, Integer.parseInt(peerInfo.port)));
                }
                System.out.println("Connected to peer " + peerInfo.peerID);
                out = new DataOutputStream(clientSocket.get(i).getOutputStream());
                out.flush();
                intput = new DataInputStream(clientSocket.get(i).getInputStream());
                handShake(new HandShakeMessage(id));
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        // Check whether this peer has the file or not

    }

    // 
    public void handShake(HandShakeMessage handShake){
        out.writeObject(handShake.getString);
    }

    // Response handshake sent by the client to the peer which sent it the handshake
    public void reshandShake(String peerId){
        String eachPeerLine, address, serverPort;
        try{
            BufferedReader in = new BufferedReader(new FileReader("p2p/PeerInfo.cfg"));
            while ((eachPeerLine = in.readLine()) != null) {
                String[] tokens = eachPeerLine.split("\\s+");
                if(tokens[0].equals(peerId)){
                    address = tokens[1];
                    serverPort = tokens[2];
                    attacker = false;
                    break;
                }
            }
            if(!attacker){
                System.out.println("Trying to connect to peer " + peerId);
                synchronized(clientSocket){
                    clientSocket.add(new Socket(address, Integer.parseInt(serverPort));
                }
                System.out.println("Connected to peer " + peerInfo.peerID);
                out = new DataOutputStream(socket.getOutputStream());
                out.flush();
                intput = new DataInputStream(socket.getInputStream());
                handShake(new HandShakeMessage(id));
            }
        } catch(IOException e){
                e.printStackTrace();
        }
    }
}
