package XOS;

import java.io.IOException;
import java.io.OutputStream;

public abstract class StreamClient extends Client {

    // A client is a uniquely identified data source and target. XOS maintains
    // a collection of clients that can be referenced by XVM via their names.
    // User code can read data from a client and write data to a client via the
    // id.

    private XChannel        in;  // Data from the client.

    private OutputStream    out; // Data to the client.

    public StreamClient(OperatingSystem os, XChannel in,OutputStream out) {

        // Use this constructor when the name is to be read from the input
        // stream. The id should be set immediately after the client has
        // been created.

        this("Anon",os,in,out);
    }

    public StreamClient(String name, OperatingSystem os, XChannel in, OutputStream out) {

        // Use this constructor when the name is known at creation time. It is the
        // task of XOS to determine whether the id is legal.

        super(os,name);
        this.in = in;
        this.out = out;
    }

    public void flush() {
        try {
            out.flush();
        } catch (IOException ioe) {
            System.out.println(ioe);
            System.exit(0);
        }
    }
    
    public XChannel in() {
        return in;
    }

    public String name() {
        return name;
    }

    public OperatingSystem os() {
        return os;
    }
    
    public OutputStream out() {
        return out;
    }
    
    public int read() {
        return in.read();
    }
    
    public boolean ready() {
        return in.ready();
    }

    public void write(int c) {
        try {
            out.write(c);
        } catch (IOException ioe) {
            System.out.println(ioe);
            System.exit(0);
        }
    }
}