package XOS;

import java.io.InputStream;


public class DataInputStream extends DChannel implements XData {
    
    // A data input stream is used by external and internal clients to
    // access data that is written by XOS using the XData protocol. A
    // Java input stream is wrapped with a data input stream and the client
    // can then use the various methods to test the type of the next value
    // and read the values.
    
    public DataInputStream(InputStream in) {
        super(in);
    }

}
