package Engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.zip.GZIPInputStream;

public class XarToXML  {
    
    

//  Set on loading...

    public int              majorVersion;

    public int              minorVersion;

    protected OutputStream out;

//  Used for LOOKUP...

    protected StringBuffer  buffer          = new StringBuffer();

    public XarToXML(OutputStream out) {
        this.out = out;
    }

    public void attribute(String name, int value) {
        write(" " + name + "='" + value + "'");
    }

    public void attribute(String name, String value) {
        write(" " + name + "='" + XMLEncode(value.toString()) + "'");
    }

    // ***********************************************************
    // Debugging....
    // ***********************************************************

    private boolean     debug    = false;     // True when tracing behaviour.

    private PrintStream debugOut = System.err; // Where to send XML output to.

    public void debug(String instr) {
        if (debug)
            debugOut.println(instr);
    }

    public void debug(String instr, CharSequence cs) {
        if (debug)
            debugOut.println(instr + " " + cs.toString());
    }

    public void endOpenTag() {
        writeln(">");
    }

    public void endTag() {
        writeln("/>");
    }

    public void endTag(String name) {
        writeln("</" + name + ">");
    }

    public void hotLoad() {
        startTag("HotLoad");
        endTag();
    }
    
    public int load(InputStream in) {
        loadVersion(in);
        return loadDispatch(in);
    }


    public void loadArg() {

    }

    public void loadArray(InputStream in) {
        int length = read24(in);
        int daemonsActive = read8(in) == 1 ? Machine.trueValue : Machine.falseValue;
        startTag("Array");
        attribute("length", length);
        attribute("daemonsActive", daemonsActive);
        endOpenTag();
    }

    public void loadBuffer(InputStream in) {
        int increment = read16(in);
        int size = read16(in);
        int daemonsActive = read8(in) == 1 ? Machine.trueValue : Machine.falseValue;
        int asString = read8(in) == 1 ? Machine.trueValue : Machine.falseValue;
        startTag("Buffer");
        attribute("increment", increment);
        attribute("daemonsActive", daemonsActive);
        attribute("asString", asString);
        endOpenTag();
    }

    public void loadCode(InputStream in) {
        int length = read24(in);
        startTag("Code");
        endOpenTag();
        for (int i = 0; i < length; i++) {
            int instr = read8(in);
            int operands = read24(in);
            startTag("Instr");
            attribute("code", instr);
            attribute("operands", operands);
            endTag();
        }
        endTag("Code");
    }

    public void loadClosure(InputStream in) {
        int arity = read16(in);
        startTag("Closure");
        attribute("arity", arity);
        endOpenTag();

    }

    public void loadCodeBox(InputStream in) {
        int arity = read16(in);
        String name = readStr(in);
        String source = readStr(in);
        String resourceName = readStr(in);
        startTag("CodeBox");
        attribute("arity", arity);
        attribute("name", name);
        attribute("source", source);
        attribute("resourceName", resourceName);
        endOpenTag();
    }

    public void loadCons() {
        startTag("Cons");
        endOpenTag();
    }

    public void loadDaemon() {
        startTag("Daemon");
        endOpenTag();
    }

