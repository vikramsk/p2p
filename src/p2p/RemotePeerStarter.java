package p2p;

import java.io.IOException;
import java.util.ArrayList;

public class RemotePeerStarter {
	public static void main(String[] args) {
		ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
		
		// The "PEERS" is a dummy value. This will result in all the peers
		// getting loaded in the peers config directory.
		// NOTE: This approach expects a valid SSH setup to exist
		// between all the peers. This can be done by setting up
		// an SSH key instead of a user login prompt.
		Configs.parsePeerInfos(peers, Configs.Constants.PeerInfoConfigPath, "PEERS");
		
		String workingDir = System.getProperty("user.dir");
		try {
			for (int i = 0; i < peers.size(); i++) {
				Runtime.getRuntime().exec("ssh " + peers.get(i).hostName + " cd " + workingDir + " ; " + "java p2p.PeerProcess" + " " + peers.get(i).peerID );
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
