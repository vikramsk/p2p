package p2p;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;

public class PeerProcess {
    String peerId;
    ArrayList<PeerInfo> connPeers = new ArrayList<PeerInfo>();
    
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

    public void run(String[] args) throws IOException{
        peerId = args[0];
        String eachPeerLine;
        // Fill the connPeers list with the peers its supposed to establish a connection with instantiate the Peer
        BufferedReader in = new BufferedReader(new FileReader("p2p/PeerInfo.cfg"));
        while ((eachPeerLine = in.readLine()) != null) {
            String[] tokens = eachPeerLine.split("\\s+");
            if(tokens[0].equals(peerId)) break;
            connPeers.add(new PeerInfo(tokens[0], tokens[1], tokens[2]));
        }
        Peer peer = new Peer(peerId, connPeers);

    }

}