    public int loadDispatch(InputStream in) {
        boolean complete = false;
        while (!complete) {
            int instr = read8(in);
            switch (instr) {
            case Serializer.END:
                complete = true;
                loadEnd();
                break;
            case Serializer.GLOBAL_DYNAMICS:
                loadGlobalDynamics();
                break;
            case Serializer.HOTLOAD:
                hotLoad();
                break;
            case Serializer.LOOKUP:
                lookup(in);
                break;
            case Serializer.MKARG:
                loadArg();
                break;
            case Serializer.MKARRAY:
                loadArray(in);
                break;
            case Serializer.MKBUFFER:
                loadBuffer(in);
                break;
            case Serializer.MKCLOSURE:
                loadClosure(in);
                break;
            case Serializer.MKCODE:
                loadCode(in);
                break;
            case Serializer.MKCONS:
                loadCons();
                break;
            case Serializer.MKCONS_NONSHARE:
                loadConsNonShare();
                break;
            case Serializer.MKCODEBOX:
                loadCodeBox(in);
                break;
            case Serializer.MKDAEMON:
                loadDaemon();
                break;
            case Serializer.MKDYNAMIC_TABLE:
                loadDynamicTable();
                break;
            case Serializer.MK_ELEMENT_TYPE:
                loadElementType();
                break;
            case Serializer.MKFALSE:
                loadFalse();
                break;
            case Serializer.MKFALSESLOT:
                loadFalseSlot(in);
                break;
            case Serializer.MKFOREIGNFUN:
                loadForeignFun(in);
                break;
            case Serializer.MKFLOAT:
                loadFloat(in);
                break;
            case Serializer.MKFORWARDREF:
                loadForwardRef();
                break;
            case Serializer.MKFUN:
                loadFun(in);
                break;
            case Serializer.MKGLOBALS:
                loadGlobals();
                break;
            case Serializer.MKINT:
                loadInt(in);
                break;
            case Serializer.MKINTSLOT:
                loadIntSlot(in);
                break;
            case Serializer.MKLIST:
                loadList(in);
                break;
            case Serializer.MKNEGINT:
                loadNegInt(in);
                break;
            case Serializer.MKNIL:
                loadNil();
                break;
            case Serializer.MKNILSLOT:
                loadNilSlot(in);
                break;
            case Serializer.MKOBJ:
                loadObj(in);
                break;
            case Serializer.MKSET:
                loadSet(in);
                break;
            case Serializer.MKSETSLOT:
                loadSetSlot(in);
                break;
            case Serializer.MKSIG:
                loadSig(in);
                break;
            case Serializer.MKSLOT:
                loadSlot(in);
                break;
            case Serializer.MKSLOTS:
                loadSlots(in);
                break;
            case Serializer.MKSTRING:
                loadString(in);
                break;
            case Serializer.MKSTRINGSLOT:
                loadStringSlot(in);
                break;
            case Serializer.MKSYMBOL:
                loadSymbol(in);
                break;
            case Serializer.MKTABLE:
                loadTable(in);
                break;
            case Serializer.MKTRUE:
                loadTrue();
                break;
            case Serializer.MKTRUESLOT:
                loadTrueSlot(in);
                break;
            case Serializer.MKUNDEF:
                loadUndef();
                break;
            case Serializer.NULLGLOBS:
                loadNullGlobs();
                break;
            case Serializer.PUT:
                put();
                break;
            case Serializer.REF:
                ref(in);
                break;
            case Serializer.ROOT:
                pushRoot();
                debug("ROOT");
                break;
            case Serializer.SETARRAY:
                setArray(in);
                break;
            case Serializer.SETBUFFER:
                setBuffer();
                break;
            case Serializer.SETCLOSURE:
                setClosure();
                break;
            case Serializer.SETCODEBOX:
                setCodeBox();
                break;
            case Serializer.SETCONS:
                setCons();
                break;
            case Serializer.SETDAEMON:
                setDaemon();
                break;
            case Serializer.SETFORWARDREF:
                setForwardRef();
                break;
            case Serializer.SETFUN:
                setFun();
                break;
            case Serializer.SETOBJ:
                setObj();
                break;
            default:
                throw new Error("Unknown load instruction " + instr);
            }
        }
        return result();
    }

    private void setForwardRef() {
        startTag("SetFrwardRef");
        endTag();
    }

    private void loadList(InputStream in) {
        int length = read16(in);
        startTag("List");
        attribute("size", length);
        endTag();
    }

    private void loadForwardRef() {
        startTag("ForwardRef");
        endTag();
    }

    private void loadConsNonShare() {
        startTag("Cons");
        endOpenTag();
    }

    public void loadDynamicTable() {
    }

    public void loadElementType() {
        startTag("ElementType");
        endTag();
    }

    public void loadEnd() {
        endTag("Version");
    }

    public void loadFalse() {
        startTag("False");
        endTag();
    }

    public void loadFalseSlot(InputStream in) {
        startTag("FalseSlot");
        attribute("name", readStr(in));
        endTag();
    }

    public void loadForeignFun(InputStream in) {
        int arity = read8(in);
        startTag("ForeignFun");
        attribute("arity", arity);
        endTag();
    }

    public void loadFloat(InputStream in) {
        startTag("Float");
        endTag();
    }

    public void loadFun(InputStream in) {
        int arity = read8(in);
        startTag("Fun");
        attribute("arity", arity);
        endOpenTag();
    }

    public void loadGlobals() {
        startTag("Globals");
        endTag();
    }

    public void loadGlobalDynamics() {
        startTag("GlobalDynamics");
        endTag();
    }

    public void loadInt(InputStream in) {
        startTag("Int");
        attribute("value", read24(in));
        endTag();
    }

    public void loadIntSlot(InputStream in) {
        startTag("IntSlot");
        attribute("name", readStr(in));
        attribute("value", read24(in));
        endTag();
    }

    public void loadNegInt(InputStream in) {
        startTag("Int");
        attribute("value", -read24(in));
        endTag();
    }

    public void loadNil() {
        startTag("Nil");
        endTag();
    }

    public void loadNilSlot(InputStream in) {
        startTag("NilSlot");
        attribute("name", readStr(in));
        endTag();
    }

    public void loadNullGlobs() {
        startTag("NullGlobs");
        endTag();
    }

    public void loadObj(InputStream in) {
        int properties = read24(in);
        int daemonsActive = read8(in);
        int hotLoad = read8(in);
        startTag("Obj");
        attribute("properties", properties);
        attribute("daemonsActive", daemonsActive);
        attribute("hotLoad", hotLoad);
        endOpenTag();
    }

    public void loadSig(InputStream in) {
        int arity = read16(in);
        startTag("Sig");
        attribute("arity", arity);
        endTag();
    }

