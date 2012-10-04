package XOS;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ConnectionMonitor extends Thread {

    //  Used by the O/S to listen for connecting clients.
    // The server runs concurrently with the rest of XOS.

    private ServerSocket    server;                // Used to listen for connections.

    private OperatingSystem XOS;                   // Used for callback on connection.

    private Vector          sockets = new Vector(); // Retains all connected sockets.

    public ConnectionMonitor(OperatingSystem XOS, int port) {
        this.XOS = XOS;
        bind(port);
    }

    public void accept() {

        // Accepts a connection request from an external client.
        // Creates and initialises the ExternalClient structure.

        while (server.isBound() && !server.isClosed())
            try {
                Socket socket = server.accept();
                sockets.addElement(socket);
                XOS.debug("ConnectionMonitor got " + socket);
                XOS.newExternalClient(socket);
                XOS.debug("Registered new external client " + socket);
                XOS.debug("ConnectionMonitor created new external client");
            } catch (IOException e) {
                System.err.println(e);
            }
        XOS.debug("Connection monitor dies.");
    }

    public void bind(int port) {

        // XOS acts as a server for external and internal clients.
        // External clients connect to XOS via sockets. XOS creates
        // a server socket that generates a socket channel for each
        // new connection. The socket channel is selectable. The
        // server socket channel binds to a particular port number
        // that is used by the external clients when they connect.

        try {
            server = new ServerSocket(port);
        } catch (IOException ioe) {
            System.out.println(ioe);
            System.exit(0);
        }
    }

    public void close() {

        // Called when the connection monitor is no longer needed.

    	XOS.debug("Closing connection monitor");
    	
        try {
            for(int i = 0; i < sockets.size(); i++) {
                Socket socket = (Socket)sockets.elementAt(i);
                if(!socket.isClosed()) {
                    XOS.debug("Close " + socket);
                    socket.close();
                }
            } 
            //if (!server.isClosed()) {
            //    XOS.debug("Close " + server);
            //    server.close();
            // }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    public void run() {

        // Used to handle all external client connection requests.
        // Runs in a separate thread to all other XOS processing.
        // Loops until XOS terminates.

        while (true) {
            accept();
        }
    }

}