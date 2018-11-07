package p2p;

import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
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

    // HandshakeMessage handshakeMessage;
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

        public void run(){
            System.out.println("\n\nListener: A peer just connected. Total Peers connected = " + clientPeers);
            try{
                out = new DataOutputStream(connection.getOutputStream());
                out.flush();
                in = new DataInputStream(connection.getInputStream());            

                listner: while(true){
                    message = (String) in.readUTF();
                    if(!recievedHandshake){
                        recievedHandshake = true;
                        HandshakeMessage rechandShake = new HandshakeMessage(message.substring(28, 32));
                        System.out.println("Received handshake from = " + rechandShake.peerID);
                        // System.out.println("rechandShake.header = " + rechandShake.header + " message = " + message.substring(0, 18) + " from = " + rechandShake.peerID);
                        if(!rechandShake.header.equals(message.substring(0, 18))){
                            System.out.println("HandShake header check failed");
                            System.exit(-1);
                        }
                        for(int i = 0; i<connToPeers.size(); i++){
                            if(rechandShake.peerID.equals(connToPeers.get(i).peerID)){
                                System.out.println("It is a response to my handshake");
                                break listner;
                            }
                        }
                        peer.reshandShake(rechandShake.peerID);
                        break listner;
                    } else{
                        switch(message.charAt(0)){
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
            } catch(IOException e){
                e.printStackTrace();
            }
        }

    }
}