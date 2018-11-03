package p2p;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;

public class PeerProcess {
    String peerId, address, serverPort;
    ArrayList<PeerInfo> connToPeers = new ArrayList<PeerInfo>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("The number of arguments passed to the program is " + args.length + " while it should be 1.\nUsage: java PeerProcess <peerId>");
            return;
        }
        // TODO Auto-generated method stub
        System.out.println("Launching Peer " + args[0]);
        try{
            PeerProcess peerProcess = new PeerProcess();
            peerProcess.run(args);
        } catch(IOException e){
			e.printStackTrace();
        }
	}

    // Run implements peerInfo parser of the file p2p/PeerInfo.cfg
    // and instantiates a Peer and establishes TCP connection
    public void run(String[] args) throws IOException{
        peerId = args[0];
        peerInfoParser();
        Peer peer = new Peer(peerId, connToPeers, address, serverPort);
        peer.establishTCPConnection();
        // peer.handShake();
    }

    // peerInfoParser parses p2p/PeerInfo.cfg and fills the connToPeers list 
    // with the peers before it
    void peerInfoParser() throws IOException{
        String eachPeerLine;
        BufferedReader in = new BufferedReader(new FileReader("p2p/PeerInfo.cfg"));
        while ((eachPeerLine = in.readLine()) != null) {
            String[] tokens = eachPeerLine.split("\\s+");
            if(tokens[0].equals(peerId)){
                address = tokens[1];
                serverPort = tokens[2];
                break;
            }
            connToPeers.add(new PeerInfo(tokens[0], tokens[1], tokens[2]));
        }
    }

}