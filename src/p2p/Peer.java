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

    // connPeers contains the list of peers it's supposed
	// to establish a connection with on launch.
    ArrayList<PeerInfo> connPeers;

    // address represents the peer's IP address
    String address;

    // serverPort represents the server port number on which 
    // the peer is listening
    String serverPort;

    P2PLog logger;

	// Peer initializes a peer with the required info.
	public Peer(String id, ArrayList<PeerInfo> connPeers, String address, String serverPort) {
		this.id = id;
        this.connPeers = connPeers;
        this.address = address;
        this.serverPort = serverPort;
		this.prefNeighborLimit = Configs.Common.NumberOfPreferredNeighbors;
		this.unchokeInterval = Configs.Common.UnchokingInterval;
        this.logger = new P2PLog(id);
	}

    // peer establishes TCP connection with all the peers
    // above it according to the PeerInfo.cfg file.
    public void establishTCPConnection(){
        Socket clientSocket;
        
        // Start peer's server in a separate thread to listen for connections
        Server server = new Server(serverPort);
        Thread listener = new Thread(server);
        listener.start();

        // peer/client connects to the other servers 
        for(int i = 0; i<connPeers.size(); i++){
            PeerInfo peerInfo = connPeers.get(i);
            System.out.println("Trying to connect to peer " + peerInfo.peerID);
            try{
                clientSocket = new Socket(peerInfo.hostName, Integer.parseInt(peerInfo.port));
                System.out.println("Connected to peer " + peerInfo.peerID);
                logger.log("Peer " + id + " makes a connection to Peer " + peerInfo.peerID);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
