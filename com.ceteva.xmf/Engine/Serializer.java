package Engine;

import java.io.*;
import java.util.Hashtable;

public final class Serializer implements Value, Errors {

    // The following two values are used to keep track ot the
    // version of XAR data files. They will be incremented as
    // new versions of XAR are introduced. When a file is d
    // the version numbers are saved in the file. This allows
    // future versions of the loader to upgrade the saved format
    // where this is possible.

    public static final int            VERSION_MAJOR   = 1;

    public static final int            VERSION_MINOR   = 9;

    // A save and load machine defines a common language and
    // caching structure for saving and rebuilding the values
    // that can be found in the heap.

    // ***********************************************************
    // The following are 8-bit machine instructions.
    // ***********************************************************

    public static final int            MKARRAY         = 1;

    public static final int            SETARRAY        = 2;

    public static final int            MKCODE          = 3;

    public static final int            MKINT           = 4;

    public static final int            MKSTRING        = 5;

    public static final int            MKDAEMON        = 6;

    public static final int            MKCODEBOX       = 7;

    public static final int            SETCODEBOX      = 8;

    public static final int            MKTRUE          = 9;

    public static final int            MKFALSE         = 10;

    public static final int            MKFUN           = 11;

    public static final int            SETFUN          = 12;

    public static final int            MKUNDEF         = 13;

    public static final int            MKOBJ           = 14;

    public static final int            SETOBJ          = 15;

    public static final int            MKCONS          = 16;

    public static final int            SETCONS         = 17;

    public static final int            MKNIL           = 18;

    public static final int            MKSYMBOL        = 19;

    public static final int            MKSET           = 20;

    public static final int            MKTABLE         = 21;

    public static final int            PUT             = 22;

    public static final int            LOOKUP          = 23;

    public static final int            MKFOREIGNFUN    = 24;

    public static final int            GLOBAL_DYNAMICS = 25;

    public static final int            MKDYNAMIC_TABLE = 26;

    public static final int            MKNEGINT        = 27;

    public static final int            MKFLOAT         = 28;

    public static final int            REF             = 29;

    public static final int            MKSLOT          = 30;

    public static final int            MKSLOTS         = 31;

    public static final int            MKINTSLOT       = 32;

    public static final int            MKSTRINGSLOT    = 33;

    public static final int            MKTRUESLOT      = 34;

    public static final int            MKFALSESLOT     = 35;

    public static final int            MKNILSLOT       = 36;

    public static final int            MKSETSLOT       = 37;

    public static final int            HOTLOAD         = 39;

    public static final int            ROOT            = 40;

    public static final int            NULLGLOBS       = 41;

    public static final int            MKGLOBALS       = 42;

    public static final int            LISTCONS        = 44;

    public static final int            MKSIG           = 45;

    public static final int            MKARG           = 46;

    public static final int            MK_ELEMENT_TYPE = 47;

    public static final int            MKLIST          = 48;

    public static final int            MKCONS_NONSHARE = 49;

    public static final int            LOOKUPFUN       = 58;

    public static final int            SETDAEMON       = 59;

    public static final int            VERSION         = 60;

    public static final int            MKBUFFER        = 61;

    public static final int            SETBUFFER       = 62;

    public static final int            MKCLOSURE       = 63;

    public static final int            SETCLOSURE      = 64;

    public static final int            MKFORWARDREF    = 65;

    public static final int            SETFORWARDREF   = 66;

    public static final int            END             = 255;

    // Set on loading...

    public int                         majorVersion;

    public int                         minorVersion;

    // Used for LOOKUP...

    protected StringBuffer             buffer          = new StringBuffer();

    // Used for saving values and indexing for sharing...

    private static int                 VALUES          = 1024 * 500;

    private int[]                      values          = new int[VALUES];

    private int                        index           = 0;

    private ValueStack                 valueStack      = new ValueStack(1024 * 500);

    // The machine...

    private Machine                    machine;

    // Objects that run their 'hotLoad' operation....

    private int                        hotLoads        = Machine.nilValue;

    // The following name spaces have contents whose eleents are saved by
    // name lookup...

    private int                        nameSpaces      = Machine.nilValue;

    // Cache the null globals for all the operations...

    private int                        nullGlobs       = -1;

    // Cache the element type in a sig...

    private int                        elementType     = -1;

    // Cache the Root namespace...

    private int                        root            = -1;
    
    // Errors that occur during serialization...
    
    public String errorMessage;
    
    public boolean errorOccurred;

    // START NEW CODE

    //private Hashtable<String, Integer> codeBoxTable    = new Hashtable<String, Integer>();

    // END NEW CODE

