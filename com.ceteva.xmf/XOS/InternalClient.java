package XOS;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class InternalClient extends StreamClient {

    // An internal client corresponds to a lightweight process that
    // acts as a data source and target. Data is written to and read
    // from buffers.

    public InternalClient(OperatingSystem XOS, String name,PipedInputStream in,
            PipedOutputStream out) throws IOException {
        super(name,XOS,new XChannel(new BufferedInputStream(new PipedInputStream(out))),new BufferedOutputStream(new PipedOutputStream(in)));
    }
    
    public String toString() {
        return "InternalClient(" + name() + "," + in() + "," + out() + ")";
    }

}