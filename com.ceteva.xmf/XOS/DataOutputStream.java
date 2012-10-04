package XOS;

import java.io.IOException;
import java.io.OutputStream;


public class DataOutputStream extends OutputStream implements XData {

    // A data output stream is used by external and internal clients to
    // encode data when writing to XOS. Users wrap an output stream as a
    // data output stream and then just write data using the appropriate
    // methods. The data output stream handles encoding the values using
    // the XData protocol that is then read by XVM code.

    private OutputStream out;

    public DataOutputStream(OutputStream out) {
        this.out = out;
    }
    
    public void flush() throws IOException {
        out.flush();
    }
    
    public void startString(int length) throws IOException {
        
        // Clients may not have a string to write. Rather than force
        // a client to allocate a string to pass to the data output 
        // stream, a client may start a string and then write the chars
        // out separately using write (remembering to flush).
        
        out.write(STRING);
        writeIntValue(length);
    }
    
    public void write(int b) throws IOException {
        out.write(b);
    }

    public void writeBool(boolean b) throws IOException {
        out.write(BOOL);
        if (b)
            out.write(1);
        else
            out.write(0);
    }

    public void writeChars(String s) throws IOException {
        int length = s.length();
        writeIntValue(length);
        for (int i = 0; i < length; i++)
            out.write((int) s.charAt(i));
    }

    public void writeByte(int b) throws IOException {
        out.write(BYTE);
        out.write(b);
    }

    public void writeFloat(float f) throws IOException {
        out.write(FLOAT);
        writeChars("" + f);
    }
    
    public void writeInt(int i) throws IOException {
        if(i < 0)
            writeNegInt(i);
        else writePosInt(i);
    }
    
    public void writeIntValue(int i) throws IOException {
        int low = i & 0xFF;
        int med = (i & 0xFF00) >> 8;
        int hi = (i & 0xFF0000) >> 16;
        out.write(low);
        out.write(med);
        out.write(hi);
    }

    public void writeNegInt(int i) throws IOException {
        out.write(NEG);
        writeIntValue(i);
    }
    
    public void writePosInt(int i) throws IOException {
        out.write(INT);
        writeIntValue(i);
    }
    
    public void writeString(String s) throws IOException {
        out.write(STRING);
        writeChars(s);
    }

}