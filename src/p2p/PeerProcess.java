package p2p;
import java.util.ArrayList;
import java.io.IOException;

public class PeerProcess {

	public static void main(String[] args) {
		if (args.length > 2) {
			System.out.println("The number of arguments passed to the program is " + args.length 
					+ " while it should be 1.\nUsage: java PeerProcess <peerId>");
			return;
		}
		
		new Configs.Common(Configs.Constants.CommonConfigPath);
		
		// option to override the default filename.
		if (args.length == 2) {
			Configs.Common.FileName = args[1];
		}
		
		System.out.println("Launching Peer " + args[0]);
		try{
			run(args[0]);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	public static void run(String peerID) throws IOException{
		ArrayList<PeerInfo> neighborPeers = new ArrayList<PeerInfo>();
		PeerInfo peerInfo = Configs.parsePeerInfos(neighborPeers, Configs.Constants.PeerInfoConfigPath, peerID);
		if (peerInfo == null) {
			throw new IOException("could not parse peer info");
		}
		Peer localPeer = new Peer(peerInfo);
		localPeer.start(neighborPeers);
	}
}