    public Serializer(Machine m) {
        machine = m;
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

    public void debugString(String instr, int string) {
        if (debug)
            debugOut.println(instr + " " + machine.valueToString(string));
    }

    public void debug(String instr, CharSequence cs) {
        if (debug)
            debugOut.println(instr + " " + cs.toString());
    }

    public int filterPersistentDaemons(int daemons) {
        int pdaemons = Machine.nilValue;
        while (daemons != Machine.nilValue) {
            int daemon = machine.consHead(daemons);
            daemons = machine.consTail(daemons);
            if (Machine.isFun(daemon) && machine.funPersistentDaemon(daemon))
                pdaemons = machine.mkCons(daemon, pdaemons);
            if (Machine.isDaemon(daemon) && machine.daemonIsPersistent(daemon))
                pdaemons = machine.mkCons(daemon, pdaemons);
        }
        return pdaemons;
    }

    public int findImportedNameSpace(Machine machine, int table) {
        // Returns the imported name space whose contents is 'table' otherwise
        // -1.
        int nameSpace = -1;
        boolean found = false;
        int ns = nameSpaces;
        while (ns != Machine.nilValue && !found) {
            nameSpace = machine.consHead(ns);
            ns = machine.consTail(ns);
            if (table == machine.objGetContents(nameSpace))
                found = true;
        }
        if (found)
            return nameSpace;
        else
            return -1;
    }

    public void hotLoad() {
        int obj = valueStack.top();
        registerHotLoad(obj);
    }

    public int index(int value) {

        // Returns the saved index of a value if it has been encountered
        // before. Otherwise returns -1...

        if (machine.hasSaveIndex(value))
            return machine.saveIndex(value);
        else
            return -1;
    }

    public boolean isElementType(int type) {

        // Type should be of the form:
        // Seq{1,Seq{"XCore","Element"}}

        return Machine.isCons(type) && machine.consHead(type) == Machine.mkInt(2)
                && isXCoreElement(machine.consHead(machine.consTail(type)));
    }

    public boolean isXCoreElement(int path) {
        return Machine.isCons(path) && isString(machine.consHead(path), "XCore")
                && isString(machine.consHead(machine.consTail(path)), "Element");
    }

    public boolean isString(int string1, String string2) {
        int length = string2.length();
        if (machine.stringLength(string1) == length) {
            boolean isString = true;
            for (int i = 0; i < length && isString; i++)
                isString = machine.stringRef(string1, i) == string2.charAt(i);
            return isString;
        } else
            return false;
    }

    public boolean isFunRef(int fun) {

        // Returns true when the function can be referenced by name in
        // a name space that is assumed to be present when the function
        // is re-loaded.

        int owner = machine.funOwner(fun);
        if (machine.includes(nameSpaces, owner)) {
            int contents = machine.objGetContents(owner);
            if (contents != -1)
                return machine.hashTableGet(contents, machine.funName(fun)) == fun;
            else
                return false;
        } else
            return false;
    }

    public boolean isNamedObj(int obj) {

        // An object is named if it has a name and an owner
        // slot...
        return Machine.isObj(obj) && machine.objGetName(obj) != -1 && machine.objGetOwner(obj) != -1;

    }

    public boolean isReference(int obj) {

        // A named element is a reference when it occurs in one of
        // the ref tables. It may be referenced because all entries
        // in the ref tables are assumed to persist across different
        // instantiations of XMF.

        return machine.isSaveAsLookup(obj) && machine.includes(nameSpaces, machine.objGetOwner(obj));
    }

    public boolean isStringDaemons(int value) {
        String daemons = "daemons";
        boolean isDaemons = true;
        if (Machine.isString(value) && machine.stringLength(value) == daemons.length()) {
            for (int i = 0; i < daemons.length() && isDaemons; i++)
                isDaemons = daemons.charAt(i) == machine.stringRef(value, i);
            return isDaemons;
        } else
            return false;
    }

    public int load(String file) {

        // Load and return a value from a file....

        try {
            FileInputStream in = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(in, 1024 * 50);
            int value = load(bin);
            in.close();
            return value;
        } catch (IOException ioe) {
            throw new Error(ioe.toString());
        }
    }

    public int load(InputStream in) {
        loadVersion(in);
        return loadDispatch(in);
    }

    public void loadArg() {
        int type = valueStack.pop();
        int name = valueStack.pop();
        valueStack.push(machine.mkCons(name, machine.mkCons(type, Machine.nilValue)));
        debug("ARG");
    }

    public void loadArray(InputStream in) {
        int length = read24(in);
        int daemonsActive = read8(in) == 1 ? Machine.trueValue : Machine.falseValue;
        int array = machine.mkArray(length);
        machine.arraySetDaemonsActive(array, daemonsActive);
        valueStack.push(array);
        values[index++] = array;
        debug("MKARRAY");
    }

    public int loadDispatch(InputStream in) {
        boolean complete = false;
        while (!complete) {
            int instr = read8(in);
            switch (instr) {
            case END:
                complete = true;
                loadEnd();
                break;
            case GLOBAL_DYNAMICS:
                loadGlobalDynamics();
                break;
            case HOTLOAD:
                hotLoad();
                break;
            case LOOKUP:
                lookup(in);
                break;
            case MKARG:
                loadArg();
                break;
            case MKARRAY:
                loadArray(in);
                break;
            case MKBUFFER:
                loadBuffer(in);
                break;
            case MKCLOSURE:
                loadClosure(in);
                break;
            case MKCODE:
                loadCode(in);
                break;
            case MKCONS:
                loadCons();
                break;
            case MKCONS_NONSHARE:
                loadConsNonShare();
                break;
            case MKCODEBOX:
                loadCodeBox(in);
                break;
            case MKDAEMON:
                loadDaemon();
                break;
            case MKDYNAMIC_TABLE:
                loadDynamicTable();
                break;
            case MK_ELEMENT_TYPE:
                loadElementType();
                break;
            case MKFALSE:
                loadFalse();
                break;
            case MKFALSESLOT:
                loadFalseSlot(in);
                break;
            case MKFOREIGNFUN:
                loadForeignFun(in);
                break;
            case MKFLOAT:
                loadFloat(in);
                break;
            case MKFORWARDREF:
                loadForwardRef();
                break;
            case MKFUN:
                loadFun(in);
                break;
            case MKGLOBALS:
                loadGlobals();
                break;
            case MKINT:
                loadInt(in);
                break;
            case MKINTSLOT:
                loadIntSlot(in);
                break;
            case MKLIST:
                loadList(in);
                break;
            case MKNEGINT:
                loadNegInt(in);
                break;
            case MKNIL:
                loadNil();
                break;
            case MKNILSLOT:
                loadNilSlot(in);
                break;
            case MKOBJ:
                loadObj(in);
                break;
            case MKSET:
                loadSet(in);
                break;
            case MKSETSLOT:
                loadSetSlot(in);
                break;
            case MKSIG:
                loadSig(in);
                break;
            case MKSLOT:
                loadSlot(in);
                break;
            case MKSLOTS:
                loadSlots(in);
                break;
            case MKSTRING:
                loadString(in);
                break;
            case MKSTRINGSLOT:
                loadStringSlot(in);
                break;
            case MKSYMBOL:
                loadSymbol(in);
                break;
            case MKTABLE:
                loadTable(in);
                break;
            case MKTRUE:
                loadTrue();
                break;
            case MKTRUESLOT:
                loadTrueSlot(in);
                break;
            case MKUNDEF:
                loadUndef();
                break;
            case NULLGLOBS:
                loadNullGlobs();
                break;
            case PUT:
                put();
                break;
            case REF:
                ref(in);
                break;
            case ROOT:
                pushRoot();
                debug("ROOT");
                break;
            case SETARRAY:
                setArray(in);
                break;
            case SETBUFFER:
                setBuffer();
                break;
            case SETCLOSURE:
                setClosure();
                break;
            case SETCODEBOX:
                setCodeBox();
                break;
            case SETCONS:
                setCons();
                break;
            case SETDAEMON:
                setDaemon();
                break;
            case SETFORWARDREF:
                setForwardRef();
                break;
            case SETFUN:
                setFun();
                break;
            case SETOBJ:
                setObj();
                break;
            default:
                throw new Error("Unknown load instruction " + instr);
            }
        }
        return result();
    }

    public void loadClosure(InputStream in) {
        int arity = read16(in);
        int fun = machine.mkFun();
        machine.funSetArity(fun, arity);
        valueStack.push(fun);
        values[index++] = fun;

    }

    public int closureCodeBox(int template) {
        int constants = machine.codeBoxConstants(machine.funCode(template));
        int length = machine.arrayLength(constants);
        for (int i = 0; i < length; i++) {
            int value = machine.arrayRef(constants, i);
            if (Machine.isCodeBox(value))
                return value;
        }
        return -1;
    }

    public void loadCodeBox(InputStream in) {
        int codeBox = machine.mkCodeBox(read16(in));
        int name = readSymbol(in);
        int source = readString(in);
        int resourceName = readString(in);
        machine.codeBoxSetName(codeBox, name);
        machine.codeBoxSetSource(codeBox, source);
        machine.codeBoxSetResourceName(codeBox, resourceName);
        values[index++] = codeBox;
        valueStack.push(codeBox);
        debug("CODEBOX");
    }

    public void loadCons() {
        int cons = machine.mkCons(Machine.undefinedValue, Machine.undefinedValue);
        values[index++] = cons;
        valueStack.push(cons);
        debug("CONS");
    }

    public void loadBuffer(InputStream in) {
        int buffer = machine.mkBuffer();
        machine.bufferSetIncrement(buffer, read16(in));
        machine.bufferSetSize(buffer, read16(in));
        machine.bufferSetDaemonsActive(buffer, read8(in) == 1 ? Machine.trueValue : Machine.falseValue);
        machine.bufferSetAsString(buffer, read8(in) == 1 ? Machine.trueValue : Machine.falseValue);
        values[index++] = buffer;
        valueStack.push(buffer);
        debug("BUFFER");
    }

    public void loadCode(InputStream in) {
        int length = read24(in);
        int code = machine.mkCode(length);
        for (int i = 0; i < length; i++) {
            int instr = read8(in);
            int operands = read24(in);
            machine.codeSet(code, i, Machine.mkImmediate(instr, operands));
        }
        valueStack.push(code);
        debug("CODE");
    }

    public void loadConsNonShare() {
        int cons = machine.mkCons(Machine.undefinedValue, Machine.undefinedValue);
        valueStack.push(cons);
    }

    public void loadDaemon() {
        int daemon = machine.mkDaemon();
        values[index++] = daemon;
        valueStack.push(daemon);
        debug("DAEMON");
    }

    public void loadDynamicTable() {
        int nameSpace = valueStack.pop();
        valueStack.push(machine.mkDynamicTable(machine.objGetContents(nameSpace)));
        debug("DYNAMICTABLE");
    }

    public void loadElementType() {
        if (elementType == -1) {
            int two = Machine.mkInt(2);
            int XCore = machine.mkString("XCore");
            int Element = machine.mkString("Element");
            int path = machine.mkCons(XCore, machine.mkCons(Element, Machine.nilValue));
            elementType = machine.mkCons(two, machine.mkCons(path, Machine.nilValue));
        }
        valueStack.push(elementType);
        debug("ELEMENTTYPE");
    }

    public void loadEnd() {
    }

    public void loadFalse() {
        valueStack.push(Machine.falseValue);
        debug("FALSE");
    }

    public void loadFalseSlot(InputStream in) {
        valueStack.push(machine.mkCons(readSymbol(in), Machine.falseValue));
        debug("FALSESLOT");
    }

    public void loadForeignFun(InputStream in) {
        int arity = read8(in);
        int name = valueStack.pop();
        int className = valueStack.pop();
        ForeignFun fun = new ForeignFun(machine.valueToString(className), machine.valueToString(name), arity);
        int foreignFun = machine.newForeignFun(fun);
        valueStack.push(foreignFun);
        debug("FOREIGNFUN");
    }

    public void loadFloat(InputStream in) {
        int stringRep = valueStack.pop();
        int f = machine.mkFloat(machine.valueToString(stringRep));
        valueStack.push(f);
        debug("FLOAT");
    }

    public void loadForwardRef() {
        int ref = machine.mkForwardRef(Machine.nilValue);
        valueStack.push(ref);
        values[index++] = ref;
        debug("MKFORWARDREF");
    }

    public void loadFun(InputStream in) {
        int fun = machine.mkFun();
        machine.funSetArity(fun, Machine.mkInt(read8(in)));
        values[index++] = fun;
        valueStack.push(fun);
        debug("FUN");
    }

    public void loadGlobals() {
        int prev = valueStack.pop();
        int array = valueStack.pop();
        valueStack.push(machine.mkGlobals(array, prev));
        debug("GLOBALS");
    }

    public void loadGlobalDynamics() {
        valueStack.push(machine.globalDynamics());
        debug("GLOBALDYNAMICS");
    }

    public void loadInt(InputStream in) {
        debug("INT");
        valueStack.push(Machine.mkInt(read24(in)));
    }

    public void loadIntSlot(InputStream in) {
        int name = readSymbol(in);
        int value = read24(in);
        valueStack.push(machine.mkCons(name, Machine.mkInt(value)));
        debug("INTSLOT");
    }

    public void loadList(InputStream in) {
        int length = read16(in);
        int list = Machine.nilValue;
        for (int i = 0; i < length; i++)
            list = machine.mkCons(valueStack.pop(), list);
        valueStack.push(list);
    }

    public void loadNegInt(InputStream in) {
        debug("NEGINT");
        valueStack.push(Machine.mkInt(-read24(in)));
    }

    public void loadNil() {
        debug("NIL");
        valueStack.push(Machine.nilValue);
    }

    public void loadNilSlot(InputStream in) {
        valueStack.push(machine.mkCons(readSymbol(in), Machine.nilValue));
        debug("NILSLOT");
    }

    public void loadNullGlobs() {
        debug("NULLGLOBS");
        if (nullGlobs == -1)
            nullGlobs = machine.mkGlobals(machine.emptyArray, Machine.undefinedValue);
        valueStack.push(nullGlobs);
    }

    public void loadObj(InputStream in) {
        int properties = read24(in);
        int daemonsActive = read8(in);
        int hotLoad = read8(in);
        int obj = machine.mkObj();
        machine.objSetProperties(obj, Machine.mkInt(properties));
        machine.objSetDaemonsActive(obj, daemonsActive == 1 ? Machine.trueValue : Machine.falseValue);
        machine.objSetHotLoad(obj, hotLoad == 1 ? Machine.trueValue : Machine.falseValue);
        valueStack.push(obj);
        values[index++] = obj;
        debug("OBJ");
    }

    public void loadSig(InputStream in) {
        int arity = read16(in);
        int type = valueStack.pop();
        int sig = machine.mkCons(type, Machine.nilValue);
        for (int i = 0; i < arity; i++)
            sig = machine.mkCons(valueStack.pop(), sig);
        valueStack.push(sig);
        debug("SIG");
    }

    public void loadSet(InputStream in) {
        int length = read24(in);
        int elements = Machine.nilValue;
        for (int i = 0; i < length; i++) {
            elements = machine.mkCons(valueStack.pop(), elements);
        }
        valueStack.push(machine.mkSet(elements));
        debug("MKSET");
    }

    public void loadSetSlot(InputStream in) {
        int name = readSymbol(in);
        loadSet(in);
        valueStack.push(machine.mkCons(name, valueStack.pop()));
        debug("SETSLOT");
    }

    public void loadSlot(InputStream in) {
        int name = readSymbol(in);
        int value = valueStack.pop();
        valueStack.push(machine.mkCons(name, value));
        debug("SLOT");
    }

    public void loadSlots(InputStream in) {
        int length = read16(in);
        int slots = Machine.nilValue;
        for (int i = 0; i < length; i++)
            slots = machine.mkCons(valueStack.pop(), slots);
        valueStack.push(slots);
        debug("SLOTS");
    }

    public void loadString(InputStream in) {
        int string = readString(in);
        valueStack.push(string);
        debugString("STRING", string);
    }

    public void loadStringSlot(InputStream in) {
        int name = readSymbol(in);
        int value = readString(in);
        valueStack.push(machine.mkCons(name, value));
        debug("STRINGSLOT");
    }

    public void loadSymbol(InputStream in) {
        int symbol = readSymbol(in);
        valueStack.push(symbol);
        debug("SYMBOL");
    }

    public void loadTable(InputStream in) {
        int table = machine.mkHashtable(read24(in));
        valueStack.push(table);
        values[index++] = table;
        debug("TABLE");
    }

    public void loadTrue() {
        valueStack.push(Machine.trueValue);
        debug("TRUE");
    }

    public void loadTrueSlot(InputStream in) {
        valueStack.push(machine.mkCons(readSymbol(in), Machine.trueValue));
        debug("TRUESLOT");
    }

    public void loadUndef() {
        valueStack.push(Machine.undefinedValue);
        debug("MKUNDEF");
    }

    public void loadVersion(InputStream in) {

        // The first instruction in a XAR file should be
        // VERSION. If this is not the case then report an
        // error and don't go any further...

        int instr = read8(in);
        if (instr == -1)
            throw new Error("EOF in XAR file");
        if (instr != VERSION)
            throw new Error("Not a XAR file (no VERSION)");
        debug("VERSION");
        majorVersion = read8(in);
        minorVersion = read8(in);
    }

    public void lookup(InputStream in) {
        int length = read16(in);
        buffer.setLength(0);
        for (int i = 0; i < length; i++)
            buffer.append((char) read8(in));
        int nameSpace = valueStack.pop();
        if (Machine.isForwardRef(nameSpace))
            lookupForwardRef(nameSpace, buffer);
        else {
            machine.pushStack(nameSpace);
            if (machine.getElement(buffer) == -1)
                forwardRef(nameSpace, buffer);
            else
                valueStack.push(machine.popStack());
            if (majorVersion > 1 || minorVersion > 8)
                values[index++] = valueStack.top();
            debug("LOOKUP", buffer);
        }
    }

    public void forwardRef(int nameSpace, CharSequence buffer) {
        int path = machine.mkCons(machine.mkSymbol(buffer), Machine.nilValue);
        while (nameSpace != root()) {
            path = machine.mkCons(machine.objGetName(nameSpace), path);
            nameSpace = machine.objGetOwner(nameSpace);
        }
        valueStack.push(machine.getForwardRef(path));
    }

    public void lookupForwardRef(int forwardRef, CharSequence buffer) {
        int path = machine.forwardRefPath(forwardRef);
        int symbol = machine.mkSymbol(buffer);
        path = machine.consAppend(path, machine.mkCons(symbol, Machine.nilValue));
        forwardRef = machine.getForwardRef(path);
        valueStack.push(forwardRef);
    }

    public void pushRoot() {
        valueStack.push(root());
    }

    public void put() {
        int key = valueStack.pop();
        int value = valueStack.pop();
        int table = valueStack.top();
        machine.hashTablePut(table, key, value);
        debug("PUT");
    }

    public int readString(InputStream in) {
        int length = read16(in);
        int string = machine.mkString(length);
        for (int i = 0; i < length; i++)
            machine.stringSet(string, i, read8(in));
        return string;
    }

    public int readSymbol(InputStream in) {
        return machine.mkSymbol(readString(in));
    }

    public int read24(InputStream in) {
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
        valueStack.push(values[read24(in)]);
    }

    public void registerHotLoad(int obj) {
        hotLoads = machine.mkCons(obj, hotLoads);
    }

    public void reset() {
        index = 0;
        valueStack.reset();
        hotLoads = Machine.nilValue;
        nameSpaces = Machine.nilValue;
        nullGlobs = -1;
        elementType = -1;
        root = -1;
        errorMessage = "";
        errorOccurred = false;
        //codeBoxTable.clear();
    }

    public void resetIndices() {

        // Shareable values that are saved in the values array
        // have all had their first words replaced with a SAVEINDEX
        // value. This must be reset before the saver returns...

        for (int i = 0; i < index; i = i + 2) {
            int value = values[i];
            int firstWord = values[i + 1];
            // System.err.println("RESET TAG : Address = " + Machine.value(value)
            // + " - " + Machine.tag(value) + " value = "
            // + Machine.value(firstWord));
            machine.set(Machine.ptr(value), firstWord);
        }
    }

    public int resolvePath(int path) throws MachineError {
        // Path is a '::' separated string.
        StringBuffer name = new StringBuffer();
        int root = machine.dynamicValue(machine.mkSymbol("Root"));
        int i = 0;
        int length = machine.stringLength(path);
        while (i < length) {
            int c = machine.stringRef(path, i++);
            if (c == ':') {
                root = machine.refNameSpace(root, name);
                i++;
                name.setLength(0);
            } else
                name.append((char) c);
        }
        return machine.refNameSpace(root, name);
    }

    public int result() {
        return machine.mkCons(valueStack.pop(), hotLoads);
    }

    public int root() {
        if (root == -1)
            root = machine.dynamicValue(machine.mkSymbol("Root"));
        return root;
    }

    public void save(int value, String file) {

        // Save the value to a file...

        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file), 1024 * 50);
            OutputStream xout = new FileOutputStream("C:/tmp.xml");
            save(out, value);
            out.close();
        } catch (FileNotFoundException e) {
            throw new Error(e.toString());
        } catch (IOException ioe) {
            throw new Error(ioe.toString());
        } catch (Throwable t) {
            t.printStackTrace();
            throw new Error(t.toString());
        }
    }

    public void save(OutputStream out, int value) {

        // Save the value to the output streamand terminate
        // with an END instruction...

        try {
            writeHeader(out);
            saveDispatch(out, value);
            resetIndices();
            writeEnd(out);
        } catch (Throwable t) {
            resetIndices();
            throw new Error(t.toString());
        }
    }

    public void saveDispatch(OutputStream out, int value) {

        // Dispatch on the type tag in the value to serialize it...

        switch (Machine.tag(value)) {
        case ARRAY:
            saveArray(out, value);
            break;
        case BOOL:
            saveBool(out, value);
            break;
        case BUFFER:
            saveBuffer(out, value);
            break;
        case CODE:
            saveCode(out, value);
            break;
        case CODEBOX:
            saveCodeBox(out, value);
            break;
        case CONS:
            saveCons(out, value);
            break;
        case DAEMON:
            saveDaemon(out, value);
            break;
        case FLOAT:
            saveFloat(out, value);
            break;
        case FOREIGNFUN:
            saveForeignFun(out, value);
            break;
        case FORWARDREF:
            saveForwardRef(out, value);
            break;
        case FUN:
            saveFun(out, value);
            break;
        case INT:
            saveInt(out, value);
            break;
        case NEGINT:
            saveNegInt(out, value);
            break;
        case NIL:
            saveNil(out);
            break;
        case OBJ:
            saveObj(out, value);
            break;
        case SET:
            saveSet(out, value);
            break;
        case STRING:
            saveString(out, value);
            break;
        case SYMBOL:
            saveSymbol(out, value);
            break;
        case HASHTABLE:
            saveTable(out, value);
            break;
        case UNDEFINED:
            saveNull(out);
            break;
        default:
            errorOccurred = true;
            errorMessage = "Unknown type of value to save [" + Machine.value(value) + "]:" + Machine.tag(value);
            saveString(out, machine.mkString("" + Machine.value(value)));
        }
    }

    public boolean saveAsRef(OutputStream out, int value) {

        // If the value has been encountered before then the
        // index has been saved in the first word. In this
        // case generate a REF instruction. Return true if a
        // reference was emitted...

        int index = index(value);
        if (index == -1)
            return false;
        else {
            writeInstr(out, REF);
            write24(out, index);
            return true;
        }
    }

    public void saveArray(OutputStream out, int array) {
        if (!saveAsRef(out, array)) {
            int length = machine.arrayLength(array);
            int daemonsActive = machine.arrayDaemonsActive(array);
            int daemons = machine.arrayDaemons(array);
            saveIndex(array, Machine.mkInt(length));
            writeInstr(out, MKARRAY);
            write24(out, length);
            if (daemonsActive == Machine.trueValue)
                write8(out, 1);
            else
                write8(out, 0);
            saveDispatch(out, daemons);
            for (int i = 0; i < length; i++)
                saveDispatch(out, machine.arrayRef(array, i));
            writeInstr(out, SETARRAY);
            write24(out, length);
        }
    }

    public void saveBasicSlot(OutputStream out, int name, int value) {
        saveDispatch(out, value);
        writeInstr(out, MKSLOT);
        writeString(out, machine.symbolName(name));
    }

    public void saveBool(OutputStream out, int bool) {
        if (bool == Machine.trueValue) {
            writeInstr(out, MKTRUE);
        } else {
            writeInstr(out, MKFALSE);
        }
    }

    public void saveBuffer(OutputStream out, int buffer) {
        if (!saveAsRef(out, buffer)) {
            int inc = machine.bufferIncrement(buffer);
            int daemons = machine.bufferDaemons(buffer);
            int active = machine.bufferDaemonsActive(buffer);
            int storage = machine.bufferStorage(buffer);
            int size = machine.bufferSize(buffer);
            int asString = machine.bufferAsString(buffer);
            saveIndex(buffer, inc);
            writeInstr(out, MKBUFFER);
            write16(out, Machine.value(inc));
            write16(out, Machine.value(size));
            write8(out, Machine.trueValue == active ? 1 : 0);
            write8(out, Machine.trueValue == asString ? 1 : 0);
            saveDispatch(out, daemons);
            saveDispatch(out, storage);
            writeInstr(out, SETBUFFER);
        }
    }

    public boolean saveClosure(OutputStream out, int fun) {
        int path = closurePath(fun);
        if (path == -1)
            return false;
        else {
            int arity = machine.funArity(fun);
            writeInstr(out, MKCLOSURE);
            write16(out, arity);
            saveIndex(fun, arity);
            saveDispatch(out, path);
            saveSig(out, machine.funSig(fun));
            saveDispatch(out, machine.at(machine.funGlobals(fun), 0));
            writeInstr(out, SETCLOSURE);
            return true;
        }
    }

    public int closurePath(int fun) {
        int properties = machine.funProperties(fun);
        int path = -1;
        while (properties != Machine.nilValue && path == -1) {
            int cell = machine.consHead(properties);
            int label = machine.consHead(cell);
            if (machine.stringEqual(label, "closure"))
                path = machine.consTail(cell);
            else
                properties = machine.consTail(properties);
        }
        return path;
    }

    public void saveBoolSlot(OutputStream out, int name, int value) {
        if (value == Machine.trueValue)
            saveTrueSlot(out, name);
        else
            saveFalseSlot(out, name);
    }

    public void saveCode(OutputStream out, int code) {
        int length = machine.codeLength(code);
        writeInstr(out, MKCODE);
        write24(out, length);
        for (int i = 0; i < machine.codeLength(code); i++) {
            int instr = machine.codeRef(code, i);
            write8(out, Machine.tag(instr));
            write24(out, Machine.value(instr));
        }
    }

    public void saveCodeBox(OutputStream out, int codeBox) {
        // START NEW CODE
        //String sourceString = machine.valueToString(machine.codeBoxSource(codeBox));
        //if (codeBoxTable.containsKey(sourceString) && codeBoxTable.get(sourceString) != codeBox) {
        //    System.err.println("Duplicate:\n" + sourceString);
        //    codeBox = codeBoxTable.get(sourceString);
        //} else
        //    codeBoxTable.put(sourceString, codeBox);
        // END NEW CODE
        if (!saveAsRef(out, codeBox)) {
            int locals = machine.codeBoxLocals(codeBox);
            int constants = machine.codeBoxConstants(codeBox);
            int instrs = machine.codeBoxInstrs(codeBox);
            int name = machine.codeBoxName(codeBox);
            int source = machine.codeBoxSource(codeBox);
            int resourceName = machine.codeBoxResourceName(codeBox);
            saveIndex(codeBox, locals);
            writeInstr(out, MKCODEBOX);
            write16(out, Machine.value(locals));
            writeString(out, machine.symbolName(name));
            writeString(out, source);
            writeString(out, resourceName);
            saveDispatch(out, constants);
            saveDispatch(out, instrs);
            writeInstr(out, SETCODEBOX);
        }
    }

    public void saveCons(OutputStream out, int word) {
        if (!saveAsRef(out, word)) {
            int head = machine.consHead(word);
            int tail = machine.consTail(word);
            saveIndex(word, head);
            writeInstr(out, MKCONS);
            saveDispatch(out, head);
            saveDispatch(out, tail);
            writeInstr(out, SETCONS);
        }
    }

    public void saveDaemon(OutputStream out, int daemon) {
        if (!saveAsRef(out, daemon)) {
            int id = machine.daemonId(daemon);
            int type = machine.daemonType(daemon);
            int slot = machine.daemonSlot(daemon);
            int action = machine.daemonAction(daemon);
            int persistent = machine.daemonPersistent(daemon);
            int traced = machine.daemonTraced(daemon);
            int target = machine.daemonTarget(daemon);
            saveIndex(daemon, id);
            writeInstr(out, MKDAEMON);
            saveDispatch(out, id);
            saveDispatch(out, type);
            saveDispatch(out, slot);
            saveDispatch(out, action);
            saveDispatch(out, persistent);
            saveDispatch(out, traced);
            saveDispatch(out, target);
            writeInstr(out, SETDAEMON);
        }
    }

    public void saveDynamics(OutputStream out, int dynamics) {
        if (dynamics == Machine.nilValue) {
            writeInstr(out, MKNIL);
        } else if (dynamics == machine.globalDynamics()) {
            writeInstr(out, GLOBAL_DYNAMICS);
        } else {
            if (!saveAsRef(out, dynamics)) {
                int dynamic = machine.consHead(dynamics);
                int tail = machine.consTail(dynamics);
                saveIndex(dynamics, dynamic);
                writeInstr(out, MKCONS);
                if (machine.isDynamicValue(dynamic))
                    saveDispatch(out, dynamic);
                else
                    saveDynamicTable(out, dynamic);
                saveDynamics(out, tail);
                writeInstr(out, SETCONS);
            }
        }
    }

    public void saveDynamicTable(OutputStream out, int dynamicTable) {
        int table = machine.dynamicCellValue(dynamicTable);
        int owner = findImportedNameSpace(machine, table);
        if (owner == -1)
            saveDispatch(out, dynamicTable);
        else {
            saveDispatch(out, owner);
            writeInstr(out, MKDYNAMIC_TABLE);
        }
    }

    public void saveFalseSlot(OutputStream out, int name) {
        writeInstr(out, MKFALSESLOT);
        writeString(out, machine.symbolName(name));
    }

    public void saveForeignFun(OutputStream out, int word) {
        if (!saveAsRef(out, word)) {
            ForeignFun fun = machine.foreignFun(machine.foreignFunIndex(word));
            String className = fun.className();
            String funName = fun.name();
            saveJavaString(out, className);
            saveJavaString(out, funName);
            writeInstr(out, MKFOREIGNFUN);
            write8(out, fun.arity());
        }
    }

    public void saveFloat(OutputStream out, int word) {
        saveDispatch(out, machine.floatString(word));
        writeInstr(out, MKFLOAT);
    }

    public void saveForwardRef(OutputStream out, int ref) {
        if (!saveAsRef(out, ref)) {
            int path = machine.forwardRefPath(ref);
            int value = machine.forwardRefValue(ref);
            int listeners = machine.forwardRefListeners(ref);
            saveIndex(ref, path);
            writeInstr(out, MKFORWARDREF);
            saveDispatch(out, path);
            saveDispatch(out, value);
            saveDispatch(out, listeners);
            writeInstr(out, SETFORWARDREF);
        }
    }

    public void saveFun(OutputStream out, int fun) {
        if (!saveAsRef(out, fun))
            if (isFunRef(fun))
                saveFunRef(out, fun);
            else {
                if (!saveClosure(out, fun)) {
                    int arity = machine.funArity(fun);
                    int globals = machine.funGlobals(fun);
                    int code = machine.funCode(fun);
                    int target = machine.funSelf(fun);
                    int supers = machine.funSupers(fun);
                    int dynamics = machine.funDynamics(fun);
                    int owner = machine.funOwner(fun);
                    int properties = machine.funProperties(fun);
                    int sig = machine.funSig(fun);
                    saveIndex(fun, arity);
                    writeInstr(out, MKFUN);
                    write8(out, Machine.value(arity));
                    saveGlobals(out, globals);
                    saveDispatch(out, code);
                    saveDispatch(out, target);
                    saveDynamics(out, dynamics);
                    saveDispatch(out, owner);
                    saveFunProperties(out, fun);
                    saveSig(out, sig);
                    writeInstr(out, SETFUN);
                }
            }
    }

    public void saveFunProperties(OutputStream out, int fun) {
        int properties = machine.funProperties(fun);
        int length = machine.consLength(properties);
        while (properties != Machine.nilValue) {
            int property = machine.consHead(properties);
            saveFunProperty(out, property);
            properties = machine.consTail(properties);
        }
        writeInstr(out, MKLIST);
        write16(out, length);
    }

    public void saveFunProperty(OutputStream out, int property) {
        int key = machine.consHead(property);
        int values = machine.consTail(property);
        if (isStringDaemons(key)) {
            writeInstr(out, MKCONS_NONSHARE);
            saveDispatch(out, key);
            saveProperList(out, filterPersistentDaemons(values));
            writeInstr(out, SETCONS);
        } else
            saveDispatch(out, property);
    }

    public void saveFunRef(OutputStream out, int fun) {

        // It has been established that the function can be reached by
        // name via a name space that is assumed to be present when the
        // function is reloaded. Calculate the path to the function and
        // save as a LOOKUP instruction.

        int owner = machine.funOwner(fun);
        saveLookup(out, owner);
        writeInstr(out, LOOKUP);
        writeString(out, machine.symbolName(machine.funName(fun)));
        saveIndex(fun, machine.funArity(fun));
    }

    public void saveGlobals(OutputStream out, int globals) {

        // A globals structure is an array. The first element of
        // the array is local storage for a function. The second
        // element is either null or another globals structure.

        if (isEmptyGlobals(globals)) {
            writeInstr(out, NULLGLOBS);
        } else {
            saveDispatch(out, machine.globalsArray(globals));
            saveGlobals(out, machine.globalsPrev(globals));
            writeInstr(out, MKGLOBALS);
        }
    }

    public boolean isEmptyGlobals(int globals) {

        // An empty globals structure has the form:
        // null or
        // Array(Array(),EMPTYGLOBS)

        return Machine.isNull(globals)
                || (Machine.isArray(globals) && machine.arrayLength(globals) == 2
                        && isEmptyArray(machine.arrayRef(globals, 0)) && isEmptyGlobals(machine.arrayRef(globals, 1)));
    }

    public boolean isEmptyArray(int value) {
        return Machine.isArray(value) && machine.arrayLength(value) == 0;
    }

    public void saveIndex(int value, int firstWord) {

        // Allocate a new index to the value. The first word
        // of the value is saved and replaced with a SAVEINDEX
        // value that will be discovered if the value is ever
        // re-saved via the save dispatch method. This makes
        // saving references very efficient...

        // System.err.println("TAG : Address = " + Machine.value(value) + " - "
        // + Machine.tag(value) + " index = " + (index - 2) / 2);
        values[index++] = value;
        values[index++] = firstWord;
        machine.setSaveIndex(value, (index - 2) / 2);
    }

    public void saveInt(OutputStream out, int word) {
        writeInstr(out, MKINT);
        write24(out, Machine.value(word));
    }

    public void saveIntSlot(OutputStream out, int name, int value) {
        writeInstr(out, MKINTSLOT);
        writeString(out, machine.symbolName(name));
        write24(out, Machine.value(value));
    }

    public void saveJavaString(OutputStream out, String string) {
        int length = string.length();
        writeInstr(out, MKSTRING);
        write16(out, length);
        for (int i = 0; i < length; i++)
            write8(out, string.charAt(i));
    }

    public void saveLookup(OutputStream out, int obj) {

        int name = machine.objGetName(obj);
        int owner = machine.objGetOwner(obj);

        if (name == -1)
            throw new MachineError(TYPE, "Cannot save type reference (no name) to " + machine.valueToString(obj));

        if (owner == -1)
            throw new MachineError(TYPE, "Cannot save type reference (no owner) to " + machine.valueToString(obj));

        if (owner == obj) {
            writeInstr(out, ROOT);
        } else {
            if (!saveAsRef(out, obj)) {
                int string = machine.symbolName(name);
                saveLookup(out, owner);
                writeInstr(out, LOOKUP);
                saveIndex(obj, machine.objType(obj));
                writeString(out, string);
            }
        }
    }

    public boolean saveNamedObj(OutputStream out, int obj) {
        if (isReference(obj)) {
            saveLookup(out, obj);
            return true;
        } else
            return false;
    }

    public void saveNegInt(OutputStream out, int word) {
        writeInstr(out, MKNEGINT);
        write24(out, Machine.value(word));
    }

    public void saveNil(OutputStream out) {
        writeInstr(out, MKNIL);
    }

    public void saveNilSlot(OutputStream out, int name) {
        writeInstr(out, MKNILSLOT);
        writeString(out, machine.symbolName(name));
    }

    public void saveNull(OutputStream out) {
        writeInstr(out, MKUNDEF);
    }

    public void saveObj(OutputStream out, int obj) {
        if (!saveAsRef(out, obj))
            if (!saveNamedObj(out, obj)) {
                int type = machine.objType(obj);
                int slots = machine.objAttributes(obj);
                int daemons = machine.objPersistentDaemons(obj);
                int properties = machine.objProperties(obj);
                int daemonsActive = machine.objDaemonsActive(obj);
                int hotLoad = machine.objHotLoad(obj);
                //System.err.println("MKOBJ: " + machine.valueToString(machine.objGetName(machine.type(obj))));
                saveIndex(obj, type);
                writeInstr(out, MKOBJ);
                write24(out, Machine.value(properties));
                write8(out, daemonsActive == Machine.trueValue ? 1 : 0);
                write8(out, hotLoad == Machine.trueValue ? 1 : 0);
                saveDispatch(out, type);
                saveSlots(out, slots, machine.consLength(slots));
                saveDispatch(out, daemons);
                writeInstr(out, SETOBJ);
                if (machine.isHotLoad(obj)) {
                    writeInstr(out, HOTLOAD);
                }
            }
    }

    public void saveProperList(OutputStream out, int list) {

        // The list is non-sharable and ends with NIL. Therefore
        // we can save the elements and pop them off the stack at
        // load time.

        int length = machine.consLength(list);
        while (list != Machine.nilValue) {
            int head = machine.consHead(list);
            saveDispatch(out, head);
            list = machine.consTail(list);
        }
        writeInstr(out, MKLIST);
        write16(out, length);
    }

    public void saveSet(OutputStream out, int set) {
        int elements = machine.setElements(set);
        while (elements != Machine.nilValue) {
            int element = machine.consHead(elements);
            elements = machine.consTail(elements);
            saveDispatch(out, element);
        }
        writeInstr(out, MKSET);
        write24(out, machine.setLength(set));
    }

    public void saveSetSlot(OutputStream out, int name, int set) {
        saveSetSlotElements(out, machine.setElements(set));
        writeInstr(out, MKSETSLOT);
        writeString(out, machine.symbolName(name));
        write24(out, machine.setLength(set));
    }

    public void saveSetSlotElements(OutputStream out, int elements) {
        while (elements != Machine.nilValue) {
            int element = machine.consHead(elements);
            elements = machine.consTail(elements);
            saveDispatch(out, element);
        }
    }

    public void saveSig(OutputStream out, int sig) {

        // A function signature is either null, Seq{} or a
        // sequence of argument definitions followined by the
        // return type of the function. An arg is a sequence
        // of the arg name and the arg type. A type is a
        // direct type (2) followed by a path or a parametric
        // type (1) followed by a path to a parameteric type
        // followed by a type path for the argument type.

        if (Machine.isNull(sig))
            saveDispatch(out, sig);
        else if (Machine.isNil(sig))
            saveDispatch(out, sig);
        else if (Machine.isCons(sig)) {
            int arity = machine.consLength(sig) - 1;
            while (!Machine.isNil(machine.consTail(sig))) {
                int arg = machine.consHead(sig);
                sig = machine.consTail(sig);
                saveSigArg(out, arg);
            }
            saveSigType(out, machine.consHead(sig));
            writeInstr(out, MKSIG);
            write16(out, arity);
        } else
            saveDispatch(out, sig);
    }

    public void saveSigArg(OutputStream out, int arg) {
        int name = machine.consHead(arg);
        int type = machine.consHead(machine.consTail(arg));
        saveDispatch(out, name);
        saveSigType(out, type);
        writeInstr(out, MKARG);
    }

    public void saveSigType(OutputStream out, int type) {

        // A type is a sequence. The head of the sequence
        // is a number and the tail is the data describing
        // where the type lives. Legal numbers are 2 for
        // named types or 1 for parameteric types.
        // A special case is the type Seq{2,Seq{"XCore","Element"}}
        // which is used by default when no type is specified.

        if (isElementType(type)) {
            writeInstr(out, MK_ELEMENT_TYPE);
        } else
            saveDispatch(out, type);
    }

    public void saveSlots(OutputStream out, int slots, int length) {
        if (slots == Machine.nilValue) {
            writeInstr(out, MKSLOTS);
            write16(out, length);
        } else {
            int slot = machine.consHead(slots);
            slots = machine.consTail(slots);
            saveSlot(out, slot);
            saveSlots(out, slots, length);
        }
    }

    public void saveSlot(OutputStream out, int slot) {
        int name = machine.consHead(slot);
        int value = machine.consTail(slot);
        switch (Machine.tag(value)) {
        case INT:
            saveIntSlot(out, name, value);
            break;
        case STRING:
            saveStringSlot(out, name, value);
            break;
        case BOOL:
            saveBoolSlot(out, name, value);
            break;
        case NIL:
            saveNilSlot(out, name);
            break;
        case SET:
            saveSetSlot(out, name, value);
            break;
        default:
            saveBasicSlot(out, name, value);
        }
    }

    public void saveString(OutputStream out, int string) {
        int length = machine.stringLength(string);
        writeInstr(out, MKSTRING);
        write16(out, length);
        for (int i = 0; i < machine.stringLength(string); i++)
            write8(out, machine.stringRef(string, i));
    }

    public void saveStringSlot(OutputStream out, int name, int value) {
        writeInstr(out, MKSTRINGSLOT);
        writeString(out, machine.symbolName(name));
        writeString(out, value);
    }

    public void saveSymbol(OutputStream out, int symbol) {
        int string = machine.symbolName(symbol);
        writeInstr(out, MKSYMBOL);
        writeString(out, string);
    }

    public void saveTable(OutputStream out, int table) {
        if (!saveAsRef(out, table)) {
            int length = machine.arrayLength(table);
            saveIndex(table, length);
            writeInstr(out, MKTABLE);
            write24(out, Machine.value(length));
            for (int i = 0; i < Machine.value(length); i++) {
                int bucket = machine.arrayRef(table, i);
                while (bucket != Machine.nilValue) {
                    int cell = machine.consHead(bucket);
                    bucket = machine.consTail(bucket);
                    int key = machine.consHead(cell);
                    int value = machine.consTail(cell);
                    saveDispatch(out, value);
                    saveDispatch(out, key);
                    writeInstr(out, PUT);
                }
            }
        }
    }

    public void saveTrueSlot(OutputStream out, int name) {
        writeInstr(out, MKTRUESLOT);
        writeString(out, machine.symbolName(name));
    }

    public void setArray(InputStream in) {
        int length = read24(in);
        int array = valueStack.fromTop(length + 1);
        for (int i = length - 1; i >= 0; i--)
            machine.arraySet(array, i, valueStack.pop());
        machine.arraySetDaemons(array, valueStack.pop());
        debug("SETARRAY");
    }

    public void setBuffer() {
        int storage = valueStack.pop();
        int daemons = valueStack.pop();
        int buffer = valueStack.top();
        machine.bufferSetStorage(buffer, storage);
        machine.bufferSetDaemons(buffer, daemons);
        debug("SETBUFFER");
    }

    public void setClosure() {
        int data = valueStack.pop();
        int sig = valueStack.pop();
        int path = valueStack.pop();
        int fun = valueStack.top();
        int newGlobals = machine.mkArray(2);
        int template = resolvePath(path);
        if (template == -1)
            machine.error(NAMESPACEERR, "Cannot find " + machine.valueToString(path));
        else {
            int codeBox = closureCodeBox(template);
            int closureCell = machine.mkCons(machine.mkString("closure"), path);
            int properties = machine.mkCons(closureCell, Machine.nilValue);
            machine.arraySet(newGlobals, 0, data);
            machine.arraySet(newGlobals, 1, machine.funGlobals(fun));
            machine.funSetSig(fun, sig);
            machine.funSetGlobals(fun, newGlobals);
            machine.funSetCode(fun, codeBox);
            machine.funSetProperties(fun, properties);
        }
    }

    public void setCodeBox() {
        int instrs = valueStack.pop();
        int constants = valueStack.pop();
        int codeBox = valueStack.top();
        machine.codeBoxSetInstrs(codeBox, instrs);
        machine.codeBoxSetConstants(codeBox, constants);
        debug("SETCODEBOX");
    }

    public void setCons() {
        int tail = valueStack.pop();
        int head = valueStack.pop();
        int cons = valueStack.top();
        machine.consSetHead(cons, head);
        machine.consSetTail(cons, tail);
        debug("SETCONS");
    }

    public void setDaemon() {
        int target = valueStack.pop();
        int traced = valueStack.pop();
        int persistent = valueStack.pop();
        int action = valueStack.pop();
        int slot = valueStack.pop();
        int type = valueStack.pop();
        int id = valueStack.pop();
        int daemon = valueStack.top();
        machine.daemonSetId(daemon, id);
        machine.daemonSetType(daemon, type);
        machine.daemonSetSlot(daemon, slot);
        machine.daemonSetAction(daemon, action);
        machine.daemonSetPersistent(daemon, persistent);
        machine.daemonSetTraced(daemon, traced);
        machine.daemonSetTarget(daemon, target);
        debug("SETDAEMON");
    }

    public void setForwardRef() {
        int listeners = valueStack.pop();
        int value = valueStack.pop();
        int path = valueStack.pop();
        int ref = valueStack.top();
        machine.forwardRefSetPath(ref, path);
        machine.forwardRefSetValue(ref, value);
        machine.forwardRefSetListeners(ref, listeners);
    }

    public void setFun() {
        int sig = valueStack.pop();
        int properties = valueStack.pop();
        int owner = valueStack.pop();
        int dynamics = valueStack.pop();
        int target = valueStack.pop();
        int code = valueStack.pop();
        int globals = valueStack.pop();
        int fun = valueStack.top();
        machine.funSetGlobals(fun, globals);
        machine.funSetCode(fun, code);
        machine.funSetSelf(fun, target);
        machine.funSetDynamics(fun, dynamics);
        machine.funSetOwner(fun, owner);
        machine.funSetProperties(fun, properties);
        machine.funSetSig(fun, sig);
        machine.funSetSupers(fun, machine.mkCons(fun, Machine.nilValue));
        debug("SETFUN");
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public void setNameSpaces(int nameSpaces) {
        this.nameSpaces = nameSpaces;
    }

    public void setObj() {
        int daemons = valueStack.pop();
        int slots = valueStack.pop();
        int type = valueStack.pop();
        int obj = valueStack.top();
        machine.objSetDaemons(obj, daemons);
        machine.objSetAttributes(obj, slots);
        machine.objSetType(obj, type);
        debug("SETOBJ");
    }

    public void writeInstr(OutputStream out, int instr) {
        write8(out, instr);
    }

    public void write8(OutputStream out, int value) {
        if (value > 0xFF)
            throw new Error("ValueSaver.write8 " + value + " > 0xFF");

        try {
            out.write(value & 0xFF);
        } catch (IOException e) {
            throw new Error("ValueSaver.write8");
        }
    }

    public void write16(OutputStream out, int value) {
        if (value > 0xFFFF)
            throw new Error("ValueSaver.write16 " + value + " > 0xFFFF");
        try {
            out.write((value & 0xFF00) >> 8);
            out.write(value & 0xFF);
        } catch (IOException e) {
            throw new Error("ValueSaver.write16");
        }
    }

    public void write24(OutputStream out, int value) {
        if (value > 0xFFFFFF)
            throw new Error("ValueSaver.write24 " + value + " > 0xFFFFFF");

        try {
            int high = (value & 0xFF0000) >> 16;
            int medium = (value & 0x00FF00) >> 8;
            int low = value & 0x0000FF;
            out.write(high);
            out.write(medium);
            out.write(low);
        } catch (IOException e) {
            throw new Error("ValueSaver.write24.");
        }
    }

    public void writeEnd(OutputStream out) {
        writeInstr(out, END);
    }

    public void writeHeader(OutputStream out) {
        writeInstr(out, VERSION);
        write8(out, VERSION_MAJOR);
        write8(out, VERSION_MINOR);
    }

    public void writeString(OutputStream out, int string) {
        int length = machine.stringLength(string);
        write16(out, length);
        for (int i = 0; i < machine.stringLength(string); i++)
            write8(out, machine.stringRef(string, i));
    }

}
