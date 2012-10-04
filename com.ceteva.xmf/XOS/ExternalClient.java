package XOS;

import java.io.IOException;
import java.io.OutputStream;

public class ExternalClient extends StreamClient {

    // An external client is connected to XOS via a socket. Data can be read
    // and written to the data streams of the socket. When the client initially
    // connects, the first sequence of characters sent via the socket is the
    // 0 terminated name of the client.
    
    // When a client connects to XOS the first communication must be a
    // name. If the name is unique then XOS replies with NAME_OK. Otherwise
    // XOS replies with NAME_FAIL.
    
    public static final int NAME_OK = 1; 

    public static final int NAME_FAIL = 2; 

    public ExternalClient(String name, OperatingSystem os,XChannel in,OutputStream out) throws IOException {

        // When an external client connects the first thing sent must be a
        // 0-terminated sequence of characters that is the id of the client.
        // The id is used to reference the client via XOS.

        super(name,os,in,out);
    }

    public String toString() {
        return "ExternalClient(" + name() + "," + in() + "," + out() + ")";
    }

}