package Engine;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class XarDB extends SaveLoadMachine {

    private final static int   INSTRWIDTH = 10;        // Used for debug output.

    private static final int   LEFT       = 0;         // Debug output alignment.

    private static final int   RIGHT      = 1;         // Debug output alignment.

    private static InputStream in;                     // The stream of load instructions.

    private static PrintStream out        = System.out;

    private static int         instrCount = 0;

    public static void debug(String instr) {
        debug(instr, -1);
    }

    public static void debug(String instr, int operand) {
        debug(instr, operand + "");
    }

    public static void debug(String instr, String operand) {

        String countField = pad("" + instrCount++, 5, "0", LEFT);
        String instrField = pad(instr, INSTRWIDTH, " ", RIGHT);
        String operandField = operand.equals("-1") ? "" : operand;
        out.println(countField + " " + instrField + " " + operandField);
    }

    public static String pad(String string, int width, String padChar, int alignment) {

        // Used to align a string for debug output.

        if (string.length() > width)
            return string.substring(0, width);
        else {
            for (int i = string.length(); i < width; i++)
                switch (alignment) {
                case LEFT:
                    string = padChar + string;
                    break;
                case RIGHT:
                    string = string + " ";
                    break;
                }
            return string;
        }
    }

    public static int read24() {
        // Read a 24 bit value from the input stream.
        try {
            int high = in.read();
            int medium = in.read();
            int low = in.read();
            return (high << 16) | (medium << 8) | low;
        } catch (IOException e) {
            throw new Error("ValueLoader.read24");
        }
    }

    public static int read16() {
        // Read a 16 bit value from the input stream.
        try {
            int high = in.read();
            int low = in.read();
            return (high << 8) | low;
        } catch (IOException e) {
            throw new Error("ValueLoader.read16");
        }
    }

    public static int read8() {
        // Read an 8 bit value from the input stream.
        try {
            int low = in.read();
            return low;
        } catch (IOException e) {
            throw new Error("ValueLoader.read8");
        }
    }

    public static void process() {

        int instr;
        while ((instr = read8()) != -1) {
            //System.out.println((count++) + " " + instrToString(instr));
            //System.out.println(valueStack.toString(machine));
            switch (instr) {
            case MKTRUE:
                debug("MKTRUE");
                break;
            case MKFALSE:
                debug("MKFALSE");
                break;
            case MKFUN:
                debug("MKFUN", read16());
                break;
            case SETFUN:
                debug("SETFUN");
                break;
            case MKINT:
                debug("MKINT", read24());
                break;
            case MKSTRING:
                debug("MKSTRING", readString());
                break;
            case MKARRAY:
                debug("MKARRAY", read16());
                break;
            case MKARRAY_NONSHARE:
                debug("MKARRAY", read16());
                break;
            case SETARRAY:
                debug("SETARRAY", read16());
                break;
            case REFARRAY:
                debug("REFARRAY", read24());
                break;
            case REFSTRING:
                debug("REFSTRING", read24());
                break;
            case REFOBJ:
                debug("REFSTRING", read24());
                break;
            case REFFUN:
                debug("REFFUN", read24());
                break;
            case REFCONS:
                debug("REFCONS", read24());
                break;
            case REFSYMBOL:
                debug("REFSYMBOL", read24());
                break;
            case REFVALUE:
                debug("REFVALUE", read24());
                break;
            case MKCODEBOX:
                debug("MKCODEBOX", read24());
                break;
            case SETCODEBOX:
                debug("SETCODEBOX");
                break;
            case MKOBJ:
                debug("MKOBJ");
                break;
            case SETOBJ:
                debug("SETOBJ");
                break;
            case MKCODE:
                debug("MKCODE", skipCode(read24()));
                break;
            case MKUNDEF:
                debug("MKUNDEF");
                break;
            case MKCONS:
                debug("MKCONS");
                break;
            case MKCONS_NONSHARE:
                debug("MKCONS_NONSHARE");
                break;
            case SETCONS:
                debug("SETCONS");
                break;
            case MKNIL:
                debug("MKNIL");
                break;
            case MKSYMBOL:
                debug("MKSYMBOL");
                break;
            case MKSYMBOL2:
                debug("MKSYMBOL2", readString());
                break;
            case MKSET:
                debug("MKSET");
                break;
            case TABLE:
                debug("TABLE");
                break;
            case PUT:
                debug("PUT");
                break;
            case LOOKUP:
                debug("LOOKUP");
                break;
            case MKFOREIGNFUN:
                debug("MKFOREIGNFUN,read8()");
                break;
            case GLOBAL_DYNAMICS:
                debug("GLOBAL_DYNAMICS");
                break;
            case MKDYNAMIC_TABLE:
                debug("MKDYNAMIC_TABLE");
                break;
            case MKNEGINT:
                debug("MKNEGINT", read24());
                break;
            case MKFLOAT:
                debug("MKFLOAT");
                break;
            case HOTLOAD:
                debug("HOTLOAD");
                break;
            case MKLIST:
                debug("MKLIST", read16());
                break;
            case SETOBJ2:
                debug("SETOBJ2", read16());
                break;
            case MKSET2:
                debug("MKSET2", read16());
                break;
            case LISTCONS:
                debug("LISTCONS");
                break;
            case MKSIG:
                debug("MKSIG", read16());
                break;
            case MKARG:
                debug("MKARG");
                break;
            case MK_ELEMENT_TYPE:
                debug("MK_ELEMENT_TYPE");
                break;
            case NULLGLOBS:
                debug("NULLGLOBS");
                break;
            case MKGLOBALS:
                debug("MKGLOBALS");
                break;
            case MKDAEMON:
                debug("MKDAEMON");
                break;
            case LOOKUPFUN:
               debug("LOOKUPFUN", read8());
               break;
            case LOOKUP2:
                debug("LOOKUP2", read8());
                break;
            case MKNULLSLOT:
                debug("MKNULSLOT", readString());
                break;
            case MKNILSLOT:
                debug("MKNILSLOT", readString());
                break;
            case MKEMPTYSLOT:
                debug("MKEMPTYSLOT", readString());
                break;
            case MKZEROSLOT:
                debug("MKZEROSLOT", readString());
                break;
            case MKTRUESLOT:
                debug("MKTRUESLOT", readString());
                break;
            case MKFALSESLOT:
                debug("MKFALSESLOT", readString());
                break;
            case END:
                debug("END");
                return;
            default:
                throw new Error("XARDB: unknown load instruction: " + instr);
            }
        }

    }

    public static String readString() {
        int length = read16();
        String s = "";
        for (int i = 0; i < length; i++)
            s = s + ("" + (char) read8());
        return s;
    }
    
    public static int skipCode(int bytes) {
        for(int i = 0; i < bytes; i++) {
            read8();
            read24();
        }
        return bytes;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            String path = args[0];
            File file = new File(path);
            if (file.exists())
                try {
                    in = new GZIPInputStream(new FileInputStream(file));
                    process();
                    in.close();
                } catch (FileNotFoundException fnf) {
                    System.out.println(fnf);
                } catch (IOException ioe) {
                    System.out.println(ioe);
                }
        }
    }

}