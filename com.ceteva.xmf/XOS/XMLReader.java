package XOS;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import java.io.*;

public class XMLReader extends DefaultHandler {

    // An XML reader will read an XML document from an input stream and serialize the
    // document to the output stream. The encoding follows the standard SAX event
    // model. A standard XMF input channel is then used to read the encoded events
    // at the program level.

    public static final int START_ELEMENT = 1;

    public static final int END_ELEMENT   = 2;

    public static final int CHARS         = 3;

    public static final int ERROR         = 4;

    private InputStream     in;

    private OutputStream    out;

    public XMLReader(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }
    
    public boolean allWhiteSpace(char[] chars,int start,int length) {
        for(int i = start; i < start + length; i++) {
            char c = chars[i];
            if(c != ' ' && c != '\n' && c != '\t' && c != '\r')
                return false;
        }
        return true;
    }

    public void characters(char[] chars, int start, int length) {
        if(!allWhiteSpace(chars,start,length)) {
        try {
            writeInstr(CHARS);
            write24(length);
            for (int i = 0; i < length; i++)
                writeByte(chars[i + start]);
        } catch (IOException ioe) {
            throw new Error(ioe.getMessage());
        }
        }
    }

    public void endElement(String tag) {
        try {
            writeInstr(END_ELEMENT);
            writeByte(tag.length());
            writeString(tag);
        } catch (IOException ioe) {
            throw new Error(ioe.getMessage());
        }
    }

    public void endElement(String uri, String localName, String qname) {
        endElement(qname);
    }

    public void error(SAXParseException e) {
        System.out.println("SAX Parse Error: " + e.toString());
    }

    public void fatalError(SAXParseException e) {
        System.out.println("SAX Parse Fatal Error: " + e.toString());
    }

    public void parse() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, this);
            in.close();
            out.close();
        } catch (org.xml.sax.SAXParseException x) {
            System.out.println("XML Parsing Error");
            try {
                in.close();
                out.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        } catch (Throwable e) {
            System.out.println(e);
            try {
                in.close();
                out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void startElement(String uri, String localName, String qname, Attributes attributes) {
        try {
            writeInstr(START_ELEMENT);
            writeByte(qname.length());
            writeString(qname);
            writeAttributes(attributes);
        } catch (IOException ioe) {
            throw new Error(ioe.getMessage());
        }
    }

    public void startProcess() {
        Thread thread = new Thread() {
            public void run() {
                parse();
            }
        };
        thread.start();
    }

    public void warning(SAXParseException e) {
        System.out.println(e.toString());
    }

    public void writeAttributes(Attributes attributes) throws IOException {
        writeByte(attributes.getLength());
        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getQName(i);
            String value = attributes.getValue(i);
            writeAtt(name, value);
        }
    }

    public void writeAtt(String name, String value) throws IOException {
        writeByte(name.length());
        write24(value.length());
        writeString(name);
        writeString(value);

    }

    public void writeByte(int b) throws IOException {
        out.write(b);
    }

    public void writeInstr(int instr) throws IOException {
        writeByte(instr);
    }

    public void writeString(String s) throws IOException {
        for (int i = 0; i < s.length(); i++)
            writeByte(s.charAt(i));
    }

    public void write24(int v) throws IOException {
        int high = (v & 0xFF0000) >> 16;
        int med = (v & 0xFF00) >> 8;
        int low = v & 0xFF;
        writeByte(high);
        writeByte(med);
        writeByte(low);
    }

}