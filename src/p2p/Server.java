package p2p;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

// Server defines a socketserver on which the peer
// listens to connections from other peers
public class Server implements Runnable{

    int port;
    ServerSocket serverSocket;

    // Server initializes the Server with the port number
    public Server(String port){
        this.port = Integer.parseInt(port);
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

        public Handler(Socket connection, int clientPeers){
            this.connection = connection;
            this.clientPeers = clientPeers;
        }

        public void run(){
            System.out.println("Peers connected = " + clientPeers);
        }

    }
}