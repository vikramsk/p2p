package p2p;

import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;

// ClientSocket defines a single socket connection a client uses to connect 
// to the server, the output and input streams associated with it
class ClientSocket{

    Socket clientSocket;

    // Initialize output stream to write to the socket
    DataOutputStream out = null;

    // Initialize input stream to read from the socket
    DataInputStream in = null;

    public ClientSocket(Socket clientSocket, DataInputStream in, DataOutputStream out){
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
    }

}