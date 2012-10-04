package XOS;

import java.io.IOException;
import java.io.InputStream;

public class XChannel extends XInputStream {

    // All XOS byte input streams are instances of this class.
    // This allows us to override any appropriate methods
    // from the Java input streams. An XChannel provides a
    // single byte buffer that is used to allow single byte
    // lookahead.

    private int             buffer      = -1;

    private boolean         alwaysReady = false;

    private InputStream     in;

    public XChannel(InputStream in) {
        this.in = in;
    }

    public XChannel(InputStream in, boolean alwaysReady) {
        this(in);
        this.alwaysReady = alwaysReady;
    }

    public int available() {

        // All XChannels must provide accurate and non-blocking
        // information about how many characters are currently
        // available on the underlying Java stream without blocking.
        // This is used by XOS to schedule threads.

        int available = 0;
        try {
            available = in.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (buffer == -1)
            return available;
        else
            return available + 1;
    }

    public int buffer() {
        return buffer;
    }

    public int buffer(int c) {
        buffer = c;
        return c;
    }

    public boolean bufferEmpty() {
        return buffer == -1;
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean eof() {
        return peek() == -1;
    }

    public synchronized int peek() {

        // Peek at the next input character and return it. Blocks until a character is
        // available. Uses a single element buffer to implement the lookahead.

        if (buffer == -1) {
            buffer = read();
            return buffer;
        } else
            return buffer;
    }

    public synchronized int read() {

        // Reads a single byte from the input stream, blocking if necessary.
        // Sets the value of eof if the end of stream is encountered so that
        // XOS can service requests for EOF without retrying the stream.

        if (buffer == -1) {
            while (true) {
                try {
                    return in.read();
                } catch (java.net.SocketException se) {
                    return -1;
                } catch (IOException e) {
                    System.out.println(e);
                    System.exit(0);
                }
            }
        } else {
            int c = buffer;
            buffer = -1;
            return c;
        }
    }

    public boolean ready() {

        // Returns true when a read will not block.

        if (alwaysReady)
            return true;
        else
            return available() > 0;
    }

    public void resetToInitialState() {

        // Called when we want to loase all saved state in the input channel.

        buffer = -1;
    }

    public String toString() {
        return "XChannel(" + in + ")";
    }

}