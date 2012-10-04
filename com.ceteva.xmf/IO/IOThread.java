package IO;

import java.io.*;

public class IOThread extends Thread {

    private InputStream  in;

    private OutputStream out;

    public IOThread(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        while (true) {
            try {
                //System.out.println("About to read on " + in);
                int c = in.read();
                //System.out.println("read '" + (char)c + "'");
                //System.out.println("About to write on " + out);
                out.write(c);
                //System.out.println("written. About to flush");
                out.flush();
                //System.out.println("flushed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}