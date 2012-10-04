package XOS;

import java.net.*;
import java.io.*;

import IO.IOThread;

public class ExampleClient extends java.lang.Thread  {

     OutputStream     out;

     InputStream      in;

    private OperatingSystem xos;

    public static InetAddress localHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static InetAddress address(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + address);
            return null;
        }
    }

    public boolean connect(InetAddress address, int port, String id) {
        try {
            Socket socket = new Socket(address, port);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            for (int i = 0; i < id.length(); i++)
                out.write((byte) id.charAt(i));
            out.write(0);
            out.flush();
            System.out.println("Connected: " + id + " success = " + in.read());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            return false;
        }
        return true;
    }

    public void init(InputStream in, OutputStream out) {

        // Init as an internal client.

        this.in = in;
        this.out = out;
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            usage();
            System.exit(0);
        } else {
            int port = Integer.parseInt(args[0]);
            String id = args[1];
            new ExampleClient().initExternal(port, id);
        }
    }

    public void initExternal(int port, String id) {
        connect(localHost(), port, id);
        new IOThread(System.in, out).start();
        new IOThread(in, System.out).start();
    }

    public synchronized void run() {
        System.out.println("Start client.");
         while (true) {
                try {
                    synchronized(out) {
                        for(int i = 0; i < 5000; i++)
                            out.write('x');
                        out.write('\n');
                    out.flush();
                    out.notifyAll();
                    wait(1000);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
         }
    }

    public static void usage() {
        System.out.println("java ExampleClient PORT ID { * | stdin } { * | stdout }");
    }

}