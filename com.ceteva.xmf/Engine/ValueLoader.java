package Engine;

import java.io.*;

public class ValueLoader extends SaveLoadMachine implements Errors {

    // A value loader reads and interprets load instructions from a
    // given file. The values are created in the given heap; pointers
    // to the values in the heap are maintained on the value stack.

    private static final int VALUE_STACK_SIZE = 1024 * 10;

    private Machine          machine;                                            // The machine that provides access to data constructors

    private InputStream      in;                                                 // The stream of load instructions.

    private ValueStack       valueStack       = new ValueStack(VALUE_STACK_SIZE); // Save values as they are consed.

    private int              hotLoads         = Machine.nilValue;                // Objects that run their 'hotLoad' operation.

    private StringBuffer     string           = new StringBuffer();              // Used for reading string chars.

    private int              elementType      = -1;                              // Used to cache the value of Seq(2,Seq{"XCore","Element"}}

    private int              nullGlobs        = -1;                              // Used to cache Array(Array(),null) for function globals.

    public ValueLoader(Machine machine) {
        this.machine = machine;
    }

    public void setInput(String fileName) {
        try {
            this.in = new FileInputStream(fileName);
        } catch (IOException e) {
            throw new Error("ValueLoader: " + e.toString());
        }
    }

    public void setInput(InputStream in) {
        this.in = new BufferedInputStream(in);
    }

    public void reset() {
        super.reset();
        hotLoads = Machine.nilValue;
        valueStack.reset();
        elementType = -1;
        nullGlobs = -1;
    }

