package p2p;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

// Server defines a socketserver on which the peer
// listens to connections from other peers
public class Server implements Runnable{

    int port;
    ServerSocket serverSocket;
    ArrayList<PeerInfo> connToPeers;

    // Initialize output stream to write to the socket
    DataOutputStream out = null;

    // Initialize input stream to read from the socket
    DataInputStream in = null;

    // 
    String message;

    Peer peer;

    // Server initializes the Server with the port number
    public Server(Peer peer, String port, ArrayList<PeerInfo> connToPeers){
        this.peer = peer;
        this.port = Integer.parseInt(port);
        this.connToPeers = connToPeers;
    }

    // Run implements the server socket connection and 
    // spawns a new Handler/thread to handle every clent connection
    public void run(){
        int clientPeers = 1;
        try{
            serverSocket = new ServerSocket(port);
            while(true){
                new Handler(serverSocket.accept(), clientPeers).start();
                clientPeers++;
                // handshake received, if !in conToPeers, add to connToPeers and call client handshake

            }
        } catch(IOException e){
            e.printStackTrace();
        }
    // Should fix serverSocket.close() for next submission 
        try{
            serverSocket.close();
        } catch(IOException f){
            f.printStackTrace();
        }

    }

    // Handler implements the handler for every client connected
    // to the server
    public class Handler extends Thread{
        Socket connection;
        int clientPeers;
        boolean recievedHandshake = false;
        
        public Handler(Socket connection, int clientPeers){
            this.connection = connection;
            this.clientPeers = clientPeers;
        }

        public void run() throws IOException{
            System.out.println("Peers connected = " + clientPeers);
            out = new DataOutputStream(socket.getOutputStream());
            out.flush();
            intput = new DataInputStream(socket.getInputStream());            

            listner: while(true){
                message = (String) in.readObject();
                if(!recievedHandshake){
                    recievedHandshake = true;
                    HandShakeMessage rechandShake = new HandShakeMessage(message.substring(28, 32));
                    if(!rechandShake.equals(message.substring(0, 19))){
                        // HandShake header check failed
                        System.exit(0);
                    }
                    for(int i = 0; i<connToPeers.size(); i++){
                        if(rechandShake.peerID.equals(connToPeers.get(i))) break listner;
                    }
                    peer.reshandShake(rechandShake.peerID);
                    
                } else{
                    switch(message[0]){
                        case '1':

                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                    }
                }
            }
        }

    }
}