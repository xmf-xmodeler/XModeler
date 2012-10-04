package XOS;

import java.io.InputStream;
import java.io.OutputStream;

public class ExampleDataClient extends java.lang.Thread  {

    private DataInputStream  in;

    private DataOutputStream out;

    public void init(InputStream in, OutputStream out) {
        this.in = new DataInputStream(in);
        this.out = new DataOutputStream(out);
    }

    public void run() {
        try {
            while (true) {
                switch (in.read()) {
                case DataInputStream.BOOL:
                    System.out.println("BOOL: " + in.boolValue);
                    out.writeBool(!in.boolValue);
                    break;
                case DataInputStream.INT:
                    System.out.println("INT: " + in.intValue);
                    out.writeInt(in.intValue + 1);
                    break;
                case DataInputStream.STRING:
                    System.out.println("STRING: " + in.stringValue);
                    out.writeString(in.stringValue + " ****************\n");
                    break;
                }
            }
        } catch (java.io.IOException ioe) {
            System.out.println(ioe);
            System.exit(0);
        }
    }

}