    public void loadSet(InputStream in) {
        int length = read24(in);
        startTag("Set");
        attribute("length", length);
        endTag();
    }

    public void loadSetSlot(InputStream in) {
        String name = readStr(in);
        int length = read24(in);
        startTag("SetSlot");
        attribute("name", name);
        attribute("length", length);
        endTag();
    }

    public void loadSlot(InputStream in) {
        String name = readStr(in);
        startTag("Slot");
        attribute("name", name);
        endTag();
    }

    public void loadSlots(InputStream in) {
        int length = read16(in);
        startTag("Slots");
        attribute("length", length);
        endTag();
    }

    public void loadString(InputStream in) {
        String string = readStr(in);
        startTag("String");
        attribute("value", string);
        endTag();
    }

    public void loadStringSlot(InputStream in) {
        String name = readStr(in);
        String value = readStr(in);
        startTag("StringSlot");
        attribute("name", name);
        attribute("value", value);
        endTag();
    }

    public void loadSymbol(InputStream in) {
        String symbol = readStr(in);
        startTag("Symbol");
        attribute("value", symbol);
        endTag();
    }

    public void loadTable(InputStream in) {
        startTag("Table");
        attribute("size", read24(in));
        endTag();
    }

    public void loadTrue() {
        startTag("True");
        endTag();
    }

    public void loadTrueSlot(InputStream in) {
        startTag("TrueSlot");
        attribute("name", readStr(in));
        endTag();
    }

    public void loadUndef() {
        startTag("Undef");
        endTag();
    }

    public void loadVersion(InputStream in) {

        // The first instruction in a XAR file should be
        // VERSION. If this is not the case then report an
        // error and don't go any further...

        int instr = read8(in);
        if (instr == -1)
            throw new Error("EOF in XAR file");
        if (instr != Serializer.VERSION)
            throw new Error("Not a XAR file (no VERSION)");
        debug("VERSION");
        setMajorVersion(read8(in));
        setMinorVersion(read8(in));
        startTag("Version");
        attribute("major", majorVersion);
        attribute("minor", minorVersion);
        endOpenTag();
    }

    public void lookup(InputStream in) {
        int length = read16(in);
        buffer.setLength(0);
        for (int i = 0; i < length; i++)
            buffer.append((char) read8(in));
        startTag("Lookup");
        attribute("path", buffer.toString());
        endTag();
    }

    public static void main(String[] args) {
        try {
            String inFile = args[0];
            FileInputStream fin = new FileInputStream(inFile);
            BufferedInputStream bin = new BufferedInputStream(fin);
            XarToXML translator = new XarToXML(System.out);
            translator.load(new GZIPInputStream(bin));
            bin.close();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    public void pushRoot() {
        startTag("Root");
        endTag();
    }

    public void put() {
        startTag("Put");
        endTag();
    }

    public String readStr(InputStream in) {
        int length = read16(in);
        buffer.setLength(0);
        for (int i = 0; i < length; i++)
            buffer.append((char) read8(in));
        return buffer.toString();
    }

    public int read24(InputStream in) {
        // Read a 24 bit value from the input stream.
        try {
            int high = in.read();
            int medium = in.read();
            int low = in.read();
            return (high << 16) | (medium << 8) | low;
        } catch (IOException e) {
            throw new Error("ValueLoader.read24: " + e.toString());
        }
    }

    public int read16(InputStream in) {
        // Read a 16 bit value from the input stream.
        try {
            int high = in.read();
            int low = in.read();
            return (high << 8) | low;
        } catch (IOException e) {
            throw new Error("ValueLoader.read16");
        }
    }

    public int read8(InputStream in) {
        // Read an 8 bit value from the input stream.
        try {
            int low = in.read();
            return low;
        } catch (IOException e) {
            throw new Error("ValueLoader.read8");
        }
    }

    public void ref(InputStream in) {
        startTag("Ref");
        attribute("index", read24(in));
        endTag();
    }

    public int result() {
        return 0;
    }

    public void setArray(InputStream in) {
        int length = read24(in);
        endTag("Array");
    }

    public void setBuffer() {
        endTag("Buffer");
    }

    public void setClosure() {
        endTag("Closure");
    }

    public void setCodeBox() {
        endTag("CodeBox");
    }

    public void setCons() {
        endTag("Cons");
    }

    public void setDaemon() {
        endTag("Daemon");
    }

    public void setFun() {
        endTag("Fun");
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public void setObj() {
        endTag("Obj");
    }

    public void startTag(String tag) {
        write("<" + tag);
    }

    public void write(String s) {
        for (int i = 0; i < s.length(); i++)
            try {
                out.write(s.charAt(i));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public void writeln(String s) {
        write(s);
        write("\n");
    }

    public String XMLEncode(String att) {
        return att.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&gt;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;").replaceAll(
                "\n", "&#xa;").replaceAll("\r", "&#xd;");
    }

}
