package XOS;

import java.io.InputStream;


public class DChannel extends XChannel implements XData {

    // A data channel is used to read data from a Java input stream.
    // When a value is read its type is returned and the value is
    // left in an appropriate channel register. The channel will
    // block until the complete data value has been read. This class
    // is used by XOS.

    // When a data value is read, its value is recorded in the
    // following variable.

    private int         type        = UNKNOWN;

    // When values are read from the underlying input stream they are
    // recorded in the following type specific variables...

    public boolean      boolValue;

    public float        floatValue;

    public int          intValue;

    public StringBuffer stringValue = new StringBuffer();

    public DChannel(InputStream in) {
        super(in);
    }

    public synchronized int peek() {

        // Override XChannel.peek to use super.read().

        if (bufferEmpty())
            return buffer(super.read());
        else
            return buffer();
    }

    public synchronized int read() {

        // When a data value is requested we must read until the complete
        // value has been registered and then return the type of the recorded
        // value.

        type = super.read();
        switch (type) {
        case INT:
            readInt();
            break;
        case NEG:
            readNeg();
            break;
        case STRING:
            readString();
            break;
        case FLOAT:
            readFloat();
            break;
        case BYTE:
            readByte();
            break;
        case BOOL:
            readBool();
            break;
        default:
            intValue = type;
            type = UNKNOWN;
        }
        return type;
    }

    private void readBool() {

        // A boolean value is encoded in the next byte. 1 is
        // true and 0 is false.

        boolValue = super.read() == 1 ? true : false;
    }

    private void readByte() {

        // Just read a single byte and place it in the intValue register.

        intValue = super.read();
    }

    private void readInt() {

        // Integer format is LOW MED HI bytes:

        int low = super.read();
        int med = super.read();
        int hi = super.read();

        intValue = low | (med << 8) | (hi << 16);
    }

    private void readFloat() {
        readString();
        floatValue = Float.parseFloat(stringValue.toString());
    }

    private void readNeg() {
        readInt();
        intValue = -intValue;
    }

    private void readString() {

        // Read a string into the stringValue buffer.
        // The length of the string is sent as the first
        // three bytes in LOW MED HI order followed by the
        // string characters.

        stringValue.delete(0, stringValue.length());
        int low = super.read();
        int med = super.read();
        int hi = super.read();
        int length = low | (med << 8) | (hi << 16);
        for (int i = 0; i < length; i++)
            stringValue.append((char) super.read());
    }

    public int type() {
        return type;
    }

}