    public void registerHotLoad(int obj) {
        hotLoads = machine.mkCons(obj, hotLoads);
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            throw new Error("ValueLoader.close");
        }
    }

    public int read24() {
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

    public int read16() {
        // Read a 16 bit value from the input stream.
        try {
            int high = in.read();
            int low = in.read();
            return (high << 8) | low;
        } catch (IOException e) {
            throw new Error("ValueLoader.read16");
        }
    }

    public int read8() {
        // Read an 8 bit value from the input stream.
        try {
            int low = in.read();
            return low;
        } catch (IOException e) {
            throw new Error("ValueLoader.read8");
        }
    }

    public int load1() {
        return machine.consHead(load());
    }

    public int load() throws MachineError {

        // Interpret the load instructions in the input stream.
        // Return a sequence consisting of the loaded value at the
        // head of the machine stack and the hot loaded objects.

        int instr;
        //int count = 0;
        while (!isNewSerializer() && (instr = read8()) != -1) {
            //System.err.println((count++) + " " + instrToString(instr) + " : STACK POS = " + valueStack.index);
            //System.out.println(valueStack.toString(machine));
            switch (instr) {
            case MKTRUE:
                loadTrue();
                break;
            case MKFALSE:
                loadFalse();
                break;
            case MKFUN:
                loadFun();
                break;
            case SETFUN:
                setFun();
                break;
            case MKINT:
                loadInt();
                break;
            case MKSTRING:
                loadString();
                break;
            case MKARRAY:
                loadArray();
                break;
            case MKARRAY_NONSHARE:
                loadArrayNonShare();
                break;
            case SETARRAY:
                setArray();
                break;
            case REFARRAY:
                refStack(ARRAYSTACKBASE);
                break;
            case REFSTRING:
                refStack(STRINGSTACKBASE);
                break;
            case REFOBJ:
                refStack(OBJSTACKBASE);
                break;
            case REFFUN:
                refStack(FUNSTACKBASE);
                break;
            case REFCONS:
                refStack(CONSSTACKBASE);
                break;
            case REFSYMBOL:
                refStack(SYMBOLSTACKBASE);
                break;
            case REFVALUE:
                refStack(VALUESTACKBASE);
                break;
            case MKCODEBOX:
                loadCodeBox();
                break;
            case SETCODEBOX:
                setCodeBox();
                break;
            case MKOBJ:
                loadObj();
                break;
            case SETOBJ:
                setObj();
                break;
            case MKCODE:
                loadCode();
                break;
            case MKUNDEF:
                valueStack.push(Machine.undefinedValue);
                break;
            case MKCONS:
                loadCons();
                break;
            case MKCONS_NONSHARE:
                loadConsNonShare();
                break;
            case SETCONS:
                setCons();
                break;
            case MKNIL:
                valueStack.push(Machine.nilValue);
                break;
            case MKSYMBOL:
                loadSymbol();
                break;
            case MKSYMBOL2:
                loadSymbol2();
                break;
            case MKSET:
                loadSet();
                break;
            case TABLE:
                loadTable();
                break;
            case PUT:
                put();
                break;
            case LOOKUP:
                lookup();
                break;
            case MKFOREIGNFUN:
                loadForeignFun();
                break;
            case GLOBAL_DYNAMICS:
                valueStack.push(machine.globalDynamics());
                break;
            case MKDYNAMIC_TABLE:
                loadDynamicTable();
                break;
            case MKNEGINT:
                loadNegInt();
                break;
            case MKFLOAT:
                loadFloat();
                break;
            case MKNEWLISTENER:
                loadNewListener();
                break;
            case HOTLOAD:
                hotLoad();
                break;
            case MKLIST:
                loadList();
                break;
            case SETOBJ2:
                setObj2();
                break;
            case MKSET2:
                loadSet2();
                break;
            case LISTCONS:
                listCons();
                break;
            case MKSIG:
                loadSig();
                break;
            case MKARG:
                loadArg();
                break;
            case MK_ELEMENT_TYPE:
                loadElementType();
                break;
            case NULLGLOBS:
                loadNullGlobs();
                break;
            case MKGLOBALS:
                loadGlobals();
                break;
            case MKDAEMON:
                loadDaemon();
                break;
            case SETDAEMON:
                setDaemon();
                break;
            case LOOKUP2:
            case LOOKUPFUN:
                lookup2();
                break;
            case MKNULLSLOT:
                loadSlot(Machine.undefinedValue);
                break;
            case MKNILSLOT:
                loadSlot(Machine.nilValue);
                break;
            case MKEMPTYSLOT:
                loadSlot(machine.emptySet);
                break;
            case MKZEROSLOT:
                loadSlot(Machine.mkInt(0));
                break;
            case MKTRUESLOT:
                loadSlot(Machine.trueValue);
                break;
            case MKFALSESLOT:
                loadSlot(Machine.falseValue);
                break;
            case END:
                return machine.mkCons(valueStack.pop(), hotLoads);
            case VERSION:
                setMajorVersion(read8());
                setMinorVersion(read8());
                break;
            case MKBUFFER:
                loadBuffer();
                break;
            case SETBUFFER:
                setBuffer();
                break;
            case MKCLOSURE:
                loadClosure();
                break;
            case SETCLOSURE:
                setClosure();
                break;
            default:
                throw new Error("ValueLoader.load: unknown load instruction: " + instr);
            }
        }
        if(isNewSerializer())
            return useNewSerializer();
        else
        return machine.mkCons(valueStack.pop(), hotLoads);
    }
    
    public int useNewSerializer() {
        machine.serializer().reset();
        machine.serializer().setMajorVersion(loadMajorVersion);
        machine.serializer().setMinorVersion(loadMinorVersion);
        return machine.serializer().loadDispatch(in);
    }

    public void loadInt() {
        valueStack.push(Machine.mkInt(read24()));
    }

    public void loadNegInt() {
        valueStack.push(Machine.mkInt(-read24()));
    }

    public void loadTrue() {
        valueStack.push(Machine.trueValue);
    }

    public void loadFalse() {
        valueStack.push(Machine.falseValue);
    }

    public void loadFun() {
        int arity = read16();
        int fun = machine.mkFun();
        machine.funSetArity(fun, arity);
        valueStack.push(fun);
        saveValue(fun);
    }

    public void setFun() {

        // Note that we currently do not set the supers. This needs to be updated.

        int doc = valueStack.pop();
        int sig = valueStack.pop();
        int properties = valueStack.pop();
        int owner = valueStack.pop();
        int dynamics = valueStack.pop();
        int self = valueStack.pop();
        int codeBox = valueStack.pop();
        int globals = valueStack.pop();
        int fun = valueStack.top();

        machine.funSetProperties(fun, properties);
        machine.funSetDocumentation(fun, doc);
        machine.funSetSig(fun, sig);
        machine.funSetOwner(fun, owner);
        machine.funSetDynamics(fun, dynamics);
        machine.funSetSelf(fun, self);
        machine.funSetGlobals(fun, globals);
        machine.funSetCode(fun, codeBox);
    }

    public void loadString() {
        int length = read16();
        int string = machine.mkString(length);
        for (int i = 0; i < length; i++)
            machine.stringSet(string, i, read8());
        //System.err.println("  string = '" + machine.valueToString(string) + "'");
        valueStack.push(string);
    }

    public void loadArray() {
        int length = read16();
        int array = machine.mkArray(length);
        valueStack.push(array);
        saveValue(array);
    }

    public void loadArrayNonShare() {
        int length = read16();
        int array = machine.mkArray(length);
        valueStack.push(array);
    }

    public void setArray() {
        int length = read16();
        int array = valueStack.fromTop(length);
        for (int i = length; i > 0; i--)
            machine.arraySet(array, i - 1, valueStack.pop());
    }

    public void loadBuffer() {
        int buffer = machine.mkBuffer();
        valueStack.push(buffer);
        saveValue(buffer);
    }

    public void setBuffer() {
        int buffer = valueStack.fromTop(6);
        machine.bufferSetAsString(buffer, valueStack.pop());
        machine.bufferSetSize(buffer, valueStack.pop());
        machine.bufferSetStorage(buffer, valueStack.pop());
        machine.bufferSetDaemonsActive(buffer, valueStack.pop());
        machine.bufferSetDaemons(buffer, valueStack.pop());
        machine.bufferSetIncrement(buffer, valueStack.pop());
    }

    public void refStack(int stackBase) {
        int index = read24();
        int value = ref(stackBase + index);
        valueStack.push(value);
    }

    public void loadClosure() {
        int arity = read16();
        int fun = machine.mkFun();
        machine.funSetArity(fun, arity);
        valueStack.push(fun);
        saveValue(fun);
        
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
            int closureCell = machine.mkCons(machine.mkString("closure"),path);
            int properties = machine.mkCons(closureCell,Machine.nilValue);
            machine.arraySet(newGlobals, 0, data);
            machine.arraySet(newGlobals, 1, machine.funGlobals(fun));
            machine.funSetSig(fun, sig);
            machine.funSetGlobals(fun, newGlobals);
            machine.funSetCode(fun, codeBox);
            machine.funSetProperties(fun,properties);
        }
    }
    
    public int closureCodeBox(int template) {
        int constants = machine.codeBoxConstants(machine.funCode(template));
        int length = machine.arrayLength(constants);
        for(int i = 0; i < length; i++) {
            int value = machine.arrayRef(constants,i);
            if(Machine.isCodeBox(value))
                return value;
        }
        return -1;
    }

    public void loadCodeBox() {
        int codeBox = machine.mkCodeBox(read24());
        valueStack.push(codeBox);
        saveValue(codeBox);
    }

    public void setCodeBox() {
        int codeBox = valueStack.fromTop(5);
        machine.codeBoxSetResourceName(codeBox, valueStack.pop());
        machine.codeBoxSetSource(codeBox, valueStack.pop());
        machine.codeBoxSetName(codeBox, valueStack.pop());
        machine.codeBoxSetInstrs(codeBox, valueStack.pop());
        machine.codeBoxSetConstants(codeBox, valueStack.pop());
    }

    public void loadCode() {
        int length = read24();
        int code = machine.mkCode(length);
        for (int i = 0; i < length; i++) {
            int instr = read8();
            int operands = read24();
            machine.codeSet(code, i, Machine.mkImmediate(instr, operands));
        }
        valueStack.push(code);
        saveValue(code);
    }

    public void loadObj() {
        int obj = machine.mkObj();
        valueStack.push(obj);
        saveValue(obj);
    }

    public void setObj() {
        int properties = valueStack.pop();
        int daemons = valueStack.pop();
        int atts = valueStack.pop();
        int type = valueStack.pop();
        int obj = valueStack.top();
        // Legacy, for upgrading - delete when no legacy projects exist...
        if (Machine.isInt(properties))
            machine.objSetProperties(obj, properties);
        else
            machine.objSetDaemonsActive(obj, properties);
        machine.objSetDaemons(obj, daemons);
        machine.objSetType(obj, type);
        machine.objSetAttributes(obj, atts);
    }

    public void setObj2() {

        // Object attributes are spread out on the stack and the number
        // of attributes is an instruction operand.

        int type = valueStack.pop();
        int properties = valueStack.pop();
        int daemons = valueStack.pop();
        int atts = Machine.nilValue;
        int length = read16();
        for (int i = 0; i < length; i++) {
            int name = valueStack.pop();
            int value = valueStack.pop();
            atts = machine.mkCons(machine.mkAttribute(name, value), atts);
        }
        int obj = valueStack.top();
        machine.objSetProperties(obj, properties);
        machine.objSetDaemons(obj, daemons);
        machine.objSetType(obj, type);
        machine.objSetAttributes(obj, atts);
    }

    public int resolveRef(int path) throws MachineError {
        int root = machine.dynamicValue(machine.mkSymbol("Root"));
        while (path != Machine.nilValue) {
            int name = machine.consHead(path);
            if (!Machine.isSymbol(name))
                machine.error(TYPE, "ValueLoader expects path names to be sequences of symbols: " + machine.valueToString(name));
            machine.refNameSpace(root, name);
            root = machine.popStack();
            path = machine.consTail(path);
        }
        return root;
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
                name.append((char)c);
        }
        return machine.refNameSpace(root, name);
    }

    public void loadCons() {
        int cons = machine.mkCons(Machine.undefinedValue, Machine.undefinedValue);
        valueStack.push(cons);
        saveValue(cons);
    }

    public void loadConsNonShare() {
        int cons = machine.mkCons(Machine.undefinedValue, Machine.undefinedValue);
        valueStack.push(cons);
    }

    public void setCons() {
        int tail = valueStack.pop();
        int head = valueStack.pop();
        int cons = valueStack.top();
        machine.consSetHead(cons, head);
        machine.consSetTail(cons, tail);
    }

    public void loadSymbol() {
        int string = valueStack.pop();
        int symbol = machine.mkSymbol(string);
        saveValue(symbol);
        //System.err.println("  loadSymbol = " + machine.valueToString(symbol));
        valueStack.push(symbol);
    }

    public void loadSymbol2() {

        // MKSYMBOL2 expects the symbol name after the instruction in the
        // input stream. The characters are loaded into a string buffer and
        // we look for an existing symbol with the given name. If none exists
        // then the symbol is consed.

        int length = read16();
        string.delete(0, string.length());
        for (int i = 0; i < length; i++)
            string.append((char) read8());
        int symbol = machine.findSymbol(string);
        if (symbol == -1)
            symbol = machine.mkSymbol(machine.mkString(string.toString()));
        //System.err.println("  loadSymbol2 = " + machine.valueToString(symbol));
        valueStack.push(symbol);
    }

    public void loadSet() {
        int elements = valueStack.pop();
        valueStack.push(machine.mkSet(elements));
    }

    public void loadSet2() {
        int length = read16();
        int elements = Machine.nilValue;
        for (int i = 0; i < length; i++)
            elements = machine.mkCons(valueStack.pop(), elements);
        valueStack.push(machine.mkSet(elements));
    }

    public void put() {
        int key = valueStack.pop();
        int value = valueStack.pop();
        int table = valueStack.top();
        machine.hashTablePut(table, key, value);
    }

    public void lookup() throws MachineError {
        int path = valueStack.pop();
        int obj = resolveRef(path);
        valueStack.push(obj);
        //System.err.println("  lookup = " + machine.valueToString(obj));
        saveValue(obj);
    }

    public void lookup2() throws MachineError {
        int pathLength = read8();
        int path = Machine.nilValue;
        for (int i = 0; i < pathLength; i++)
            path = machine.mkCons(valueStack.pop(), path);
        int obj = resolveRef(path);
        valueStack.push(obj);
        //System.err.println("  lookup2 = " + machine.valueToString(obj));
        saveValue(obj);
    }

    public void loadTable() {
        int table = machine.mkHashtable(1024);
        saveValue(table);
        valueStack.push(table);
    }

    public void loadForeignFun() {
        int arity = read8();
        int name = valueStack.pop();
        int className = valueStack.pop();
        ForeignFun fun = new ForeignFun(machine.valueToString(className), machine.valueToString(name), arity);
        int foreignFun = machine.newForeignFun(fun);
        saveValue(foreignFun);
        valueStack.push(foreignFun);
    }

    public void loadDynamicTable() {
        int nameSpace = valueStack.pop();
        valueStack.push(machine.mkDynamicTable(machine.objGetContents(nameSpace)));
    }

    public void loadFloat() {
        int stringRep = valueStack.pop();
        int f = machine.mkFloat(machine.valueToString(stringRep));
        saveValue(f);
        valueStack.push(f);
    }

    public void loadNewListener() {
        int fun = valueStack.pop();
        machine.registerNewListener(fun);
        valueStack.push(fun);
    }

    public void hotLoad() {
        int obj = valueStack.top();
        registerHotLoad(obj);
    }

    public void loadList() {
        int length = read16();
        int list = Machine.nilValue;
        for (int i = 0; i < length; i++)
            list = machine.mkCons(valueStack.pop(), list);
        valueStack.push(list);
    }

    public void listCons() {
        int head = valueStack.pop();
        int cons = valueStack.top();
        machine.consSetHead(cons, head);
        machine.consSetTail(cons, Machine.nilValue);
    }

    public void loadArg() {
        int type = valueStack.pop();
        int name = valueStack.pop();
        valueStack.push(machine.mkCons(name, machine.mkCons(type, Machine.nilValue)));
    }

    public void loadSig() {
        int arity = read16();
        int type = valueStack.pop();
        int sig = machine.mkCons(type, Machine.nilValue);
        for (int i = 0; i < arity; i++)
            sig = machine.mkCons(valueStack.pop(), sig);
        valueStack.push(sig);
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
    }

    public void loadNullGlobs() {
        if (nullGlobs == -1)
            nullGlobs = machine.mkGlobals(machine.emptyArray, Machine.undefinedValue);
        valueStack.push(nullGlobs);
    }

    public void loadGlobals() {
        int prev = valueStack.pop();
        int array = valueStack.pop();
        valueStack.push(machine.mkGlobals(array, prev));
    }

    public void loadDaemon() {
        int daemon = machine.mkDaemon();
        valueStack.push(daemon);
        saveValue(daemon);
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
    }

    public void loadSlot(int value) {
        valueStack.push(value);
        loadSymbol2();
    }

}