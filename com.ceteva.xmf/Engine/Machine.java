package Engine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import xjava.AlienObject;
import Engine.Undo.UndoEngine;
import XOS.DChannel;
import XOS.DataInputStream;
import XOS.Message;
import XOS.MessagePacket;
import XOS.OperatingSystem;
import XOS.TChannel;
import XOS.XData;

public final class Machine implements Instr, Value, Errors, SignalHandler {

    // The VM executes machine instructions with respect to a stack and
    // a heap. Machine words are Java integers in which the top byte is
    // used as a tag field and the rest is used to encode data. Stacks
    // and heaps are all integer arrays, therefore pointers are Java ints
    // that index array elements. The machine implements its own heap
    // management including a stop and copy garbage collector.

    // Constants and default values.

    public final static int     BYTE1                     = 0x000000FF;

    public final static int     BYTE2                     = 0x0000FF00;

    public final static int     BYTE3                     = 0x00FF0000;

    public final static int     BYTE4                     = 0xFF000000;

    public final static int     PTR                       = BYTE1 | BYTE2 | BYTE3;

    public final static int     DATA                      = BYTE1 | BYTE2 | BYTE3;

    public final static int     K                         = 1024;

    public final static int     HEAPSIZE                  = 30 * K;

    public final static int     STACKSIZE                 = 50 * K;

    // Daemon types ...

    // Fires on any slot value change.

    public static final int     DAEMON_ANY                = mkInt(0);

    // Fires when the valuechanges.

    public static final int     DAEMON_VALUE              = mkInt(1);

    // Fires when a value is addedto a collection.

    public static final int     DAEMON_ADD                = mkInt(2);

    // Fires when a value is removed from a collection.

    public static final int     DAEMON_SUB                = mkInt(3);

    // Object properties.

    public final static int     OBJ_DAEMONS_ACTIVE        = 0;

    public final static int     OBJ_HOT_LOAD              = 1;

    public final static int     OBJ_SAVE_AS_LOOKUP        = 2;

    public final static int     OBJ_DEFAULT_GET_MOP       = 3;

    public final static int     OBJ_DEFAULT_SET_MOP       = 4;

    public final static int     OBJ_DEFAULT_SEND_MOP      = 5;

    public final static int     OBJ_NOT_VM_NEW            = 6;

    public final static int     OBJ_DEFAULT_PROPS         = 1 << OBJ_DAEMONS_ACTIVE;

    // Machine control.

    public int                  heapSize                  = HEAPSIZE;

    public int                  stackSize                 = STACKSIZE;

    public int                  gcLimit                   = HEAPSIZE - K;

    // Try to keep this amount available.

    public int                  freeHeap                  = 10 * K;

    // Increase when heap is exhausted.

    public int                  incHeap                   = 10 * K;

    public int                  operatorTableSize         = K;

    public int                  constructorTableSize      = 100;

    public String               initFile                  = null;

    public String               imageFile                 = null;

    public Vector               imageArgs                 = new Vector();

    public boolean              interrupt                 = false;

    // Frame offsets.

    public static final int     PREVFRAME                 = 0;

    public static final int     PREVOPENFRAME             = 1;

    public static final int     FRAMECODEBOX              = 2;

    public static final int     FRAMECODEINDEX            = 3;

    public static final int     FRAMEGLOBALS              = 4;

    public static final int     FRAMEDYNAMICS             = 5;

    public static final int     FRAMELOCALS               = 6;

    public static final int     FRAMESELF                 = 7;

    public static final int     FRAMESUPER                = 8;

    public static final int     FRAMEHANDLER              = 9;

    public static final int     FRAMELINECOUNT            = 10;

    public static final int     FRAMECHARCOUNT            = 11;

    public static final int     FRAMELOCAL0               = 12;

    // Value constants that are accessible at the program level.

    public static final int     trueValue                 = mkBool(1);

    public static final int     falseValue                = mkBool(0);

    public static final int     nilValue                  = mkNil();

    public static final int     undefinedValue            = mkUndefined();

    public int                  emptyArray                = mkUndefined();

    public int                  emptySet                  = mkUndefined();

    // Debugging.

    // Where to send debug output to...

    private PrintStream         debugOutput               = System.out;

    // true when tracing machine instructions...

    public boolean              traceInstr                = false;

    // true when tracing call frames.

    public boolean              traceFrames               = false;

    // Used to control indentation of output.

    public int                  indent                    = 0;

    // true when printing/ machine stats on completion.

    public boolean              stats                     = false;

    // true when printing a stack dump on error.

    public boolean              stackDump                 = false;

    public int                  maxPrintElements          = 30;

    public int                  maxPrintDepth             = 20;

    // Calculating the time. The following can be used to calculate the duration
    // of activities. Use resetTime() and elapsedTime(). Time0 is used to record
    // the beginning of time.

    public long                 time                      = System.currentTimeMillis();

    private long                time0                     = System.currentTimeMillis();

    private String[]            XVMargSpecs               = { "-instr:0", "-frames:0", "-stats:0", "-heapSize:1",
            "-stackSize:1", "-initFile:1", "-freeHeap:1", "-stackDump:0", "-image:1", "-arg:1" };

    // The following maintain various stats on categories of memory usage. The
    // gc variants of the stats counters are used to cache the counts during a
    // garbage collect.

    public Memory               memory                    = new Memory(time, null);

    public int                  instrsPerformed           = 0;

    public int                  dynamicLookupSteps        = 0;

    public int                  calls                     = 0;

    // A heap is an array of words. Each word is tagged with a
    // type tag. The freePtr is the index of the next available
    // free word in the heap.

    private int[]               words;

    private int                 freePtr                   = 0;

    // The garbage collection mechanism is stop-and-copy. When GC is
    // required, the current heap is scanned and all reachable data
    // is copied into a new heap. When this is complete the new heap
    // becomes current. When GC is invoked, values may be saved on
    // the stack by the garbage collector for tidy up after the sweep
    // has occurred. The collector uses gcTOS to mark the top of the
    // stack for this purpose.

    private int[]               gcWords;

    private int                 gcFreePtr                 = 0;

    private int                 gcCopiedPtr               = 0;

    private int                 gcTOS                     = 0;

    // Symbols are symbolic names. Their names are strings. It is important
    // that two equal strings produce the same symbol. Symbols are maintained
    // in a table. Keys are strings and values are symbols.

    private int                 symbolTable;

    // When creating definitions, there may be forward references. The
    // following table contains known forward references that need to
    // be resolved. Each forward reference contains a path.

    private int                 forwardRefs               = Machine.nilValue;

    // The VM knows about its operating system. The operating system provides
    // access to the outside world and schedules the VM threads.

    private OperatingSystem     XOS;

    // The machine operates is multi-threaded. Each thread has its own stack.
    // The threads are maintained on a cyclic list called threads.

    private Thread              threads;

    // The following flag is set when the current thread yields. The machine
    // will then halt and control will trabsfer to the operating system.

    private boolean             yield                     = false;

    // The machine operates by pushing and popping values on the stack.
    // The stack contains stack frames; a stack frame is pushed when
    // functions are called or methods are accessed. A frame contains
    // information such as as the current code, the code index and
    // a pointer to the return frame.

    private ValueStack          valueStack;

    // The current frame describes information relating to the currently
    // executing code. In particular it contains the code, an index into
    // the code, the args and locals and the return address.

    private int                 currentFrame              = -1;

    // The currently open frame is the frame under construction. For example
    // this occurs when a function call has started but is currently
    // executing its argument sub-expressions.

    private int                 openFrame                 = -1;

    // The foreign functions are maintained in a table of objects
    // that all implement the foreign function interface.

    private Stack               foreignFuns               = new Stack();

    // The foreign objects are maintained in a table. Foreign objects are
    // Java objects whose fields and methods are available via the calculus
    // '.' operator.

    private Stack               foreignObjects            = new Stack();

    // The Alien objects are maintained in a table

    private Stack               AlienObjects              = new Stack();

    // Serialized data is loaded by the machine using a value loader.

    private ValueLoader         valueLoader               = new ValueLoader(this);

    private Serializer          serializer                = new Serializer(this);

    // Serialized data is saved by the machine using a value saver.

    // The machine must know about a handful of objects that implement key
    // data types. These are set at boot time.

    public int                  theClassElement           = mkUndefined();

    public int                  theClassException         = mkUndefined();

    public int                  theClassForeignObject     = mkUndefined();

    public int                  theClassForeignOperation  = mkUndefined();

    public int                  theClassForwardRef        = mkUndefined();

    public int                  theClassBind              = mkUndefined();

    public int                  theClassBuffer            = mkUndefined();

    public int                  theClassCodeBox           = mkUndefined();

    public int                  theClassClass             = mkUndefined();

    public int                  theClassPackage           = mkUndefined();

    public int                  theClassDataType          = mkUndefined();

    public int                  theClassDaemon            = mkUndefined();

    public int                  theClassCompiledOperation = mkUndefined();

    public int                  theClassSymbol            = mkUndefined();

    public int                  theClassTable             = mkUndefined();

    public int                  theClassThread            = mkUndefined();

    public int                  theClassVector            = mkUndefined();

    public int                  theTypeBoolean            = mkUndefined();

    public int                  theTypeInteger            = mkUndefined();

    public int                  theTypeFloat              = mkUndefined();

    public int                  theTypeString             = mkUndefined();

    public int                  theTypeSeqOfElement       = mkUndefined();

    public int                  theTypeSetOfElement       = mkUndefined();

    public int                  theTypeSeq                = mkUndefined();

    public int                  theTypeSet                = mkUndefined();

    public int                  theTypeNull               = mkUndefined();

    // Various symbols must be known to the machine. They are defined here.
    // The machine permits '.' to be used with lists to extract the head
    // and tail. The symbols operations and parents are used in the default
    // message passing protocol.

    private int                 theSymbolArity            = mkUndefined();

    private int                 theSymbolAttributes       = mkUndefined();

    private int                 theSymbolContents         = mkUndefined();

    private int                 theSymbolDefault          = mkUndefined();

    private int                 theSymbolDot              = mkUndefined();

    private int                 theSymbolInvoke           = mkUndefined();

    private int                 theSymbolInit             = mkUndefined();

    private int                 theSymbolMachineInit      = mkUndefined();

    private int                 theSymbolName             = mkUndefined();

    private int                 theSymbolOperations       = mkUndefined();

    private int                 theSymbolOwner            = mkUndefined();

    private int                 theSymbolParents          = mkUndefined();

    private int                 theSymbolType             = mkUndefined();

    private int                 theSymbolFire             = mkUndefined();

    private int                 theSymbolValue            = mkUndefined();

    private int                 theSymbolDocumentation    = mkUndefined();

    private int                 theSymbolHead             = mkUndefined();

    private int                 theSymbolTail             = mkUndefined();

    private int                 theSymbolIsEmpty          = mkUndefined();

    private int                 theSymbolNewListener      = mkUndefined();

    // Certain dynamic variables are available everywhere. The following dynamics
    // structure records those dynamics and tables that are available globally.
    // This is used, for example when a file is loaded to record the dynamics
    // that are imported by default.

    private int                 globalDynamics            = nilValue;

    // The operator table caches the list of operators defined by a classifier.
    // The table is used by the message passing instructions.

    private int                 operatorTable             = mkUndefined();

    // The constructor table maps classes to sequences of name sequences
    // that are used when classes hav default instantiation semantics...

    private int                 constructorTable          = mkUndefined();

    // The following table maps classes to sequences of operators that listen
    // for the classes to be instantiated. It is up to the upper level code to use
    // this table as appropriate - it can be accessed via Kernel_newListeners. A
    // function marks itself as a new listener via its 'newListener' property,
    // the value of which is a sequence of classes that it acts as a new listener for.
    // When the value saver encounters an operator that is marked in this way, it
    // must arrange for the operator to be added to this table when the operator
    // is re-loaded.

    private int                 newListenersTableSize     = K;

    private int                 newListenersTable         = mkUndefined();

    // Zip files are indexed by the name of the file to allow multiple random
    // access to the component entries.

    private Hashtable           zipFiles                  = new Hashtable();

    // Each saved image has a header that describes properties of the
    // image.

    private Header              header                    = defaultHeader();

    // If all else fails a backtrace is dumped to the folowing file...

    private static StringBuffer buffer                    = new StringBuffer();

    private String              backtraceDumpFile         = "C:/XMFDump";

    // The undo engine...

    public UndoEngine           undo                      = new UndoEngine();

    public Header defaultHeader() {
        Header header = new Header();
        header.setProperty("Tool", "XMF-Mosaic");
        header.setProperty("Version", "1.0");
        return header;
    }

    public Header header() {
        return header;
    }

    public int newListeners() {
        return newListenersTable;
    }

    public void registerNewListener(int classifier, int fun) {
        int classes = hashTableGet(newListenersTable, classifier);
        if (classes == -1)
            hashTablePut(newListenersTable, classifier, mkCons(fun, nilValue));
        else
            hashTablePut(newListenersTable, classifier, mkCons(fun, classes));
    }

    // *************************************************************************
    // * Initialization *
    // *************************************************************************

    public Machine(OperatingSystem XOS) {
        this.XOS = XOS;
    }

    public void init() {
        // initialise the data structures.
        if (imageFile == null) {
            words = new int[heapSize];
            valueStack = new ValueStack(stackSize);
            threads = new Thread("INIT", valueStack, 0, 0);
        }
        gcWords = new int[heapSize];
        gcLimit = heapSize - freeHeap;
        initConstants();
    }

    void initConstants() {
        if (imageFile == null) {
            emptyArray = mkEmptyArray();
            emptySet = mkEmptySet();
            initSymbols();
            operatorTable = mkHashtable(operatorTableSize);
            constructorTable = mkHashtable(constructorTableSize);
            newListenersTable = mkHashtable(newListenersTableSize);
        }
    }

    void initSymbols() {
        symbolTable = mkHashtable(5000);
        theSymbolAttributes = mkSymbol("attributes");
        theSymbolDefault = mkSymbol("default");
        theSymbolInit = mkSymbol("init");
        theSymbolMachineInit = mkSymbol("machineInit");
        theSymbolType = mkSymbol("type");
        theSymbolOperations = mkSymbol("operations");
        theSymbolParents = mkSymbol("parents");
        theSymbolName = mkSymbol("name");
        theSymbolArity = mkSymbol("arity");
        theSymbolOwner = mkSymbol("owner");
        theSymbolContents = mkSymbol("contents");
        theSymbolInvoke = mkSymbol("invoke");
        theSymbolDot = mkSymbol("dot");
        theSymbolFire = mkSymbol("fire");
        theSymbolValue = mkSymbol("value");
        theSymbolDocumentation = mkSymbol("documentation");
        theSymbolHead = mkSymbol("head");
        theSymbolTail = mkSymbol("tail");
        theSymbolIsEmpty = mkSymbol("isEmpty");
        theSymbolNewListener = mkSymbol("newListener");
    }

    public void resetOperatorTable() {
        hashTableClear(operatorTable);
        hashTableClear(constructorTable);
    }

    public void loadImage() {

        // If an image file has been specified then load the image.
        // We may have specified an arg to return as the result of
        // the original save image call.

        if (imageFile != null) {
            load(imageFile);
            setDynamicValue(mkSymbol("Kernel_stdout"), mkImmediate(INPUT_CHANNEL, 0));
            setDynamicValue(mkSymbol("Kernel_stdin"), mkImmediate(OUTPUT_CHANNEL, 0));
            XOS.imageLoaded();
            valueStack.push(imageArgs());
        }
    }

    public int imageArgs() {
        int args = nilValue;
        for (int i = 0; i < imageArgs.size(); i = i + 2) {
            String key = (String) imageArgs.elementAt(i);
            String value = (String) imageArgs.elementAt(i + 1);
            args = mkCons(mkCons(mkString(key), mkString(value)), args);
        }
        return args;
    }

    public void loadInit() {
        // Initialise the machine from the given binary file.
        if (initFile != null) {
            valueLoader.reset();
            valueLoader.setInput(initFile);
            int codeBox = valueLoader.load1();
            valueLoader.close();
            init(codeBox);
        }
    }

    // *************************************************************************
    // * Initialization End *
    // *************************************************************************

    public int loadBin(String fileName) {
        // Loads a binary file and turns it into a function.
        valueLoader(fileName);
        int codeBox = valueLoader.load1();
        valueLoader.close();
        return codeBoxToFun(codeBox, 0, globalDynamics);
    }

    public int loadBin(InputStream in) {
        // Loads a binary file and turns it into a function.
        valueLoader(in);
        int codeBox = valueLoader.load1();
        valueLoader.close();
        return codeBoxToFun(codeBox, 0, globalDynamics);
    }

    public int codeBoxToFun(int codeBox, int arity, int dynamics) {
        int fun = mkFun();
        funSetArity(fun, mkInt(arity));
        funSetGlobals(fun, undefinedValue);
        funSetCode(fun, codeBox);
        funSetSelf(fun, undefinedValue);
        funSetOwner(fun, undefinedValue);
        funSetSupers(fun, mkCons(fun, nilValue));
        funSetDynamics(fun, dynamics);
        return fun;
    }

    public ValueLoader valueLoader(String fileName) {
        valueLoader.reset();
        valueLoader.setInput(fileName);
        return valueLoader;
    }

    public ValueLoader valueLoader(InputStream in) {
        valueLoader.reset();
        valueLoader.setInput(in);
        return valueLoader;
    }

    public int loadValue(String fileName) throws MachineError {
        if (needsGC())
            gc();
        valueLoader(fileName);
        int value = valueLoader.load();
        // valueLoader.printStats();
        return value;
    }

    public int loadValue(InputStream in) throws MachineError {
        if (needsGC())
            gc();
        valueLoader(in);
        int value = valueLoader.load();
        // valueLoader.printStats();
        return value;
    }

    public int save(int value, String fileName) {
        throw new Error("Machine.save/2 is deprecated");
    }

    public int save(int value, OutputStream out) {
        throw new Error("Machine.save/2 is deprecated");
    }

    // The following replace the two save definitions above.

    public String save(int value, String fileName, int nameSpaces) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            return save(value, fout, nameSpaces);
        } catch (Throwable t) {
            return t.getMessage();
        }
    }

    public String save(int value, OutputStream out, int nameSpaces) {
        try {
            BufferedOutputStream buf = new BufferedOutputStream(out, 1024 * 50);
            serializer.reset();
            serializer.setNameSpaces(nameSpaces);
            serializer.save(buf, value);
            buf.flush();
            if(serializer.errorOccurred)
                return serializer.errorMessage;
            else return null;
        } catch (Throwable t) {
            return t.getMessage();
        }
    }

    public Serializer serializer() {
        return serializer;
    }

    public void setDebugOutputFile(String file) {
        try {
            debugOutput = new PrintStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }

    public void resetSaveLoad() {
        valueLoader.reset();
    }

    public int result() {
        return valueStack.top();
    }

    public void pushStack(int value) {
        valueStack.push(value);
    }

    public int popStack() {
        return valueStack.pop();
    }

    public int heapSize() {
        return heapSize;
    }

    public int usedHeap() {
        return freePtr;
    }

    public void printStats(PrintStream out) {
        long now = System.currentTimeMillis();
        float elapsedTimeMillis = (now - time0) / 1000F;
        // float MIPS = (instrsPerformed / elapsedTimeMillis) / 1000000F;
        out.println();
        out.println("Machine Statistics");
        out.println("------------------");
        out.println(calls + " function calls");
        out.println(dynamicLookupSteps + " dynamic lookup steps.");
        out.println(foreignFuns.size() + " foreign functions allocated.");
        out.println(foreignObjects.size() + " foreign objects allocated.");
        out.println(threadsAllocated() + " threads allocated.");
        out.println("operator table = " + valueToString(operatorTable));
        out.println("constructor table = " + valueToString(constructorTable));
        out.println("symbol table = " + valueToString(symbolTable));
        out.println("undo = [" + undo.undoStackSize() + "," + undo.undoCommandSize() + "]");
        out.println("redo = [" + undo.redoStackSize() + "," + undo.redoCommandSize() + "]");
        out.println(instrsPerformed + " instructions performed.");
        out.println("time since boot = " + elapsedTimeMillis + " sec");
    }

    // The following can be used to print out indented messages.

    public void incIndent() {
        indent += 2;
    }

    public void decIndent() {
        indent -= 2;
    }

    public void indent(String message) {
        for (int i = 0; i < indent; i++)
            System.out.print(" ");
        System.out.print(message);
    }

    public void indentln(String message) {
        indent(message);
        System.out.println();
    }

    // *************************************************************************
    // * Heap Management *
    // *************************************************************************

    // Access bytes in machine words.

    public static final int mkWord(int byte4, int byte3, int byte2, int byte1) {
        return byte1(byte4) << 24 | byte1(byte3) << 16 | byte1(byte2) << 8 | byte1(byte1);
    }

    public static final int byte1(int word) {
        return word & BYTE1;
    }

    public static final int byte2(int word) {
        return (word & BYTE2) >>> 8;
    }

    public static final int byte3(int word) {
        return (word & BYTE3) >>> 16;
    }

    public static final int byte4(int word) {
        return (word & BYTE4) >>> 24;
    }

    public static final boolean isVarArgs(int arity) {
        // The top bit of a functions arity byte is set if the
        // function can take multiple arguments.
        return (arity & 0x80) != 0;
    }

    public static final int stripVarArgs(int arity) {

        // The arity operand in a MKFUN or MKFUNE instruction contains
        // a top bit that is set when the function takes var args.
        // The bit is stripped when the arity in a function is set.

        return arity & 0x7F;
    }

    // Raw access to the heap is restricted to 'alloc', 'ref' and 'set'

    public int alloc(int length) {

        // Allocate 'length' words of heap return a
        // raw pointer to the new storage.

        int ptr = -1;
        if ((freePtr + length) < words.length) {
            ptr = freePtr;
            freePtr = freePtr + length;
            return ptr;
        } else
            return allocFails(length);
    }

    public int allocFails(int requested) {

        // Try to extend the heap. This may fail due to the OS
        // refusing to allocate more memory. If so we print a
        // message and die.

        if (!extendHeap()) {
            System.out.println("\n****** Alloc Fails **********\n");
            System.out.println("Request to allocate " + requested + " words of memory failed.");
            System.out
                    .println("Available memory is " + (words.length - freePtr) + " out of " + words.length + " words");
            System.out.println("Current GC limit is " + gcLimit + " (available < " + freeHeap
                    + ") words allocated before GC.");
            System.out.println("Current value of freePtr is " + freePtr);
            saveBacktrace(currentFrame);
            System.out.println("Backtrace Saved in " + backtraceDumpFile);
            throw new MachineError(ALLOCERROR, "Memory allocation failed.");

        }
        return alloc(requested);
    }

    public boolean extendHeap() {

        // Called when the system runs out of memory and wants to extend the
        // heap by the amount defined by incHeap.

        System.out.println("[ Heap exhausted, increase by " + incHeap + " words. ]");
        System.out.println("[ Re-allocating heap at " + (words.length + incHeap) + " words. ]");
        return extendHeap(incHeap);
    }

    public boolean extendHeap(int amount) {

        // Extend the heap by the supplied amount. Returns true when the
        // extension
        // is successful and false otherwise.

        try {
            int[] newWords = new int[words.length + amount];
            for (int i = 0; i < words.length; i++)
                newWords[i] = words[i];
            words = newWords;
            gcWords = new int[words.length + amount];
            heapSize = words.length;
            gcLimit = heapSize - freeHeap;
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void memStat() {

        // If you are having problems with memory management then you can use
        // this routine to print out the current state of the VM heap in
        // XMF words.

        System.out.println("Free memory:" + calc(Runtime.getRuntime().freeMemory()));
        System.out.println("Max memory: " + calc(Runtime.getRuntime().maxMemory()));
        System.out.println("Total memory: " + calc(Runtime.getRuntime().totalMemory()));
    }

    public static long calc(long value) {
        return (value / 4);
    }

    public boolean needsGC() {
        return freePtr > gcLimit;
    }

    public final int ref(int ptr) {
        // Return the machine word at the given
        // heap location.
        return words[ptr];
    }

    public final void set(int ptr, int value) {
        // Set the contents of the heap location at
        // 'ptr' to be value.
        words[ptr] = value;
    }

    public void printHeap() {
        for (int i = 0; i < words.length; i++)
            System.out.println("[" + i + "] " + tag(words[i]) + ":" + value(words[i]));
    }

    // The machine uses a tagged architecture. Each machine value is a Java
    // int that is tagged with a type tag (defined in Value.java). The following
    // methods are used to manipulate machine words by decomposing them into
    // their components. There are two types of machine word:
    //
    // o Immediate. An immediate encodes its value in the machine word.
    // o Pointer. A pointer tags an index into the heap, the value starts
    // at this index.

    public static final int tag(int word) {
        // Get the type tag on a machine word.
        return (word & BYTE4) >>> 24;
    }

    public static final int ptr(int word) {
        // Get the pointer part of a machine word.
        return word & PTR;
    }

    public static final int value(int word) {
        // Get the value part of a machine word.
        return word & DATA;
    }

    public static final int mkImmediate(int tag, int value) {
        // Combine the tag in the top byte of the word
        // with the value in the bottom 24 bits. Returns
        // a machine word.
        return (tag << 24) | (value & DATA);
    }

    public static final int mkPtr(int tag, int value) {
        // Combine the tag in the top byte of the word
        // with the pointer in the bottom 24 bits. Returns
        // a machine word.
        return (tag << 24) | (value & DATA);
    }

    public static final int getBit(int word, int bit) {
        int mask = 1 << bit;
        return (word & mask) >> bit;
    }

    public static final int setBit(int word, int bit, int value) {
        int allSet = 0xFFFFFF;
        int reset = (1 << bit) ^ allSet;
        int resetWord = word & reset;
        return resetWord | (value << bit);
    }

    // *************************************************************************
    // * Heap Management End *
    // *************************************************************************

    // *************************************************************************
    // * Machine Data Structures *
    // *************************************************************************

    // All values have a type that is implemented as an object. A small number
    // of
    // basic types are known to the machine. All other types are object types.

    public int type(int value) {
        switch (value >> 24) {
        case ARRAY:
            return theClassVector;
        case BOOL:
            return theTypeBoolean;
        case BUFFER:
            return theClassBuffer;
        case CODEBOX:
            return theClassCodeBox;
        case FUN:
            return theClassCompiledOperation;
        case FOREIGNFUN:
            return theClassForeignOperation;
        case FORWARDREF:
            return theClassForwardRef;
        case FOREIGNOBJ:
            return theClassForeignObject;
        case INT:
        case NEGINT:
            return theTypeInteger;
        case ALIENOBJ:
        case OBJ:
            return objType(value);
        case STRING:
            return theTypeString;
        case CONS:
        case NIL:
            return theTypeSeqOfElement;
        case SET:
            return theTypeSetOfElement;
        case HASHTABLE:
            return theClassTable;
        case SYMBOL:
            return theClassSymbol;
        case UNDEFINED:
            return theTypeNull;
        case FLOAT:
            return theTypeFloat;
        case THREAD:
            return theClassThread;
        case DAEMON:
            return theClassDaemon;
        default:
            return theClassElement;
        }
    }

    public int getName(int value) {
        // Many values have names. This returns the name of the value
        // or -1 if the value has no name.
        switch (tag(value)) {
        case OBJ:
            return objGetName(value);
        case FOREIGNFUN:
            return foreignFunName(value);
        case FUN:
            return funName(value);
        default:
            return -1;
        }
    }

    // Arrays are a fixed size contiguous sequence of machine words. The length
    // of the array is added as an extra word at the start of the data elements.

    public int mkEmptyArray() {
        int ptr = alloc(3);
        set(ptr, mkInt(0)); // length
        set(ptr + 1, trueValue); // daemonsActive
        set(ptr + 2, nilValue); // daemons
        return mkPtr(ARRAY, ptr);
    }

    public int mkArray(int length) {
        // Return a machine word for a freshly allocated
        // array.
        if (length == 0 && emptyArray != undefinedValue)
            return emptyArray;
        memory.alloc(ARRAY, length + ARRAY_HEADER);
        int ptr = alloc(length + ARRAY_HEADER);
        set(ptr, mkInt(value(length)));
        set(ptr + 1, trueValue);
        set(ptr + 2, nilValue);
        return mkPtr(ARRAY, ptr);
    }

    public static boolean isArray(int word) {
        return tag(word) == ARRAY;
    }

    public int arrayLength(int word) {
        return value(ref(ptr(word)));
    }

    public int arrayDaemonsActive(int array) {
        return ref(ptr(array) + 1);
    }

    public void arraySetDaemonsActive(int array, int active) {
        set(ptr(array) + 1, active);
    }

    public int arrayDaemons(int array) {
        return ref(ptr(array) + 2);
    }

    public void arraySetDaemons(int array, int daemons) {
        undo.setArrayDaemons(array,daemons,arrayDaemons(array));
        set(ptr(array) + 2, daemons);
    }

    public int arrayRef(int word, int index) {
        // Reference the machine word at the index
        // in the array.
        return ref(ptr(word) + index + 3);
    }

    public void arraySet(int word, int index, int value) {
        // Set the machine word at the given index
        // of the array.
        undo.setArray(word, index, value, ref(ptr(word) + index + 3));
        set(ptr(word) + index + 3, value);
    }

    public String arrayToString(int word, int depth) {
        // Return a string representation of an array.
        String s = "Array(";
        for (int i = 0; i < arrayLength(word) && i < maxPrintElements; i++) {
            s = s + valueToString(arrayRef(word, i), depth + 1);
            if ((i + 1) < arrayLength(word))
                s = s + ",";
        }
        if (arrayLength(word) > maxPrintElements)
            s = s + "...";
        return s + ")";
    }

    public int arrayAsSeq(int array) {
        int seq = nilValue;
        for (int i = arrayLength(array) - 1; i >= 0; i--)
            seq = mkCons(arrayRef(array, i), seq);
        return seq;
    }

    public int arrayAsString(int array) {
        int string = mkString(arrayLength(array));
        for (int i = 0; i < arrayLength(array); i++)
            stringSet(string, i, value(arrayRef(array, i)));
        return string;
    }

    // A buffer is a dynamic array. The storage for a buffer is a list of
    // arrays. Each array has a size specified in the dynamic array header.
    // Values are indexed in the list of arrays as though they were contiguous
    // storage. If an element is added beyond the current storage then
    // the buffer storage is extended to contain the indexed element.
    // The SIZE of a buffer is the current number of elements it contains.
    // The LENGTH of a buffer is the total amount of storage it has available.
    // If the buffer is to be interpreted as a string then the asString flag
    // is set. XMF should then treat the buffer as a normal string except that
    // characters can be destrictively added to the string.

    public int mkBuffer() {
        int buffer = mkPtr(BUFFER, alloc(BUFFER_SIZE));
        memory.alloc(BUFFER, BUFFER_SIZE);
        bufferSetIncrement(buffer, mkInt(0));
        bufferSetDaemons(buffer, nilValue);
        bufferSetDaemonsActive(buffer, trueValue);
        bufferSetStorage(buffer, nilValue);
        bufferSetSize(buffer, mkInt(0));
        bufferSetAsString(buffer, falseValue);
        return buffer;
    }

    public int mkBuffer(int increment) {
        int buffer = mkBuffer();
        bufferSetIncrement(buffer, mkInt(increment));
        bufferSetDaemons(buffer, nilValue);
        bufferSetDaemonsActive(buffer, trueValue);
        bufferSetStorage(buffer, mkCons(mkArray(increment), nilValue));
        bufferSetSize(buffer, mkInt(0));
        bufferSetAsString(buffer, falseValue);
        return buffer;
    }

    public static final boolean isBuffer(int value) {
        return tag(value) == BUFFER;
    }

    public int copyBuffer(int buffer) {
        int copy = mkBuffer(value(bufferIncrement(buffer)));
        bufferSetDaemons(copy, bufferDaemons(buffer));
        bufferSetDaemonsActive(copy, bufferDaemonsActive(buffer));
        bufferSetStorage(copy, copyBufferStorage(bufferStorage(buffer)));
        bufferSetSize(copy, bufferSize(buffer));
        bufferSetAsString(copy, bufferAsString(buffer));
        return copy;
    }

    public boolean bufferStringEqual(int buffer, int string) {
        if (bufferAsString(buffer) == trueValue) {
            if (value(bufferSize(buffer)) == stringLength(string)) {
                for (int i = 0; i < value(bufferSize(buffer)); i++)
                    if (value(bufferRef(buffer, i)) != stringRef(string, i))
                        return false;
                return true;
            } else
                return false;
        } else
            return false;
    }

    public int copyBufferStorage(int arrays) {
        if (arrays == nilValue)
            return arrays;
        else {
            int array = consHead(arrays);
            return mkCons(copy(array), copyBufferStorage(consTail(arrays)));
        }
    }

    public int bufferLength(int buffer) {
        return value(bufferIncrement(buffer)) * consLength(bufferStorage(buffer));
    }

    public int bufferIncrement(int buffer) {
        return ref(ptr(buffer));
    }

    public void bufferSetIncrement(int buffer, int increment) {
        set(ptr(buffer), increment);
    }

    public int bufferDaemons(int buffer) {
        return ref(ptr(buffer) + 1);
    }

    public void bufferSetDaemons(int buffer, int daemons) {
        set(ptr(buffer) + 1, daemons);
    }

    public int bufferDaemonsActive(int buffer) {
        return ref(ptr(buffer) + 2);
    }

    public void bufferSetDaemonsActive(int buffer, int active) {
        set(ptr(buffer) + 2, active);
    }

    public int bufferStorage(int buffer) {
        return ref(ptr(buffer) + 3);
    }

    public void bufferSetStorage(int buffer, int storage) {
        set(ptr(buffer) + 3, storage);
    }

    public int bufferSize(int buffer) {
        return ref(ptr(buffer) + 4);
    }

    public int bufferAsString(int buffer) {
        return ref(ptr(buffer) + 5);
    }

    public void bufferSetAsString(int buffer, int asString) {
        set(ptr(buffer) + 5, asString);
    }

    public void bufferSetSize(int buffer, int size) {
        set(ptr(buffer) + 4, size);
    }

    public int bufferRef(int buffer, int index) {

        // Reference the indexed element in the buffer. If the indexed element
        // does not exist then this is an error.

        int size = value(bufferSize(buffer));
        index = value(index);
        if (index >= size)
            throw new MachineError(ARRAYACCESS, "Index " + index + " out of range 0 - " + size + " in buffer.");
        else {
            int increment = value(bufferIncrement(buffer));
            int array = bufferStorageArray(buffer, index);
            int localIndex = index % increment;
            return arrayRef(array, localIndex);
        }
    }

    public int bufferStorageArray(int buffer, int index) {

        // Returns the array that contains the indexable element.
        // Assumes that the array exists and that the index is an
        // untagged Java integer.

        int increment = value(bufferIncrement(buffer));
        int storage = bufferStorage(buffer);
        int drop = index / increment;
        storage = drop(storage, drop);
        return consHead(storage);
    }

    public void bufferSet(int buffer, int index, int value) {

        // Set the element in the buffer. If the indexed element does not
        // exist then the buffer is extended by one array until it does
        // exist.

        index = value(index);
        int size = value(bufferSize(buffer));
        if (index < size)
            bufferSetElement(buffer, index, value);
        else {
            while (bufferLength(buffer) < index + 1)
                bufferGrow(buffer);
            bufferSetElement(buffer, index, value);
        }
    }

    public void bufferSetElement(int buffer, int index, int value) {
        int array = bufferStorageArray(buffer, value(index));
        int increment = value(bufferIncrement(buffer));
        int localIndex = value(index) % increment;
        arraySet(array, localIndex, value);
        if (value(index) >= value(bufferSize(buffer)))
            bufferSetSize(buffer, mkInt(value(index) + 1));
    }

    public void bufferGrow(int buffer) {

        // Called when the buffer must increment its storage by one
        // array.

        int increment = value(bufferIncrement(buffer));
        int array = mkArray(increment);
        int storage = bufferStorage(buffer);
        storage = consAppend(storage, mkCons(array, nilValue));
        bufferSetStorage(buffer, storage);
    }

    public String bufferToString(int buffer, int depth) {
        String s = "";
        if (bufferAsString(buffer) == trueValue) {
            for (int i = 0; i < value(bufferSize(buffer)); i++)
                s = s + (char) value(bufferRef(buffer, i));
            return s;
        } else
            return "<buffer size = " + value(bufferSize(buffer)) + " length = " + bufferLength(buffer) + ">";
    }

    public int bufferAsSeq(int buffer) {
        int seq = nilValue;
        for (int i = value(bufferSize(buffer)) - 1; i >= 0; i--)
            seq = mkCons(bufferRef(buffer, i), seq);
        return seq;
    }

    // A hashtable is an array that contains a collection
    // of a-lists. Note that we tag a pointer to a hashtable
    // using HASHTABLE, but the data is an array. You can therefore
    // treat the hashtable as an array for access.

    public int mkHashtable(int size) {
        int table = mkArray(size);
        hashTableClear(table);
        return mkPtr(HASHTABLE, table);
    }

    public boolean isTable(int word) {
        return tag(word) == HASHTABLE;
    }

    public int copyHashTable(int table) {
        int copy = copyArray(table);
        int length = arrayLength(table);
        for (int i = 0; i < length; i++)
            arraySet(copy, i, copyHashTableBucket(arrayRef(copy, i)));
        return mkPtr(HASHTABLE, copy);
    }

    public int copyHashTableBucket(int bucket) {
        if (bucket == nilValue)
            return bucket;
        else
            return mkCons(copy(consHead(bucket)), copyHashTableBucket(consTail(bucket)));
    }

    public int hashCode(int value) {
        switch (tag(value)) {
        case STRING:
            return stringHashCode(value);
        case SET:
            return setHashCode(value);
        default:
            return ptr(value);
        }
    }

    public int hashCode(CharSequence value) {
        return stringHashCode(value);
    }

    public int setHashCode(int set) {
        int elements = setElements(set);
        int hashCode = 0;
        while (elements != nilValue) {
            hashCode = hashCode | hashCode(consHead(elements));
            elements = consTail(elements);
        }
        return hashCode;
    }

    public int stringHashCode(int string) {
        int hashCode = 0;
        for (int i = 0; i < stringLength(string); i++)
            hashCode = hashCode + stringRef(string, i);
        return hashCode;
    }

    public int stringHashCode(CharSequence string) {
        int hashCode = 0;
        for (int i = 0; i < string.length(); i++)
            hashCode = hashCode + string.charAt(i);
        return hashCode;
    }

    public void hashTableClear(int table) {
        int size = arrayLength(table);
        for (int i = 0; i < size; i++)
            arraySet(table, i, nilValue);
    }

    public boolean hashTableIsEmpty(int table) {
        boolean isEmpty = true;
        int size = arrayLength(table);
        for (int i = 0; i < size && isEmpty; i++)
            isEmpty = arrayRef(table, i) == nilValue;
        return isEmpty;
    }

    public int hashTableGet(int table, int key) {
        int cell = hashTableGetCell(table, key);
        if (cell == -1)
            return -1;
        else
            return consTail(cell);
    }

    public int hashTableGet(int table, CharSequence key) {
        int cell = hashTableGetCell(table, key);
        if (cell == -1)
            return -1;
        else
            return consTail(cell);
    }

    public boolean hashTableHasKey(int table, int key) {
        return hashTableGet(table, key) != -1;
    }

    public int hashTableBucket(int table, int key) {
        return arrayRef(table, hashTableIndex(table, key));
    }

    public int hashTableBucket(int table, CharSequence key) {
        return arrayRef(table, hashTableIndex(table, key));
    }

    public int hashTableIndex(int table, int key) {
        return hashCode(key) % arrayLength(table);
    }

    public int hashTableIndex(int table, CharSequence key) {
        return hashCode(key) % arrayLength(table);
    }

    public int hashTableGetCell(int table, int key) {
        int bucket = hashTableBucket(table, key);
        boolean found = false;
        while ((bucket != nilValue) && !found) {
            int pair = consHead(bucket);
            if (equalValues(consHead(pair), key))
                return pair;
            bucket = consTail(bucket);
        }
        return -1;
    }

    public int hashTableGetCell(int table, CharSequence key) {
        int bucket = hashTableBucket(table, key);
        boolean found = false;
        while ((bucket != nilValue) && !found) {
            int pair = consHead(bucket);
            int pairKey = consHead(pair);
            if (isString(pairKey) && stringEqual(pairKey, key))
                return pair;
            bucket = consTail(bucket);
        }
        return -1;
    }

    public void hashTablePut(int table, int key, int value) {
        int cell = hashTableGetCell(table, key);
        if (cell == -1) {
            int newCell = mkCons(key, value);
            int index = hashCode(key) % arrayLength(table);
            int bucket = arrayRef(table, index);
            arraySet(table, index, mkCons(newCell, bucket));
        } else {
            undo.setTable(table,key,value,consTail(cell));
            consSetTail(cell, value);
        }
    }

    public int hashTableRemove(int table, int key) {
        int length = arrayLength(table);
        for (int i = 0; i < length; i++) {
            int bucket = arrayRef(table, i);
            int newBucket = nilValue;
            while (bucket != nilValue) {
                int cell = consHead(bucket);
                int k = consHead(cell);
                if (!equalValues(key, k))
                    newBucket = mkCons(cell, newBucket);
                bucket = consTail(bucket);
            }
            arraySet(table, i, newBucket);
        }
        return table;
    }

    public int hashTableKeys(int table) {
        int keys = nilValue;
        int length = arrayLength(table);
        for (int i = 0; i < length; i++) {
            int bucket = arrayRef(table, i);
            while (bucket != nilValue) {
                int cell = consHead(bucket);
                int key = consHead(cell);
                keys = mkCons(key, keys);
                bucket = consTail(bucket);
            }
        }
        return keys;
    }

    public int hashTableContents(int table) {
        int set = emptySet;
        int length = arrayLength(table);
        for (int i = 0; i < length; i++) {
            int bucket = arrayRef(table, i);
            while (bucket != nilValue) {
                int cell = consHead(bucket);
                set = setIncluding(set, consTail(cell));
                bucket = consTail(bucket);
            }
        }
        return set;
    }

    public boolean hashTableHasValue(int table, int value) {
        int length = arrayLength(table);
        for (int i = 0; i < length; i++) {
            int bucket = arrayRef(table, i);
            while (bucket != nilValue) {
                int cell = consHead(bucket);
                int cellValue = consTail(cell);
                if (equalValues(cellValue, value))
                    return true;
                bucket = consTail(bucket);
            }
        }
        return false;
    }

    public void rehash(int newTable, int oldTable) {
        int length = arrayLength(oldTable);
        for (int i = 0; i < length; i++) {
            int bucket = arrayRef(oldTable, i);
            while (bucket != nilValue) {
                int cell = consHead(bucket);
                int key = consHead(cell);
                int value = consTail(cell);
                hashTablePut(newTable, key, value);
            }
        }
    }

    public void rehash(int table) {
        int TOS = valueStack.getTOS();
        hashTablePushCells(table);
        hashTableClear(table);
        hashTablePopCells(table, TOS);
    }

    public void hashTablePushCells(int table) {
        for (int i = 0; i < arrayLength(table); i++) {
            int bucket = arrayRef(table, i);
            while (bucket != nilValue) {
                int cell = consHead(bucket);
                valueStack.vpush(cell);
                bucket = consTail(bucket);
            }
        }
    }

    public void hashTablePopCells(int table, int TOS) {
        while (valueStack.getTOS() != TOS) {
            int cell = valueStack.pop();
            int key = consHead(cell);
            int value = consTail(cell);
            hashTablePut(table, key, value);
        }
    }

    public String hashTableToStringFull(int table) {
        String s = "<Table ";
        int length = arrayLength(table);
        for (int i = 0; i < length; i++) {
            int bucket = arrayRef(table, i);
            while (bucket != nilValue) {
                int cell = consHead(bucket);
                int key = consHead(cell);
                int value = consTail(cell);
                s = s + valueToString(key) + " = " + valueToString(value);
                bucket = consTail(bucket);
                if (bucket != nilValue)
                    s = s + ",";
            }
            if ((i + 1) < length && arrayRef(table, i + 1) != nilValue)
                s = s + ",";
        }
        return s + ">";
    }

    public String hashTableToString(int table) {
        String s = "<Table ";
        int length = arrayLength(table);
        int entries = 0;
        int usedBuckets = 0;
        int maxBucketLength = 0;
        for (int i = 0; i < length; i++) {
            int bucket = arrayRef(table, i);
            int bucketLength = 0;
            if (bucket != nilValue)
                usedBuckets++;
            while (bucket != nilValue) {
                entries++;
                bucketLength++;
                bucket = consTail(bucket);
            }
            maxBucketLength = Math.max(bucketLength, maxBucketLength);
        }
        int percentFull = (int) (((double) usedBuckets / (double) length) * 100);
        s = s + "length = " + length;
        s = s + " entries = " + entries;
        s = s + " full = " + percentFull + "%";
        s = s + " max bucket length = " + maxBucketLength + ">";
        return s;
    }

    public int copyBucket(int bucket) {
        // This is called when we want to copy a bucket prior to
        // setting a value in the bucket. For example, this occurs
        // when daemons are fired.
        int newBucket = nilValue;
        while (bucket != nilValue) {
            int cell = consHead(bucket);
            bucket = consTail(bucket);
            int newCell = mkCons(consHead(cell), consTail(cell));
            newBucket = mkCons(newCell, newBucket);
        }
        return newBucket;
    }

    // Booleans are immediates. 1 is used for true and 0 for false.

    public static int mkBool(int value) {
        // Return a machine word for a boolean.
        return mkImmediate(BOOL, value);
    }

    public static int mkBool(boolean value) {
        if (value)
            return mkBool(1);
        else
            return mkBool(0);
    }

    public static final boolean isBool(int word) {
        return tag(word) == BOOL;
    }

    public static String boolToString(int word) {
        if (value(word) == 1)
            return "true";
        else
            return "false";
    }

    public int copyArray(int array) {
        int length = arrayLength(array);
        int newArray = mkArray(length);
        for (int i = 0; i < length; i++)
            arraySet(newArray, i, arrayRef(array, i));
        return newArray;
    }

    // A code box is used to contain program code and associated information
    // necessary to perform the instructions. A code box has the following:
    // components:
    // o An array of constants used as operands to instructions. The
    // instructions in the code box have operands that are indexes into
    // the constants array.
    // o A number of locals that are required to perform the code. When the
    // machine starts to perform the code in the code box it will allocate
    // storage for the locals in the stack frame.
    // o A code array containing the instructions.
    // o The name of the owner of the code box. If the owner is not known or
    // is an unnamed function then the name is "anonymous".
    // o The source code for the code box (or null).
    // o The resource name for the code box (or null).

    /*
     * public int mkCodeBox(int locals) { // This is a temporary fix for cleaning saved files corrupted by the //
     * duplicate code box saving problem... locals = Math.min(value(locals), 50); memory.alloc(CODEBOX, CODEBOX_SIZE);
     * int ptr = alloc(CODEBOX_SIZE); set(ptr,mkInt(value(locals))); return mkPtr(CODEBOX, ptr); }
     */

    public int mkCodeBox(int locals) {
        memory.alloc(CODEBOX, CODEBOX_SIZE);
        int ptr = alloc(CODEBOX_SIZE);
        set(ptr, mkInt(value(locals)));
        return mkPtr(CODEBOX, ptr);
    }

    public int mkCodeBox(int constArray, int locals, int instrs) {
        int codeBox = mkCodeBox(locals);
        codeBoxSetConstants(codeBox, constArray);
        codeBoxSetInstrs(codeBox, instrs);
        return codeBox;
    }

    public void codeBoxSetConstants(int codeBox, int constArray) {
        set(ptr(codeBox) + 1, constArray);
    }

    public void codeBoxSetInstrs(int codeBox, int instrs) {
        set(ptr(codeBox) + 2, instrs);
    }

    public static final boolean isCodeBox(int word) {
        return tag(word) == CODEBOX;
    }

    public int codeBoxConstants(int word) {
        return ref(ptr(word) + 1);
    }

    public int codeBoxLocals(int word) {
        return value(ref(ptr(word)));
    }

    public int codeBoxInstrs(int word) {
        return ref(ptr(word) + 2);
    }

    public int codeBoxName(int codeBox) {
        return ref(ptr(codeBox) + 3);
    }

    public void codeBoxSetName(int codeBox, int name) {
        set(ptr(codeBox) + 3, name);
    }

    public int codeBoxSource(int codeBox) {
        return ref(ptr(codeBox) + 4);
    }

    public void codeBoxSetSource(int codeBox, int source) {
        set(ptr(codeBox) + 4, source);
    }

    public int codeBoxResourceName(int codeBox) {
        return ref(ptr(codeBox) + 5);
    }

    public void codeBoxSetResourceName(int codeBox, int resourceName) {
        set(ptr(codeBox) + 5, resourceName);
    }

    public int copyCodeBox(int codeBox) {
        int newBox = mkCodeBox(codeBoxLocals(codeBox));
        codeBoxSetConstants(newBox, codeBoxConstants(codeBox));
        codeBoxSetInstrs(newBox, codeBoxInstrs(codeBox));
        codeBoxSetName(newBox, codeBoxName(codeBox));
        codeBoxSetSource(newBox, codeBoxSource(codeBox));
        codeBoxSetResourceName(newBox, codeBoxResourceName(codeBox));
        return newBox;
    }

    public String codeBoxToString(int codeBox, int depth) {
        int constants = codeBoxConstants(codeBox);
        return "CodeBox(" + valueToString(codeBoxName(codeBox), depth + 1) + "," + codeBoxLocals(codeBox) + "," +
        // valueToString(constants) + "," +
                codeToString(codeBoxInstrs(codeBox), constants) + ")";
    }

    // A cons is a value-pair with a head and a tail. The tail
    // should be either another cons or nil.

    public int mkCons(int head, int tail) {
        // Return a new cons.
        int ptr = alloc(CONS_SIZE);
        memory.alloc(CONS, CONS_SIZE);
        set(ptr, head);
        set(ptr + 1, tail);
        return mkPtr(CONS, ptr);
    }

    public static boolean isCons(int word) {
        return tag(word) == CONS;
    }

    public static boolean isSeq(int word) {
        return isCons(word) || isNil(word);
    }

    public int consHead(int cons) {
        // return ref(ptr(cons));
        return words[cons & DATA];
    }

    public void consSetHead(int cons, int value) {
        set(ptr(cons), value);
    }

    public int consTail(int cons) {
        // return ref(ptr(cons) + 1);
        return words[(cons & DATA) + 1];
    }

    public void consSetTail(int cons, int value) {
        set(ptr(cons) + 1, value);
    }

    public int consAppend(int l1, int l2) {
        int TOS = valueStack.getTOS();
        while (l1 != nilValue)
            if (isCons(l1)) {
                valueStack.push(consHead(l1));
                l1 = consTail(l1);
            } else
                throw new MachineError(TYPE, "Machine::consAppend expecting a proper sequence " + valueToString(l1));
        while (valueStack.getTOS() != TOS)
            l2 = mkCons(valueStack.pop(), l2);
        return l2;
    }

    public int consLength(int l) {
        int length = 0;
        while (isCons(l)) {
            length++;
            l = consTail(l);
        }
        return length;
    }

    public int consRemove(int cons, int value) {
        if (cons == nilValue)
            return cons;
        else if (equalValues(consHead(cons), value))
            return consTail(cons);
        else
            return mkCons(consHead(cons), consRemove(consTail(cons), value));
    }

    public String consToString(int cons, int depth) {
        if (properList(cons)) {
            String s = "Seq{";
            int index = 0;
            while (cons != nilValue) {
                index++;
                if (index > maxPrintElements) {
                    cons = nilValue;
                    s = s + "...";
                } else {
                    s = s + valueToString(consHead(cons), depth + 1);
                    cons = consTail(cons);
                    if (cons != nilValue)
                        s = s + ",";
                }
            }
            return s + "}";
        } else
            return "Seq{" + valueToString(consHead(cons)) + "|" + valueToString(consTail(cons)) + "}";
    }

    public boolean properList(int cons) {
        while (true) {
            if (tag(consTail(cons)) == NIL)
                return true;
            if (tag(consTail(cons)) == CONS)
                cons = consTail(cons);
            else
                return false;
        }
    }

    public int asSeq(int value) {
        switch (tag(value)) {
        case BUFFER:
            return bufferAsSeq(value);
        case SET:
            return setElements(value);
        case CONS:
        case NIL:
            return value;
        case STRING:
            return stringAsSeq(value);
        case SYMBOL:
            return stringAsSeq(symbolName(value));
        case ARRAY:
            return arrayAsSeq(value);
        default:
            throw new MachineError(TYPE, "Machine::asSeq " + valueToString(value));
        }
    }

    public int removeDuplicates(int seq) {
        if (seq == nilValue)
            return nilValue;
        else
            return mkCons(consHead(seq), excluding(removeDuplicates(consTail(seq)), consHead(seq)));
    }

    public int seqDifference(int seq1, int seq2) {
        while (seq2 != nilValue) {
            seq1 = seqExcluding(seq1, consHead(seq2));
            seq2 = consTail(seq2);
        }
        return seq1;
    }

    public int seqExcluding(int seq, int element) {
        if (seqIncludes(seq, element))
            return seqExcluding1(seq, element);
        else
            return seq;
    }

    public int seqExcluding1(int seq, int element) {
        int head = nilValue;
        int tail = nilValue;
        boolean done = false;
        while (seq != nilValue && !done) {
            if (equalValues(element, consHead(seq))) {
                if (head == nilValue)
                    head = consTail(seq);
                else
                    consSetTail(tail, consTail(seq));
                done = true;
            } else if (head == nilValue) {
                head = mkCons(consHead(seq), nilValue);
                tail = head;
                seq = consTail(seq);
            } else {
                consSetTail(tail, mkCons(consHead(seq), nilValue));
                tail = consTail(tail);
                seq = consTail(seq);
            }
        }
        return head;
    }

    public int excluding(int collection, int value) {
        switch (tag(collection)) {
        case SET:
            return setExcluding(collection, value);
        case CONS:
        case NIL:
            return seqExcluding(collection, value);
        default:
            throw new MachineError(TYPE, "Machine.excluding: " + valueToString(collection));
        }
    }

    public boolean includes(int collection, int element) {
        switch (tag(collection)) {
        case SET:
            return seqIncludes(setElements(collection), element);
        case CONS:
            return seqIncludes(collection, element);
        case NIL:
            return false;
        case STRING:
            return stringIncludes(collection, value(element));
        default:
            throw new MachineError(TYPE, "Machine.includes: " + valueToString(collection));
        }
    }

    public boolean seqIncludes(int seq, int element) {

        // Redefined to be iterative....

        while (seq != nilValue) {
            if (equalValues(consHead(seq), element))
                return true;
            else
                seq = consTail(seq);
        }
        return false;
    }

    public int take(int seq, int n) {
        if (n <= 0)
            return nilValue;
        else
            return mkCons(consHead(seq), take(consTail(seq), n - 1));
    }

    public int drop(int seq, int n) {
        while (n > 0 && seq != nilValue) {
            seq = consTail(seq);
            n--;
        }
        return seq;
    }

    public int list(int x) {
        return mkCons(x, nilValue);
    }

    public int including(int collection, int element) {
        switch (tag(collection)) {
        case SET:
            return setIncluding(collection, element);
        case CONS:
        case NIL:
            return consAppend(collection, mkCons(element, nilValue));
        default:
            throw new MachineError(TYPE, "Machine.including: " + valueToString(collection));
        }
    }

    public int sel(int collection) {
        if (isSet(collection) || isSeq(collection)) {
            int seq = asSeq(collection);
            if (seq == nilValue)
                throw new MachineError(TYPE, "Machine.sel: empty collection.");
            else
                return consHead(seq);
        } else
            throw new MachineError(TYPE, "Machine.sel expects a collection " + valueToString(collection));
    }

    public int at(int seq, int index) {
        switch (tag(seq)) {
        case BUFFER:
            return bufferRef(seq, index);
        case NIL:
            throw new MachineError(TYPE, "Machine.at: empty sequence.");
        case CONS:
            while (index > 0) {
                seq = consTail(seq);
                index--;
                if (isNil(seq))
                    throw new MachineError(TYPE, "Machine.at: encountered Seq{} - index too big?");
                if (!isCons(seq))
                    throw new MachineError(TYPE, "Machine.at: expecting a sequence " + valueToString(seq));
            }
            return consHead(seq);
        case STRING:
            return mkInt(stringRef(seq, index));
        case SYMBOL:
            return at(symbolName(seq), index);
        case ARRAY:
            if (index < arrayLength(seq))
                return arrayRef(seq, index);
            else
                throw new MachineError(TYPE, "Machine.at: array index out of range: " + valueToString(seq)
                        + " index = " + index);
        default:
            throw new MachineError(TYPE, "Machine.at: expecting a sequence " + valueToString(seq));
        }
    }

    public int size(int collection) {
        switch (tag(collection)) {
        case BUFFER:
            return value(bufferSize(collection));
        case CONS:
            return consLength(collection);
        case NIL:
            return 0;
        case SET:
            return consLength(setElements(collection));
        case STRING:
            return stringLength(collection);
        case SYMBOL:
            return stringLength(symbolName(collection));
        case ARRAY:
            return arrayLength(collection);
        case HASHTABLE:
            return arrayLength(collection);
        default:
            throw new MachineError(TYPE, "Machine.size: expecting a collection " + valueToString(collection));
        }
    }

    public int sortNamedElements(int elements, boolean casep) {
        int[] array = new int[size(elements)];
        int index = 0;
        while (elements != nilValue) {
            array[index++] = consHead(elements);
            elements = consTail(elements);
        }
        try {
            QuickSort.sort(array, casep, this);
        } catch (Throwable t) {
            System.out.println("Sorting error: " + t);
        }
        int sorted = nilValue;
        for (int i = array.length - 1; i >= 0; i--)
            sorted = mkCons(array[i], sorted);
        return sorted;
    }

    public void assoc() {

        // An a-list is at the head of the stack over a key.
        // return the list from the pair headed by the key
        // or return null.

        int aList = popStack();
        int key = popStack();
        boolean found = false;
        while (aList != nilValue && !found) {
            int pair = consHead(aList);
            if (isCons(pair))
                if (equalValues(consHead(pair), key))
                    found = true;
                else
                    aList = consTail(aList);
        }
        if (found)
            pushStack(aList);
        else
            pushStack(undefinedValue);
    }

    // A set is a pointer to a proper list. A set takes up 1 word of
    // storage over a list in order to store the list. Set modification
    // operations always produce a new set.

    public int mkEmptySet() {
        int ptr = alloc(1);
        set(ptr, nilValue);
        return mkPtr(SET, ptr);
    }

    public int mkSet(int elements) {
        if (elements == nilValue && emptySet != undefinedValue)
            return emptySet;
        memory.alloc(SET, SET_SIZE);
        int ptr = alloc(SET_SIZE);
        set(ptr, elements);
        return mkPtr(SET, ptr);
    }

    public static boolean isSet(int value) {
        return tag(value) == SET;
    }

    public int setElements(int set) {
        return ref(ptr(set));
    }

    public void setSetElements(int set, int list) {
        set(ptr(set), list);
    }

    public int setLength(int set) {
        return consLength(setElements(set));
    }

    public String setToString(int set, int depth) {
        int list = setElements(set);
        String s = "Set{";
        int index = 0;
        while (list != nilValue) {
            index++;
            if (index > maxPrintElements) {
                list = nilValue;
                s = s + "...";
            } else {
                s = s + valueToString(consHead(list), depth + 1);
                list = consTail(list);
                if (list != nilValue)
                    s = s + ",";
            }
        }
        s = s + "}";
        return s;
    }

    public int asSet(int value) {
        switch (tag(value)) {
        case SET:
            return value;
        case CONS:
        case NIL:
            return mkSet(removeDuplicates(value));
        case STRING:
            return stringAsSet(value);
        default:
            throw new MachineError(TYPE, "Machine.asSet: expecting a collection " + valueToString(value));
        }
    }

    public boolean subSet(int set1, int set2) {
        int elements = setElements(set1);
        while (elements != nilValue) {
            int element = consHead(elements);
            if (setMember(element, set2))
                elements = consTail(elements);
            else
                return false;
        }
        return true;
    }

    public boolean setMember(int value, int set) {
        int elements = setElements(set);
        while (elements != nilValue) {
            int element = consHead(elements);
            if (equalValues(element, value))
                return true;
            else
                elements = consTail(elements);
        }
        return false;
    }

    public int setIncluding(int set, int element) {
        if (setMember(element, set))
            return set;
        else {
            int seq = setElements(set);
            return mkSet(mkCons(element, seq));
        }
    }

    public int setDifference(int set1, int set2) {
        int elements = setElements(set2);
        while (elements != nilValue) {
            set1 = setExcluding(set1, consHead(elements));
            elements = consTail(elements);
        }
        return set1;
    }

    public int setExcluding(int set, int element) {
        if (includes(set, element))
            return setExcluding1(set, element);
        else
            return set;
    }

    public int setExcluding1(int set, int element) {
        int elements = setElements(set);
        int newElements = nilValue;
        while (elements != nilValue) {
            if (!equalValues(consHead(elements), element))
                newElements = mkCons(consHead(elements), newElements);
            elements = consTail(elements);
        }
        return mkSet(newElements);
    }

    public int union(int set1, int set2) {
        int seq1 = setElements(set1);
        int seq2 = setElements(set2);
        if (seq2 == nilValue)
            return set1;
        int union = seq2;
        while (seq1 != nilValue) {
            int value = consHead(seq1);
            boolean member = false;
            int seq3 = seq2;
            while (seq3 != nilValue && !member) {
                if (equalValues(consHead(seq3), value))
                    member = true;
                else
                    seq3 = consTail(seq3);
            }
            if (!member)
                union = mkCons(value, union);
            seq1 = consTail(seq1);
        }
        return mkSet(union);
    }

    // A code array contains machine instructions. The first word of the
    // code array is its length. Each instruction is a machine word. The
    // type tag of the word indicates the instruction (see Instr.java). The
    // operands of the instruction are encoded in the rest of the machine
    // word. If the operands are complex (e.g. strings or arrays) then
    // the operand is an index into an associated constants array.

    public int mkCode(int length) {
        // Return a machine word for a freshly
        // allocated code vector.
        memory.alloc(CODE, value(length) + CODE_HEADER);
        int ptr = alloc(value(length) + CODE_HEADER);
        set(ptr, mkImmediate(CODELENGTH, value(length)));
        return mkPtr(CODE, ptr);
    }

    public boolean isCode(int word) {
        return tag(word) == CODE;
    }

    public String codeToString(int word) {
        String s = "Code(";
        for (int i = 0; i < codeLength(word); i++) {
            s = s + tag(codeRef(word, i));
            if ((i + 1) < codeLength(word))
                s = s + ",";
        }
        return s + ")";
    }

    public String codeToString(int code, int constants) {
        String s = "Code(";
        for (int i = 0; i < codeLength(code); i++) {
            s = s + instrToString(codeRef(code, i), constants);
            if ((i + 1) < codeLength(code))
                s = s + ",";
        }
        return s + ")";
    }

    public int codeLength(int word) {
        return value(ref(ptr(word)));
    }

    public int codeRef(int word, int index) {
        // Return the instruction word at the given
        // index.
        return ref(ptr(word) + index + 1);
    }

    public void codeSet(int word, int index, int value) {
        // Reference the instruction word at the given
        // index.
        set(ptr(word) + index + 1, value);
    }

    // Continuations are just copies of the stack at a given
    // point in time. When the continuation is applied then
    // the stack is re-installed and the argument is pushed.

    public int mkCont(int length) {
        int ptr = alloc(length + CONT_HEADER);
        memory.alloc(CONT, length + CONT_HEADER);
        set(ptr, mkInt(value(length)));
        return mkPtr(CONT, ptr);
    }

    public boolean isCont(int word) {
        return tag(word) == CONT;
    }

    public int contLength(int cont) {
        return value(ref(ptr(cont)));
    }

    public int contCurrentFrame(int cont) {
        return value(ref(ptr(cont) + 1));
    }

    public void contSetCurrentFrame(int cont, int value) {
        set(ptr(cont) + 1, value);
    }

    public int contOpenFrame(int cont) {
        return value(ref(ptr(cont) + 2));
    }

    public void contSetOpenFrame(int cont, int value) {
        set(ptr(cont) + 2, value);
    }

    public int contRef(int cont, int index) {
        return ref(ptr(cont) + index + 3);
    }

    public void contSet(int cont, int index, int value) {
        set(ptr(cont) + index + 3, value);
    }

    public String contToString(int cont) {
        return "<cont>";
    }

    // Integers are immediates.

    public static int mkInt(int value) {

        // Return a machine word for an integer. Negative values are
        // represented as their absolute values tagged with NEGINT.
        // Positive values are represented as the value tagged with
        // INT. Use the appropriate tag tester below for type testing
        // Use intValue to make sure you get an appropriate Java int.

        if (value < 0)
            return mkImmediate(NEGINT, Math.abs(value));
        else
            return mkImmediate(INT, value);
    }

    public final static boolean isInt(int word) {
        return isPosInt(word) || isNegInt(word);
    }

    public final static boolean isPosInt(int word) {
        return tag(word) == INT;
    }

    public final static boolean isNegInt(int word) {
        return tag(word) == NEGINT;
    }

    public final static int intValue(int word) {
        if (isNegInt(word))
            return -value(word);
        else
            return value(word);
    }

    public static String intToString(int word) {
        return "" + (intValue(word));
    }

    public int intSlash(int i1, int i2) {
        float f1 = (float) intValue(i1);
        float f2 = (float) intValue(i2);
        float f3 = f1 / f2;
        return mkFloat("" + f3);
    }

    // A floating point value is represented as a string. The
    // string is boxed to allow the representation to be changed
    // easily if this ever turns out to be too inefficient.

    public int mkFloat() {
        return mkFloat("0.0");
    }

    public int mkFloat(int prePoint, int postPoint) {
        return mkFloat(intValue(prePoint) + "." + intValue(postPoint));
    }

    public int mkFloat(double d) {
        return mkFloat("" + d);
    }

    public int mkFloat(float f) {
        return mkFloat("" + f);
    }

    public int mkFloat(String value) {
        memory.alloc(FLOAT, FLOAT_SIZE);
        int ptr = alloc(FLOAT_SIZE);
        int str = mkString(value);
        set(ptr, str);
        return mkPtr(FLOAT, ptr);
    }

    public static final boolean isFloat(int value) {
        return tag(value) == FLOAT;
    }

    public int floatString(int f) {
        return ref(ptr(f));
    }

    public void floatSetString(int f, int str) {
        set(ptr(f), str);
    }

    public String floatToString(int f) {
        return valueToString(floatString(f));
    }

    public int floatAdd(int f1, int f2) {
        return mkFloat(Float.toString(Float.parseFloat(valueToString(f1)) + Float.parseFloat(valueToString(f2))));
    }

    public int floatSub(int f1, int f2) {
        return mkFloat(Float.toString(Float.parseFloat(valueToString(f1)) - Float.parseFloat(valueToString(f2))));
    }

    public int floatMul(int f1, int f2) {
        return mkFloat(Float.toString(Float.parseFloat(valueToString(f1)) * Float.parseFloat(valueToString(f2))));
    }

    public int floatDiv(int f1, int f2) {
        return mkFloat(Float.toString(Float.parseFloat(valueToString(f1)) / Float.parseFloat(valueToString(f2))));
    }

    public int floatFloor(int f) {
        return mkInt((int) Math.floor(Double.parseDouble(valueToString(f))));
    }

    public boolean floatLess(int f1, int f2) {
        float float1 = Float.parseFloat(valueToString(f1));
        float float2 = Float.parseFloat(valueToString(f2));
        return float1 < float2;
    }

    public boolean floatGreater(int f1, int f2) {
        float float1 = Float.parseFloat(valueToString(f1));
        float float2 = Float.parseFloat(valueToString(f2));
        return float1 > float2;
    }

    public int floatRound(int f) {
        return mkInt((int) Math.round(Double.parseDouble(valueToString(f))));
    }

    public int floatSqrt(int f) {
        float float1 = Float.parseFloat(valueToString(f));
        float float2 = (float) Math.sqrt((double) float1);
        return mkFloat("" + float2);
    }

    public float asFloat(int f) {
        return Float.parseFloat(valueToString(f));
    }

    // The value saver uses a tag in the first word of a sharable value
    // to be the index in the save engine when the value is first encountered.

    public boolean hasSaveIndex(int value) {
        return isSaveIndex(ref(ptr(value)));
    }

    public boolean isSaveIndex(int word) {
        return tag(word) == SAVEINDEX;
    }

    public int firstWord(int word) {
        return ref(ptr(word));
    }

    public void setFirstWord(int word, int value) {
        set(ptr(word), value);
    }

    public int mkSaveIndex(int index) {
        return mkImmediate(SAVEINDEX, index);
    }

    public void setSaveIndex(int word, int index) {
        set(ptr(word), mkSaveIndex(index));
    }

    public int saveIndex(int word) {
        return value(firstWord(word));
    }

    // An object attribute is just a cons cell. Object attributes cannot be
    // passed as a first class data structure at the program level
    // therefore they do not need a special data type. The methods
    // defined here for constructing and manipulating attributes are
    // a convenience.
    //
    // The attributes of an object are symbols and values.

    public int mkAttribute() {
        return mkCons(undefinedValue, undefinedValue);
    }

    public int mkAttribute(int name, int value) {
        int attribute = mkAttribute();
        attributeSetName(attribute, name);
        attributeSetValue(attribute, value);
        return attribute;
    }

    public int attributeName(int field) {
        return consHead(field);
    }

    public void attributeSetName(int field, int name) {
        consSetHead(field, name);
    }

    public int attributeValue(int field) {
        return consTail(field);
    }

    public void attributeSetValue(int field, int value) {
        consSetTail(field, value);
    }

    public int copyAttribute(int att) {
        return mkAttribute(attributeName(att), attributeValue(att));
    }

    // A foreign function is an object that can be used as
    // a function by the machine, but that implements its
    // own calling protocol. The machine invokes the calling
    // method with the args and pushes the return value.

    public int mkForeignFun(int index) {
        return mkImmediate(FOREIGNFUN, index);
    }

    public boolean isForeignFun(int word) {
        return tag(word) == FOREIGNFUN;
    }

    public int foreignFunIndex(int word) {
        return value(word);
    }

    public String foreignFunToString(int word) {
        return "ForeignFun(" + foreignFunIndex(word) + ")";
    }

    public int foreignFunName(int fun) {
        return mkString(foreignFun(value(fun)).name());
    }

    public int foreignFunArity(int fun) {
        return foreignFun(value(fun)).arity();
    }

    public int mkAlienObj(int index) {
        // return mkImmediate(ALIENOBJ, index);

        memory.alloc(ALIENOBJ, OBJ_SIZE + 1);
        int ptr = alloc(OBJ_SIZE + 1);
        set(ptr, undefinedValue);
        set(ptr + 1, nilValue);
        set(ptr + 2, nilValue);
        set(ptr + 3, mkInt(OBJ_DEFAULT_PROPS));
        set(ptr + 4, mkInt(0));
        set(ptr + 5, mkInt(0));
        set(ptr + 6, mkInt(index));
        return mkPtr(ALIENOBJ, ptr);
    }

    public static final boolean isAlienObj(int word) {
        return tag(word) == ALIENOBJ;
    }

    // A foreign object is an object that responds to '.'.
    // The machine uses the java.lang.reflect interface to
    // provide access to the internals of an object.

    public int mkForeignObj(int index) {
        return mkImmediate(FOREIGNOBJ, index);
    }

    public static final boolean isForeignObj(int word) {
        return tag(word) == FOREIGNOBJ;
    }

    public int AlienObjIndex(int word) {
        // return value(word);
        return intValue(ref(ptr(word) + 6));
    }

    public String AlienObjToString(int word) {
        return AlienObj(AlienObjIndex(word)).toString();
    }

    public int foreignObjIndex(int word) {
        return value(word);
    }

    public String foreignObjToString(int word) {
        return foreignObj(foreignObjIndex(word)).toString();
    }

    // A function contains the following components:
    //
    // o The arity (number of arguments it expects).
    // o The globals structure containing values for global variables.
    // o The value of 'self' (unless the function is invoked using 'send').
    // o A code box containing the body of the function.
    // o The owner of the function.
    // o The supers of the function. The supers are statically determined and
    // contains the function at the head of the list.
    // o The current dynamics.
    // o A documentation string.
    // o The names of the function arguments.
    // o isVarArgs a machine boolean.
    // o An a-list used for arbitrary properties.

    public int mkFun() {
        memory.alloc(FUN, FUN_SIZE);
        int ptr = alloc(FUN_SIZE);
        int fun = mkPtr(FUN, ptr);
        // The owner of a fun is either 'null' in which case there is no
        // designated owner or it is a named element. The 'owner' link
        // is used to resolve NameSpaceRef instructions.
        funSetOwner(fun, undefinedValue);
        funSetProperties(fun, nilValue);
        funSetDynamics(fun, globalDynamics);
        funSetSig(fun, nilValue);
        return fun;
    }

    public static final boolean isFun(int word) {
        return tag(word) == FUN;
    }

    public int funArity(int fun) {
        return value(ref(ptr(fun)));
    }

    public void funSetArity(int fun, int arity) {
        set(ptr(fun), mkInt(value(arity)));
    }

    public int funGlobals(int fun) {
        return ref(ptr(fun) + 1);
    }

    public void funSetGlobals(int fun, int globals) {
        set(ptr(fun) + 1, globals);
    }

    public int funCode(int fun) {
        return ref(ptr(fun) + 2);
    }

    public void funSetCode(int fun, int codeBox) {
        set(ptr(fun) + 2, codeBox);
    }

    public int funSelf(int fun) {
        return ref(ptr(fun) + 3);
    }

    public void funSetSelf(int fun, int self) {
        set(ptr(fun) + 3, self);
    }

    public int funOwner(int fun) {
        return ref(ptr(fun) + 4);
    }

    public void funSetOwner(int fun, int owner) {
        set(ptr(fun) + 4, owner);
    }

    public int funName(int fun) {
        return codeBoxName(funCode(fun));
    }

    public void funSetName(int fun, int name) {
        codeBoxSetName(funCode(fun), name);
    }

    public int funSupers(int fun) {
        return ref(ptr(fun) + 5);
    }

    public void funSetSupers(int fun, int supers) {
        set(ptr(fun) + 5, supers);
    }

    public int funDynamics(int fun) {
        return ref(ptr(fun) + 6);
    }

    public void funSetDynamics(int fun, int dynamics) {
        set(ptr(fun) + 6, dynamics);
    }

    public int funDocumentation(int fun) {
        int doc = funGetStringProperty(fun, "Doc");
        if (doc == -1)
            return mkString("");
        else
            return doc;
    }

    public void funSetDocumentation(int fun, int doc) {
        if (doc == undefinedValue)
            funRemoveStringProperty(fun, "Doc");
        else
            funSetStringProperty(fun, "Doc", doc);
    }

    public int funSig(int fun) {
        return ref(ptr(fun) + 7);
    }

    public void funSetSig(int fun, int args) {
        set(ptr(fun) + 7, args);
    }

    public int funTraced(int fun) {
        int traced = funGetStringProperty(fun, "Traced");
        if (traced == -1)
            return undefinedValue;
        else
            return traced;
    }

    public void funSetTraced(int fun, int traced) {
        if (traced == undefinedValue)
            funRemoveStringProperty(fun, "Traced");
        else
            funSetStringProperty(fun, "Traced", traced);
    }

    public int funIsVarArgs(int fun) {
        int varArgs = funGetStringProperty(fun, "VarArgs");
        if (varArgs == -1)
            return falseValue;
        else
            return varArgs;
    }

    public void funSetIsVarArgs(int fun, int isVarArgs) {
        if (isVarArgs == falseValue)
            funRemoveStringProperty(fun, "VarArgs");
        else
            funSetStringProperty(fun, "VarArgs", isVarArgs);
    }

    public int funProperties(int fun) {
        return ref(ptr(fun) + 8);
    }

    public void funSetProperties(int fun, int properties) {
        set(ptr(fun) + 8, properties);
    }

    public boolean funPersistentDaemon(int fun) {

        // Finds whether the function has a persistent daemon
        // property. Used to calculate the collection of persistent
        // daemons for an object.

        int properties = funProperties(fun);
        while (properties != nilValue) {
            int property = consHead(properties);
            properties = consTail(properties);
            if (consHead(property) == mkSymbol("persistentDaemon"))
                return true;
        }
        return false;
    }

    public String funToString(int fun) {
        return "Fun(" + valueToString(codeBoxName(funCode(fun))) + ")";
    }

    public int copyFun(int fun) {
        int newFun = mkFun();
        funSetArity(newFun, funArity(fun));
        funSetGlobals(newFun, funGlobals(fun));
        funSetCode(newFun, copy(funCode(fun)));
        funSetSelf(newFun, funSelf(fun));
        funSetOwner(newFun, funOwner(fun));
        funSetSupers(newFun, funSupers(fun));
        funSetDynamics(newFun, funDynamics(fun));
        funSetSig(newFun, funSig(fun));
        funSetProperties(newFun, funProperties(fun));
        return newFun;
    }

    public int funGetStringProperty(int fun, String name) {
        int cell = funGetStringPropertyCell(fun, name);
        if (cell == -1)
            return -1;
        else
            return consTail(cell);
    }

    public int funGetStringPropertyCell(int fun, String name) {
        int properties = funProperties(fun);
        while (properties != nilValue) {
            int cell = consHead(properties);
            int value = consHead(cell);
            if (isString(value) && stringEqual(value, name)) {
                return cell;
            } else
                properties = consTail(properties);
        }
        return -1;
    }

    public void funSetStringProperty(int fun, String name, int value) {
        int cell = funGetStringPropertyCell(fun, name);
        if (cell != -1)
            consSetTail(cell, value);
        else {
            int props = funProperties(fun);
            funSetProperties(fun, mkCons(mkCons(mkString(name), value), props));
        }
    }

    public void funRemoveStringProperty(int fun, String name) {
        int cell = funGetStringPropertyCell(fun, name);
        int props = funProperties(fun);
        if (cell != -1)
            funSetProperties(fun, consRemove(props, cell));
    }

    // A function is a new listener when it has a property named
    // 'newListener' and the value of the property is a non-empty
    // sequence of classes.

    public boolean isNewListener(int fun) {
        return newListenerClasses(fun) != nilValue;
    }

    public int newListenerClasses(int fun) {
        int properties = funProperties(fun);
        while (properties != nilValue) {
            int property = consHead(properties);
            int pname = consHead(property);
            if (pname == theSymbolNewListener)
                return consTail(property);
            else
                properties = consTail(properties);
        }
        return nilValue;
    }

    public void registerNewListener(int fun) {
        int classes = newListenerClasses(fun);
        while (classes != nilValue) {
            int c = consHead(classes);
            classes = consTail(classes);
            registerNewListener(c, fun);
        }
    }

    // Nil is an empty lisp-list.

    public static int mkNil() {
        return mkImmediate(NIL, 0);
    }

    public static boolean isNil(int word) {
        return tag(word) == NIL;
    }

    public String nilToString() {
        return "()";
    }

    // A globals structure contains the variables that are global to some
    // code. A global variable is heap allocated. Since global structures
    // cannot be manipulated at the program level they do not need a special
    // data type. The methods defined here are a convenience.
    //
    // A globals structure contains:
    //
    // o An array of variables that were local in the immediately surrounding
    // binding contour.
    // o A link to the globals structure for the immediately surrounding
    // binding contour.

    public int mkGlobals() {
        return mkArray(2);
    }

    public int mkGlobals(int array, int prev) {
        int globals = mkGlobals();
        globalsSetArray(globals, array);
        globalsSetPrev(globals, prev);
        return globals;
    }

    public int globalsArray(int globals) {
        return arrayRef(globals, 0);
    }

    public void globalsSetArray(int globals, int array) {
        arraySet(globals, 0, array);
    }

    public int globalsPrev(int globals) {
        return arrayRef(globals, 1);
    }

    public void globalsSetPrev(int globals, int prev) {
        arraySet(globals, 1, prev);
    }

    // All objects have a type and a sequence of attributes.
    // The type is a value that is initially set to the undefined
    // value. An object has daemons that are used to monitor changes
    // to the object. When the slots of an object are changed the
    // daemons are fired. Each daemon must be a function.

    public int mkObj(int type) {
        int obj = mkObj();
        objSetType(obj, type);
        return obj;
    }

    public int mkObj() {
        memory.alloc(OBJ, OBJ_SIZE);
        int ptr = alloc(OBJ_SIZE);
        set(ptr, undefinedValue);
        set(ptr + 1, nilValue);
        set(ptr + 2, nilValue);
        set(ptr + 3, mkInt(OBJ_DEFAULT_PROPS));
        return mkPtr(OBJ, ptr);
    }

    public int objType(int obj) {
        return ref(ptr(obj));
    }

    public void objSetType(int obj, int type) {
        set(ptr(obj), type);
    }

    public int objAttributes(int word) {
        return ref(ptr(word) + 1);
    }

    public void objSetAttributes(int obj, int attributes) {
        set(ptr(obj) + 1, attributes);
    }

    public int objDaemons(int obj) {
        return ref(ptr(obj) + 2);
    }

    public void objSetDaemons(int obj, int daemons) {
        undo.setDaemons(obj,daemons,objDaemons(obj));
        set(ptr(obj) + 2, daemons);
    }

    public int objPersistentDaemons(int obj) {

        // The daemons on an object are invocable things.
        // Any operations that do no have their 'persistent'
        // property set are not returned by this method.

        int pdaemons = nilValue;
        int daemons = objDaemons(obj);
        while (daemons != nilValue) {
            int daemon = consHead(daemons);
            daemons = consTail(daemons);
            if (isFun(daemon) && funPersistentDaemon(daemon))
                pdaemons = mkCons(daemon, pdaemons);
            if (isDaemon(daemon) && daemonIsPersistent(daemon))
                pdaemons = mkCons(daemon, pdaemons);
        }
        return pdaemons;
    }

    public int objProperties(int obj) {
        return ref(ptr(obj) + 3);
    }

    public void objSetProperties(int obj, int properties) {
        set(ptr(obj) + 3, properties);
    }

    public int objDaemonsActive(int obj) {
        int propertyMask = value(objProperties(obj));
        if (getBit(propertyMask, OBJ_DAEMONS_ACTIVE) == 1)
            return trueValue;
        else
            return falseValue;
    }

    public void objSetDaemonsActive(int obj, int bool) {
        int propertyMask = value(objProperties(obj));
        if (bool == trueValue)
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_DAEMONS_ACTIVE, 1)));
        else
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_DAEMONS_ACTIVE, 0)));
    }

    public int objHotLoad(int obj) {
        int propertyMask = value(objProperties(obj));
        if (getBit(propertyMask, OBJ_HOT_LOAD) == 1)
            return trueValue;
        else
            return falseValue;
    }

    public void objSetHotLoad(int obj, int bool) {
        int propertyMask = value(objProperties(obj));
        if (bool == trueValue)
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_HOT_LOAD, 1)));
        else
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_HOT_LOAD, 0)));
    }

    public boolean isHotLoad(int obj) {
        return getBit(value(objProperties(obj)), OBJ_HOT_LOAD) == 1;
    }

    public void objSetSaveAsLookup(int obj, boolean bool) {
        int propertyMask = value(objProperties(obj));
        if (bool)
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_SAVE_AS_LOOKUP, 1)));
        else
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_SAVE_AS_LOOKUP, 0)));
    }

    public boolean isSaveAsLookup(int obj) {
        return getBit(value(objProperties(obj)), OBJ_SAVE_AS_LOOKUP) == 1;
    }

    public void objSetDefaultGetMOP(int obj, boolean bool) {
        int propertyMask = value(objProperties(obj));
        if (bool)
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_DEFAULT_GET_MOP, 1)));
        else
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_DEFAULT_GET_MOP, 0)));
    }

    public boolean isDefaultGetMOP(int obj) {
        return getBit(value(objProperties(obj)), OBJ_DEFAULT_GET_MOP) == 1;
    }

    public void objSetDefaultSetMOP(int obj, boolean bool) {
        int propertyMask = value(objProperties(obj));
        if (bool)
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_DEFAULT_SET_MOP, 1)));
        else
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_DEFAULT_SET_MOP, 0)));
    }

    public boolean isDefaultSetMOP(int obj) {
        return getBit(value(objProperties(obj)), OBJ_DEFAULT_SET_MOP) == 1;
    }

    public void objSetDefaultSendMOP(int obj, boolean bool) {
        int propertyMask = value(objProperties(obj));
        if (bool)
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_DEFAULT_SEND_MOP, 1)));
        else
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_DEFAULT_SEND_MOP, 0)));
    }

    public boolean isDefaultSendMOP(int obj) {
        return getBit(value(objProperties(obj)), OBJ_DEFAULT_SEND_MOP) == 1;
    }

    public void objSetNotVMNew(int obj, boolean bool) {
        int propertyMask = value(objProperties(obj));
        if (bool)
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_NOT_VM_NEW, 1)));
        else
            objSetProperties(obj, mkInt(setBit(propertyMask, OBJ_NOT_VM_NEW, 0)));
    }

    public boolean isNotVMNew(int obj) {
        return getBit(value(objProperties(obj)), OBJ_NOT_VM_NEW) == 1;
    }

    public void objAddAttribute(int obj, int name, int value) {
        if (isString(name))
            name = mkSymbol(name);
        int att = mkAttribute();
        attributeSetName(att, name);
        attributeSetValue(att, value);
        objAddAttribute(obj, att);
    }

    public void objRemoveAttribute(int obj, int name) {
        if (isString(name))
            name = mkSymbol(name);
        int att = objAttribute(obj, name);
        int atts = objAttributes(obj);
        if (att != -1) {
            objSetAttributes(obj, this.consRemove(atts, att));
        }
    }

    public static boolean isObj(int word) {
        return tag(word) == OBJ || tag(word) == ALIENOBJ;
    }

    public String objToString(int obj, int depth) {
        if (objType(obj) == theClassClass)
            return classToString(obj);
        else
            return basicObjToString(obj, depth);
    }

    public String classToString(int c) {
        int name = objAttValue(c, theSymbolName);
        if (name == -1)
            return "<Class ???>";
        else
            return "<Class " + valueToString(symbolName(name)) + ">";
    }

    public String basicObjToString(int obj, int depth) {
        int atts = objAttributes(obj);
        String s = valueToString(symbolName(objGetName(type(obj)))) + "[fields=";
        while (atts != nilValue) {
            int att = consHead(atts);
            atts = consTail(atts);
            int name = symbolName(attributeName(att));
            s = s + valueToString(name, depth + 1);
            if (atts != nilValue)
                s = s + ",";
        }
        return s + "]";
    }

    public int objAttribute(int obj, int name) {
        int atts = objAttributes(obj);
        int att = 0;
        boolean found = false;
        while (atts != nilValue && !found) {
            att = consHead(atts);
            if (equalValues(attributeName(att), name))
                found = true;
            else
                atts = consTail(atts);
        }
        if (found)
            return att;
        else
            return -1;
    }

    public int objAttValue(int obj, int name) {
        int att = objAttribute(obj, name);
        if (att == -1)
            return -1;
        else
            return attributeValue(att);
    }

    public int objSetAttValue(int obj, int name, int value) {
        int att = objAttribute(obj, name);
        if (att == -1)
            return -1;
        else {
            undo.setSlot(obj, name, value, attributeValue(att));
            attributeSetValue(att, value);
            return value;
        }
    }

    public boolean objHasAtt(int obj, int name) {
        return objAttribute(obj, name) != -1;
    }

    public int objGetName(int obj) {
        // Important that this returns -1 if the object does not
        // have an attribute 'name'. Can then be used as a test
        // for a named element.
        return objAttValue(obj, theSymbolName);
    }

    public int objGetOwner(int obj) {
        // Important that this returns -1 if the object does not
        // have an attribute 'owner'. Can then be used as a test
        // for an owned element.
        return objAttValue(obj, theSymbolOwner);
    }

    public int objGetContents(int obj) {
        // Important that this returns -1 if the object does not
        // have an attribute 'owner'. Can then be used as a test
        // for an owned element.
        return objAttValue(obj, theSymbolContents);
    }

    public void replaceAttribute(int obj, int att1) {

        // Obj should have a field with the same name as field1.
        // On completion, the current field has been replaced with
        // field1.

        int atts = objAttributes(obj);
        boolean found = false;
        while (!found && atts != nilValue) {
            int att2 = consHead(atts);
            if (attributeName(att1) == attributeName(att2))
                found = true;
            else
                atts = consTail(atts);
        }
        if (found)
            consSetHead(atts, att1);
        else
            throw new Error("replaceAttribute: no att named " + valueToString(att1));
    }

    public void objAddAttribute(int obj, int att) {

        // Add an attribute to an object in place.

        int hasAtt = objAttribute(obj, attributeName(att));
        if (hasAtt != -1)
            attributeSetValue(hasAtt, attributeValue(att));
        else
            objSetAttributes(obj, mkCons(att, objAttributes(obj)));
    }

    public int copyObj(int obj) {
        int atts = objAttributes(obj);
        int newObj = mkObj(objType(obj));
        int newAtts = nilValue;
        while (atts != nilValue) {
            int att = copyAttribute(consHead(atts));
            newAtts = mkCons(att, newAtts);
            atts = consTail(atts);
        }
        objSetAttributes(newObj, newAtts);
        return newObj;
    }

    // Forward references are just the path as a sequence of
    // symbols...

    public int forwardRefs() {
        return forwardRefs;
    }

    public void setForwardRefs(int forwardRefs) {
        this.forwardRefs = forwardRefs;
    }

    public int mkForwardRef(int path) {
        int ptr = alloc(FORWARDREF_SIZE);
        forwardRefSetPath(ptr, path);
        forwardRefSetValue(ptr, undefinedValue);
        forwardRefSetListeners(ptr, nilValue);
        return mkPtr(FORWARDREF, ptr);
    }

    public static final boolean isForwardRef(int word) {
        return tag(word) == FORWARDREF;
    }

    public int forwardRefPath(int ref) {
        return ref(value(ref));
    }

    public void forwardRefSetPath(int ref, int path) {
        set(value(ref), path);
    }

    public int forwardRefValue(int ref) {
        return ref(value(ref) + 1);
    }

    public void forwardRefSetValue(int ref, int value) {
        set(value(ref) + 1, value);
    }

    public int forwardRefListeners(int ref) {
        return ref(value(ref) + 2);
    }

    public void forwardRefSetListeners(int ref, int listeners) {
        set(value(ref) + 2, listeners);
    }

    public boolean forwardRefFor(int forwardRef, int path) {
        return equalRefPaths(forwardRefPath(forwardRef), path);
    }

    public boolean equalRefPaths(int p1, int p2) {
        if (p1 == nilValue && p2 == nilValue)
            return true;
        else if (p1 == nilValue || p2 == nilValue)
            return false;
        else {
            int n1 = consHead(p1);
            int n2 = consHead(p2);
            if (n1 == n2)
                return equalRefPaths(consTail(p1), consTail(p2));
            else
                return false;
        }
    }

    public int getForwardRef(int path) {
        int forwardRef = -1;
        int refs = forwardRefs;
        while (forwardRef == -1 && refs != nilValue) {
            int ref = consHead(refs);
            if (forwardRefFor(ref, path))
                forwardRef = ref;
            else
                refs = consTail(refs);
        }
        if (refs == nilValue) {
            forwardRef = mkForwardRef(path);
            forwardRefs = mkCons(forwardRef, forwardRefs);
        }
        return forwardRef;
    }

    // Strings are encoded as the length of the string as a machine word
    // follwed by the characters encoded as bytes rounded up to the nearest
    // machine word. At the language level characters are represented as
    // integers.

    public int mkString(int chars) {

        // Return a machine word for a freshly allocated
        // string. The first word of the string contains
        // the length and the rest are chars encoded as
        // bytes.

        int words = (chars / 4) + 1;
        memory.alloc(STRING, words + STRING_HEADER);
        int ptr = alloc(words + STRING_HEADER);
        set(ptr, mkImmediate(STRINGLENGTH, value(chars)));
        return mkPtr(STRING, ptr);
    }

    public int mkString(CharSequence string) {

        // Return a machine string created from a Java string.

        int length = string.length();
        int str = mkString(length);
        for (int i = 0; i < length; i++)
            stringSet(str, i, string.charAt(i));
        return str;
    }

    public static boolean isString(int word) {
        return tag(word) == STRING;
    }

    public int stringLength(int word) {
        return value(ref(ptr(word)));
    }

    public int stringRef(int word, int index) {

        // Strings are encoded as an integer (the character length)
        // followed by chars encoded as bytes rounded up to the
        // smallest word boundary.

        int wordIndex = index / 4;
        int charIndex = (index % 4) * 8;
        int chars = ref(ptr(word) + wordIndex + 1);
        int mask = BYTE1 << charIndex;
        return (chars & mask) >>> charIndex;
    }

    public void stringSet(int word, int index, int value) {

        // Update the character at position 'index' with the
        // character whose code is 'value'.

        int wordIndex = index / 4;
        int charIndex = (index % 4) * 8;
        int chars = ref(ptr(word) + wordIndex + 1);
        chars = chars | (value << charIndex);
        set(ptr(word) + wordIndex + 1, chars);
    }

    public String stringToString(int word) {
        return basicStringToString(word);
    }

    public String basicStringToString(int word) {
        String s = "";
        int length = stringLength(word);
        for (int i = 0; i < length; i++)
            s = s + (char) stringRef(word, i);
        return s;
    }

    public int stringAppend(int s1, int s2) {

        // Takes two strings 's1' and 's2' and returns a new string
        // that contains the characters of 's1' followed by the
        // characters of 's2'.

        int stringLength1 = stringLength(s1);
        int stringLength2 = stringLength(s2);
        int wordLength1 = (stringLength1 / 4) + 1;
        // int wordLength2 = (stringLength2 / 4) + 1;
        int length = stringLength1 + stringLength2;
        int newString = mkString(length);
        for (int i = 0; i < wordLength1; i++)
            set(ptr(newString) + 1 + i, ref(ptr(s1) + 1 + i));
        for (int i = 0; i < stringLength2; i++)
            stringSet(newString, stringLength1 + i, stringRef(s2, i));
        return newString;
    }

    public int copyString(int s) {
        int newString = mkString(stringLength(s));
        for (int i = 0; i < stringLength(s); i++)
            stringSet(newString, i, stringRef(s, i));
        return newString;
    }

    public int stringAsSeq(int string) {
        int seq = nilValue;
        for (int i = stringLength(string) - 1; i >= 0; i--) {
            int c = stringRef(string, i);
            seq = mkCons(mkInt(c), seq);
        }
        return seq;
    }

    public int stringAsSet(int string) {
        return asSet(stringAsSeq(string));
    }

    public boolean stringIncludes(int string, int c) {
        for (int i = 0; i < stringLength(string); i++)
            if (stringRef(string, i) == value(c))
                return true;
        return false;
    }

    public boolean stringGreater(int s1, int s2) {
        int l1 = stringLength(s1);
        int l2 = stringLength(s2);
        int length = Math.min(l1, l2);
        for (int i = 0; i < length; i++) {
            int c1 = stringRef(s1, i);
            int c2 = stringRef(s2, i);
            if (c1 < c2)
                return false;
            if (c1 > c2)
                return true;
        }
        return l1 > l2;
    }

    public boolean stringEqual(int s1, CharSequence s2) {
        int l1 = stringLength(s1);
        int l2 = s2.length();
        if (l1 == l2) {
            for (int i = 0; i < l1; i++)
                if (stringRef(s1, i) != s2.charAt(i))
                    return false;
            return true;
        } else
            return false;
    }

    public boolean stringLess(int s1, int s2) {
        int l1 = stringLength(s1);
        int l2 = stringLength(s2);
        int length = Math.min(l1, l2);
        for (int i = 0; i < length; i++) {
            int c1 = stringRef(s1, i);
            int c2 = stringRef(s2, i);
            if (c1 > c2)
                return false;
            if (c1 < c2)
                return true;
        }
        return l1 < l2;
    }

    public int asString(int value) {
        switch (tag(value)) {
        case STRING:
            return value;
        case CONS:
            return consAsString(value);
        case ARRAY:
            return arrayAsString(value);
        case BUFFER:
            if (bufferAsString(value) == trueValue)
                return bufAsString(value);
            else
                return mkString(valueToString(value));
        default:
            return mkString(valueToString(value));
        }
    }

    public int bufAsString(int buffer) {
        int size = value(bufferSize(buffer));
        int string = mkString(size);
        for (int i = 0; i < size; i++) {
            int c = bufferRef(buffer, i);
            stringSet(string, i, (char) c);
        }
        return string;
    }

    public int consAsString(int cons) {
        int length = consLength(cons);
        int string = mkString(length);
        for (int i = 0; i < length; i++) {
            stringSet(string, i, value(consHead(cons)));
            cons = consTail(cons);
        }
        return string;
    }

    // Symbols are symbolic names. The name of a symbol is a string. It is
    // important that the same symbol is returned when two equal strings are
    // used as the name.

    public int mkSymbol(CharSequence name) {

        // If we look up a symbol via a Java name then try to match the
        // characters
        // with an existing symbol before allocating a new string and a symbol
        // with
        // the same name.

        int symbol = findSymbol(name);
        if (symbol == -1)
            return mkSymbol(mkString(name));
        else
            return symbol;
    }

    public int mkSymbol(int name) {
        int symbol = findSymbol(name);
        if (symbol == -1) {
            symbol = mkSymbol();
            symbolSetName(symbol, name);
            setSymbol(name, symbol);
        }
        return symbol;
    }

    public int mkSymbol() {
        memory.alloc(SYMBOL, SYMBOL_SIZE);
        int ptr = alloc(SYMBOL_SIZE);
        int symbol = mkPtr(SYMBOL, ptr);
        symbolSetName(symbol, undefinedValue);
        symbolSetValue(symbol, undefinedValue);
        return symbol;
    }

    public int findSymbol(CharSequence s) {

        // Used to look up a symbol that might exist. The CharSequence
        // could be a String or a StringBuffer. Return -1 if no symbol
        // exists with the given name.

        return hashTableGet(symbolTable, s);
    }

    public int findSymbol(int name) {

        // Use equalValues to compare the supplied string with the
        // name of an existing symbol. Return -1 if no symbol exists
        // with the given name.

        return hashTableGet(symbolTable, name);
    }

    public void setSymbol(int name, int symbol) {
        hashTablePut(symbolTable, name, symbol);
    }

    public int symbolName(int symbol) {
        return ref(ptr(symbol));
    }

    public void symbolSetName(int symbol, int string) {
        set(ptr(symbol), string);
    }

    public int symbolValue(int symbol) {
        return ref(ptr(symbol) + 1);
    }

    public void symbolSetValue(int symbol, int value) {
        set(ptr(symbol) + 1, value);
    }

    public static final boolean isSymbol(int value) {
        return tag(value) == SYMBOL;
    }

    public String symbolToString(int symbol) {
        return "'" + valueToString(symbolName(symbol)) + "'";
    }

    // Undefined is used to initialise memory. It is the default type of
    // objects.

    public static int mkUndefined() {
        return mkImmediate(UNDEFINED, 0);
    }

    public static boolean isNull(int word) {
        return tag(word) == UNDEFINED;
    }

    // Input channels are maintained by the operating system. XVM makes
    // requests on XVM to create and manipulate input channels.

    public static final int mkInputChannel(int index) {
        return mkImmediate(INPUT_CHANNEL, index);
    }

    public final int mkInputChannel(InputStream in) {
        return mkInputChannel(XOS.newInputChannel(in));
    }

    public InputStream inputChannel(int inputChannel) {
        return XOS.inputChannel(value(inputChannel));
    }

    public final int mkOutputChannel(OutputStream out) {
        return mkOutputChannel(XOS.newOutputChannel(out));
    }

    public String inputChannelToString(int index) {
        return "<InputChannel " + value(index) + ">";
    }

    public int mkDataInputChannel(int inputChannel) {
        if (isInputChannel(inputChannel)) {
            int tinch = XOS.newDataInputChannel(value(inputChannel));
            if (tinch == -1)
                throw new MachineError(TYPE, "Cannot create data input channel.");
            return mkInputChannel(tinch);
        } else
            throw new MachineError(TYPE, "Data input channels are based on input channels: "
                    + valueToString(inputChannel));
    }

    public int mkDataOutputChannel(int outputChannel) {
        if (isOutputChannel(outputChannel)) {
            int tinch = XOS.newDataOutputChannel(value(outputChannel));
            if (tinch == -1)
                throw new MachineError(TYPE, "Cannot create data output channel.");
            return mkOutputChannel(tinch);
        } else
            throw new MachineError(TYPE, "Data output channels are based on output channels: "
                    + valueToString(outputChannel));
    }

    public int mkFileInputChannel(String fileName) {
        int finch = XOS.newFileInputChannel(fileName);
        if (finch == -1)
            throw new MachineError(TYPE, "Cannot create input channel to file: " + fileName);
        else {
            return mkInputChannel(finch);
        }
    }

    public int mkGZipInputChannel(int inputChannel) {
        if (isInputChannel(inputChannel)) {
            int ginch = XOS.newGZipInputChannel(value(inputChannel));
            if (ginch == -1)
                throw new MachineError(TYPE, "Cannot create gzip input channel.");
            else {
                return mkInputChannel(ginch);
            }
        } else
            throw new MachineError(TYPE, "GZip input channels are based on input channels: "
                    + valueToString(inputChannel));
    }

    public int mkSAXInputChannel(int inputChannel) {
        if (isInputChannel(inputChannel)) {
            int xinch = XOS.newSAXInputChannel(value(inputChannel));
            if (xinch == -1)
                throw new MachineError(TYPE, "Cannot create a SAX input channel: " + valueToString(inputChannel));
            else {
                return mkInputChannel(xinch);
            }
        } else
            throw new MachineError(TYPE, "SAX input channel expects an input channel as an argument: "
                    + valueToString(inputChannel));
    }

    public int mkStringInputChannel(int string) {
        String s = valueToString(string);
        int sinch = XOS.newStringInputChannel(s);
        if (sinch == -1)
            throw new MachineError(TYPE, "Cannot create input channel to string: " + s);
        else {
            return mkInputChannel(sinch);
        }
    }

    public int mkTokenInputChannel(int inputChannel) {
        if (isInputChannel(inputChannel)) {
            int tinch = XOS.newTokenInputChannel(value(inputChannel));
            if (tinch == -1)
                throw new MachineError(TYPE, "Cannot create token input channel.");
            return mkInputChannel(tinch);
        } else
            throw new MachineError(TYPE, "Token input channels are based on input channels: "
                    + valueToString(inputChannel));
    }

    public int mkURLInputChannel(String url) {
        int urlin = XOS.newURLInputChannel(url);
        if (urlin == -1)
            throw new MachineError(TYPE, "Cannot create input channel to URL: " + url);
        else {
            return mkInputChannel(urlin);
        }
    }

    public int nextToken(int inputChannel) {
        if (isInputChannel(inputChannel))
            return readNextToken(value(inputChannel));

        else
            throw new MachineError(TYPE, "Machine.nextToken expects an input channel: " + valueToString(inputChannel));
    }

    public int readNextToken(int index) {
        XOS.nextToken(index);
        if (XOS.tokenError(index))
            throw new MachineError(ERROR, XOS.tokenErrorMessage(index));
        else {
            int type = mkInt(XOS.tokenType(index));
            int rawChars = mkString(XOS.rawChars(index));
            int posValue = mkInt(XOS.posValue(index));
            int lineCount = mkInt(XOS.lineCount(index));
            int charCount = mkInt(XOS.charCount(index));
            switch (XOS.tokenType(index)) {
            case TChannel.EOF:
                return undefinedValue;
            case TChannel.INT:
                return token(type, mkInt(XOS.tokenValue(index)), posValue, lineCount, charCount, rawChars);
            default:
                return token(type, mkString(XOS.token(index)), posValue, lineCount, charCount, rawChars);
            }
        }
    }

    public int token(int type, int token, int posValue, int lineCount, int charCount, int rawChars) {
        int cons = nilValue;
        cons = mkCons(rawChars, cons);
        cons = mkCons(charCount, cons);
        cons = mkCons(lineCount, cons);
        cons = mkCons(posValue, cons);
        cons = mkCons(token, cons);
        cons = mkCons(type, cons);
        // System.out.println("Token " + valueToString(cons));
        return cons;
    }

    public int textTo(int tokenChannel, int position) {
        if (isInputChannel(tokenChannel))
            return mkString(XOS.textTo(value(tokenChannel), value(position)));
        else
            throw new MachineError(TYPE, "Machine.textTo expects an input channel: " + valueToString(tokenChannel));
    }

    public boolean isInputChannel(int value) {
        return tag(value) == INPUT_CHANNEL;
    }

    public boolean eof(int inputChannel) {
        if (isInputChannel(inputChannel))
            return XOS.eof(value(inputChannel));
        else
            throw new MachineError(TYPE, "Machine.eof expects an input channel " + valueToString(inputChannel));
    }

    public int available(int inputChannel) {
        if (isInputChannel(inputChannel))
            return XOS.available(value(inputChannel));
        else
            throw new MachineError(TYPE, "Machine.available expects an input channel " + valueToString(inputChannel));

    }

    // An output channel is allocated is allocated and maintained by the
    // XOS....

    public static final int mkOutputChannel(int index) {
        return mkImmediate(OUTPUT_CHANNEL, index);
    }

    public OutputStream outputChannel(int outputChannel) {
        return XOS.outputChannel(value(outputChannel));
    }

    public String outputChannelToString(int index) {
        return "<OutputChannel " + value(index) + ">";
    }

    public int mkFileOutputChannel(String fileName) {
        int foutch = XOS.newFileOutputChannel(fileName);
        if (foutch == -1)
            throw new MachineError(TYPE, "Cannot create output channel to file: " + fileName);
        else {
            return mkImmediate(OUTPUT_CHANNEL, foutch);
        }
    }

    public int mkGZipOutputChannel(int outputChannel) {
        if (isOutputChannel(outputChannel)) {
            int gouch = XOS.newGZipOutputChannel(value(outputChannel));
            if (gouch == -1)
                throw new MachineError(TYPE, "Cannot create gzip output channel.");
            else {
                return mkImmediate(OUTPUT_CHANNEL, gouch);
            }
        } else
            throw new MachineError(TYPE, "GZip output channels are based on output channels: "
                    + valueToString(outputChannel));
    }

    public int mkZipInputChannel(int fileName, int entryName) {
        try {
            ZipFile zipFile = zipFile(fileName);
            String label = valueToString(entryName);
            ZipEntry entry = new ZipEntry(label);
            InputStream in = zipFile.getInputStream(entry);
            if (in == null) {
                String entries = "";
                Enumeration e = zipFile.entries();
                while (e.hasMoreElements()) {
                    entries = entries + " " + e.nextElement();
                }
                throw new MachineError(TYPE, "No zip entry named " + label + " entries are: " + entries);
            } else
                return mkInputChannel(XOS.newInputChannel(in));
        } catch (IOException ioe) {
            throw new MachineError(TYPE, ioe.getMessage());
        }
    }

    public void closeZipInputChannel(int fileName) {
        String name = valueToString(fileName);
        ZipFile zipFile = zipFile(fileName);
        if (zipFile != null) {
            zipFiles.remove(name);
            try {
                zipFile.close();
            } catch (IOException e) {
                throw new MachineError(TYPE, e.getMessage());
            }
        }
    }

    public ZipFile zipFile(int fileName) {
        String name = valueToString(fileName);
        ZipFile zipFile = null;
        try {
            if (zipFiles.containsKey(name))
                return (ZipFile) zipFiles.get(name);
            else {
                File file = new File(name);
                if (file.exists()) {
                    zipFile = new ZipFile(file);
                    zipFiles.put(name, zipFile);
                } else
                    throw new MachineError(TYPE, "Cannot find zip file " + name);
            }
        } catch (ZipException e) {
            throw new MachineError(TYPE, e.getMessage() + " " + name);
        } catch (IOException e) {
            new MachineError(TYPE, e.getMessage() + " " + name);
        }
        return zipFile;
    }

    public int mkZipOutputChannel(int outputChannel) {
        if (isOutputChannel(outputChannel)) {
            int zouch = XOS.newZipOutputChannel(value(outputChannel));
            if (zouch == -1)
                throw new MachineError(TYPE, "Cannot create zip output channel.");
            else {
                return mkImmediate(OUTPUT_CHANNEL, zouch);
            }
        } else
            throw new MachineError(TYPE, "Zip output channels are based on output channels: "
                    + valueToString(outputChannel));
    }

    public void zipNewEntry(int zipOutputChannel, int name) {
        String label = valueToString(name);
        if (isOutputChannel(zipOutputChannel)) {
            OutputStream out = this.outputChannel(zipOutputChannel);
            if (out instanceof ZipOutputStream) {
                ZipOutputStream zout = (ZipOutputStream) out;
                ZipEntry entry = new ZipEntry(label);
                try {
                    zout.putNextEntry(entry);
                } catch (IOException e) {
                    throw new MachineError(TYPE, "IO error creating new zip entry " + label);
                }
            } else
                throw new MachineError(TYPE, "New zip entry requires a zip output channel");
        } else
            throw new MachineError(TYPE, "Create new zip entry requires an output channel");
    }

    public boolean isOutputChannel(int value) {
        return tag(value) == OUTPUT_CHANNEL;
    }

    public int call(int channel, int value) {

        // Only used with message channels.

        return writeMessage(channel, value, true);

    }

    public void write(int channel, int value) {

        // This method is supplied with machine words for an
        // output channel and a value. The value is written to the
        // output channel.

        if (isOutputChannel(channel))
            if (XOS.isDataOutputChannel(value(channel)))
                writeData(channel, value);
            else
                writeByte(channel, value);
        else if (isString(channel))
            writeMessage(channel, value, false);
        else
            throw new MachineError(TYPE, "Machine.writeChar: expecting an output channel " + valueToString(channel));
    }

    public void writeByte(int channel, int value) {
        switch (tag(value)) {
        case INT:
            XOS.write(value(channel), value(value));
            break;
        case STRING:
            for (int i = 0; i < stringLength(value); i++)
                XOS.write(value(channel), value(stringRef(value, i)));
            break;
        default:
            throw new MachineError(TYPE, "Unknown type to write to an output channel: " + valueToString(value));
        }
    }

    public int writeMessage(int client, int value, boolean isCall) {
        String name = valueToString(client);
        int result = client;
        if (XOS.isMessageClient(name)) {
            if (isArray(value)) {
                String messageName = valueToString(arrayRef(value, 0));
                int arity = arrayLength(value) - 1;
                Message message = XOS.allocMessage(messageName, arity);
                for (int i = 0; i < arity; i++) {
                    int arg = arrayRef(value, i + 1);
                    message.args[i] = messageValue(arg);
                }
                if (isCall)
                    result = messageValue(XOS.call(name, message));
                else
                    XOS.writeMessage(name, message);
                XOS.freeMessage(message);
            } else
                error(TYPE, "Message args must be arrays: " + valueToString(value));
        } else
            error(TYPE, "Unknown client: " + name);
        return result;
    }

    public int writePacket(int client, int packet, int size) {

        // Called from the var arg foreign function writeCommand.
        // Works hard not to cons any unecessary data structures.

        clientName.setLength(0);
        for (int i = 0; i < stringLength(client); i++)
            clientName.append((char) stringRef(client, i));
        int result = client;
        XOS.MessageClient messageClient = XOS.messageClient(clientName);
        if (messageClient != null) {
            if (isBuffer(packet)) {
            	// System.out.println("Writing packet size: " + Machine.intValue(size));
                MessagePacket pack = XOS.allocPacket(value(size));
                for (int i = 0; i < value(size); i++) {
                    int message = bufferRef(packet, i);
                    XOS.Value mess = messageValue(message);
                    if (mess.type == XData.VECTOR) {
                        String messageName = mess.values[0].strValue();
                        int arity = mess.values.length - 1;
                        Message m = XOS.allocMessage(messageName, arity);
                        for (int a = 0; a < arity; a++) {
                            m.args[a] = mess.values[a + 1];
                        }
                        pack.addMessage(i, m);
                    } else
                        error(TYPE, "A message packet must contain messages of type array: " + valueToString(message));
                }
                messageClient.sendPacket(pack);
                XOS.freePacket(pack);
            } else
                error(TYPE, "Packet must be array: " + valueToString(packet));

        } else
            error(TYPE, "Unknown client: " + clientName);
        return result;
    }

    private StringBuffer clientName = new StringBuffer();

    public int writeCommand(int client, int mname, int arity, boolean isCall) {

        // Called from the var arg foreign function writeCommand.
        // Works hard not to cons any unecessary data structures.

        clientName.setLength(0);
        for (int i = 0; i < stringLength(client); i++)
            clientName.append((char) stringRef(client, i));
        int result = client;
        XOS.MessageClient messageClient = XOS.messageClient(clientName);
        if (messageClient != null) {
            Message message = XOS.allocMessage();
            message.setArity(value(arity));
            for (int i = 0; i < stringLength(mname); i++)
                message.appendNameChar((char) stringRef(mname, i));
            for (int i = 0; i < value(arity); i++) {
                int arg = frameLocal(3 + i);
                message.args[i] = messageValue(arg);
            }
            if (isCall)
                result = messageValue(messageClient.call(message));
            else
                messageClient.sendMessage(message);
            XOS.freeMessage(message);
        } else
            error(TYPE, "Unknown client: " + clientName);
        return result;
    }

    public XOS.Value messageValue(int value) {
        switch (tag(value)) {
        case INT:
        case NEGINT:
            return XOS.allocValue(intValue(value));
        case BOOL:
            return XOS.allocValue(value == trueValue);
        case STRING:
            XOS.Value v1 = XOS.allocValue();
            v1.type = XData.STRING;
            for (int i = 0; i < stringLength(value); i++)
                v1.appendChar((char) stringRef(value, i));
            return v1;
        case SYMBOL:
            XOS.Value v2 = XOS.allocValue();
            v2.type = XData.STRING;
            for (int i = 0; i < stringLength(symbolName(value)); i++)
                v2.appendChar((char) stringRef(symbolName(value), i));
            return v2;
        case FLOAT:
            return XOS.allocValue(asFloat(value));
        case CONS:
            if (properList(value)) {
                XOS.Value[] values = new XOS.Value[consLength(value)];
                int index = 0;
                while (value != nilValue) {
                    values[index++] = messageValue(consHead(value));
                    value = consTail(value);
                }
                return new XOS.Value(values);
            } else
                return XOS.allocValue(valueToString(value));
        case ARRAY:
            XOS.Value[] values = new XOS.Value[arrayLength(value)];
            for (int j = 0; j < arrayLength(value); j++)
                values[j] = messageValue(arrayRef(value, j));
            return XOS.allocValue(values);
        case NIL:
            return new XOS.Value(new XOS.Value[0]);
        default:
            return XOS.allocValue(valueToString(value));
        }
    }

    public void writeData(int channel, int value) {
        switch (tag(value)) {
        case INT:
            XOS.writeInt(value(channel), value(value));
            break;
        case BOOL:
            XOS.writeBool(value(channel), value == trueValue);
            break;
        case STRING:
            XOS.startString(value(channel), stringLength(value));
            for (int i = 0; i < stringLength(value); i++)
                XOS.write(value(channel), stringRef(value, i));
            break;
        default:
            XOS.writeString(value(channel), valueToString(value));
        }
    }

    public void close(int channel) {
        switch (tag(channel)) {
        case INPUT_CHANNEL:
            XOS.closeInputChannel(value(channel));
            break;
        case OUTPUT_CHANNEL:
            XOS.flush(value(channel));
            XOS.closeOutputChannel(value(channel));
            break;
        }
    }

    public void closeAll() {

        // Close all connections prior to shutting down.

        XOS.closeAll();
    }

    public void rebindStdin(int in) {
        if (isInputChannel(in))
            XOS.rebindStandardInput(value(in));
        else
            error(TYPE, "Machine.rebindStdin expects an input channel: " + valueToString(in));
    }

    public void rebindStdout(int out) {
        if (isOutputChannel(out))
            XOS.rebindStandardOutput(value(out));
        else
            error(TYPE, "MAchine,rebindStdout expects an output channel: " + valueToString(out));
    }

    public void exit(int value) {
        XOS.shutDown(value(value));
    }

    public void exitAbnormal() {
        System.exit(0);
    }

    public void flush(int channel) {
        switch (tag(channel)) {
        case OUTPUT_CHANNEL:
            XOS.flush(value(channel));
            break;
        default:
            System.out.println("Machine.flush: Unknown channel type " + tag(channel));
        }
    }

    // Clients are used to connect to servers....

    public String clientToString(int client) {
        return "<Client>";
    }

    public final static int mkThread(int threadId) {
        return mkImmediate(THREAD, threadId);
    }

    public final static boolean isThread(int word) {
        return tag(word) == THREAD;
    }

    public final static int threadId(int thread) {
        return value(thread);
    }

    public final static String threadToString(int word) {
        return "<Thread " + threadId(word) + ">";
    }

    public int threadsAllocated() {
        if (threads == null)
            return 0;
        else
            return threads.length();
    }

    // A daemon is a special type of operation that is invoked with respect to 3
    // arguments:
    // a slot, a new value and an object value. The daemon is attached to an
    // object and is
    // invoked when a slot of the object changes. The invocation is for side
    // effect only. If
    // the slot of the object matches the slot monitored by the daemon then the
    // action of
    // the daemon will be performed subject to a test being satisfied. The test
    // depends on the
    // type of the daemon.
    // 
    // A daemon has the following fields:
    //
    // o An id (Element).
    // o A type (integer enumeration).
    // o A slot (Symbol or null).
    // o An action (Fun).
    // o A persistence (Boolean).
    // o Traced (Boolean).
    // o Target (Element) the value of 'self' when the daemon is created.

    public int mkDaemon() {
        int daemon = mkPtr(DAEMON, alloc(DAEMON_SIZE));
        memory.alloc(DAEMON, DAEMON_SIZE);
        daemonSetId(daemon, undefinedValue);
        daemonSetType(daemon, DAEMON_ANY);
        daemonSetSlot(daemon, undefinedValue);
        daemonSetAction(daemon, undefinedValue);
        daemonSetPersistent(daemon, falseValue);
        daemonSetTraced(daemon, falseValue);
        daemonSetTarget(daemon, frameSelf());
        return daemon;
    }

    public static final boolean isDaemon(int value) {
        return tag(value) == DAEMON;
    }

    public String daemonToString(int daemon) {
        if (isSymbol(daemonId(daemon)))
            return "<Daemon " + valueToString(symbolName(daemonId(daemon))) + ">";
        else
            return "<Daemon " + valueToString(symbolName(funName(daemonAction(daemon)))) + " => "
                    + valueToString(daemonId(daemon)) + ">";
    }

    public int daemonId(int daemon) {
        return ref(ptr(daemon));
    }

    public void daemonSetId(int daemon, int id) {
        set(ptr(daemon), id);
    }

    public int daemonType(int daemon) {
        return ref(ptr(daemon) + 1);
    }

    public void daemonSetType(int daemon, int type) {
        set(ptr(daemon) + 1, type);
    }

    public int daemonSlot(int daemon) {
        return ref(ptr(daemon) + 2);
    }

    public void daemonSetSlot(int daemon, int slot) {
        set(ptr(daemon) + 2, slot);
    }

    public int daemonAction(int daemon) {
        return ref(ptr(daemon) + 3);
    }

    public void daemonSetAction(int daemon, int fun) {
        set(ptr(daemon) + 3, fun);
    }

    public int daemonPersistent(int daemon) {
        return ref(ptr(daemon) + 4);
    }

    public void daemonSetPersistent(int daemon, int bool) {
        set(ptr(daemon) + 4, bool);
    }

    public boolean daemonIsPersistent(int daemon) {
        return daemonPersistent(daemon) == trueValue;
    }

    public int daemonTraced(int daemon) {
        return ref(ptr(daemon) + 5);
    }

    public void daemonSetTraced(int daemon, int bool) {
        set(ptr(daemon) + 5, bool);
    }

    public int daemonTarget(int daemon) {
        return ref(ptr(daemon) + 6);
    }

    public void daemonSetTarget(int daemon, int target) {
        set(ptr(daemon) + 6, target);
    }

    // *************************************************************************
    // * Machine Data Structures End *
    // *************************************************************************

    // Values can be compared for equality using two types of equiality:
    // o Use java == to test if they are the same values.
    // o Use type specific tests to determine whether they are equivalent
    // structures in memory.

    public boolean equalValues(int v1, int v2) {
        if (isString(v1) && isString(v2))
            return equalStrings(v1, v2);
        if (isSet(v1) && isSet(v2))
            return equalSets(v1, v2);
        if (isFloat(v1) || isFloat(v2))
            return equalNumbers(v1, v2);
        // This is incomplete....but most of
        // the rest of the datatypes have
        // = implemented as Java ==.
        else
            return v1 == v2;
    }

    public boolean equalStrings(int s1, int s2) {

        // Compare strings character by character.

        if (stringLength(s1) == stringLength(s2)) {
            int length = stringLength(s1);
            int index = 0;
            boolean same = true;
            while ((index < length) && same)
                if (stringRef(s1, index) != stringRef(s2, index))
                    same = false;
                else
                    index++;
            return same;
        } else
            return false;
    }

    public boolean equalSets(int set1, int set2) {
        return subSet(set1, set2) && subSet(set2, set1);
    }

    public boolean equalNumbers(int n1, int n2) {
        if (isFloat(n1) && isFloat(n2))
            return equalFloats(n1, n2);
        if (isInt(n1) && isInt(n2))
            return n1 == n2;
        if (isFloat(n1) && isInt(n2))
            return equalFloats(n1, mkFloat(n2, 0));
        if (isInt(n1) && isFloat(n2))
            return equalFloats(mkFloat(n1, 0), n2);
        return false;
    }

    public boolean equalFloats(int f1, int f2) {
        return valueToString(f1).equals(valueToString(f2));
    }

    public int copy(int value) {
        // Return a shallow copy of the value.
        switch (tag(value)) {
        case ARRAY:
            return copyArray(value);
        case INT:
        case BOOL:
        case FLOAT:
        case CLIENT:
        case INPUT_CHANNEL:
        case OUTPUT_CHANNEL:
        case DAEMON:
            return value;
        case OBJ:
            return copyObj(value);
        case FUN:
            return copyFun(value);
        case STRING:
            return copyString(value);
        case CODEBOX:
            return copyCodeBox(value);
        case CONS:
            return mkCons(consHead(value), copy(consTail(value)));
        case NIL:
            return value;
        case SET:
            return value;
        case HASHTABLE:
            return copyHashTable(value);
        case BUFFER:
            return copyBuffer(value);
        default:
            throw new MachineError(TYPE, "machine.copy: unknown type to copy. " + tag(value));
        }
    }

    public String valueToString(int word) {

        String string = valueToString(word, 0);
        // System.err.println("VALUE TO STRING = " + string);
        return string;

    }

    public String valueToString(int word, int depth) {

        // Translate a value into a string.

        if (depth >= maxPrintDepth)
            return "...";
        else
            switch (tag(word)) {
            case UNDEFINED:
                return "null";
            case ARRAY:
                return arrayToString(word, depth);
            case BUFFER:
                return bufferToString(word, depth);
            case CODE:
                return codeToString(word);
            case INT:
            case NEGINT:
                return intToString(word);
            case STRING:
                return stringToString(word);
            case CODEBOX:
                return codeBoxToString(word, depth);
            case BOOL:
                return boolToString(word);
            case OBJ:
                return objToString(word, depth);
            case FUN:
                return funToString(word);
            case FOREIGNFUN:
                return foreignFunToString(word);
            case FOREIGNOBJ:
                return foreignObjToString(word);
            case ALIENOBJ:
                return AlienObjToString(word);
            case CONT:
                return contToString(word);
            case CONS:
                return consToString(word, depth);
            case NIL:
                return nilToString();
            case SYMBOL:
                return symbolToString(word);
            case STRINGLENGTH:
                return "StringLength(" + value(word) + ")";
            case CODELENGTH:
                return "CodeLength(" + value(word) + ")";
            case SET:
                return setToString(word, depth);
            case INPUT_CHANNEL:
                return inputChannelToString(word);
            case OUTPUT_CHANNEL:
                return outputChannelToString(word);
            case HASHTABLE:
                return hashTableToString(word);
            case FLOAT:
                return floatToString(word);
            case CLIENT:
                return clientToString(word);
            case THREAD:
                return threadToString(word);
            case DAEMON:
                return daemonToString(word);
            default:
                return "valueToString: unknown tag " + tag(word);
            }
    }

    public String instrToString(int instr, int constants) {

        // Translate an instruction to a string.

        switch (tag(instr)) {
        case MKSEQ:
            return "MKSEQ " + value(instr);
        case MKSET:
            return "MKSET " + value(instr);
        case MKCONS:
            return "MKCONS";
        case PUSHINT:
            return "PUSHINT " + value(instr);
        case PUSHTRUE:
            return "PUSHTRUE";
        case PUSHFALSE:
            return "PUSHFALSE";
        case PUSHSTR:
            return "PUSHSTR " + valueToString(arrayRef(constants, value(instr)));
        case RETURN:
            return "RETURN";
        case ADD:
            return "ADD";
        case SUB:
            return "SUB";
        case MUL:
            return "MUL";
        case DIV:
            return "DIV";
        case GRE:
            return "GRE";
        case LESS:
            return "LESS";
        case EQL:
            return "EQL";
        case AND:
            return "AND";
        case OR:
            return "OR";
        case NOT:
            return "NOT";
        case DOT:
            return "DOT " + valueToString(arrayRef(constants, value(instr)));
        case DOTSELF:
            return "DOTSELF " + valueToString(arrayRef(constants, value(instr)));
        case DOTLOCAL:
            return "DOTLOCAL " + valueToString(arrayRef(constants, (byte3(instr) << 8) | byte2(instr))) + " "
                    + byte1(instr);
        case SELF:
            return "SELF " + value(instr);
        case SKPF:
            return "SKPF " + value(instr);
        case SKP:
            return "SKP " + value(instr);
        case DYNAMIC:
            return "DYNAMIC(" + value(instr) + ") " + valueToString(arrayRef(constants, value(instr)));
        case SETDYN:
            return "SETDYN " + valueToString(arrayRef(constants, value(instr)));
        case BINDDYN:
            return "BINDDYN " + valueToString(arrayRef(constants, value(instr)));
        case UNBINDDYN:
            return "UNBINDDYN " + valueToString(arrayRef(constants, value(instr)));
        case MKFUN:
            return "MKFUN " + byte3(instr) + " " + byte2(instr) + " " + byte1(instr);
        case MKFUNE:
            return "MKFUNE " + byte3(instr) + " " + byte2(instr);
        case LOCAL:
            return "LOCAL " + value(instr);
        case STARTCALL:
            return "STARTCALL";
        case ENTER:
            return "ENTER " + value(instr);
        case TAILENTER:
            return "TAILENTER " + value(instr);
        case SEND:
            return "SEND " + byte2(instr) + " " + valueToString(arrayRef(constants, byte1(instr)));
        case SENDSELF:
            return "SENDSELF " + byte2(instr) + " " + valueToString(arrayRef(constants, byte1(instr)));
        case SENDLOCAL:
            return "SENDLOCAL " + byte3(instr) + " " + valueToString(arrayRef(constants, byte2(instr))) + byte1(instr);
        case TAILSEND:
            return "TAILSEND " + byte2(instr) + " " + valueToString(arrayRef(constants, byte1(instr)));
        case SEND0:
            return "SEND0 " + valueToString(arrayRef(constants, value(instr)));
        case TAILSEND0:
            return "TAILSEND0 " + valueToString(arrayRef(constants, value(instr)));
        case SUPER:
            return "SUPER " + value(instr);
        case TAILSUPER:
            return "TAILSUPER " + value(instr);
        case SETLOC:
            return "SETLOC " + value(instr);
        case POP:
            return "POP";
        case GLOBAL:
            return "GLOBAL " + byte3(instr) + " " + byte2(instr);
        case SETSLOT:
            return "SETSLOT " + valueToString(arrayRef(constants, value(instr)));
        case SETSELFSLOT:
            return "SETSELFSLOT " + valueToString(arrayRef(constants, value(instr)));
        case SETLOCALSLOT:
            return "SETLOCALSLOT " + valueToString(arrayRef(constants, (byte2(instr) << 8) | byte1(instr))) + " "
                    + byte3(instr);
        case SETGLOB:
            return "SETGLOB " + byte3(instr) + " " + byte2(instr);
        case MKARRAY:
            return "MKARRAY " + value(instr);
        case NAMESPACEREF:
            return "NAMESPACEREF " + byte3(instr) + " " + valueToString(arrayRef(constants, (byte2(instr))));
        case HEAD:
            return "HEAD";
        case TAIL:
            return "TAIL";
        case SIZE:
            return "SIZE";
        case DROP:
            return "DROP";
        case ISEMPTY:
            return "ISEMPTY";
        case INCLUDES:
            return "INCLUDES";
        case EXCLUDING:
            return "EXCLUDING";
        case INCLUDING:
            return "INCLUDING";
        case SEL:
            return "SEL";
        case UNION:
            return "UNION";
        case ASSEQ:
            return "ASSEQ";
        case AT:
            return "AT";
        case SKPBACK:
            return "SKPBACK " + value(instr);
        case NULL:
            return "NULL";
        case OF:
            return "OF";
        case THROW:
            return "THROW";
        case TRY:
            return "TRY" + byte2(instr) + " " + byte1(instr);
        case ISKINDOF:
            return "ISKINDOF";
        case SOURCEPOS:
            return "SOURCEPOS line:" + ((byte3(instr) << 8) | byte2(instr)) + " char:" + byte1(instr);
        case GETELEMENT:
            return "GETELEMENT " + valueToString(arrayRef(constants, byte1(instr)));
        case SETHEAD:
            return "SETHEAD";
        case SETTAIL:
            return "SETTAIL";
        case READ:
            return "READ";
        case ACCEPT:
            return "ACCEPT";
        case ARRAYREF:
            return "ARRAYREF";
        case ARRAYSET:
            return "ARRAYSET";
        case TABLEGET:
            return "TABLEGET";
        case TABLEPUT:
            return "TABLEPUT";
        case NOOP:
            return "NOOP";
        case SLEEP:
            return "SLEEP";
        case CONST:
            return "CONST " + valueToString(arrayRef(constants, value(instr)));
        case SETLOCPOP:
            return "SETLOCPOP " + value(instr);
        case DISPATCH:
            return "DISPATCH " + valueToString(arrayRef(constants, value(instr)));
        case INCSELFSLOT:
            return "INCSELFSLOT " + valueToString(arrayRef(constants, value(instr)));
        case DECSELFSLOT:
            return "DECSELFSLOT " + valueToString(arrayRef(constants, value(instr)));
        case INCLOCAL:
            return "INCLOCAL " + value(instr);
        case DECLOCAL:
            return "DECLOCAL " + value(instr);
        case ADDLOCAL:
            return "ADDLOCAL " + value(instr);
        case SUBLOCAL:
            return "SUBLOCAL " + value(instr);
        case PREPEND:
            return "PREPEND";
        case ENTERDYN:
            return "ENTERDYN " + byte2(instr) + " " + valueToString(arrayRef(constants, byte1(instr)));
        case TAILENTERDYN:
            return "TAILENTERDYN " + byte2(instr) + " " + valueToString(arrayRef(constants, byte1(instr)));
        case LOCALHEAD:
            return "LOCALHEAD " + value(instr);
        case LOCALTAIL:
            return "LOCALTAIL " + value(instr);
        case LOCALASSEQ:
            return "LOCALASSEQ " + value(instr);
        case LOCALISEMPTY:
            return "LOCALISEMPTY " + value(instr);
        case LOCALREFPOS:
            return "LOCALREFPOS " + byte3(instr) + " " + ((byte2(instr) << 8) + byte1(instr));
        case DYNREFPOS:
            return "DYNREFPOS " + valueToString(arrayRef(constants, byte3(instr))) + " "
                    + ((byte2(instr) << 8) + byte1(instr));
        case ASSOC:
            return "ASSOC";
        case RETDOTSELF:
            return "RETDOTSELF " + valueToString(arrayRef(constants, value(instr)));
        default:
            return "<Unknown instruction " + tag(instr) + ">";
        }
    }

    // Foreign functions are maintained in a table. New foreign
    // functions are added to the table and maintained as an index
    // from the heap into the table.

    public int newForeignFun(ForeignFun fun) {
        int index = foreignFuns.size();
        foreignFuns.push(fun);
        return mkForeignFun(index);
    }

    public ForeignFun foreignFun(int index) {
        return (ForeignFun) foreignFuns.elementAt(index);
    }

    public int newAlienObj(Object obj) {
        for (int i = 0; i < AlienObjects.size(); i++)
            if (AlienObjects.elementAt(i).equals(obj))
                return mkAlienObj(i);
        int index = AlienObjects.size();
        AlienObjects.push(obj);
        return mkAlienObj(index);
    }

    public Object AlienObj(int index) {
        return AlienObjects.elementAt(index);
    }

    // Foreign objects are maintained in a table. New foreign
    // objects are added to the table and maintained as an index
    // from the heap into the table.

    public int newForeignObj(Object obj) {
        for (int i = 0; i < foreignObjects.size(); i++)
            if (foreignObjects.elementAt(i).equals(obj))
                return mkForeignObj(i);
        int index = foreignObjects.size();
        foreignObjects.push(obj);
        return mkForeignObj(index);
    }

    public Object foreignObj(int index) {
        return foreignObjects.elementAt(index);
    }

    // *************************************************************************
    // * Machine Stack Frame *
    // *************************************************************************

    // The machine is stack based. It pushes a new stack frame on the value
    // stack
    // when a new procedure is invoked. The current stack frame contains
    // information
    // necessary to perform the procedure. When the procedure returns, the
    // stack
    // frame is popped and the previous stack frame becomes current.

    public int stackFrames() {
        return stackFrames(currentFrame);
    }

    public int stackFrames(int frame) {

        // Construct and return a sequence of stack frames. Each stack frame is
        // represented as a sequence of values. This will return a sopy of the
        // stack frame elements from the frame given as an argument in the
        // order of youngest frame first.

        if (frame == -1)
            return nilValue;
        else {
            return mkCons(stackFrame(frame), stackFrames(valueStack.ref(frame + PREVFRAME)));
        }
    }

    public int stackFrame(int frame) {
        int codeBox = valueStack.ref(frame + FRAMECODEBOX);
        int index = valueStack.ref(frame + FRAMECODEINDEX);
        int globals = valueStack.ref(frame + FRAMEGLOBALS);
        int dynamics = valueStack.ref(frame + FRAMEDYNAMICS);
        int locals = valueStack.ref(frame + FRAMELOCALS);
        int self = valueStack.ref(frame + FRAMESELF);
        int supers = valueStack.ref(frame + FRAMESUPER);
        int handler = valueStack.ref(frame + FRAMEHANDLER);
        int localValues = stackLocalValues(frame, locals);
        int lineCount = valueStack.ref(frame + FRAMELINECOUNT);
        int charCount = valueStack.ref(frame + FRAMECHARCOUNT);
        int stackFrame = nilValue;
        stackFrame = mkCons(mkInt(charCount), stackFrame);
        stackFrame = mkCons(mkInt(lineCount), stackFrame);
        stackFrame = mkCons(localValues, stackFrame);
        stackFrame = mkCons(handler, stackFrame);
        stackFrame = mkCons(supers, stackFrame);
        stackFrame = mkCons(self, stackFrame);
        stackFrame = mkCons(mkInt(locals), stackFrame);
        stackFrame = mkCons(dynamics, stackFrame);
        stackFrame = mkCons(globals, stackFrame);
        stackFrame = mkCons(mkInt(index), stackFrame);
        stackFrame = mkCons(codeBox, stackFrame);
        return stackFrame;
    }

    public int stackLocalValues(int frame, int values) {
        int locals = nilValue;
        for (int i = values - 1; i >= 0; i--)
            locals = mkCons(valueStack.ref(frame + FRAMELOCAL0 + i), locals);
        return locals;
    }

    public void printFrame() {
        printFrame(currentFrame);
    }

    public void printFrame(int frame) {
        System.out.println("Frame at index " + frame + "---------------------------------------");
        System.out.println("PREVFRAME = " + valueStack.ref(frame + PREVFRAME));
        System.out.println("PREVOPENFRAME = " + valueStack.ref(frame + PREVOPENFRAME));
        System.out.println("INSTRS = " + valueToString(codeBoxInstrs(valueStack.ref(frame + FRAMECODEBOX))));
        System.out.println("CODEINDEX = " + valueStack.ref(frame + FRAMECODEINDEX));
        System.out.println("GLOBALS = " + valueToString(valueStack.ref(frame + FRAMEGLOBALS)));
        System.out.println("SELF = " + valueToString(valueStack.ref(frame + FRAMESELF)));
        System.out.println("SUPER = " + valueToString(valueStack.ref(frame + FRAMESUPER)));
        System.out.println("HANDLER = " + valueToString(valueStack.ref(frame + FRAMEHANDLER)));
        System.out.println("CONSTANTS = " + valueToString(codeBoxConstants(valueStack.ref(frame + FRAMECODEBOX))));
        for (int i = 0; i < valueStack.ref(frame + FRAMELOCALS); i++) {
            String local = valueToString(valueStack.ref(frame + FRAMELOCAL0 + i));
            System.out.println("[" + (frame + FRAMELOCAL0 + i) + "] LOCAL" + i + " = " + local);
        }
        System.out.println("--------------------------------------------------------------------");
        System.out.println("");
    }

    public void printCurrentFrameToTOS() {
        // printFrame(currentFrame);
        // printStackToTop(currentFrame + FRAMELOCAL0 +
        // valueStack.ref(currentFrame + FRAMELOCALS));
    }

    public void printStackToTop(int stackPtr) {
        int TOS = valueStack.getTOS();
        System.out.println("---------------- Stack from " + stackPtr + " to " + TOS + " ----------");
        if (stackPtr > TOS)
            for (int i = stackPtr; i >= TOS; i--)
                printStackElement(i, valueStack.ref(i));
        else
            for (int i = stackPtr; i <= TOS; i++)
                printStackElement(i, valueStack.ref(i));
    }

    public void printStackElement(int i, int value) {
        if (value == -1)
            System.out.println("[" + i + "] -1");
        else {
            String string = valueToString(valueStack.ref(i));
            string = string.substring(0, Math.min(20, string.length()));
            System.out.println("[" + i + "] " + string);
        }
    }

    public void traceInstrs(boolean trace) {
        traceInstr = trace;
    }

    public void printCalls(PrintStream out) {
        printCalls(out, currentFrame);
    }

    public void printCalls(PrintStream out, int frame) {
        while (frame != -1) {
            int codeBox = valueStack.ref(frame + FRAMECODEBOX);
            int name = codeBoxName(codeBox);
            String nameSpace = frameNameSpaceName(frame);
            out.println("Called(" + frame + "): " + nameSpace + "::" + valueToString(name));
            frame = valueStack.ref(frame + PREVFRAME);
        }
    }

    public void printBacktrace() {
        printBacktrace(System.out, currentFrame);
    }

    public void saveBacktrace() {
        saveBacktrace(currentFrame);
    }

    public void saveBacktrace(int frame) {
        saveBacktrace(frame, backtraceDumpFile());
    }

    public String backtraceDumpFile() {
        return backtraceDumpFile + new Date().getTime() + ".txt";
    }

    public void saveBacktrace(int frame, String file) {
        try {
            FileOutputStream fout = new FileOutputStream(file);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            PrintStream pout = new PrintStream(bout);
            printBacktrace(pout, frame);
            new Exception().printStackTrace(pout);
            pout.close();
        } catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
    }

    public void printLine(PrintStream out) {
        for (int i = 0; i < 100; i++)
            out.print("-");
        out.println();
    }

    public void printBacktrace(PrintStream out, int frame) {

        // The code boxes in stack frames contain the names of the methods and functions
        // that have been invoked. Assuming that frame is a pointer to a legal frame, traverse
        // back up the stack and print the names in the frames and the locals.

        out.println();
        out.println("Call frame backtrace:");
        out.println();
        printInstrBacktrace(out, frame);
        while (frame != -1) {
            int codeBox = valueStack.ref(frame + FRAMECODEBOX);
            int name = symbolName(codeBoxName(codeBox));
            String nameSpace = frameNameSpaceName(frame);
            printLine(out);
            out.println("Called(" + frame + "): " + nameSpace + "::" + valueToString(name) + " at line "
                    + value(valueStack.ref(frame + FRAMELINECOUNT)) + " char "
                    + value(valueStack.ref(frame + FRAMECHARCOUNT)));
            int locals = valueStack.ref(frame + FRAMELOCALS);
            out.println("Self: " + valueToString(valueStack.ref(frame + FRAMESELF)));
            int supers = valueStack.ref(frame + FRAMESUPER);
            out.print("Supers: ");
            while (isCons(supers) && supers != nilValue) {
                int op = consHead(supers);
                if (isFun(op))
                    out.print(valueToString(symbolName(funName(op))));
                supers = consTail(supers);
                if (supers != nilValue)
                    out.print(",");
            }
            out.println();
            for (int i = 0; i < locals; i++)
                out.println("Local(" + i + "): " + valueToString(valueStack.ref(frame + FRAMELOCAL0 + i)));
            out.println("Source Code:\n    " + valueToString(codeBoxSource(codeBox)));
            int constants = codeBoxConstants(valueStack.ref(frame + FRAMECODEBOX));
            for (int i = 0; i < arrayLength(constants); i++)
                out.println("constant(" + i + ") = " + valueToString(arrayRef(constants, i)));
            frame = valueStack.ref(frame + PREVFRAME);
            printLine(out);
            out.println();
            out.println();
        }
        out.println();
    }

    public void printInstrBacktrace(PrintStream out, int frame) {
        out.println("Current stack frame instructions:");
        int codeBox = valueStack.ref(frame + FRAMECODEBOX);
        int instrs = codeBoxInstrs(codeBox);
        int constants = codeBoxConstants(codeBox);
        int index = valueStack.ref(frame + FRAMECODEINDEX);
        for (int i = 0; i < index; i++) {
            out.print("  " + instrToString(codeRef(instrs, i), constants));
            if (i + 1 == index)
                out.println(" <--- ");
            else
                out.println();
        }
    }

    public String frameNameSpaceName(int frame) {
        int nameSpace = frameNameSpace(frame);
        if (nameSpace == undefinedValue)
            return "?";
        else
            return valueToString(symbolName(objAttValue(nameSpace, theSymbolName)));
    }

    public int frameNameSpace(int frame) {
        int supers = valueStack.ref(frame + FRAMESUPER);
        if (isCons(supers)) {
            int active = consHead(supers);
            int owner = funOwner(active);
            return owner;
        } else
            return undefinedValue;
    }

    public int currentFrame() {
        return currentFrame;
    }

    public int prevFrame() {

        // The previously active stack frame. This is used to restore the value
        // of 'currentFrame' when the current frame is popped.

        return valueStack.ref(currentFrame + PREVFRAME);
    }

    public int prevPrevFrame() {
        return valueStack.ref(prevFrame() + PREVFRAME);
    }

    public int prevOpenFrame() {

        // The previously open frame. This is restored when the current frame
        // is
        // popped.

        return valueStack.ref(currentFrame + PREVOPENFRAME);
    }

    public int prevPrevOpenFrame() {
        return valueStack.ref(prevOpenFrame() + PREVOPENFRAME);
    }

    public int frameCodeBox() {
        // The currently executing code box.
        return valueStack.ref(currentFrame + FRAMECODEBOX);
    }

    public int frameInstrs() {
        // The instruction array being performed.
        return codeBoxInstrs(frameCodeBox());
    }

    public int frameCodeIndex() {
        // The index into the current frame instructions.
        return value(valueStack.ref(currentFrame + FRAMECODEINDEX));
    }

    public void incFrameCodeIndex() {
        // Used when the next instruction is fetched.
        valueStack.set(currentFrame + FRAMECODEINDEX, frameCodeIndex() + 1);
    }

    public void incFrameCodeIndex(int offset) {
        // Used to jump about the current frame instructions.
        valueStack.set(currentFrame + FRAMECODEINDEX, frameCodeIndex() + offset);
    }

    public int frameGlobals() {
        // The global variable values in the current frame.
        return valueStack.ref(currentFrame + FRAMEGLOBALS);
    }

    public void setFrameGlobals(int globals) {
        valueStack.set(currentFrame + FRAMEGLOBALS, globals);
    }

    public int frameDynamics() {
        // The dynamic variables introduced using let-dynamic.
        return valueStack.ref(currentFrame + FRAMEDYNAMICS);
    }

    public void setFrameDynamics(int dynamics) {
        // Required for let-dynamic.
        valueStack.set(currentFrame + FRAMEDYNAMICS, dynamics);
    }

    public int frameSelf() {
        // The value of 'self' with respect to the current frame.
        return valueStack.ref(currentFrame + FRAMESELF);
    }

    public void setFrameSelf(int self) {
        valueStack.set(currentFrame + FRAMESELF, self);
    }

    public int frameSuper() {
        // A sequence of operations headed by the currently executing
        // operation.
        return valueStack.ref(currentFrame + FRAMESUPER);
    }

    public void setFrameSuper(int supers) {
        valueStack.set(currentFrame + FRAMESUPER, supers);
    }

    public int frameHandler() {
        // Either undefined or a single argument function that is used
        // to handle values that are thrown from younger stack frames.
        return valueStack.ref(currentFrame + FRAMEHANDLER);
    }

    public void setFrameHandler(int handler) {
        valueStack.set(currentFrame + FRAMEHANDLER, handler);
    }

    public int frameLineCount() {
        return valueStack.ref(currentFrame + FRAMELINECOUNT);
    }

    public void setFrameLineCount(int lineCount) {
        valueStack.set(currentFrame + FRAMELINECOUNT, mkInt(lineCount));
    }

    public int frameCharCount() {
        return valueStack.ref(currentFrame + FRAMECHARCOUNT);
    }

    public void setFrameCharCount(int charCount) {
        valueStack.set(currentFrame + FRAMECHARCOUNT, mkInt(charCount));
    }

    public int frameNameSpace() {
        int operations = frameSuper();
        int operation = consHead(operations);
        return funOwner(operation);
    }

    public int frameConstants() {
        // The constants for the instructions in the current frame.
        // return valueStack.ref(currentFrame + FRAMECONSTANTS);
        return codeBoxConstants(frameCodeBox());
    }

    public int frameConstant(int index) {
        // Index a particular constant.
        return arrayRef(frameConstants(), index);
    }

    public int frameLocal(int index) {
        // The locals live above the fixed size part of the current
        // stack frame (at FRAMELOCAL0). This method is used to index
        // a particular local. Note that arguments of functions are
        // locals (occurring on the stack before let-bound locals).
        return valueStack.ref(currentFrame + FRAMELOCAL0 + index);
    }

    public int frameLocals() {
        // The total number of locals required by the frame. In the
        // case of functions this includes the arguments.
        return valueStack.ref(currentFrame + FRAMELOCALS);
    }

    public void setFrameLocal(int index, int value) {
        // Update a given local.
        valueStack.set(currentFrame + FRAMELOCAL0 + index, value);
    }

    public int frameGlobal(int frame, int index) {
        // Index a global variable. Note that globals require binding
        // contour information (frame) and an index into the globals
        // structure for that contour. When global structures are created
        // they are chained to the global structure for the surrounding
        // contour.
        int globals = frameGlobals();
        for (int i = 0; i < frame; i++)
            globals = globalsPrev(globals);
        return arrayRef(globalsArray(globals), index);
    }

    public void setFrameGlobal(int frame, int index, int value) {
        // Set a global variable.
        int globals = frameGlobals();
        for (int i = 0; i < frame; i++)
            globals = globalsPrev(globals);
        arraySet(globalsArray(globals), index, value);
    }

    // Use the following methods to access elements of the current open frame.
    // In general these methods are used when setting up and finalising the
    // execution of a procedure.

    public void setOpenFramePrevFrame(int index) {
        valueStack.set(openFrame + PREVFRAME, index);
    }

    public void setOpenFramePrevOpenFrame(int index) {
        valueStack.set(openFrame + PREVOPENFRAME, index);
    }

    public void setOpenFrameCodeIndex(int index) {
        valueStack.set(openFrame + FRAMECODEINDEX, index);
    }

    public void setOpenFrameGlobals(int globals) {
        valueStack.set(openFrame + FRAMEGLOBALS, globals);
    }

    public void setOpenFrameDynamics(int dynamics) {
        valueStack.set(openFrame + FRAMEDYNAMICS, dynamics);
    }

    public void setOpenFrameCodeBox(int codeBox) {
        valueStack.set(openFrame + FRAMECODEBOX, codeBox);
    }

    public void setOpenFrameLocals(int locals) {
        valueStack.set(openFrame + FRAMELOCALS, locals);
    }

    public void setOpenFrameSelf(int self) {
        valueStack.set(openFrame + FRAMESELF, self);
    }

    public void setOpenFrameSuper(int supers) {
        valueStack.set(openFrame + FRAMESUPER, supers);
    }

    public void setOpenFrameHandler(int handler) {
        valueStack.set(openFrame + FRAMEHANDLER, handler);
    }

    public int openFrameLocal(int index) {
        return valueStack.ref(openFrame + FRAMELOCAL0 + index);
    }

    public void popFrame() {

        // The current frame has completed. The top value on the stack
        // is the return value. The machine registers are restored from
        // the saved previous values in the current frame which is then
        // discarded.

        int returnValue = valueStack.pop();
        valueStack.setTOS(currentFrame);
        openFrame = prevOpenFrame();
        currentFrame = prevFrame();
        valueStack.push(returnValue);
    }

    public void openFrame() {

        // Create a new open frame. An open frame is one that is under
        // construction either due to a function call (args must be
        // evaluated before closing the frame) or due to a field reference.

        int savedOpenFrame = openFrame;
        openFrame = valueStack.getTOS();
        valueStack.pushn(FRAMELOCAL0, undefinedValue);
        setOpenFramePrevFrame(currentFrame);
        setOpenFramePrevOpenFrame(savedOpenFrame);
        setOpenFrameCodeIndex(mkInt(0));
        setOpenFrameDynamics(frameDynamics());
    }

    public void closeFrame(int locals) {
        // Close the current open frame by making it current.
        valueStack.pushn(locals, undefinedValue);
        currentFrame = openFrame;
        openFrame = -1;
    }

    // *************************************************************************
    // * Machine Stack Frame End *
    // *************************************************************************

    // *************************************************************************
    // * Continuations *
    // *************************************************************************

    // A continuation is a machine stack that can be used as an operator. When
    // the
    // continuation is applied to a single argument the stack is reinstated and
    // the argument is pushed. The effect is to continue from the point at
    // which
    // the continuation was created. This mechanism can be used to construct a
    // wide variety of control constructs, especially non-local gotos and
    // backtracking.

    public void callcc(int fun) {
        if (!isFun(fun))
            throw new Error("Machine.callcc: expecting a function.");
        if (funArity(fun) != 1)
            throw new Error("Machine.callcc: function arity must be 1.");
        // Get rid of the callcc frame...
        pushStack(undefinedValue);
        popFrame();
        popStack();
        // Create a new continuation.
        int cont = newCont();
        // Start a new frame for the function supplied with the new
        // continuation.
        openFrame();
        // Set up the call.
        pushStack(cont);
        pushStack(fun);
        // Enter the body of fun.
        enter(1);
    }

    public int newCont() {
        // A new continuation is created in the context of a callcc
        // stack frame. For example:
        // f = fun(...) let g = callcc(fun(value) ... end) in ...
        // The continuation captured must return from f when invoked.
        int cont = mkCont(valueStack.getTOS());
        contSetCurrentFrame(cont, mkInt(currentFrame));
        contSetOpenFrame(cont, mkInt(openFrame));
        for (int i = 0; i < valueStack.getTOS(); i++)
            contSet(cont, i, valueStack.ref(i));
        return cont;
    }

    // *************************************************************************
    // * Continuations End *
    // *************************************************************************

    // *************************************************************************
    // * Threads Start *
    // *************************************************************************

    public void killCurrentThread() {

        // Called when the current thread is to be removed from the machine.
        // The currently active thread is then selected at random (actually)
        // the next thread in the sequence). Note that if the current thread
        // is the only active thread then the machine will be left in a
        // terminated state.

        threads.kill();
        threads = threads.remove();
    }

    public int fork(int name, int fun) {

        // Creates a new thread but does not make it current.

        ValueStack newStack = new ValueStack(STACKSIZE);
        String s = valueToString(name);
        Thread newThread = threads.add(new Thread(s, newStack, 0, -1));
        saveCurrentThread();
        valueStack = newStack;
        currentFrame = -1;
        openFrame = -1;
        initStack(newStack, funCode(fun));
        currentFrame = 0;
        setFrameGlobals(funGlobals(fun));
        setFrameDynamics(funDynamics(fun));
        setFrameSelf(funSelf(fun));
        setFrameSuper(funSupers(fun));
        restoreCurrentThread();
        XOS.schedule(newThread);
        return mkThread(newThread.id());
    }

    public void sleep() {

        // Set the current thread to be sleeping and yield.

        threads.sleep();
        yield();
    }

    public void wake(int value, int result) {

        // Wake the sleeping thread by changing its state to ACTIVE,
        // pushing the return value and scheduling the thread.

        Thread t = getThread(value);
        if (t != null) {
            if (t.state() == Thread.SLEEPING) {
                t.wake(result);
                XOS.schedule(t);
            }
        } else
            error(ERROR, "Machine.wake expects a thread: " + valueToString(value));
    }

    public void yield() {

        yield = true;
    }

    public Thread currentThread() {
        return threads;
    }

    public void nextThread() {

        // Select the next thread that is not sleeping.
        // There must be one thread that is awake.

        do {
            threads = threads.next();
        } while (threads.isSleeping());
    }

    public void setThread(Thread thread) {
        threads = thread;
        restoreCurrentThread();
    }

    public void saveCurrentThread() {
        if (threads != null) {
            threads.setCurrentFrame(currentFrame);
            threads.setOpenFrame(openFrame);
        }
    }

    public void restoreCurrentThread() {
        if (threads != null) {
            valueStack = threads.stack();
            currentFrame = threads.currentFrame();
            openFrame = threads.openFrame();
        }
    }

    public int thread() {
        // Return the current thread as a user level value.
        return mkThread(threads.id());
    }

    public Thread getThread(int thread) {
        // Find and return the thread with the supplied id.
        Thread t = threads;
        do {
            if (t.id() == threadId(thread))
                return t;
            else
                t = t.next();
        } while (t != threads);
        return null;
    }

    public void kill(int thread) {
        if (getThread(thread) == threads)
            currentFrame = -1;
        else {
            Thread t = getThread(thread);
            if (t != null) {
                t.setCurrentFrame(-1);
            }
        }
    }

    // *************************************************************************
    // * Threads End *
    // *************************************************************************

    public void init(int codeBox) {
        initStack(valueStack, codeBox);
        currentFrame = 0;
        setGlobalDynamics();
    }

    public void initStack(ValueStack stack, int codeBox) {
        stack.push(currentFrame);
        stack.push(openFrame);
        stack.push(codeBox);
        stack.push(mkInt(0)); // Code Index!!
        stack.push(undefinedValue); // Globals Vector!!
        stack.push(nilValue); // Dynamics Linked List!!
        stack.push(codeBoxLocals(codeBox));
        stack.push(undefinedValue); // Self!!
        stack.push(nilValue); // Super!!
        stack.push(undefinedValue); // Handler!!
        stack.push(mkInt(0)); // LineCount!!
        stack.push(mkInt(0)); // charCount!!
        stack.pushn(codeBoxLocals(codeBox), undefinedValue);
    }

    public void setGlobalDynamics() {
        // Called when a new machine is initialised to construct
        // the built-in dynamics.
        if (imageFile == null) {
            int Kernel_Root_Table = mkHashtable(100);
            int Kernel_XCore_Table = mkHashtable(100);
            int Kernel_XCore_Table_Symbol = mkSymbol("Kernel_XCore_Table");
            int Kernel_Root_Table_Symbol = mkSymbol("Kernel_Root_Table");
            int Kernel_stdout_Symbol = mkSymbol("Kernel_stdout");
            int Kernel_stdin_Symbol = mkSymbol("Kernel_stdin");
            int stdout = mkImmediate(OUTPUT_CHANNEL, 0);
            int stdin = mkImmediate(INPUT_CHANNEL, 0);
            bindDynamic(mkSymbol("nil"), nilValue);
            bindDynamic(mkSymbol("null"), undefinedValue);
            bindDynamic(mkSymbol("Kernel_Symbol_Table"), symbolTable);
            bindDynamic(Kernel_Root_Table_Symbol, Kernel_Root_Table);
            symbolSetValue(Kernel_Root_Table_Symbol, Kernel_Root_Table);
            bindDynamic(Kernel_XCore_Table_Symbol, Kernel_XCore_Table);
            symbolSetValue(Kernel_XCore_Table_Symbol, Kernel_XCore_Table);
            bindDynamic(Kernel_stdout_Symbol, stdout);
            symbolSetValue(Kernel_stdout_Symbol, stdout);
            bindDynamic(Kernel_stdin_Symbol, stdin);
            symbolSetValue(Kernel_stdin_Symbol, stdin);
            importTable(Kernel_Root_Table);
            importTable(Kernel_XCore_Table);
            ForeignFuns.builtinForeignFuns(this, Kernel_Root_Table);
            globalDynamics = frameDynamics();
        }
    }

    public int globalDynamics() {
        return globalDynamics;
    }

    // *************************************************************************
    // * Machine Instruction Cycle *
    // *************************************************************************

    // The machine is performed by continually fetching the next instruction,
    // inspecting its type tag and then dispatching to the appropriate method.

    public void perform() {
        yield = false;
        while (!terminatedThread() && !yield)
            try {
                performInstrs();
            } catch (ArrayIndexOutOfBoundsException boundsException) {
                valueStack.setTOS(currentFrame);
                currentFrame = prevFrame();
                saveBacktrace();
                boundsException.printStackTrace(System.err);
                error(ERROR, "Stack Overflow");
            } catch (MachineError machineError) {
                // If we receive a machine error then we create an
                // exception and throw it to the nearest handler.
                error(machineError.error, machineError.getMessage());
            } catch (Throwable throwable) {
                // If we receive an error we don't know about then deal as
                // best we can.
                handleThrow(throwable);
            }
    }

    public void performInstrs() {

        // This method is the main workhorse of the VM. It assumes that a
        // thread has been installed and is ready to start. The thread is
        // then performed until it either terminates or decides to stop
        // (for example calls yield()).
        //
        // NB a number of instructions have been in-lined in this method
        // and the various calls have been expanded. In most cases the
        // original calls to instruction implementations have been left as
        // comments.

        // Registers...

        int instr;
        int tag;
        int R0;
        int R1;
        int R2;

        // while (!terminatedThread() && !yield) {
        while (currentFrame != -1 && !yield) {

            // Get the next instruction. Perform any debugging and then
            // dispatch to an instruction routine based on the type tag
            // of the instruction.

            if (interrupt)
                interrupt();

            // Fetch an instruction and dispatch on the instruction tag.
            // The following single call has been macro-expanded to the
            // several lines of code that follow. Change back if ever
            // the format of stack frames changes; re-macro-expand if
            // necessary,
            //
            // int instr = fetchInstr();
            // ...
            // switch (tag(instr)) {

            R0 = valueStack.elements[currentFrame + FRAMECODEINDEX] & DATA;
            valueStack.elements[currentFrame + FRAMECODEINDEX] = (valueStack.elements[currentFrame + FRAMECODEINDEX] + 1)
                    & DATA;
            instrsPerformed++;
            instr = words[(words[(valueStack.elements[currentFrame + FRAMECODEBOX] & PTR) + 2] & PTR) + R0 + 1];
            tag = (instr & BYTE4) >>> 24;

            switch (tag) {

            case MKCONS:
                // Push a cons pair.
                cons();
                break;
            case MKSEQ:
                // Pop elements and create a sequence.
                mkSeqFrom(value(instr));
                break;
            case MKSET:
                // Pop elements and create a set.
                mkSetFrom(value(instr));
                break;
            case PUSHINT:
                // Push a constant positive integer.
                valueStack.elements[valueStack.index++] = INT_MASK | (instr & DATA);
                break;
            case PUSHTRUE:
                // Push the value true.
                valueStack.elements[valueStack.index++] = trueValue;
                break;
            case PUSHFALSE:
                // Push the value false.
                valueStack.elements[valueStack.index++] = falseValue;
                break;
            case PUSHSTR:
                // Push a string found in the constants box.
                R0 = words[(valueStack.elements[currentFrame + FRAMECODEBOX] & DATA) + 1];
                valueStack.elements[valueStack.index++] = arrayRef(R0, instr & DATA);
                break;
            case RETURN:
                // Return from the current frame, passing back the
                // value at TOS.
                popFrame();
                break;
            case ADD:
                // Add various data structures.
                add();
                break;
            case SUB:
                // Numeric subtraction.
                sub();
                break;
            case MUL:
                // Numeric multiplication.
                mul();
                break;
            case DIV:
                // Numeric division.
                div();
                break;
            case GRE:
                // Numeric >.
                gre();
                break;
            case LESS:
                // Numeric <.
                less();
                break;
            case EQL:
                // The same machine value.
                eql();
                break;
            case AND:
                // Boolean and.
                and();
                break;
            case IMPLIES:
                // Boolean implies.
                implies();
                break;
            case OR:
                // Boolean or.
                or();
                break;
            case NOT:
                // Boolean not.
                not();
                break;
            case DOT:
                // Object field reference. Object is at the TOS.
                dot(frameConstant(value(instr)));
                break;
            case SELF:
                // Refer to an object in scope.
                valueStack.elements[valueStack.index++] = frameSelf();
                break;
            case SKPF:
                // Skip instructions if the TOS is false.
                // skpf(value(instr));
                R0 = valueStack.elements[--valueStack.index];
                if ((R0 >> 24) == BOOL) {
                    if (R0 == falseValue)
                        valueStack.elements[currentFrame + FRAMECODEINDEX] = (valueStack.elements[currentFrame
                                + FRAMECODEINDEX] & DATA)
                                + (instr & DATA);
                } else
                    throw new MachineError(TYPE, "Machine.skpf: expecting a boolean");
                break;
            case SKP:
                // Skip instructions.
                // skp(value(instr));
                valueStack.elements[currentFrame + FRAMECODEINDEX] += (instr & DATA);
                break;
            case DYNAMIC:
                // Push the value of a dynamic variable.
                dynamic(frameConstant(value(instr)));
                break;
            case MKFUN:
                // Make a function. The global variable values are the
                // top n stack values.
                if (needsGC())
                    gc();
                mkfun(value(instr));
                break;
            case MKFUNE:
                // Make a function. The global variable values are the
                // top n stack values.
                if (needsGC())
                    gc();
                mkfune(byte3(instr), byte2(instr));
                break;
            case LOCAL:
                // Refer to a local variable value in the current stack
                // frame.
                valueStack.elements[valueStack.index++] = valueStack.elements[currentFrame + FRAMELOCAL0
                        + (instr & DATA)];
                break;
            case STARTCALL:
                // Set up a stack frame. The fixed size part of the
                // frame is
                // pushed ready for the argument values.
                if (needsGC())
                    gc();
                openFrame();
                break;
            case ENTER:
                // The current open frame is complete, make it current.
                enter(value(instr));
                break;
            case TAILENTER:
                // The arguments and function of the last call have
                // been pushed.
                // Overwrite the current frame.
                tailEnter(value(instr));
                break;
            case SETLOC:
                // Set the value of a local variable in the current
                // frame.
                setFrameLocal(value(instr), valueStack.top());
                break;
            case SEND:
                // Send the object at the top of the stack a message.
                send(popStack(), byte2(instr), frameConstant(byte3(instr) << 8 | byte1(instr)));
                break;
            case SENDSELF:
                // Send self a message.
                send(frameSelf(), byte2(instr), frameConstant(byte3(instr) << 8 | byte1(instr)));
                break;
            case SENDLOCAL:
                // Send a local a message.
                send(frameLocal(byte1(instr)), byte3(instr), frameConstant(byte2(instr)));
                break;
            case TAILSEND:
                // Send the object at the top of the stack a tail
                // message.
                tailSend(byte2(instr), frameConstant(byte3(instr) << 8 | byte1(instr)));
                break;
            case SEND0:
                // Send the object at the top of the stack a message
                // with 0 args.
                send0(frameConstant(value(instr)));
                break;
            case TAILSEND0:
                // Send the object at the top of the stack a tail
                // message with 0 args.
                tailSend0(frameConstant(value(instr)));
                break;
            case POP:
                // Pop the TOS.
                valueStack.index--;
                break;
            case GLOBAL:
                // Push the value of a global variable in the current
                // stack frame.
                global(value(instr));
                break;
            case SETSLOT:
                // Set the value of the named attribute to be the TOS
                // value.
                setSlot(frameConstant(value(instr)));
                break;
            case SETGLOB:
                // Set the value of the global to be the TOS value.
                setGlob(byte2(instr), byte1(instr));
                break;
            case MKARRAY:
                // Create a new array initialised from stack values.
                mkFixedArray(value(instr));
                break;
            case SUPER:
                // Continue the current lookup with possibly different
                // args.
                sendSuper(value(instr));
                break;
            case TAILSUPER:
                // As for SUPER, but reuse the call frame.
                tailSuper(value(instr));
                break;
            case NAMESPACEREF:
                // Refer to a name in a containing namespace.
                nameSpaceRef(byte3(instr), frameConstant(byte1(instr) << 8 | byte2(instr)));
                break;
            case HEAD:
                // Push head of sequence.
                head();
                break;
            case TAIL:
                // Push tail of sequence.
                tail();
                break;
            case SIZE:
                // Push size of sequence.
                size();
                break;
            case DROP:
                // Drop elements from sequence.
                drop();
                break;
            case ISEMPTY:
                // Check whether sequence is empty.
                isEmpty();
                break;
            case INCLUDES:
                // Check whether a sequence includes an element.
                includes();
                break;
            case EXCLUDING:
                // Remove an element from a sequence.
                excluding();
                break;
            case INCLUDING:
                // Add an element to a collection.
                including();
                break;
            case SEL:
                // Select an element from a collection.
                sel();
                break;
            case UNION:
                // Form the union of two sets.
                union();
                break;
            case ASSEQ:
                // Transform a collection to a sequence.
                asSeq();
                break;
            case AT:
                // Index a sequence.
                at();
                break;
            case SKPBACK:
                // Jump back through the instruction stream.
                // Take into account that we have moved on by 1
                // instruction.
                // skpBack(value(instr) + 1);
                R0 = valueStack.elements[currentFrame + FRAMECODEINDEX] & DATA;
                valueStack.elements[currentFrame + FRAMECODEINDEX] = R0 - ((instr & DATA) + 1);
                break;
            case NULL:
                // The constant....
                valueStack.elements[valueStack.index++] = undefinedValue;
                break;
            case OF:
                // Pop an element, push its classifier.
                valueStack.push(type(valueStack.pop()));
                break;
            case THROW:
                // Pop an element and throw to nearest catch.
                throwIt();
                break;
            case TRY:
                // Pop handler, pop free vars, call code box.
                tryIt(byte2(instr), frameConstant(byte3(instr) << 8 | byte1(instr)));
                break;
            case ISKINDOF:
                isKindOf();
                break;
            case SOURCEPOS:
                // sourcePos((byte3(instr) << 8) | byte2(instr), byte1(instr));
                R0 = (instr & BYTE3) >>> 8;
                R1 = (instr & BYTE2) >>> 8;
                R2 = instr & BYTE1;
                valueStack.elements[currentFrame + FRAMELINECOUNT] = INT_MASK | R0 | R1;
                valueStack.elements[currentFrame + FRAMECHARCOUNT] = INT_MASK | R2;
                break;
            case GETELEMENT:
                getElement(frameConstant(value(instr)));
                break;
            case SETHEAD:
                setHead();
                break;
            case SETTAIL:
                setTail();
                break;
            case READ:
                read();
                break;
            case ACCEPT:
                accept();
                break;
            case ARRAYREF:
                arrayRef();
                break;
            case ARRAYSET:
                arraySet();
                break;
            case TABLEGET:
                tableGet();
                break;
            case TABLEPUT:
                tablePut();
                break;
            case NOOP:
                break;
            case SLEEP:
                sleep();
                break;
            case CONST:
                pushStack(frameConstant(value(instr)));
                break;
            case SYMBOLVALUE:
                pushStack(symbolValue(frameConstant(value(instr))));
                break;
            case SETLOCPOP:
                // Set the value of a local variable in the current
                // frame and remove the value from the top of the stack.
                valueStack.elements[currentFrame + FRAMELOCAL0 + (instr & DATA)] = valueStack.elements[--valueStack.index];
                break;
            case DISPATCH:
                // Perform an indexed jump using a jump table from the
                // constants area. The top of the stack should be an
                // integer suitable as an index into the table.
                dispatch(frameConstant(value(instr)), valueStack.pop());
                break;
            case INCSELFSLOT:
                // Increment the names slot in the value of self.
                incSelfSlot(frameConstant(value(instr)));
                break;
            case DECSELFSLOT:
                decSelfSlot(frameConstant(value(instr)));
                break;
            case INCLOCAL:
                // Increment the specified local by 1.
                // setFrameLocal(instr & DATA, mkInt(intValue(frameLocal(instr & DATA)) + 1));
                R0 = valueStack.elements[currentFrame + FRAMELOCAL0 + (instr & DATA)];
                if ((R0 >> 24) == INT)
                    R0 = R0 & DATA;
                else
                    R0 = -(R0 & DATA);
                R0++;
                if (R0 < 0)
                    R0 = NEGINT_MASK | -R0;
                else
                    R0 = INT_MASK | R0;
                valueStack.elements[currentFrame + FRAMELOCAL0 + (instr & DATA)] = R0;
                valueStack.elements[valueStack.index++] = R0;
                break;
            case DECLOCAL:
                // Decrement the specified local by 1.
                setFrameLocal(value(instr), mkInt(intValue(frameLocal(value(instr))) - 1));
                pushStack(frameLocal(value(instr)));
                break;
            case ADDLOCAL:
                // Add to the value of a local.
                pushStack(mkInt(intValue(frameLocal(value(instr))) + 1));
                break;
            case SUBLOCAL:
                // Subtract 1 from the value of a local.
                pushStack(mkInt(intValue(frameLocal(value(instr))) - 1));
                break;
            case PREPEND:
                // Like CONS except TOS must be a sequence.
                prepend();
                break;
            case ENTERDYN:
                // The args are at the top of the stack. Close the call frame and
                // enter the dynamic variable.
                enterDynamic(frameConstant(byte3(instr) << 8 | byte1(instr)), byte2(instr));
                break;
            case TAILENTERDYN:
                // The args are at the top of the stack. Close the call frame and
                // enter the dynamic variable.
                tailEnterDynamic(frameConstant(byte3(instr) << 8 | byte1(instr)), byte2(instr));
                break;
            case ISNOTEMPTY:
                // isNotEmpty();
                R0 = valueStack.elements[--valueStack.index];
                R1 = (R0 >> 24);
                switch (R1) {
                case CONS:
                case NIL:
                    valueStack.elements[valueStack.index++] = R0 == nilValue ? falseValue : trueValue;
                    break;
                case SET:
                    valueStack.elements[valueStack.index++] = setElements(R0) == nilValue ? falseValue : trueValue;
                    break;
                case OBJ:
                    valueStack.push(R0);
                    send0(mkSymbol("isNotEmpty"));
                    break;
                case HASHTABLE:
                    valueStack.push(hashTableIsEmpty(R0) ? falseValue : trueValue);
                    break;
                default:
                    throw new MachineError(TYPE, "Machine.isEmpty expects a collection: " + valueToString(R0));
                }
                break;
            case LOCALHEAD:
                // Refer to a local variable value in the current stack
                // frame and push head.
                R0 = valueStack.elements[currentFrame + FRAMELOCAL0 + (instr & DATA)];
                switch (R0 >> 24) {
                case NIL:
                    throw new MachineError(TYPE, "Cannot take the head of Seq{}");
                case CONS:
                    valueStack.elements[valueStack.index++] = consHead(R0);
                    break;
                default:
                    valueStack.push(R0);
                    send0(theSymbolHead);
                }
                break;
            case LOCALTAIL:
                // Refer to a local variable value in the current stack
                // frame and push tail.
                // pushTail(frameLocal(value(instr)));
                R0 = valueStack.elements[currentFrame + FRAMELOCAL0 + (instr & DATA)];
                switch (R0 >> 24) {
                case NIL:
                    throw new MachineError(TYPE, "Cannot take the tail of Seq{}");
                case CONS:
                    valueStack.elements[valueStack.index++] = consTail(R0);
                    break;
                default:
                    valueStack.push(R0);
                    send0(theSymbolTail);
                }
                break;
            case LOCALASSEQ:
                // Push the indexed local as a sequence.
                valueStack.push(asSeq(frameLocal(value(instr))));
                break;
            case LOCALISEMPTY:
                // Push whether the local is empty.
                // pushIsEmpty(frameLocal(value(instr)));
                R0 = valueStack.elements[currentFrame + FRAMELOCAL0 + (instr & DATA)];
                switch (R0 >> 24) {
                case CONS:
                case NIL:
                    valueStack.elements[valueStack.index++] = (R0 == nilValue ? trueValue : falseValue);
                    break;
                case SET:
                    valueStack.elements[valueStack.index++] = (setElements(R0) == nilValue ? trueValue : falseValue);
                    break;
                case OBJ:
                    valueStack.push(R0);
                    send0(theSymbolIsEmpty);
                    break;
                case HASHTABLE:
                    valueStack.push(hashTableIsEmpty(R0) ? trueValue : falseValue);
                    break;
                default:
                    throw new MachineError(TYPE, "Machine.isEmpty expects a collection: " + valueToString(R0));
                }
                break;
            case DOTSELF:
                // Reference the slot via self...
                // dot(frameConstant(instr & DATA), frameSelf());
                dot(frameConstant(instr & DATA), valueStack.elements[currentFrame + FRAMESELF]);
                break;
            case DOTLOCAL:
                dot(frameConstant(byte2(instr) << 8 | byte1(instr)), frameLocal(byte3(instr)));
                break;
            case SETLOCALSLOT:
                setSlot(frameLocal(byte3(instr)), frameConstant(byte2(instr) << 8 | byte1(instr)), valueStack.pop());
                break;
            case SETSELFSLOT:
                setSlot(frameSelf(), frameConstant(value(instr)), valueStack.pop());
                break;
            case LOCALREFPOS:
                // References the local and records the line in the
                // original source code.
                valueStack.elements[valueStack.index++] = valueStack.elements[currentFrame + FRAMELOCAL0 + byte3(instr)];
                valueStack.elements[currentFrame + FRAMELINECOUNT] = INT_MASK | ((byte2(instr) << 8) | byte1(instr));
                valueStack.elements[currentFrame + FRAMECHARCOUNT] = INT_MASK;
                break;
            case DYNREFPOS:
                // References a dynamic and sets the line count...
                dynamic(frameConstant(value(byte3(instr))));
                valueStack.elements[currentFrame + FRAMELINECOUNT] = INT_MASK | ((byte2(instr) << 8) | byte1(instr));
                valueStack.elements[currentFrame + FRAMECHARCOUNT] = INT_MASK;
                break;
            case ASSOC:
                assoc();
                break;
            case RETDOTSELF:
                // Return the value of a slot via self...
                dot(frameConstant(instr & DATA), valueStack.elements[currentFrame + FRAMESELF]);
                popFrame();
                break;
            case TOSTRING:
                // Turn the value into a string...
                if (needsGC())
                    gc();
                toStringInstr();
                break;
            case ARITY:
                // Get the number of expected arguments...
                arity();
                break;
            case STRINGEQL:
                // A string equal to the first argument...
                stringEqual();
                break;
            case GET:
                // Tables or objects...
                get();
                break;
            case PUT:
                // Tables...
                put();
                break;
            case HASKEY:
                // Tables...
                hasKey();
                break;
            default:
                throw new MachineError(INSTR, "Machine.perform: unknown instruction " + tag(instr));
            }
        }

    }

    private void hasKey() {
        int table = valueStack.pop();
        int key = valueStack.pop();
        switch (tag(table)) {
        case HASHTABLE:
            if (hashTableHasKey(table, key))
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
            break;
        default:
            openFrame();
            valueStack.push(table);
            valueStack.push(key);
            send(1, mkSymbol("hasKey"));
        }
    }

    private void put() {
        int table = valueStack.pop();
        int value = valueStack.pop();
        int key = valueStack.pop();
        switch (tag(table)) {
        case HASHTABLE:
            tablePut(table, key, value);
            break;
        case ARRAY:
        case BUFFER:
            arraySetValue(table, key, value);
            break;
        default:
            openFrame();
            valueStack.push(key);
            valueStack.push(value);
            valueStack.push(table);
            send(2, mkSymbol("put"));
        }

    }

    private void get() {
        int table = valueStack.pop();
        int key = valueStack.pop();
        switch (tag(table)) {
        case HASHTABLE:
            tableGet(table, key);
            break;
        case OBJ:
            if (isString(key))
                dot(mkSymbol(key), table);
            else
                dot(key, table);
            break;
        default:
            openFrame();
            valueStack.push(key);
            valueStack.push(table);
            send(1, mkSymbol("get"));
        }
    }

    private void stringEqual() {
        int v2 = popStack();
        int v1 = popStack();
        switch (tag(v1)) {
        case BUFFER:
            if (bufferStringEqual(v1, v2))
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
            break;
        default:
            openFrame();
            pushStack(v2);
            pushStack(v1);
            send(1, mkSymbol("stringEqual"));
        }

    }

    private void arity() {
        int op = valueStack.pop();
        switch (tag(op)) {
        case FUN:
            valueStack.push(mkInt(funArity(op)));
            break;
        default:
            openFrame();
            valueStack.push(op);
            send(0, mkSymbol("arity"));
        }
    }

    private void toStringInstr() {
        int value = valueStack.pop();
        switch (tag(value)) {
        case INT:
            valueStack.push(intToXMFString(value));
            break;
        case BOOL:
            valueStack.push(mkString(value == trueValue ? "true" : "false"));
            break;
        case STRING:
            valueStack.push(value);
            break;
        case SYMBOL:
            valueStack.push(symbolName(value));
            break;
        case BUFFER:
            if (bufferAsString(value) == trueValue)
                valueStack.push(stringBufferToXMFString(value));
            else {
                openFrame();
                valueStack.push(value);
                send(0, mkSymbol("toString"));
            }
            break;
        default:
            openFrame();
            valueStack.push(value);
            send(0, mkSymbol("toString"));
        }
    }

    private int stringBufferToXMFString(int buf) {
        buffer.setLength(0);
        int size = value(bufferSize(buf));
        for (int i = 0; i < size; i++)
            buffer.append((char) value(bufferRef(buf, i)));
        return mkString(buffer);
    }

    private final int intToXMFString(int value) {
        buffer.setLength(0);
        int i = value(value);

        if (i == 0)
            return mkString("0");

        while (i > 0) {
            buffer.insert(0, i % 10);
            i = i / 10;
        }
        return mkString(buffer);
    }

    public void handleThrow(Throwable throwable) {
        if (stackDump) {
            System.out.println("Java Stack Dump:");
            throwable.printStackTrace();
        }
        // printDynamics();
        saveBacktrace(currentFrame);
        System.err.println("Backtrace Saved.");
        error(INSTR, throwable.toString());
    }

    public boolean terminated() {

        // There are no further active threads in the machine and therefore
        // no further activity can be performed by the machine.

        return threads == null;
    }

    public boolean terminatedThread() {

        // The machine is complete when there is no current frame.
        // Note that there may be other threads that are active.
        // The current threads should be removed.

        return currentFrame == -1;
    }

    public int fetchInstr() {

        // Get the next instruction and move the code index on by one
        // instruction. Take this into account when performing jumps.
        // If this is the last instruction in a code array then code index
        // will run off the end of the array by 1 instruction.

        int index = frameCodeIndex();
        incFrameCodeIndex();
        instrsPerformed++;
        return codeRef(frameInstrs(), index);
    }

    // *************************************************************************
    // * Machine Instruction Cycle End *
    // *************************************************************************

    // *************************************************************************
    // * Machine Instructions *
    // *************************************************************************

    public void cons() {
        if (needsGC())
            gc();
        int tail = valueStack.pop();
        int head = valueStack.pop();
        valueStack.push(mkCons(head, tail));
    }

    public void prepend() {
        if (needsGC())
            gc();
        int tail = valueStack.pop();
        int head = valueStack.pop();
        if (isCons(tail) || tail == nilValue)
            valueStack.push(mkCons(head, tail));
        else
            throw new MachineError(TYPE, "Machine.prepend expects a sequence: " + valueToString(tail));
    }

    public void head() {
        pushHead(valueStack.pop());
    }

    public void pushHead(int cons) {
        if (cons == nilValue)
            throw new MachineError(TYPE, "Cannot take the head of Seq{}");
        if (isCons(cons))
            valueStack.push(consHead(cons));
        else {
            valueStack.push(cons);
            send0(theSymbolHead);
        }
    }

    public void tail() {
        pushTail(valueStack.pop());
    }

    public void pushTail(int cons) {
        if (cons == nilValue)
            throw new MachineError(TYPE, "Cannot take the tail of Seq{}");

        if (isCons(cons))
            valueStack.push(consTail(cons));
        else {
            valueStack.push(cons);
            send0(theSymbolTail);
        }
    }

    public void size() {
        int collection = valueStack.pop();
        valueStack.push(mkInt(size(collection)));
    }

    public void drop() {
        if (needsGC())
            gc();
        int collection = valueStack.pop();
        int length = valueStack.pop();
        valueStack.push(drop(collection, value(length)));
    }

    public void isEmpty() {
        pushIsEmpty(valueStack.pop());
    }

    public void pushIsEmpty(int collection) {
        switch (tag(collection)) {
        case CONS:
        case NIL:
            valueStack.push(collection == nilValue ? trueValue : falseValue);
            break;
        case SET:
            valueStack.push(setElements(collection) == nilValue ? trueValue : falseValue);
            break;
        case OBJ:
            valueStack.push(collection);
            send0(theSymbolIsEmpty);
            break;
        case HASHTABLE:
            valueStack.push(hashTableIsEmpty(collection) ? trueValue : falseValue);
            break;
        default:
            throw new MachineError(TYPE, "Machine.isEmpty expects a collection: " + valueToString(collection));
        }
    }

    public void isNotEmpty() {
        int collection = valueStack.pop();
        switch (tag(collection)) {
        case CONS:
        case NIL:
            valueStack.push(collection == nilValue ? falseValue : trueValue);
            break;
        case SET:
            valueStack.push(setElements(collection) == nilValue ? falseValue : trueValue);
            break;
        case OBJ:
            valueStack.push(collection);
            send0(mkSymbol("isNotEmpty"));
            break;
        case HASHTABLE:
            valueStack.push(hashTableIsEmpty(collection) ? falseValue : trueValue);
            break;
        default:
            throw new MachineError(TYPE, "Machine.isEmpty expects a collection: " + valueToString(collection));
        }
    }

    public void includes() {
        int collection = valueStack.pop();
        int element = valueStack.pop();
        valueStack.push(includes(collection, element) ? trueValue : falseValue);
    }

    public void excluding() {
        if (needsGC())
            gc();
        int collection = valueStack.pop();
        int element = valueStack.pop();
        valueStack.push(excluding(collection, element));
    }

    public void including() {
        if (needsGC())
            gc();
        int collection = valueStack.pop();
        int element = valueStack.pop();
        valueStack.push(including(collection, element));
    }

    public void sel() {
        int collection = valueStack.pop();
        valueStack.push(sel(collection));
    }

    public void union() {
        if (needsGC())
            gc();
        int set1 = valueStack.pop();
        int set2 = valueStack.pop();
        valueStack.push(union(set1, set2));
    }

    public void asSeq() {
        if (needsGC())
            gc();
        int collection = valueStack.pop();
        valueStack.push(asSeq(collection));
    }

    public void at() {
        int collection = valueStack.pop();
        int index = valueStack.pop();
        valueStack.push(at(collection, value(index)));
    }

    public void mkSeqFrom(int length) {
        if (needsGC())
            gc();
        int seq = nilValue;
        for (int i = 0; i < length; i++) {
            int element = valueStack.pop();
            seq = mkCons(element, seq);
        }
        valueStack.push(seq);
    }

    public void mkSetFrom(int length) {
        if (needsGC())
            gc();
        int seq = nilValue;
        for (int i = 0; i < length; i++) {
            int element = valueStack.pop();
            seq = mkCons(element, seq);
        }
        valueStack.push(mkSet(seq));
    }

    public void overloadedBinOp(int v1, int v2, String op) {
        openFrame();
        valueStack.push(v2);
        valueStack.push(v1);
        send(1, mkSymbol(op));
    }

    public void add() {
        // Add can allocate fairly large strings. Check for garbage collection
        // before the values are popped from the stack.
        if (needsGC())
            gc();
        int v2 = valueStack.pop();
        int v1 = valueStack.pop();

        // The ADD instruction is overloaded.
        if (isInt(v1) && isInt(v2)) {
            // Add integers.
            valueStack.push(mkInt(intValue(v1) + intValue(v2)));
            return;
        }
        if (isFloat(v1) && isFloat(v2)) {
            valueStack.push(floatAdd(v1, v2));
            return;
        }
        if (isString(v1) && isString(v2)) {
            valueStack.push(stringAppend(v1, v2));
            return;
        }
        if (isSymbol(v1)) {
            valueStack.push(symbolName(v1));
            valueStack.push(v2);
            add();
            return;
        }
        if (isSymbol(v2)) {
            valueStack.push(v1);
            valueStack.push(symbolName(v2));
            add();
            return;
        }
        if (isBuffer(v1)) {
            arraySetValue(v1, size(v1), v2);
            return;
        }
        if (isString(v1) || isString(v2)) {
            // If either argument is a string then coerce both arguments to
            // strings and produce the concatenation of the strings.
            valueStack.push(mkString(valueToString(v1) + valueToString(v2)));
            return;
        }
        if (isSeq(v1) && isSeq(v2)) {
            valueStack.push(consAppend(v1, v2));
            return;
        }
        if (isSet(v1) && isSet(v2)) {
            valueStack.push(union(v1, v2));
            return;
        }
        if (isSymbol(v1)) {
            pushStack(symbolName(v1));
            pushStack(v2);
            add();
            return;
        }
        if (isSymbol(v2)) {
            pushStack(v1);
            pushStack(symbolName(v2));
            add();
            return;
        }
        overloadedBinOp(v1, v2, "add");
    }

    public void sub() {
        if (needsGC())
            gc();
        int v1 = valueStack.pop();
        int v2 = valueStack.pop();
        if (isInt(v1) && isInt(v2))
            valueStack.push(mkInt(intValue(v2) - intValue(v1)));
        else if (isFloat(v1) && isFloat(v2))
            valueStack.push(floatSub(v2, v1));
        else if (isSet(v1) && isSet(v2))
            valueStack.push(setDifference(v2, v1));
        else if ((isSeq(v1) || isNil(v1)) && (isSeq(v2) || isNil(v2)))
            valueStack.push(seqDifference(v2, v1));
        else
            overloadedBinOp(v2, v1, "sub");
    }

    public void mul() {

        int v1 = valueStack.elements[--valueStack.index];
        int v2 = valueStack.elements[--valueStack.index];

        boolean v1IsInt = (v1 >> 24) == INT;
        boolean v1IsNegInt = (v1 >> 24) == NEGINT;
        boolean v2IsInt = (v2 >> 24) == INT;
        boolean v2IsNegInt = (v2 >> 24) == NEGINT;

        if ((v1IsInt || v1IsNegInt) && (v2IsInt || v2IsNegInt)) {
            int v = (v1 & DATA) * (v2 & DATA);
            if (v1IsNegInt ^ v2IsNegInt)
                valueStack.elements[valueStack.index++] = NEGINT_MASK | (v & DATA);
            else
                valueStack.elements[valueStack.index++] = INT_MASK | (v & DATA);
        } else if (isFloat(v1) && isFloat(v2))
            valueStack.push(floatMul(v2, v1));
        else
            overloadedBinOp(v2, v1, "mul");
    }

    public void div() {
        int v1 = valueStack.pop();
        int v2 = valueStack.pop();
        if (isInt(v1) && isInt(v2))
            valueStack.push(intSlash(v2, v1));
        else if (isFloat(v1) && isFloat(v2))
            valueStack.push(floatDiv(v2, v1));
        else
            overloadedBinOp(v2, v1, "slash");
    }

    public void gre() {
        int v1 = valueStack.pop();
        int v2 = valueStack.pop();
        if (isInt(v1) && isInt(v2))
            if (intValue(v2) > intValue(v1))
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
        else if (isFloat(v1) && isFloat(v2))
            if (floatGreater(v2, v1))
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
        else if (isString(v1) && isString(v2))
            if (stringGreater(v2, v1))
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
        else
            overloadedBinOp(v2, v1, "greater");
    }

    public void less() {

        int v1 = valueStack.elements[--valueStack.index];
        int v2 = valueStack.elements[--valueStack.index];

        boolean v1IsInt = (v1 >> 24) == INT;
        boolean v1IsNegInt = (v1 >> 24) == NEGINT;
        boolean v2IsInt = (v2 >> 24) == INT;
        boolean v2IsNegInt = (v2 >> 24) == NEGINT;

        if ((v1IsInt || v1IsNegInt) && (v2IsInt || v2IsNegInt)) {
            int i1 = v1IsNegInt ? -(v1 & DATA) : (v1 & DATA);
            int i2 = v2IsNegInt ? -(v2 & DATA) : (v2 & DATA);
            valueStack.elements[valueStack.index++] = (i2 < i1) ? trueValue : falseValue;
        } else if (isFloat(v1) && isFloat(v2))
            if (floatLess(v2, v1))
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
        else if (isString(v1) && isString(v2))
            if (stringLess(v2, v1))
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
        else
            overloadedBinOp(v2, v1, "less");
    }

    public void eql() {
        int v1 = valueStack.pop();
        int v2 = valueStack.pop();
        if (equalValues(v1, v2))
            valueStack.push(trueValue);
        else
            valueStack.push(falseValue);
    }

    public void and() {
        int v1 = valueStack.pop();
        int v2 = valueStack.pop();
        if (isBool(v1) && isBool(v2))
            if ((v1 == trueValue) && (v2 == trueValue))
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
        else if (isInt(v1) && isInt(v2))
            valueStack.push(mkInt(value(v1) & value(v2)));
        else
            overloadedBinOp(v1, v2, "booland");
    }

    public void or() {
        int v1 = valueStack.pop();
        int v2 = valueStack.pop();
        if (isBool(v1) && isBool(v2))
            if ((v1 == trueValue) || (v2 == trueValue))
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
        else if (isInt(v1) && isInt(v2))
            valueStack.push(mkInt(value(v1) | value(v2)));
        else
            overloadedBinOp(v1, v2, "boolor");
    }

    public void not() {
        int v = valueStack.pop();
        if (isBool(v))
            if (v == falseValue)
                valueStack.push(trueValue);
            else
                valueStack.push(falseValue);
        else
            throw new MachineError(TYPE, "Machine.not: operand types");
    }

    public void implies() {
        int v1 = valueStack.pop();
        int v2 = valueStack.pop();
        if (isBool(v1) && isBool(v2))
            valueStack.push(implies(v2, v1));
        else
            overloadedBinOp(v2, v1, "implies");
    }

    public int implies(int v1, int v2) {
        boolean b1 = v1 == trueValue;
        boolean b2 = v2 == trueValue;
        boolean implies = (!b1) || b2;
        return implies ? trueValue : falseValue;
    }

    public void isKindOf() {
        int value = valueStack.pop();
        int type = valueStack.pop();
        if (value == undefinedValue)
            valueStack.push(trueValue);
        else {
            int valueType = type(value);
            if (valueType == theTypeSeqOfElement || valueType == theTypeSetOfElement)
                collIsKindOf(value, type);
            else
                basicIsKindOf(value, type, valueType);
        }
    }

    public void collIsKindOf(int coll, int type) {
        openFrame();
        valueStack.push(type);
        valueStack.push(coll);
        send(1, mkSymbol("isKindOf"));
    }

    public void basicIsKindOf(int value, int type, int valueType) {
        int TOS = valueStack.getTOS();
        boolean isKindOf = false;
        valueStack.push(valueType);
        while (valueStack.getTOS() != TOS && !isKindOf) {
            valueType = valueStack.pop();
            if (valueType == type)
                isKindOf = true;
            else {
                int parents = asSeq(objAttValue(valueType, theSymbolParents));
                while (parents != nilValue) {
                    valueStack.push(consHead(parents));
                    parents = consTail(parents);
                }
            }
        }
        valueStack.setTOS(TOS);
        valueStack.push(isKindOf ? trueValue : falseValue);
    }

    public boolean inheritsFrom(int type1, int type2) {

        // Returns true when the first type inherits
        // from the second type...

        if (type1 == type2)
            return true;
        else {
            int parents = asSeq(objAttValue(type1, theSymbolParents));
            while (parents != nilValue) {
                int parent = consHead(parents);
                if (inheritsFrom(parent, type2))
                    return true;
                else
                    parents = consTail(parents);
            }
            return false;
        }
    }

    public void sourcePos(int lineCount, int charCount) {
        setFrameLineCount(lineCount);
        setFrameCharCount(charCount);
    }

    public void getElement(int name) {
        int nameSpace = valueStack.pop();
        int table = objAttValue(nameSpace, theSymbolContents);
        if (table == -1)
            throw new MachineError(TYPE, ":: expects a namespace, received " + valueToString(nameSpace));
        if (hashTableHasKey(table, name))
            valueStack.push(hashTableGet(table, name));
        else
            throw new MachineError(NAMESPACEERR, "Name space does not define a name " + valueToString(name));
    }

    public int getElement(CharSequence name) {
        int nameSpace = valueStack.pop();
        int table = objAttValue(nameSpace, theSymbolContents);
        if (table == -1)
            return -1;
        int length = arrayLength(table);
        int element = -1;
        for (int i = 0; i < length && element == -1; i++) {
            int bucket = arrayRef(table, i);
            while (bucket != nilValue && element == -1) {
                int cell = consHead(bucket);
                bucket = consTail(bucket);
                int symbol = consHead(cell);
                int string = symbolName(symbol);
                if (stringEqual(string, name))
                    element = consTail(cell);
            }
        }
        if (element != -1) {
            valueStack.push(element);
            return 0;
        } else
            return -1;
    }

    public void setHead() {
        int seq = valueStack.pop();
        int value = valueStack.top();
        if (isCons(seq))
            consSetHead(seq, value);
        else
            throw new MachineError(TYPE, "Machine.setHead expects a sequence: " + valueToString(seq));
    }

    public void setTail() {
        int seq = valueStack.pop();
        int value = valueStack.top();
        if (isCons(seq))
            consSetTail(seq, value);
        else
            throw new MachineError(TYPE, "Machine.setTail expects a sequence: " + valueToString(seq));
    }

    public void read() {
        int channel = valueStack.pop();
        if (isInputChannel(channel))
            read(channel);
        else if (isString(channel))
            readClient(valueToString(channel));
        else
            error(TYPE, "Machine.read() expects an input channel: " + valueToString(channel));
    }

    public void readClient(String name) {

        // Called with the name of a message client.

        if (XOS.isMessageClient(name))
            if (XOS.ready(name))
                readMessageClient(name);
            else {
                XOS.blockOnRead(name);
                yield();
            }
        else
            error(TYPE, "Machine.read() expects a client name: " + name);
    }

    public void readMessageClient(String name) {
        XOS.Message message = XOS.readMessage(name);
        int array = mkArray(message.arity + 1);
        arraySet(array, 0, mkString(message.name()));
        for (int i = 0; i < message.arity; i++) {
            XOS.Value value = message.args[i];
            arraySet(array, i + 1, messageValue(value));
        }
        valueStack.push(array);
    }

    public int messageValue(XOS.Value value) {
        switch (value.type) {
        case XData.INT:
            return mkInt(value.intValue);
        case XData.BOOL:
            return value.boolValue ? trueValue : falseValue;
        case XData.STRING:
            return mkString(value.strValue());
        case XData.FLOAT:
            return mkFloat(value.floatValue);
        case XData.VECTOR:
            int array = mkArray(value.values.length);
            for (int i = 0; i < value.values.length; i++)
                arraySet(array, i, messageValue(value.values[i]));
            return array;
        default:
            throw new MachineError(TYPE, "Unknown message arg type: " + value.type);
        }
    }

    public void read(int in) {

        // If the input channel is ready for input then we can go ahead
        // and read from it. Otherwise, XOS will mark the current thread
        // as blocking on input from the supplied channel and the current
        // thread yields. XOS will re-schedule the thread when input
        // becomes available on the thread.

        if (XOS.ready(value(in)))
            readReadyChannel(in);
        else {
            XOS.blockOnRead(value(in));
            yield();
        }
    }

    public void readReadyChannel(int in) {
        if (XOS.isTokenChannel(value(in)))
            readTokenChannel(in);
        else if (XOS.isDataInputChannel(value(in)))
            readDataInputChannel(in);
        else
            readChannel(in);
    }

    public void readChannel(int in) {
        int c = XOS.read(value(in));
        valueStack.push(mkInt(c));
    }

    public void readDataInputChannel(int in) {
        int type = XOS.read(value(in));
        DChannel din = XOS.dataInputStream(value(in));
        switch (type) {
        case DataInputStream.BOOL:
            valueStack.push(din.boolValue ? trueValue : falseValue);
            break;
        case DataInputStream.INT:
            valueStack.push(mkInt(din.intValue));
            break;
        case DataInputStream.STRING:
            StringBuffer s = din.stringValue;
            int length = s.length();
            int string = mkString(length);
            for (int i = 0; i < length; i++)
                stringSet(string, i, s.charAt(i));
            valueStack.push(string);
            break;
        default:
            System.out.println("Unknown type of data from input channel: " + type);
            exitAbnormal();
        }
    }

    public void readTokenChannel(int in) {
        int t = nextToken(in);
        valueStack.push(t);
    }

    public void resetToInitialState(int in) {
        if (isInputChannel(in))
            if (XOS.isTokenChannel(value(in)))
                XOS.resetToInitialState(value(in));
            else
                throw new MachineError(TYPE, "Machine.resetToInitialState expects a token channel.");
        else
            throw new MachineError(TYPE, "Machine.resetToInitialState expects a token channel.");
    }

    public int readString(int in) {
        StringBuffer s = new StringBuffer();
        int c;
        while ((c = XOS.read(value(in))) != -1)
            s.append((char) c);
        return mkString(s.toString());
    }

    public int readVector(int in, int array) {
        switch (tag(array)) {
        case ARRAY:
            return readArray(in, array);
        case BUFFER:
            return readBuffer(in, array);
        default:
            throw new MachineError(TYPE, "Machine.readVector: unknown target " + valueToString(array));
        }
    }

    public int readArray(int in, int array) {
        int length = arrayLength(array);
        int index = 0;
        while (XOS.ready(value(in)) && !XOS.eof(value(in)) && index < length) {
            int c = XOS.read(value(in));
            arraySet(array, index++, mkInt(c));
        }
        return mkInt(index);
    }

    public int readBuffer(int in, int buffer) {
        int chars = 0;
        while (XOS.ready(value(in)) && !XOS.eof(value(in))) {
            int c = XOS.read(value(in));
            chars++;
            bufferSet(buffer, bufferSize(buffer), mkInt(c));
        }
        return mkInt(chars);
    }

    public int peek(int in) {
        if (isInputChannel(in))
            return mkInt(XOS.peek(value(in)));
        else
            return error(TYPE, "Machine.peek() expects an input channel: " + valueToString(in));
    }

    public void accept() {

        // The head of the stack should be a string that names a client.
        // If the client has connected to XOS then return true otherwise
        // block until the the client connects.

        int value = valueStack.pop();
        if (isString(value)) {
            String name = valueToString(value);
            if (XOS.isConnected(name))
                valueStack.push(trueValue);
            else {
                XOS.blockOnAccept(name);
                yield();
            }
        } else
            error(TYPE, "Machine.accept expects a name: " + valueToString(value));
    }

    public int clientInputChannel(int value) {
        if (isString(value)) {
            String name = valueToString(value);
            int index = XOS.inputChannel(name);
            if (index != -1)
                return mkInputChannel(index);
            else
                return -1;
        } else
            return -1;
    }

    public int clientOutputChannel(int value) {
        if (isString(value)) {
            String name = valueToString(value);
            int index = XOS.outputChannel(name);
            if (index != -1)
                return mkOutputChannel(index);
            else
                return error(ERROR, "No client named " + name);
        } else
            return error(TYPE, "No client named " + valueToString(value));
    }

    public void arrayRef() {
        int array = valueStack.pop();
        int index = valueStack.pop();
        if ((isArray(array) || isTable(array)) && value(index) < arrayLength(array))
            valueStack.push(arrayRef(array, value(index)));
        else if (isBuffer(array))
            valueStack.push(bufferRef(array, value(index)));
        else {
            openFrame();
            valueStack.push(index);
            valueStack.push(array);
            send(1, mkSymbol("ref"));
        }
    }

    public void arraySet() {
        int array = valueStack.pop();
        int index = valueStack.pop();
        int newValue = valueStack.pop();
        arraySetValue(array, index, newValue);
    }

    public void arraySetValue(int array, int index, int newValue) {
        if (isArray(array) || isTable(array)) {
            int oldValue = arrayRef(array, value(index));
            arraySet(array, value(index), newValue);
            arraySetDaemons(array, index, newValue, oldValue);
        } else if (isBuffer(array)) {
            if (value(index) < value(bufferSize(array))) {
                int oldValue = bufferRef(array, value(index));
                bufferSet(array, value(index), newValue);
                bufferSetDaemons(array, index, newValue, oldValue);
            } else {
                bufferSet(array, value(index), newValue);
                valueStack.push(array);
            }
        } else
            error(TYPE, "Machine.arraySet() expects an array or table: " + valueToString(array));
    }

    public void arraySetDaemons(int array, int index, int newValue, int oldValue) {
        if ((arrayDaemonsActive(array) == trueValue) && (arrayDaemons(array) != nilValue)) {
            openFrame();
            valueStack.push(mkInt(index));
            valueStack.push(newValue);
            valueStack.push(oldValue);
            valueStack.push(array);
            send(3, theSymbolFire);
        } else
            valueStack.push(array);
    }

    public void bufferSetDaemons(int buffer, int index, int newValue, int oldValue) {
        if ((bufferDaemonsActive(buffer) == trueValue) && (bufferDaemons(buffer) != nilValue)) {
            openFrame();
            valueStack.push(mkInt(index));
            valueStack.push(newValue);
            valueStack.push(oldValue);
            valueStack.push(buffer);
            send(3, theSymbolFire);
        } else
            valueStack.push(buffer);
    }

    public void tableGet() {
        int table = valueStack.pop();
        int key = valueStack.pop();
        tableGet(table, key);
    }

    public void tableGet(int table, int key) {
        if (isTable(table)) {
            int value = hashTableGet(table, key);
            if (value == -1)
                error(HASHTABLEGET, "No key in table: " + valueToString(key));
            else
                valueStack.push(value);
        } else
            error(TYPE, "Machine.tableGet() expects a table: " + valueToString(table));
    }

    public void tablePut() {
        int table = valueStack.pop();
        int key = valueStack.pop();
        int value = valueStack.pop();
        tablePut(table, key, value);
    }

    public void tablePut(int table, int key, int value) {
		if (isTable(table)) {
			if (hashTableGet(table, key) != value) {
				int index = hashTableIndex(table, key);
				boolean active = arrayDaemonsActive(table) == trueValue;
				boolean hasDaemons = arrayDaemons(table) != nilValue;
				if (active && hasDaemons) {
					int oldBucket = copyBucket(hashTableBucket(table, key));
					hashTablePut(table, key, value);
					int newBucket = hashTableBucket(table, key);
					openFrame();
					valueStack.push(mkInt(index));
					valueStack.push(newBucket);
					valueStack.push(oldBucket);
					valueStack.push(table);
					send(3, theSymbolFire);
				} else {
					hashTablePut(table, key, value);
					valueStack.push(table);
				}
			} else
				valueStack.push(table);
		} else
			error(TYPE, "Machine.tablePut() expects a table: "
					+ valueToString(table));
	}

    public void dot(int name) {

        // The '.' operator accesses object fields. The object is at the head
        // of the stack. Call dot/2 since this is used elsewhere as a general
        // routine.

        int obj = valueStack.pop();
        dot(name, obj);
    }

    public void dot(int name, int obj) {

        // Field reference via '.' can occur to objects, collections and
		// non-objects.
        // Field reference in objects is fairly straightforward. Field reference in
        // non-objects simulates fields where appropriate. Field reference in
        // collections causes the reference to occur to all the elements of the
        // collection and for the results to be flattened (where appropriate) into a single
        // collection a la standard OCL.

        switch (obj >> 24) {
        case ALIENOBJ:
        case OBJ:
        case FOREIGNOBJ:
            dotObj(name, obj);
            break;
        case SET:
        case CONS:
        case NIL:
            dotCollection(name, obj);
            break;
        case FUN:
            dotFun(name, obj);
            break;
        case FOREIGNFUN:
            dotForeignFun(name, obj);
            break;
        case SYMBOL:
            dotSymbol(name, obj);
            break;
        case HASHTABLE:
            dotCollection(name, hashTableContents(obj));
            break;
        case DAEMON:
            dotDaemon(name, obj);
            break;
        default:
            throw new MachineError(TYPE, "Machine.dot: unknown type of value " + valueToString(obj) + "."
                    + valueToString(name));
        }
    }

    public boolean dotAlienObj(int name, int obj) {
        if (isAlienObj(obj)) {
            Object target = AlienObj(AlienObjIndex(obj));
            String slot = "<undefined>";
            if (isString(name))
                slot = valueToString(name);
            if (isSymbol(name))
                slot = valueToString(symbolName(name));
            int att = AlienObject.hasSlot(target, slot);
            if (att != -1) {
                valueStack.push(AlienObject.getSlot(this, target, slot));
                return true;
            }
        }
        return false;
    }

    public void dotObj(int name, int obj) {

        // Access a machine object field. If the object does not have a standard
        // slot access protocol then the class of the object is sent a message
        // that invokes an operation that implements the slot access protocol.
        // If the object has a standard slot access protocol then the machine
        // can handle the access directly.

        if (!dotAlienObj(name, obj)) {
            if (standardSlotAccessProtocol(obj) || isDefaultGetMOP(type(obj))) {
                int att = objAttribute(obj, name);
                if (att == -1)
                    sendSlotMissing(obj, name);
                else
                    valueStack.push(attributeValue(att));
            } else {
                sendSlotAccess(obj, name);
            }
        }
    }

    public void sendSlotMissing(int obj, int name) {
        openFrame();
        valueStack.push(name);
        valueStack.push(obj);
        send(1, mkSymbol("slotMissing"));
    }

    public boolean standardSlotAccessProtocol(int object) {
        int metaClass = type(type(object));
        return theClassClass == undefinedValue || metaClass == theClassClass || metaClass == theClassPackage;
    }

    public void sendSlotAccess(int obj, int name) {
        openFrame();
        valueStack.push(obj);
        valueStack.push(name);
        valueStack.push(type(obj));
        send(2, mkSymbol("getInstanceSlot"));
    }

    public void dotCollection(int name, int collection) {
        openFrame();
        valueStack.push(symbolName(name));
        valueStack.push(collection);
        send(1, theSymbolDot);
    }

    public void dotDaemon(int name, int daemon) {
        openFrame();
        valueStack.push(name);
        valueStack.push(daemon);
        send(1, mkSymbol("get"));
    }

    public void dotFun(int name, int fun) {
        if (name == theSymbolName)
            valueStack.push(funName(fun));
        else if (name == theSymbolArity)
            valueStack.push(mkInt(funArity(fun)));
        else if (name == theSymbolOwner)
            valueStack.push(funOwner(fun));
        else if (name == theSymbolDocumentation)
            valueStack.push(funDocumentation(fun));
        else {
            openFrame();
            valueStack.push(name);
            valueStack.push(fun);
            send(1, mkSymbol("get"));
        }
    }

    public void dotForeignFun(int name, int fun) {
        if (name == theSymbolName)
            valueStack.push(foreignFunName(fun));
        else if (name == mkSymbol("arity"))
            valueStack.push(mkInt(foreignFunArity(fun)));
        else {
            openFrame();
            valueStack.push(name);
            valueStack.push(fun);
            send(1, mkSymbol("get"));
        }
    }

    public void dotSymbol(int name, int symbol) {
        if (name == theSymbolName)
            valueStack.push(symbolName(symbol));
        else
            throw new MachineError(MISSINGSLOT, "Machine.dotSymbol: " + valueToString(symbol)
                    + " does not have a field named " + valueToString(name));
    }

    public void skpf(int offset) {
        // If the value at the top of the stack is false then add offset to the
        // current code index, otherwise don't jump.
        int bool = valueStack.pop();
        if (isBool(bool)) {
            if (bool == falseValue)
                incFrameCodeIndex(offset);
        } else
            throw new MachineError(TYPE, "Machine.skpf: expecting a boolean");
    }

    public void skp(int offset) { // Jump by adding offset to the current code
        // index.
        incFrameCodeIndex(offset);
    }

    public void skpBack(int offset) {
        // Jump by subtracting offset frm the current code index.
        incFrameCodeIndex(-offset);
    }

    public void dispatch(int jumpTable, int value) {
        if (isArray(jumpTable) && isInt(value)) {
            int index = value(value);
            int length = arrayLength(jumpTable);
            if (index < length)
                incFrameCodeIndex(value(arrayRef(jumpTable, index)));
            else
                throw new MachineError(TYPE, "Machine.dispatch: index out of range.");
        } else
            throw new MachineError(TYPE, "Machine.dispatch: expecting a jump table and an integer index: "
                    + valueToString(value) + " " + valueToString(jumpTable));
    }

    public void incSelfSlot(int slot) {
        int self = frameSelf();
        int value = objAttValue(self, slot);
        if (value == -1)
            throw new MachineError(MISSINGSLOT, "Machine.incSelfSlot no slot named " + valueToString(slot));
        else
            objSetAttValue(self, slot, mkInt(intValue(value) + 1));
        pushStack(self);
    }

    public void decSelfSlot(int slot) {
        int self = frameSelf();
        int value = objAttValue(self, slot);
        if (value == -1)
            throw new MachineError(MISSINGSLOT, "Machine.decSelfSlot no slot named " + valueToString(slot));
        else
            objSetAttValue(self, slot, mkInt(intValue(value) - 1));
        pushStack(self);
    }

    // *************************************************************************
    // * Dynamics *
    // *************************************************************************
    // Dynamic variables are essentially global variables with scoping. When
    // a function is created it captures the current collection of dynamic
    // variables
    // and their values. When the function is called, the dynamic variables
    // captured
    // by the function are made available.
    //
    // There are two ways in which dynamic variables can be created:
    // (1) Globally add a new dynamic. Everyone sees the new dynamic variable.
    // These
    // dynamic variables have global scope and extent.
    // (2) Locally add a new dynamic. The dynamic is available to the currently
    // executing expression. These dynamic variables have local scope and
    // extent.
    //
    // There are three types of dynamic varaible:
    // (1) Dynamic values. An association between a symbol and a value.
    // (2) Dynamic tables. A dynamic table has the effect of making all the
    // keys (symbols) and values in scope.
    // (3) Dynamic slots. The slot names of 'self' in the current stack frame
    // take precedence over all other dynamic variables.
    //
    // Dynamic scoping is implemented as a linked list of dynamic variables in
    // the stack frame. Dynamic variable reference looks up the name in the
    // stack
    // frame.

    public int mkDynamicCell(int type, int value) {
        // A dynamic cell is just a cons cell.
        int cell = mkCons(type, value);
        return cell;
    }

    public int dynamicCellType(int cell) {
        return consHead(cell);
    }

    public int dynamicCellValue(int cell) {
        return consTail(cell);
    }

    public int mkDynamicBinding(int name, int value) {
        int binding = mkCons(name, value);
        return mkDynamicCell(DYNAMIC_VALUE, binding);
    }

    public int dynamicBindingName(int binding) {
        return consHead(binding);
    }

    public int dynamicBindingValue(int binding) {
        return consTail(binding);
    }

    public void dynamicBindingSetValue(int binding, int value) {
        consSetTail(binding, value);
    }

    public int mkDynamicTable(int table) {
        return mkDynamicCell(DYNAMIC_TABLE, table);
    }

    public boolean isDynamicValue(int cell) {
        return dynamicCellType(cell) == DYNAMIC_VALUE;
    }

    public boolean isDynamicTable(int cell) {
        return dynamicCellType(cell) == DYNAMIC_TABLE;
    }

    public void bindDynamic(int name, int value) {
        setFrameDynamics(mkCons(mkDynamicBinding(name, value), frameDynamics()));
    }

    public void importTable(int table) {
        setFrameDynamics(mkCons(mkDynamicTable(table), frameDynamics()));
    }

    public int dynamicValue(int name) {
        // Returns the dynamic value in the stack frame or
        // -1 if not present.
        int dynamics = frameDynamics();
        boolean found = false;
        while (!found && (dynamics != nilValue)) {
            dynamicLookupSteps++;
            int cell = consHead(dynamics);
            dynamics = consTail(dynamics);
            switch (dynamicCellType(cell)) {
            case DYNAMIC_VALUE:
                int binding = dynamicCellValue(cell);
                if (dynamicBindingName(binding) == name)
                    return dynamicBindingValue(binding);
                break;
            case DYNAMIC_TABLE:
                int table = dynamicCellValue(cell);
                int value = hashTableGet(table, name);
                if (value != -1)
                    return value;
                break;
            default:
                throw new MachineError(TYPE, "Machine.dynamicValue: Unknown type of dynamic cell: "
                        + valueToString(cell));
            }
        }
        return -1;
    }

    public int setDynamicValue(int name, int value) {
        int dynamics = frameDynamics();
        while (dynamics != nilValue) {
            dynamicLookupSteps++;
            int cell = consHead(dynamics);
            dynamics = consTail(dynamics);
            switch (dynamicCellType(cell)) {
            case DYNAMIC_VALUE:
                int binding = dynamicCellValue(cell);
                if (dynamicBindingName(binding) == name) {
                    dynamicBindingSetValue(binding, value);
                    return value;
                }
                break;
            case DYNAMIC_TABLE:
                int table = dynamicCellValue(cell);
                if (hashTableHasKey(table, name)) {
                    hashTablePut(table, name, value);
                    return value;
                }
                break;
            default:
                throw new MachineError(TYPE, "Machine.detDynamicValue: Unknown type of dynamic cell: "
                        + valueToString(cell));
            }
        }
        return -1;
    }

    public int lookupDynamic(int name) {
        // Does not push the value on the stacj - just returns it.
        // Returns -1 if not bound.
        int value = dynamicSlotReference(name);
        if (value == -1)
            return dynamicValue(name);
        else
            return value;
    }

    public void dynamic(int name) {
        // The DYNAMIC instruction handles two cases:
        // o Unqualified slot reference in 'self'
        // o Reference to dynamic variables.
        int value = dynamicSlotReference(name);
        if (value == -1)
            dynamicReference(name);
        else
            valueStack.push(value);
    }

    public int dynamicSlotReference(int name) {
        int object = frameSelf();
        switch (tag(object)) {
        case ALIENOBJ:
        case OBJ:
            int attributes = objAttributes(object);
            while (attributes != nilValue) {
                int attribute = consHead(attributes);
                int attName = attributeName(attribute);
                if (name == attName)
                    return attributeValue(attribute);
                else
                    attributes = consTail(attributes);
            }
            return -1;
        case FUN:
            if (name == theSymbolName)
                return funName(object);
            if (name == theSymbolOwner)
                return funOwner(object);
            return -1;
        case FOREIGNFUN:
            if (name == theSymbolName)
                return foreignFunName(object);
            return -1;
        default:
            return -1;
        }
    }

    public void dynamicReference(int name) {
        int value = dynamicValue(name);
        if (value == -1)
            error(UNBOUNDVAR, "Cannot resolve dynamic reference for " + valueToString(name) + ".");
        else
            valueStack.push(value);
    }

    public void printDynamics() {
        int dynamics = frameDynamics();
        while (!(dynamics == nilValue)) {
            int cell = consHead(dynamics);
            int type = dynamicCellType(cell);
            dynamics = consTail(dynamics);
            switch (type) {
            case DYNAMIC_VALUE:
                int binding = dynamicCellValue(cell);
                int name = dynamicBindingName(binding);
                int value = dynamicBindingValue(binding);
                System.out.println(valueToString(name) + " = " + valueToString(value));
                break;
            case DYNAMIC_TABLE:
                int table = dynamicCellValue(cell);
                printDynamicTable(table);
                break;
            default:
                throw new MachineError(TYPE, "Machine.printDynamics: Unknown type of dynamic value "
                        + valueToString(cell));
            }
        }
    }

    public void printDynamicTable(int table) {
        int keys = hashTableKeys(table);
        while (keys != nilValue) {
            int key = consHead(keys);
            keys = consTail(keys);
            int namedElement = hashTableGet(table, key);
            System.out.println(valueToString(key) + " = " + valueToString(namedElement));
        }
    }

    // *************************************************************************
    // * Dynamics End *
    // *************************************************************************

    public void mkfun(int operands) {

        // Make a function. The supplied operands from the
        // machine instruction encode the offsets to the
        // code box and the globals in the constants area.
        // The arity is also encoded in the operands. If
        // the constant offsets do not fit into the operands
        // then the assembler will have emitted a MKFUNE
        // instruction that uses the following wors in
        // the code stream to place the offsets.

        int byte3 = byte3(operands);
        int arity = stripVarArgs(byte3);
        int codeBox = frameConstant(byte1(operands));
        int globals = linkGlobals(byte2(operands));
        int fun = mkFun();
        int doc = valueStack.pop();
        int data = valueStack.pop();
        int sig = take(data, arity + 1);
        int properties = drop(data, arity + 1);

        funSetArity(fun, mkInt(arity));
        funSetGlobals(fun, globals);
        funSetCode(fun, codeBox);
        funSetSelf(fun, frameSelf());
        funSetSupers(fun, mkCons(fun, consTail(frameSuper())));
        funSetDynamics(fun, frameDynamics());
        funSetOwner(fun, frameNameSpace());
        funSetProperties(fun, properties);
        funSetDocumentation(fun, doc);
        funSetSig(fun, sig);
        funSetIsVarArgs(fun, isVarArgs(byte3) ? trueValue : falseValue);
        valueStack.push(fun);
    }

    public void mkfune(int byte3, int free) {

        // Extended version of MKFUN.

        int arity = stripVarArgs(byte3);
        int codeBox = frameConstant(value(popStack()));
        int globals = linkGlobals(free);
        int fun = mkFun();
        int doc = valueStack.pop();
        int data = valueStack.pop();
        int sig = take(data, arity + 1);
        int properties = drop(data, arity + 1);

        funSetArity(fun, mkInt(arity));
        funSetGlobals(fun, globals);
        funSetCode(fun, codeBox);
        funSetSelf(fun, frameSelf());
        funSetSupers(fun, mkCons(fun, frameSuper()));
        funSetDynamics(fun, frameDynamics());
        funSetOwner(fun, frameNameSpace());
        funSetProperties(fun, properties);
        funSetDocumentation(fun, doc);
        funSetSig(fun, sig);
        funSetIsVarArgs(fun, isVarArgs(byte3) ? trueValue : falseValue);
        valueStack.push(fun);
    }

    public int linkGlobals(int globalsNum) {
        int globals = mkGlobals();
        int globalsArray = mkArray(globalsNum);
        for (int i = globalsNum - 1; i >= 0; i--)
            arraySet(globalsArray, i, valueStack.pop());
        globalsSetArray(globals, globalsArray);
        globalsSetPrev(globals, frameGlobals());
        return globals;
    }

    public void global(int operands) {
        int frame1 = byte2(operands);
        int frame2 = byte3(operands);
        int index = byte1(operands);
        valueStack.push(frameGlobal((frame2 << 8) | frame1, index));
    }

    public void nameSpaceRef(int contour, int name) {
        // System.out.println("Name Space Ref: " + contour + " " +
        // valueToString(name));
        nameSpaceRef(frameNameSpace(), contour, name);
    }

    public void nameSpaceRef(int nameSpace, int contour, int name) {
        if (contour == 0)
            refNameSpace(nameSpace, name);
        else if (topLevelNameSpace(nameSpace) && contour == 1)
            refTopLevelNameSpace(nameSpace, name);
        else
            nameSpaceRef(objAttValue(nameSpace, theSymbolOwner), contour - 1, name);
    }

    public void refNameSpace(int nameSpace, int name) {
        int contents = objAttValue(nameSpace, theSymbolContents);
        if (!hashTableHasKey(contents, name)) {
            String nameSpaceName = valueToString(objAttValue(nameSpace, theSymbolName));
            throw new MachineError(NAMESPACEERR, "Machine.nameSpaceRef: Cannot find element " + valueToString(name)
                    + " in namespace " + nameSpaceName);
        } else
            valueStack.push(hashTableGet(contents, name));
    }

    public int refNameSpace(int nameSpace, StringBuffer name) {
        int contents = objAttValue(nameSpace, theSymbolContents);
        for (int i = 0; i < arrayLength(contents); i++) {
            int bucket = arrayRef(contents, i);
            while (bucket != nilValue) {
                int cell = consHead(bucket);
                int symbol = consHead(cell);
                if (isSymbol(symbol)) {
                    int string = symbolName(symbol);
                    if (stringEqual(string, name))
                        return consTail(cell);
                }
                bucket = consTail(bucket);
            }
        }
        return -1;
    }

    public boolean topLevelNameSpace(int nameSpace) {
        int owner = objAttValue(nameSpace, theSymbolOwner);
        return owner == nameSpace || owner == undefinedValue;
    }

    public void refTopLevelNameSpace(int nameSpace, int name) {
        int nameSpaceName = objAttValue(nameSpace, theSymbolName);
        if (equalValues(nameSpaceName, name))
            valueStack.push(nameSpace);
        else
            throw new MachineError(NAMESPACEERR, "Machine.nameSpaceRef: Top level name space "
                    + valueToString(nameSpaceName) + " not called " + valueToString(name));
    }

    // *************************************************************************
    // * Calling Functions *
    // *************************************************************************

    public void tailEnter(int arity) {
        openFrame = currentFrame;
        setOpenFrameCodeIndex(0);
        int fun = valueStack.pop();
        int argIndex = valueStack.getTOS() - arity;
        valueStack.setTOS(openFrame + FRAMELOCAL0);
        for (int i = 0; i < arity; i++) {
            int value = valueStack.ref(argIndex++);
            pushStack(value);
        }
        valueStack.push(fun);
        enter(arity);
    }

    public void enterDynamic(int name, int arity) {
        int operator = lookupDynamic(name);
        if (operator == -1)
            error(UNBOUNDVAR, "Cannot resolve dynamic reference for " + valueToString(name) + ".");
        else
            enterOperator(operator, arity);
    }

    public void tailEnterDynamic(int name, int arity) {
        openFrame = currentFrame;
        setOpenFrameCodeIndex(0);
        int argIndex = valueStack.getTOS() - arity;
        valueStack.setTOS(openFrame + FRAMELOCAL0);
        for (int i = 0; i < arity; i++) {
            int value = valueStack.ref(argIndex++);
            pushStack(value);
        }
        enterDynamic(name, arity);
    }

    public void enter(int arity) {
        calls++;
        enterOperator(valueStack.pop(), arity);
    }

    public void enterOperator(int fun, int arity) {
        switch (tag(fun)) {
        case FUN:
            enterFun(fun, arity);
            break;
        case FOREIGNFUN:
            enterForeignFun(fun, arity);
            break;
        case OBJ:
            enterObj(fun, arity);
            break;
        case CONT:
            enterCont(fun, arity);
            break;
        case ALIENOBJ:
        case FOREIGNOBJ:
            invokeObj(fun, fun, arity);
            break;
        default:
            error(TYPE, "Trying to apply a non-applicable value.");
        }
    }

    public void enterCont(int cont, int arity) {
        if (arity == 1) {
            int value = valueStack.pop();
            copyContStack(cont);
            valueStack.push(value);
            currentFrame = value(contCurrentFrame(cont));
            openFrame = value(contOpenFrame(cont));
            popFrame();
        } else
            error(ARGCOUNT, "Continuations require exactly 1 argument.");

    }

    public void copyContStack(int cont) {
        int length = value(contLength(cont));
        for (int i = 0; i < length; i++)
            valueStack.set(i, contRef(cont, i));
        valueStack.setTOS(length);
    }

    public void enterFun(int fun, int arity) {

        // Called to enter a function. The function may be
        // traced and may have var args. If it is traced then
        // the appropriate trace function is entered instead.
        // If the function has var args then the args at the
        // top of the stack must be adjusted.

        int funArity = funArity(fun);
        int traced = funTraced(fun);

        if (funIsVarArgs(fun) == trueValue)
            arity = adjustVarArgs(fun, arity);

        if (traced != undefinedValue)
            enterTracedFun(fun, funArity, funSelf(fun), nilValue);
        else if (arity == funArity)
            enterFun(fun, arity, funSelf(fun), funSupers(fun));
        else
            error(ARGCOUNT, "Machine.enter: argument mismatch " + valueToString(codeBoxName(funCode(fun))));
    }

    public int adjustVarArgs(int fun, int arity) {

        // The function has var args. The last argument after the
        // (arity - 1)th argument is passed as the sequence of all
        // extra arguments. This method is called when fun has var
        // args. The extra args are popped from the stack, consed
        // into a sequence which is then pushed back on the stack.
        // Returns the new arity or -1 if there is an error.

        int funArity = funArity(fun);
        if (arity >= (funArity - 1)) {
            int args = nilValue;
            while (arity > (funArity - 1)) {
                args = mkCons(popStack(), args);
                arity = arity - 1;
            }
            pushStack(args);
            return funArity;
        } else
            return -1;
    }

    public void enterTracedFun(int fun, int arity, int target, int supers) {

        // On calling enter traced fun we have an open frame and have
        // discovered that the function we are about to call is traced.
        // We need to extract the arguments from the currently open
        // frame and discard it. Then call 'traceFun' to construct a
        // new frame that will call the appropriate tracing function.

        closeFrame(0);
        int args = nilValue;
        for (int i = arity - 1; i >= 0; i--)
            args = mkCons(frameLocal(i), args);
        valueStack.push(undefinedValue);
        popFrame();
        valueStack.pop();
        traceFun(fun, target, args, supers);
    }

    public void traceFun(int fun, int target, int args, int supers) {

        // Open a new frame and send the fun an invoke operation....
        // The invoke operation will deal with printing the trace information
        // then un-setting trace, representing the function invocation again
        // and re-setting the trace.

        openFrame();
        valueStack.push(fun);
        valueStack.push(mkCons(target, mkCons(args, mkCons(supers, nilValue))));
        valueStack.push(funTraced(fun));
        send(2, mkSymbol("invoke"));
    }

    public void enterFun(int fun, int arity, int self, int supers) {

        // Once we get to this point everything must be set up to
        // correctly invoke the function. All arity checks must have
        // ocurred and any var args modifications taken place.

        int globals = funGlobals(fun);
        int codeBox = funCode(fun);
        int locals = codeBoxLocals(codeBox);
        int dynamics = funDynamics(fun);
        setOpenFrameGlobals(globals);
        setOpenFrameCodeBox(codeBox);
        setOpenFrameLocals(locals + arity);
        setOpenFrameSelf(self);

        // When operations are saved and loaded, the supers are not made
        // persistent. This is mainly because the overhead of saving and
        // loading the data is not worth the benefit gained. If a loaded
        // operation is invoked, it must have at least itself as the head
        // super operation.

        setOpenFrameSuper(supers == undefinedValue ? mkCons(fun, nilValue) : supers);
        setOpenFrameDynamics(dynamics);
        closeFrame(locals);
    }

    public void enterForeignFun(int foreignFun, int arity) {
        ForeignFun fun = foreignFun(foreignFunIndex(foreignFun));
        if ((fun.arity() == -1) || (fun.arity() == arity))
            try {
                setOpenFrameLocals(arity); // Used for varArgs.
                setOpenFrameSelf(foreignFun);
                closeFrame(0);
                fun.invoke(this);
            } catch (Throwable x) {
                throw new MachineError(FOREIGNFUNERR, x.toString());
            }
        else
            throw new MachineError(ARGCOUNT, "Machine.enterForeignFun: arity mismatch " + fun.name());
    }

    public void enterObj(int obj, int arity) {

        // If we try to apply an object o(args) then this is shorthand for
        // o.invoke(o,args). The following cases improve efficiency.

        // If we enter the class Table then create a table. We expect that the
        // initialisation arguments contain the table size.

        if (obj == theClassTable) {
            closeFrame(0);
            valueStack.push(mkHashtable(value(valueStack.pop())));
            popFrame();
            return;
        }

        // If we enter the class Vector then create an array of the given size.

        if (obj == theClassVector) {
            closeFrame(0);
            valueStack.push(mkArray(value(valueStack.pop())));
            popFrame();
            return;
        }

        // If we enter the class Symbol then create a symbol. The
        // initialisation
        // arguments must be the name of the symbol.

        if (obj == theClassSymbol) {
            closeFrame(0);
            valueStack.push(mkSymbol(valueStack.pop()));
            popFrame();
            return;
        }

        // If we enter the type Float then we expect two integers as the
        // initialisation arguments. The first is the float part before the
        // decimal point and the second is that after the decimal point.

        if (obj == theTypeFloat) {
            closeFrame(0);
            int args = popArgs(2);
            int prePoint = at(args, 0);
            int postPoint = at(args, 1);
            valueStack.push(mkFloat(valueToString(prePoint) + "." + valueToString(postPoint)));
            popFrame();
            return;
        }

        // If we enter a class then we create an instance of the class
        // and then initialise it.

        int type = objType(obj);

        if (type == theClassClass || type == theClassPackage) {
            enterClass(obj, arity);
            return;
        }

        // If we reach here then we don't have a specialised handler for the
        // the application of the object so just call its 'invoke' operation
        // and let the object handle the rest.

        invokeObj(obj, obj, arity);
    }

    public void enterClass(int c, int arity) {

        // When we enter a class we are instantiating it.
        // Create a new object, add slots for each of the
        // attributes initialised to the default
        // value of the attribute and then call 'init'.

        int obj = mkObj(c);
        boolean hasDynamicInits = addSlots(obj, c);
        int constructorArgs = classConstructorArgs(c, arity);
        if (hasDynamicInits || (constructorArgs == -1)) {
            int args = popArgs(arity);
            // System.err.println("MISS " + valueToString(c));
            valueStack.push(args);
            // The argument to 'init'.
            valueStack.push(obj);
            // The target.
            send(1, hasDynamicInits ? theSymbolMachineInit : theSymbolInit);
        } else
            enterConstructor(obj, constructorArgs);
    }

    public void enterConstructor(int obj, int slotNames) {
        // System.err.println("HIT " + valueToString(type(obj)));
        closeFrame(0);
        int i = 0;
        while (slotNames != nilValue) {
            int arg = frameLocal(i++);
            int slot = consHead(slotNames);
            objSetAttValue(obj, slot, arg);
            slotNames = consTail(slotNames);
        }
        valueStack.push(obj);
        popFrame();
    }

    public int classConstructorArgs(int c, int arity) {

        // Returns the constructor arg list for the given arity
        // or -1 if none defined...

        if (this.hashTableHasKey(constructorTable, c)) {
            if (arity == 0)
                return nilValue;
            else {
                int argLists = hashTableGet(constructorTable, c);
                while (argLists != nilValue)
                    if (size(consHead(argLists)) == arity)
                        return consHead(argLists);
                    else
                        argLists = consTail(argLists);
                return -1;
            }
        } else
            return -1;
    }

    public int instantiate(int c) {

        // Creates an instance of c. All the slots defined by
        // c are initialised to the default values specified
        // by the attribute types. No initialization expressions
        // defined by attributes are honoured.

        int obj = mkObj();
        objSetType(obj, c);
        addSlots(obj, c);
        return obj;
    }

    public boolean addSlots(int obj, int c) {

        // Calculate all the attributes defined and inherited by the
        // class and add slots to the object. The initial value for
        // each slot is the default value for the type of the attribute.
        // Returns true when any attribute specifies an initial value.
        // Initial values must be set by the kernel XMF code.

        int TOS = valueStack.getTOS();
        boolean hasDynamicInits = false;
        valueStack.push(c);
        while (valueStack.getTOS() != TOS) {
            int nextClass = valueStack.pop();
            int parents = asSeq(objAttValue(nextClass, theSymbolParents));
            while (parents != nilValue) {
                int parent = consHead(parents);
                valueStack.push(parent);
                parents = consTail(parents);
            }
            int attributes = asSeq(objAttValue(nextClass, theSymbolAttributes));
            while (attributes != nilValue) {
                int attribute = consHead(attributes);
                attributes = consTail(attributes);
                int name = objAttValue(attribute, theSymbolName);
                int type = objAttValue(attribute, theSymbolType);
                int init = objAttValue(attribute, theSymbolInit);
                int value = dynamicInitValue(init);
                int defaultValue = objAttValue(type, theSymbolDefault);
                boolean dynamicInit = isFun(init) && (value == -1);
                objAddAttribute(obj, name, (value == -1) ? defaultValue : value);
                hasDynamicInits = hasDynamicInits || dynamicInit;
            }
        }
        return hasDynamicInits;
    }

    public int dynamicInitValue(int init) {
        if (isFun(init))
            return funGetStringProperty(init, "value");
        else
            return -1;
    }

    public void invokeObj(int obj, int target, int arity) {
        int args = popArgs(arity);
        valueStack.push(target);
        // The first argument to 'invoke'.
        valueStack.push(args);
        // The second argument to 'invoke'.
        valueStack.push(obj);
        // The target.
        send(2, theSymbolInvoke);
    }

    public int popArgs(int arity) {
        int args = nilValue;
        for (int i = 0; i < arity; i++)
            args = mkCons(valueStack.pop(), args);
        return args;
    }

    public void send(int arity, int message) {
        send(popStack(), arity, message);
    }

    public void sendSelf(int arity, int message) {
        send(frameSelf(), arity, message);
    }

    public void sendLocal(int arity, int message, int local) {
        send(frameLocal(local), arity, message);
    }

    public static String convert(Machine machine, int value) {
        if (machine.isString(value))
            return machine.valueToString(value);
        if (machine.isSymbol(value))
            return machine.valueToString(machine.symbolName(value));
        return "";
    }

    public void send(int target, int arity, int message) {
        // System.err.println(valueToString(symbolName(objGetName(type(target)))) + "::" +
        // valueToString(symbolName(message)) + " frame = " + currentFrame + " TOS = " + valueStack.getTOS());
        if (!sendAlien(target, arity, message)) {

            // Expects a target on the top of the stack above the arguments.
            // If the target does not have a standard message sending protocol
            // then the classifier of the target is sent a message
            // 'sendInstance/3';
            // otherwise the machine can send the message directly.
            // Calculate the list of operations headed by the most specific
            // operation with the given name and arity. If no message is
            // found then send the target a noOperationFound message. Otherwise
            // invoke the operation.

            // Determine whether or not we have a standard message protocol...

            int classifier = type(target);
            int metaType = type(classifier);

            boolean standardMessageProtocol = theClassClass != undefinedValue
                    && (metaType == theClassClass || metaType == theClassPackage || metaType == theClassDataType
                            || metaType == theTypeSeq || metaType == theTypeSet);

            // If not standard then send a message to the class to deliver
            // the message.

            if (!standardMessageProtocol && (isObj(target) && !isDefaultSendMOP(type(target))))
                sendInstance(target, message, arity);
            else {

                // Otherwise perform the standard protocol:
                // find an operation with the right name and
                // arity and call it.

                // int ops = getOperations(target, message, arity);

                // int opsList = operatorPrecedenceList(type(target));

                // If the operators for the classifier are not cached
                // then calculate them and cache them...

                int ops = newFindOperation(classifier, message, arity);

                if (ops == nilValue)
                    noOperationFound(target, arity, message);
                else {
                    int op = consHead(ops);
                    if (isFun(op)) {
                        if (funIsVarArgs(op) == trueValue)
                            adjustVarArgs(op, arity);
                        if (funTraced(op) != undefinedValue)
                            enterTracedFun(op, funArity(op), target, ops);
                        else
                            enterFun(op, arity, target, ops);
                    } else
                        invokeObj(op, target, arity);
                }
            }
        }
    }

    public int newFindOperation(int classifier, int message, int arity) {
        int opsList = newGetOpsList(classifier, message, arity);
        if (opsList == -1)
            return newCacheOpsList(classifier, message, arity);
        else
            return opsList;
    }

    public int newGetOpsList(int classifier, int message, int arity) {
        int opsTable = newGetOpsTable(classifier);
        int opsList = hashTableGet(opsTable, message);
        if (opsList == -1)
            return -1;
        else {
            int op = consHead(opsList);
            return value(funArity(op)) == arity ? opsList : -1;
        }
    }

    public int newGetOpsTable(int classifier) {
        int opsTable = hashTableGet(operatorTable, classifier);
        if (opsTable == -1) {
            opsTable = mkHashtable(20);
            hashTablePut(operatorTable, classifier, opsTable);
        }
        return opsTable;
    }

    public int newCacheOpsList(int classifier, int message, int arity) {
        int opsTable = newGetOpsTable(classifier);
        int allOps = hashTableGet(opsTable, classifier);
        if (allOps == -1) {
            int TOS = valueStack.getTOS();
            int classifierTOS = pushClassifiers(classifier);
            allOps = constructOperators(classifierTOS, TOS);
            while (valueStack.getTOS() != TOS)
                valueStack.pop();
            hashTablePut(opsTable, classifier, allOps);
        }
        int opsList = findOperation(allOps, message, arity);
        hashTablePut(opsTable, message, opsList);
        return opsList;
    }

    // */ // End of double caching

    public boolean sendAlien(int target, int arity, int message) {
        if (isAlienObj(target)) {
            Object obj = AlienObj(AlienObjIndex(target));
            String mess = "<undefined>";
            if (isString(message))
                mess = valueToString(message);
            if (isSymbol(message))
                mess = valueToString(symbolName(message));

            if (AlienObject.canSend(this, obj, mess, arity) != -1) {
                int args = popArgs(arity);
                int value = AlienObject.send(this, obj, mess, args);
                closeFrame(0);
                pushStack(value);
                popFrame();
                return true;
            }
        }
        return false;
    }

    public boolean standardMessageProtocol(int element) {

        // An element has a standard message protocol when it is an
        // instance of a classifier that is an instance of one of the
        // machine classes. This is likely to be the case 99% of the
        // time. If the user has defined a sub-class of one of the
        // key meta-classes then they may have shadowed the XMF MOP
        // therefore we need to send the class of the element a message
        // that delivers the message.

        int metaType = type(type(element));
        return theClassClass != undefinedValue
                && (metaType == theClassClass || metaType == theClassPackage || metaType == theClassDataType
                        || metaType == theTypeSeq || metaType == theTypeSet);
    }

    public void sendInstance(int target, int message, int arity) {

        // The target is an instance of a classifier that is itself
        // NOT an instance of one of the key machine meta-classes.
        // This means that the class of the target *MAY* have redefined
        // the operation that delivers messages. Reconstruct the
        // stack frame to send the message to the class of the target.

        closeFrame(0);
        int args = nilValue;
        for (int i = arity - 1; i >= 0; i--)
            args = mkCons(frameLocal(i), args);
        valueStack.push(undefinedValue);
        popFrame();
        valueStack.pop();
        openFrame();
        valueStack.push(target);
        valueStack.push(message);
        valueStack.push(args);
        valueStack.push(type(target));
        send(3, mkSymbol("sendInstance"));
    }

    public void send0(int message) {

        // Expects a target at the top of the stack. There are no arguments
        // and no stack frame is currently open. opens stack frame and
        // calls operation.

        int target = popStack();
        openFrame();
        int ops = getOperations(target, message, 0);
        if (ops == nilValue)
            noOperationFound(target, 0, message);
        else {
            int op = consHead(ops);
            if (isFun(op)) {
                if (funIsVarArgs(op) == trueValue)
                    adjustVarArgs(op, 0);
                if (funTraced(op) != undefinedValue)
                    enterTracedFun(op, 0, target, ops);
                else
                    enterFun(op, 0, target, ops);
            } else
                invokeObj(op, target, 0);
        }
    }

    public void tailSend(int arity, int message) {
        int target = popStack();
        openFrame = currentFrame;
        setOpenFrameCodeIndex(0);
        int argIndex = valueStack.getTOS() - arity;
        valueStack.setTOS(openFrame + FRAMELOCAL0);
        for (int i = 0; i < arity; i++) {
            int value = valueStack.ref(argIndex++);
            pushStack(value);
        }
        valueStack.push(target);
        send(arity, message);
    }

    public void tailSend0(int message) {
        tailSend(0, message);
    }

    public void sendSuper(int arity) {
        int target = frameSelf();
        int ops = frameSuper();
        int op = consHead(ops);
        int message = funName(op);
        ops = findOperation(consTail(ops), message, arity);
        if (ops == nilValue)
            noOperationFound(target, arity, message);
        else {
            op = consHead(ops);
            if (funIsVarArgs(op) == trueValue)
                adjustVarArgs(op, arity);
            if (funTraced(op) != undefinedValue)
                enterTracedFun(op, arity, target, ops);
            else
                enterFun(op, arity, target, ops);
        }
    }

    public void tailSuper(int arity) {
        openFrame = currentFrame;
        setOpenFrameCodeIndex(0);
        int argIndex = valueStack.getTOS() - arity;
        valueStack.setTOS(openFrame + FRAMELOCAL0);
        for (int i = 0; i < arity; i++) {
            int value = valueStack.ref(argIndex++);
            pushStack(value);
        }
        sendSuper(arity);
    }

    public void noOperationFound(int target, int arity, int message) {
        int args = nilValue;
        for (int i = 0; i < arity; i++)
            args = mkCons(valueStack.pop(), args);
        valueStack.push(message);
        valueStack.push(args);
        valueStack.push(target);
        send(2, mkSymbol("noOperationFound"));
    }

    public int getOperations(int target, int message, int arity) {
        // Get the operator precedence list for the target. Discard the
        // operations at the head of the list that do not have the correct
        // name and arity. Return the resulting list.
        int ops = operatorPrecedenceList(type(target));
        return findOperation(ops, message, arity);
    }

    public int findOperation(int ops, int message, int arity) {
        boolean found = false;
        while (!found && ops != nilValue) {
            int op = consHead(ops);
            int funArity = funArity(op);
            boolean isVarArgs = funIsVarArgs(op) == trueValue;
            boolean arityMatch = (arity == funArity) || (isVarArgs && (arity >= (funArity - 1)));

            switch (tag(op)) {
            case FUN:
                if (funName(op) == message && arityMatch)
                    found = true;
                else
                    ops = consTail(ops);
                break;
            case OBJ:
                if (objGetName(op) == message)
                    found = true;
                else
                    ops = consTail(ops);
                break;
            default:
                ops = consTail(ops);
            }
        }
        return ops;
    }

    public int operatorPrecedenceList(int classifier) {
        // Calculate the sequence of operators defined for the given
        // classifier.
        // To do this we traverse the inheritance lattice depth first, left to
        // right up to a join. This means that we do not add operators twice
        // into the precedence list; such operators occur in the list at the
        // last possible position.
        // In addition we try to cons a little as possible when constructing
        // this list and cache it once it has been constructed. When new
        // operators are added, the cache is reset.
        int opsTable = newGetOpsTable(classifier);
        int operators = hashTableGet(opsTable, classifier);
        if (operators == -1) {
            operators = calculateOperatorPrecedenceList(classifier);
            hashTablePut(opsTable, classifier, operators);
        }
        return operators;
    }

    public int calculateOperatorPrecedenceList(int classifier) {
        int TOS = valueStack.getTOS();
        int classifierTOS = pushClassifiers(classifier);
        int operators = constructOperators(classifierTOS, TOS);
        while (valueStack.getTOS() != TOS)
            valueStack.pop();
        return operators;
    }

    public int pushClassifiers(int classifier) {
        valueStack.push(classifier);
        int parents = setElements(objAttValue(classifier, theSymbolParents));
        while (parents != nilValue) {
            int parent = consHead(parents);
            pushClassifiers(parent);
            parents = consTail(parents);
        }
        return valueStack.getTOS();
    }

    public int constructOperators(int leastSpecificClassifier, int mostSpecificClassifier) {
        for (int i = mostSpecificClassifier; i < leastSpecificClassifier; i++) {
            int classifier = valueStack.ref(i);
            if (!classifierInRange(classifier, i + 1, leastSpecificClassifier))
                pushOperators(classifier);
        }
        return popOperators(leastSpecificClassifier);
    }

    public boolean classifierInRange(int classifier, int start, int end) {
        for (int i = start; i < end; i++)
            if (valueStack.ref(i) == classifier)
                return true;
        return false;
    }

    public void pushOperators(int classifier) {
        int operators = setElements(objAttValue(classifier, theSymbolOperations));
        while (operators != nilValue) {
            int operator = consHead(operators);
            valueStack.push(operator);
            operators = consTail(operators);
        }
    }

    public int popOperators(int lastOperator) {
        int operations = nilValue;
        while (valueStack.getTOS() != lastOperator)
            operations = mkCons(valueStack.pop(), operations);
        return operations;
    }

    public void tryIt(int freeVars, int codeBox) {
        // The top of the stack is a handler (a function expecting 1 arg).
        // Below that is a collection of values that are freely referenced
        // in the body of the code box. Call the code box as though it
        // were a 0-arity function. The handler is added to the stack
        // frame for the call of the code box so that if it, or any subsequent
        // stack frame, throws a value then the handler is used (or a
        // subsequently established handler). Note that when the stack frame
        // exits normally the handler is popped and therefore no longer
        int handler = valueStack.pop();
        int globals = linkGlobals(freeVars);
        int locals = codeBoxLocals(codeBox);
        openFrame();
        setOpenFrameGlobals(globals);
        setOpenFrameCodeBox(codeBox);
        setOpenFrameLocals(locals);
        setOpenFrameSelf(frameSelf());
        setOpenFrameSuper(frameSuper());
        setOpenFrameDynamics(frameDynamics());
        setOpenFrameHandler(handler);
        closeFrame(locals);
    }

    public void throwIt() {

        // Find the most recently established handler for a catch and
        // invoke the handler. Make sure that we don't invoke it again
        // by setting it to 'null'. The handler must be a function that
        // expects 1 argument which is currently on the stack.

        while (frameHandler() == undefinedValue && currentFrame != -1)
            popFrame();
        int value = valueStack.top();
        if (frameHandler() == undefinedValue && currentFrame == -1) {
            System.out.println("Value thrown, no handler found.");
            System.out.println("Value = " + valueToString(value));
            System.out.println(valueToString(objAttValue(value, mkSymbol("message"))));
            System.exit(-1);
        } else if (currentFrame == -1)
            throwItRestart(value);
        else
            throwItCreateFrame(value);
    }

    public void throwItCreateFrame(int value) {

        // Create a new frame from the point that the frame handler
        // was defined. Pop the frame to lose the definition of the
        // try expression.

        int handler = frameHandler();
        popFrame();
        setFrameHandler(undefinedValue);
        openFrame();
        setOpenFrameGlobals(nilValue);
        setOpenFrameCodeBox(funCode(handler));
        setOpenFrameLocals(0);
        setOpenFrameSelf(undefinedValue);
        setOpenFrameSuper(nilValue);
        setOpenFrameDynamics(frameDynamics());
        setOpenFrameHandler(undefinedValue);
        valueStack.push(value);
        valueStack.push(handler);
        enter(1);
    }

    public void throwItRestart(int value) {

        // We have reached the end of the road on the stack.
        // Recreate a stack frame and try to start from there.

        int handler = frameHandler();
        if (isFun(handler)) {
            valueStack.reset();
            initStack(valueStack, funCode(handler));
            for (int i = 0; i < value(codeBoxLocals(funCode(handler))); i++)
                valueStack.pop();
            valueStack.push(value);
            valueStack.push(handler);
        } else {
            System.err.println("Tried to restart, no handler!");
            saveBacktrace();
        }
    }

    // *************************************************************************
    // * Calling Functions End *
    // *************************************************************************

    public void setSlot(int name) {

        // Various things can have their state set using ':='.
        // Dispatch to an appropriate handler.

        int obj = valueStack.pop();
        int value = valueStack.pop();
        setSlot(obj, name, value);
    }

    public void setSlot(int obj, int name, int value) {

        switch (tag(obj)) {

        case ALIENOBJ:
        case OBJ:

            // If the object has a standard slot access protocol then just
            // update the slot and fire any daemons. Otherwise there may be
            // a specialized slot update protocol defined via 'setInstanceSlot'
            // in the class of the object.

            if (!setAlienObjSlot(obj, name, value))
                if (standardSlotAccessProtocol(obj) || isDefaultSetMOP(type(obj)))
                    setObjSlot(obj, name, value);
                else
                    sendSlotUpdate(obj, name, value);
            break;

        case FUN:
            setFunSlot(obj, name, value);
            break;

        case DAEMON:
            setDaemonSlot(obj, name, value);
            break;

        default:
            throw new MachineError(TYPE, "Machine.setSlot: Don't know how to set the " + valueToString(name)
                    + " slot of " + valueToString(obj) + " to " + valueToString(value));
        }
    }

    public void setObjSlot(int obj, int name, int value) {

        // Setting the slot of an object should update the object and
        // then fire all the daemons registered with the object. To fire
        // the daemons on an object, sendthe object a message
        // 'fire(name,oldValue,newValue)'.

        if ((objDaemonsActive(obj) == trueValue) && (objDaemons(obj) != nilValue)) {
            int oldValue = objAttValue(obj, name);
            if (objSetAttValue(obj, name, value) == -1)
                sendSlotMissing(obj, name, value);
            openFrame();
            valueStack.push(name);
            valueStack.push(value);
            valueStack.push(oldValue);
            valueStack.push(obj);
            send(3, theSymbolFire);
        } else {
            if (objSetAttValue(obj, name, value) == -1)
                sendSlotMissing(obj, name, value);
            valueStack.push(obj);
        }
    }

    public void sendSlotMissing(int obj, int name, int value) {
        openFrame();
        valueStack.push(name);
        valueStack.push(value);
        valueStack.push(obj);
        send(2, mkSymbol("slotMissing"));
    }

    public void sendSlotUpdate(int obj, int name, int value) {

        // The object is an instance of a class whose meta-class is not
        // one of the standard kernel meta-classes. Call the appropriate
        // MOP operation to update the slot.

        openFrame();
        valueStack.push(obj);
        valueStack.push(name);
        valueStack.push(value);
        valueStack.push(type(obj));
        send(3, mkSymbol("setInstanceSlot"));
    }

    public void setDaemonSlot(int daemon, int name, int value) {
        openFrame();
        valueStack.push(name);
        valueStack.push(value);
        valueStack.push(daemon);
        send(2, mkSymbol("set"));
    }

    public boolean setAlienObjSlot(int obj, int name, int value) {
        if (isAlienObj(obj)) {
            Object target = AlienObj(AlienObjIndex(obj));
            String slot = "<undefined>";
            if (isString(name))
                slot = valueToString(name);
            if (isSymbol(name))
                slot = valueToString(symbolName(name));
            int att = AlienObject.hasSlot(target, slot);
            if (att != -1) {
                AlienObject.setSlot(this, target, slot, value);
                return true;
            }
        }
        return false;
    }

    public void setFunSlot(int fun, int name, int value) {
        if (name == theSymbolOwner) {
            funSetOwner(fun, value);
            valueStack.push(fun);
            return;
        }
        if (name == theSymbolDocumentation) {
            funSetDocumentation(fun, value);
            valueStack.push(fun);
            return;
        }
        openFrame();
        valueStack.push(name);
        valueStack.push(value);
        valueStack.push(fun);
        send(2, mkSymbol("set"));
    }

    public void setGlob(int frame, int index) {
        int value = valueStack.top();
        setFrameGlobal(frame, index, value);
    }

    public void mkFixedArray(int length) {
        int array = mkArray(length);
        for (int i = (length - 1); i >= 0; i--)
            arraySet(array, i, valueStack.pop());
        valueStack.push(array);
    }

    // *************************************************************************
    // * Machine Instructions End *
    // *************************************************************************
    // *************************************************************************
    // * Garbage Collection *
    // *************************************************************************
    // Garbage collection is performed by following all values referenced from
    // the
    // machine stack and other top-level machine elements. The values are copied
    // from one heap structure to another. If a value is not reachable then it
    // is not copied.

    public void gc() {

        // Perform garbage collection. Can be called anywhere providing all
        // collectable values are accessible from the top-level machine
        // structures.

        long startTime = System.currentTimeMillis();

        try {
            clearGCHeap();
            gcResetStats();
            gcSymbols();
            gcSpecials();
            gcStack();
            gcConstants();
            gcUndo();
            gcComplete();
            swapHeap();
            gcPopStack();
            gcDiagnostics(startTime);
        } catch (Throwable t) {
        	System.err.println(t.toString());
            error(GCERROR, t.getMessage());
        }
    }

    public void gcResetStats() {
        // Reset the data structure counts at the beginning of a garbage
        // collect.
        memory.setTotalUsed(freePtr);
        memory = memory.newRecord();
        gcTOS = valueStack.getTOS();
    }

    public void clearGCHeap() {

        // The garbage collector copies into the
        // gcWords heap. This must be
        // cleared prior to copy.

        for (int i = 0; i < gcWords.length; i++)
            gcWords[i] = undefinedValue;
        gcFreePtr = 0;
        gcCopiedPtr = 0;
    }

    public void printGCHeap() {
        for (int i = 0; i < gcFreePtr; i++)
            System.out.println("[" + i + "] " + tag(gcWords[i]) + " " + value(gcWords[i]));
        System.out.println("gcFreePtr = " + gcFreePtr);
        System.out.println("gcCopiedPtr = " + gcCopiedPtr);
    }

    public void gcSpecials() {
        // Garbage collect various values that are known by the
        // machine.
        int array0 = emptyArray;
        int set0 = emptySet;
        emptyArray = undefinedValue;
        emptySet = undefinedValue;
        emptyArray = gcCopy(array0);
        emptySet = gcCopy(set0);
        globalDynamics = gcCopy(globalDynamics);
    }

    public void gcUndo() {
        undo.gc(this);
    }

    public void gcStack() {
        // Most accessible structures are found by following values on the
        // stack. To save time the garbage collector does not bother with
        // stack frames it just zooms up the stack and collects all values.
        Thread t = threads.next();
        while (t != threads) {
            ValueStack stack = t.stack();
            gcStack(stack, stack.getTOS());
            t = t.next();
        }
        gcStack(valueStack, gcTOS);
    }

    public void gcStack(ValueStack stack, int TOS) {
        for (int i = 0; i < TOS; i++) {
            int value = stack.ref(i);
            // Really all values on the stack should be tagged.
            // -1 is used for the initial values of the frame registers.
            // System.out.println("gcStack: " + i + " " + tag(value) + " " +
            // value(value));
            if (value != -1)
                stack.set(i, gcCopy(value));
        }
    }

    public void gcSymbols() {
        forwardRefs = gcCopy(forwardRefs);
        symbolTable = gcCopy(symbolTable);
    }

    public void gcConstants() {
        gcSymbolConstants();
        gcTypeConstants();
        operatorTable = gcCopy(operatorTable);
        constructorTable = gcCopy(constructorTable);
        newListenersTable = gcCopy(newListenersTable);
    }

    public void gcSymbolConstants() {
        theSymbolAttributes = gcSymbol(theSymbolAttributes);
        theSymbolInit = gcSymbol(theSymbolInit);
        theSymbolMachineInit = gcSymbol(theSymbolMachineInit);
        theSymbolType = gcSymbol(theSymbolType);
        theSymbolDefault = gcSymbol(theSymbolDefault);
        theSymbolOperations = gcSymbol(theSymbolOperations);
        theSymbolParents = gcSymbol(theSymbolParents);
        theSymbolName = gcSymbol(theSymbolName);
        theSymbolArity = gcSymbol(theSymbolArity);
        theSymbolOwner = gcSymbol(theSymbolOwner);
        theSymbolContents = gcSymbol(theSymbolContents);
        theSymbolInvoke = gcSymbol(theSymbolInvoke);
        theSymbolDot = gcSymbol(theSymbolDot);
        theSymbolFire = gcSymbol(theSymbolFire);
        theSymbolValue = gcSymbol(theSymbolValue);
        theSymbolDocumentation = gcSymbol(theSymbolDocumentation);
        theSymbolHead = gcSymbol(theSymbolHead);
        theSymbolTail = gcSymbol(theSymbolTail);
        theSymbolIsEmpty = gcSymbol(theSymbolIsEmpty);
        theSymbolNewListener = gcSymbol(theSymbolNewListener);
    }

    public void gcTypeConstants() {
        theTypeBoolean = gcCopy(theTypeBoolean);
        theClassCompiledOperation = gcCopy(theClassCompiledOperation);
        theClassElement = gcCopy(theClassElement);
        theClassForeignObject = gcCopy(theClassForeignObject);
        theClassForeignOperation = gcCopy(theClassForeignOperation);
        theClassForwardRef = gcCopy(theClassForwardRef);
        theClassException = gcCopy(theClassException);
        theClassClass = gcCopy(theClassClass);
        theClassPackage = gcCopy(theClassPackage);
        theClassBind = gcCopy(theClassBind);
        theClassCodeBox = gcCopy(theClassCodeBox);
        theTypeInteger = gcCopy(theTypeInteger);
        theTypeFloat = gcCopy(theTypeFloat);
        theTypeString = gcCopy(theTypeString);
        theClassTable = gcCopy(theClassTable);
        theClassThread = gcCopy(theClassThread);
        theTypeSeq = gcCopy(theTypeSeq);
        theTypeSet = gcCopy(theTypeSet);
        theClassDataType = gcCopy(theClassDataType);
        theClassDaemon = gcCopy(theClassDaemon);
        theClassBuffer = gcCopy(theClassBuffer);
        theClassVector = gcCopy(theClassVector);
        theClassSymbol = gcCopy(theClassSymbol);
        theTypeNull = gcCopy(theTypeNull);
        theTypeSeqOfElement = gcCopy(theTypeSeqOfElement);
        theTypeSetOfElement = gcCopy(theTypeSetOfElement);
    }

    public void gcPopStack() {
        while (valueStack.getTOS() != gcTOS) {
            int value = valueStack.pop();
            switch (tag(value)) {
            case HASHTABLE:
                rehash(value);
                break;
            }
        }
    }

    public void gcDiagnostics(long startTime) {
        // Print out some information stating how much memory was freed up.
        int freedup = gcFreePtr - freePtr;
        long endTime = System.currentTimeMillis();
        long time = endTime - startTime;
        int percentFreed = (int) (((float) freedup / (float) gcFreePtr) * 100);
        int availableMB = (heapSize - freePtr) * 4 / (1024 * 1024);
        int usedMB = freePtr * 4 / (1024 * 1024);
        System.out.print("[GC");
        // System.out.println(" before = " + gcFreePtr + " words,");
        // System.out.print(" after = " + freePtr + " words,");
        System.out.print(" " + percentFreed + "% collected in " + time + " ms,");
        System.out.print(" " + usedMB + "MB used,");
        System.out.println(" " + availableMB + "MB available.]");

    }

    public void swapHeap() {
        // Swap between the two heaps. All allocation and memory reference
        // occurs with respect to the 'words' array and 'freePtr'.
        int[] tempWords = words;
        int tempFreePtr = freePtr;
        words = gcWords;
        freePtr = gcFreePtr;
        gcWords = tempWords;
        gcFreePtr = tempFreePtr;
    }

    public boolean collected(int word) {
        // A data structure has been collected when its header word is a
        // forward
        // pointer into the gcHeap.
        return tag(ref(ptr(word))) == FORWARD;
    }

    public int forward(int tag, int word) {
        // Tag the pointer part of the forward pointer in the header word.
        return mkPtr(tag, ptr(ref(ptr(word))));
    }

    public void setForward(int source, int destination) {

        // The source structure has been copied into the destination. Set the
        // header word to be a forward pointer. Assume that the heaps are
        // correct.

        set(ptr(source), mkPtr(FORWARD, ptr(destination)));
    }

    public int gcCopy(int word) {

        // Copy a data structure into the new heap returning a machine word
        // with respect to the new heap.
        // System.out.println("gcCopy: " + valueToString(word));

        switch (tag(word)) {
        case ARRAY:
            return gcArray(word);
        case BUFFER:
            return gcBuffer(word);
        case BOOL:
            return word;
        case CODEBOX:
            return gcCodeBox(word);
        case CODE:
            return gcCode(word);
        case CONT:
            return gcCont(word);
        case INT:
        case NEGINT:
            return word;
        case ALIENOBJ:
            return gcAlienObj(word);
        case FOREIGNFUN:
            return word;
        case FOREIGNOBJ:
            return word;
        case FUN:
            return gcFun(word);
        case OBJ:
            return gcObj(word);
        case STRING:
            return gcString(word);
        case UNDEFINED:
            return word;
        case CONS:
            return gcCons(word);
        case SET:
            return gcSet(word);
        case NIL:
            return word;
        case SYMBOL:
            return gcSymbol(word);
        case INPUT_CHANNEL:
        case OUTPUT_CHANNEL:
        case CLIENT:
        case THREAD:
            return word;
        case HASHTABLE:
            return gcHashTable(word);
        case FLOAT:
            return gcFloat(word);
        case DAEMON:
            return gcDaemon(word);
        case FORWARDREF:
            return gcForwardRef(word);
        default:
            throw new MachineError(GCERROR, "Machine.gcCopy: unknown type tag " + tag(word));
        }
    }

    public int gcArray(int array) {
        if (collected(array))
            return forward(ARRAY, array);
        else {
            int length = arrayLength(array);
            int daemonsActive = arrayDaemonsActive(array);
            int daemons = arrayDaemons(array);
            swapHeap();
            int newArray = mkArray(length);
            swapHeap();
            for (int i = 0; i < length; i++) {
                int element = arrayRef(array, i);
                swapHeap();
                arraySet(newArray, i, element);
                swapHeap();
            }
            swapHeap();
            arraySetDaemonsActive(newArray, daemonsActive);
            arraySetDaemons(newArray, daemons);
            swapHeap();
            setForward(array, newArray);
            return newArray;
        }
    }

    public int gcBuffer(int buffer) {
        if (collected(buffer))
            return forward(BUFFER, buffer);
        else {
            int increment = bufferIncrement(buffer);
            int daemons = bufferDaemons(buffer);
            int daemonsActive = bufferDaemonsActive(buffer);
            int storage = bufferStorage(buffer);
            int size = bufferSize(buffer);
            int asString = bufferAsString(buffer);
            swapHeap();
            int newBuffer = mkBuffer();
            bufferSetIncrement(newBuffer, increment);
            bufferSetDaemons(newBuffer, daemons);
            bufferSetDaemonsActive(newBuffer, daemonsActive);
            bufferSetStorage(newBuffer, storage);
            bufferSetSize(newBuffer, size);
            bufferSetAsString(newBuffer, asString);
            swapHeap();
            setForward(buffer, newBuffer);
            return newBuffer;
        }
    }

    public int gcCodeBox(int codeBox) {
        if (collected(codeBox))
            return forward(CODEBOX, codeBox);
        else {
            int locals = codeBoxLocals(codeBox);
            swapHeap();
            int newCodeBox = mkCodeBox(locals);
            swapHeap();
            int constants = codeBoxConstants(codeBox);
            int instrs = codeBoxInstrs(codeBox);
            int name = codeBoxName(codeBox);
            int source = codeBoxSource(codeBox);
            int resourceName = codeBoxResourceName(codeBox);
            swapHeap();
            codeBoxSetConstants(newCodeBox, constants);
            codeBoxSetInstrs(newCodeBox, instrs);
            codeBoxSetName(newCodeBox, name);
            codeBoxSetSource(newCodeBox, source);
            codeBoxSetResourceName(newCodeBox, resourceName);
            swapHeap();
            setForward(codeBox, newCodeBox);
            return newCodeBox;
        }
    }

    public int gcCode(int code) {
        if (collected(code))
            return forward(CODE, code);
        else {
            int length = codeLength(code);
            swapHeap();
            int newCode = mkCode(length);
            swapHeap();
            for (int i = 0; i < length; i++) {
                int instr = codeRef(code, i);
                swapHeap();
                codeSet(newCode, i, instr);
                swapHeap();
            }
            setForward(code, newCode);
            return newCode;
        }
    }

    public int gcCons(int cons) {
        if (collected(cons))
            return forward(CONS, cons);
        else {
            int head = consHead(cons);
            int tail = consTail(cons);
            swapHeap();
            int newCons = mkCons(head, tail);
            swapHeap();
            setForward(cons, newCons);
            return newCons;
        }
    }

    public int gcSet(int set) {
        if (collected(set))
            return forward(SET, set);
        else {
            int elements = setElements(set);
            swapHeap();
            int newSet = mkSet(elements);
            swapHeap();
            setForward(set, newSet);
            return newSet;
        }
    }

    public int gcCont(int cont) {
        if (collected(cont))
            return forward(CONT, cont);
        else {
            int length = contLength(cont);
            int currFrame = contCurrentFrame(cont);
            int openFrame = contOpenFrame(cont);
            swapHeap();
            int newCont = mkCont(length);
            contSetCurrentFrame(newCont, mkInt(currFrame));
            contSetOpenFrame(newCont, mkInt(openFrame));
            swapHeap();
            for (int i = 0; i < length; i++) {
                int value = contRef(cont, i);
                swapHeap();
                contSet(newCont, i, value);
                swapHeap();
            }
            setForward(cont, newCont);
            return newCont;
        }

    }

    public int gcAlienObj(int obj) {
        if (collected(obj))
            return forward(ALIENOBJ, obj);
        else {
            Object object = AlienObj(AlienObjIndex(obj));
            swapHeap();
            int newObj = newAlienObj(object);
            swapHeap();
            int type = objType(obj);
            swapHeap();
            objSetType(newObj, type);
            swapHeap();
            setForward(obj, newObj);
            return newObj;
        }
    }

    public int gcForwardRef(int forwardRef) {

        // If we can resolve the forward ref then
        // do so silently....

        if (collected(forwardRef))
            return forward(FORWARDREF, forwardRef);
        else {
            int value = forwardRefValue(forwardRef);
            if (value != undefinedValue)
                return gcCopy(value);
            else {
                int path = forwardRefPath(forwardRef);
                int listeners = forwardRefListeners(forwardRef);
                System.err.println("Create forward ref");
                swapHeap();
                int newForwardRef = mkForwardRef(path);
                forwardRefSetListeners(newForwardRef, listeners);
                swapHeap();
                setForward(forwardRef, newForwardRef);
                return newForwardRef;
            }
        }
    }

    public int gcFun(int fun) {
        if (collected(fun))
            return forward(FUN, fun);
        else {
            int arity = funArity(fun);
            swapHeap();
            int newFun = mkFun();
            funSetArity(newFun, mkInt(arity));
            swapHeap();
            int globals = funGlobals(fun);
            int code = funCode(fun);
            int self = funSelf(fun);
            int owner = funOwner(fun);
            int supers = funSupers(fun);
            int dynamics = funDynamics(fun);
            int sig = funSig(fun);
            int properties = funProperties(fun);
            swapHeap();
            funSetGlobals(newFun, globals);
            funSetCode(newFun, code);
            funSetSelf(newFun, self);
            funSetOwner(newFun, owner);
            funSetSupers(newFun, supers);
            funSetDynamics(newFun, dynamics);
            funSetSig(newFun, sig);
            funSetProperties(newFun, properties);
            swapHeap();
            setForward(fun, newFun);
            return newFun;
        }
    }

    public int gcObj(int obj) {
        if (collected(obj))
            return forward(OBJ, obj);
        else {
            swapHeap();
            int newObj = mkObj();
            swapHeap();
            int atts = objAttributes(obj);
            int type = objType(obj);
            int daemons = objDaemons(obj);
            int properties = objProperties(obj);
            swapHeap();
            objSetAttributes(newObj, atts);
            objSetType(newObj, type);
            objSetDaemons(newObj, daemons);
            objSetProperties(newObj, properties);
            swapHeap();
            setForward(obj, newObj);
            return newObj;
        }
    }

    public int gcString(int string) {
        if (collected(string))
            return forward(STRING, string);
        else {
            int length = stringLength(string);
            swapHeap();
            int newString = mkString(length);
            swapHeap();
            for (int i = 0; i < length; i++) {
                int c = stringRef(string, i);
                swapHeap();
                stringSet(newString, i, c);
                swapHeap();
            }
            setForward(string, newString);
            return newString;
        }
    }

    public int gcSymbol(int symbol) {
        if (collected(symbol))
            return forward(SYMBOL, symbol);
        else {
            int name = symbolName(symbol);
            int value = symbolValue(symbol);
            swapHeap();
            int newSymbol = mkSymbol();
            symbolSetName(newSymbol, name);
            symbolSetValue(newSymbol, value);
            swapHeap();
            setForward(symbol, newSymbol);
            return newSymbol;
        }
    }

    public int gcHashTable(int table) {

        // To garbage collect a hashtable we must take into
        // account the hash codes of the keys in the table.
        // Garbage collection may cause the hash codes to change
        // since the codes may be computed on the basis of the
        // machine address of the key. It is therefore important
        // to rehash the table into the new space.

        if (collected(table))
            return forward(HASHTABLE, table);
        else {
            int length = arrayLength(table);
            int daemonsActive = arrayDaemonsActive(table);
            int daemons = arrayDaemons(table);
            swapHeap();
            int newTable = mkHashtable(length);
            swapHeap();
            for (int i = 0; i < length; i++) {
                int element = arrayRef(table, i);
                swapHeap();
                arraySet(newTable, i, element);
                swapHeap();
            }
            swapHeap();
            arraySetDaemonsActive(newTable, daemonsActive);
            arraySetDaemons(newTable, daemons);
            swapHeap();
            setForward(table, newTable);
            valueStack.push(newTable);
            return newTable;
        }
    }

    public int gcFloat(int f) {
        if (collected(f))
            return forward(FLOAT, f);
        else {
            int str = floatString(f);
            swapHeap();
            int newFloat = mkFloat();
            floatSetString(newFloat, str);
            swapHeap();
            setForward(f, newFloat);
            return newFloat;
        }
    }

    public int gcDaemon(int daemon) {
        if (collected(daemon))
            return forward(DAEMON, daemon);
        else {
            int id = daemonId(daemon);
            int type = daemonType(daemon);
            int slot = daemonSlot(daemon);
            int action = daemonAction(daemon);
            int persistent = daemonPersistent(daemon);
            int traced = daemonTraced(daemon);
            int target = daemonTarget(daemon);
            swapHeap();
            int newDaemon = mkDaemon();
            daemonSetId(newDaemon, id);
            daemonSetType(newDaemon, type);
            daemonSetSlot(newDaemon, slot);
            daemonSetAction(newDaemon, action);
            daemonSetPersistent(newDaemon, persistent);
            daemonSetTraced(newDaemon, traced);
            daemonSetTarget(newDaemon, target);
            swapHeap();
            setForward(daemon, newDaemon);
            return newDaemon;
        }
    }

    public void gcComplete() {
        while (gcFreePtr != gcCopiedPtr) {
            int value = gcWords[gcCopiedPtr];
            switch (tag(value)) {
            case ARRAY:
                gcWords[gcCopiedPtr++] = gcArray(value);
                break;
            case BUFFER:
                gcWords[gcCopiedPtr++] = gcBuffer(value);
                break;
            case BOOL:
                gcCopiedPtr++;
                break;
            case CODE:
                gcWords[gcCopiedPtr++] = gcCode(value);
                break;
            case CODEBOX:
                gcWords[gcCopiedPtr++] = gcCodeBox(value);
                break;
            case CODELENGTH:
                gcCopiedPtr = gcCopiedPtr + value(value) + 1;
                break;
            case CONT:
                gcWords[gcCopiedPtr++] = gcCont(value);
                break;
            case ALIENOBJ:
                gcWords[gcCopiedPtr++] = gcAlienObj(value);
                break;
            case FOREIGNFUN:
                gcCopiedPtr++;
                break;
            case FOREIGNOBJ:
                gcCopiedPtr++;
                break;
            case FUN:
                gcWords[gcCopiedPtr++] = gcFun(value);
                break;
            case INT:
            case NEGINT:
                gcCopiedPtr++;
                break;
            case OBJ:
                gcWords[gcCopiedPtr++] = gcObj(value);
                break;
            case STRING:
                gcWords[gcCopiedPtr++] = gcString(value);
                break;
            case STRINGLENGTH:
                gcCopiedPtr = gcCopiedPtr + (value(value) / 4) + 1;
                break;
            case UNDEFINED:
                gcCopiedPtr++;
                break;
            case CONS:
                gcWords[gcCopiedPtr++] = gcCons(value);
                break;
            case NIL:
                gcCopiedPtr++;
                break;
            case SYMBOL:
                gcWords[gcCopiedPtr++] = gcSymbol(value);
                break;
            case SET:
                gcWords[gcCopiedPtr++] = gcSet(value);
                break;
            case INPUT_CHANNEL:
            case OUTPUT_CHANNEL:
            case CLIENT:
            case THREAD:
            case DATABASE:
            case QUERYRESULT:
                gcCopiedPtr++;
                break;
            case HASHTABLE:
                gcWords[gcCopiedPtr++] = gcHashTable(value);
                break;
            case FLOAT:
                gcWords[gcCopiedPtr++] = gcFloat(value);
                break;
            case DAEMON:
                gcWords[gcCopiedPtr++] = gcDaemon(value);
                break;
            case FORWARDREF:
                gcWords[gcCopiedPtr++] = gcForwardRef(value);
                break;
            case ILLEGAL:
                // Arises when a continuation is garbage collected.
                // since the continuation contains a stack copy and
                // the stack contains machine words or the special -1.
                gcCopiedPtr++;
                break;
            default:
                throw new MachineError(GCERROR, "gcComplete: unknown value tag: " + tag(value));
            }
        }
    }

    // *************************************************************************
    // * Garbage Collection End *
    // *************************************************************************

    // *************************************************************************
    // * Saving and Loading the Machine State *
    // *************************************************************************

    public void save(String fileName) {
        try {
            System.out.print("[ Save " + fileName);
            FileOutputStream fout = new FileOutputStream(fileName);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            GZIPOutputStream zout = new GZIPOutputStream(bout);
            ObjectOutputStream out = new ObjectOutputStream(zout);
            saveHeader(out);
            saveHeap(out);
            saveStack(out);
            saveTables(out);
            saveConstants(out);
            out.flush();
            out.close();
        } catch (IOException ioe) {
            throw new MachineError(SAVEERR, ioe.getMessage());
        } finally {
            System.out.println(" ]");
        }
    }

    void saveHeader(ObjectOutputStream out) {
        try {
            out.writeObject(header);
        } catch (IOException ioe) {
            throw new MachineError(SAVEERR, ioe.getMessage());
        }
    }

    void saveHeap(ObjectOutputStream out) {
        try {
            out.writeObject(words);
            out.writeInt(freePtr);
        } catch (IOException ioe) {
            throw new MachineError(SAVEERR, ioe.getMessage());
        }
    }

    void saveStack(ObjectOutputStream out) {
        try {
            out.writeObject(valueStack);
            out.writeInt(currentFrame);
            out.writeInt(openFrame);
            threads.setCurrentFrame(currentFrame);
            threads.setOpenFrame(openFrame);
            out.writeObject(threads);
        } catch (IOException ioe) {
            throw new MachineError(SAVEERR, ioe.getMessage());
        }
    }

    void saveTables(ObjectOutputStream out) {
        try {
            out.writeObject(foreignFuns);
        } catch (IOException ioe) {
            throw new MachineError(SAVEERR, ioe.getMessage());
        }
    }

    void saveConstants(ObjectOutputStream out) {
        try {
            out.writeInt(symbolTable);
            out.writeInt(emptyArray);
            out.writeInt(emptySet);
            out.writeInt(theTypeBoolean);
            out.writeInt(theClassCompiledOperation);
            out.writeInt(theTypeInteger);
            out.writeInt(theTypeFloat);
            out.writeInt(theTypeString);
            out.writeInt(theTypeNull);
            out.writeInt(theTypeSeqOfElement);
            out.writeInt(theTypeSetOfElement);
            out.writeInt(theClassElement);
            out.writeInt(theClassForeignObject);
            out.writeInt(theClassForeignOperation);
            out.writeInt(theClassForwardRef);
            out.writeInt(theClassException);
            out.writeInt(theClassClass);
            out.writeInt(theClassPackage);
            out.writeInt(theClassBind);
            out.writeInt(theClassCodeBox);
            out.writeInt(theClassTable);
            out.writeInt(theClassThread);
            out.writeInt(theTypeSeq);
            out.writeInt(theTypeSet);
            out.writeInt(theClassDataType);
            out.writeInt(theClassDaemon);
            out.writeInt(theClassBuffer);
            out.writeInt(theClassVector);
            out.writeInt(theClassSymbol);
            out.writeInt(theSymbolAttributes);
            out.writeInt(theSymbolInit);
            out.writeInt(theSymbolMachineInit);
            out.writeInt(theSymbolType);
            out.writeInt(theSymbolDefault);
            out.writeInt(theSymbolOperations);
            out.writeInt(theSymbolParents);
            out.writeInt(theSymbolName);
            out.writeInt(theSymbolArity);
            out.writeInt(theSymbolOwner);
            out.writeInt(theSymbolContents);
            out.writeInt(theSymbolInvoke);
            out.writeInt(theSymbolDot);
            out.writeInt(theSymbolFire);
            out.writeInt(theSymbolValue);
            out.writeInt(theSymbolDocumentation);
            out.writeInt(theSymbolHead);
            out.writeInt(theSymbolTail);
            out.writeInt(theSymbolIsEmpty);
            out.writeInt(theSymbolNewListener);
            out.writeInt(operatorTable);
            out.writeInt(constructorTable);
            out.writeInt(newListenersTable);
            out.writeInt(globalDynamics);
        } catch (IOException ioe) {
            throw new MachineError(SAVEERR, ioe.getMessage());
        }
    }

    public void load(String fileName) {
        try {
            if (!new File(fileName).exists()) {
                System.out.println("Image file does not exist " + fileName);
                exitAbnormal();
            }
            System.out.print("[ Load " + fileName);
            java.io.FileInputStream fin = new java.io.FileInputStream(fileName);
            GZIPInputStream zin = new GZIPInputStream(fin);
            ObjectInputStream in = new ObjectInputStream(zin);
            loadHeader(in);
            loadHeap(in);
            loadStack(in);
            loadTables(in);
            loadConstants(in);
            in.close();
        } catch (IOException ioe) {
            throw new MachineError(LOADERR, ioe.getMessage());
        } finally {
            System.out.println(" ]");
        }
    }

    void loadHeader(ObjectInputStream in) {
        try {
            header = (Header) in.readObject();
        } catch (IOException ioe) {
            throw new MachineError(LOADERR, ioe.getMessage());
        } catch (ClassNotFoundException ioe) {
            throw new MachineError(LOADERR, ioe.getMessage());
        }
    }

    int loadWord(InputStream in) {
        try {
            int i1 = in.read();
            int i2 = in.read();
            int i3 = in.read();
            int i4 = in.read();
            return (i4 << 24) | (i3 << 16) | (i2 << 8) | i1;
        } catch (IOException ioe) {
            throw new MachineError(LOADERR, ioe.getMessage());
        }
    }

    void loadHeap(ObjectInputStream in) {
        try {
            words = (int[]) in.readObject();
            freePtr = in.readInt();
            if (heapSize != words.length)
                if (heapSize < freePtr + K) {
                    heapSize = words.length;
                    gcWords = new int[heapSize];
                } else {
                    int[] newWords = new int[heapSize];
                    for (int i = 0; i < freePtr; i++)
                        newWords[i] = words[i];
                    words = newWords;
                }
        } catch (IOException ioe) {
            throw new MachineError(LOADERR, ioe.getMessage());
        } catch (ClassNotFoundException cnf) {
            throw new MachineError(LOADERR, cnf.getMessage());
        }
    }

    void loadStack(ObjectInputStream in) {
        try {
            valueStack = (ValueStack) in.readObject();
            currentFrame = in.readInt();
            openFrame = in.readInt();
            threads = (Thread) in.readObject();
            if (stackSize != valueStack.size())
                if (stackSize < valueStack.size()) {
                    System.out.println("Cannot load stack, increase requested size " + stackSize + " to at least "
                            + valueStack.size());
                    System.exit(-1);
                } else {
                    ValueStack newStack = new ValueStack(stackSize);
                    valueStack.copyInto(newStack);
                    valueStack = newStack;
                }
        } catch (IOException ioe) {
            throw new MachineError(LOADERR, ioe.getMessage());
        } catch (ClassNotFoundException cnf) {
            throw new MachineError(LOADERR, cnf.getMessage());
        }
    }

    void loadTables(ObjectInputStream in) {
        try {
            foreignFuns = (Stack) in.readObject();
        } catch (IOException ioe) {
            throw new MachineError(LOADERR, ioe.getMessage());
        } catch (ClassNotFoundException cnf) {
            throw new MachineError(LOADERR, cnf.getMessage());
        }
    }

    void loadConstants(ObjectInputStream in) {
        try {
            symbolTable = in.readInt();
            emptyArray = in.readInt();
            emptySet = in.readInt();
            theTypeBoolean = in.readInt();
            theClassCompiledOperation = in.readInt();
            theTypeInteger = in.readInt();
            theTypeFloat = in.readInt();
            theTypeString = in.readInt();
            theTypeNull = in.readInt();
            theTypeSeqOfElement = in.readInt();
            theTypeSetOfElement = in.readInt();
            theClassElement = in.readInt();
            theClassForeignObject = in.readInt();
            theClassForeignOperation = in.readInt();
            theClassForwardRef = in.readInt();
            theClassException = in.readInt();
            theClassClass = in.readInt();
            theClassPackage = in.readInt();
            theClassBind = in.readInt();
            theClassCodeBox = in.readInt();
            theClassTable = in.readInt();
            theClassThread = in.readInt();
            theTypeSeq = in.readInt();
            theTypeSet = in.readInt();
            theClassDataType = in.readInt();
            theClassDaemon = in.readInt();
            theClassBuffer = in.readInt();
            theClassVector = in.readInt();
            theClassSymbol = in.readInt();
            theSymbolAttributes = in.readInt();
            theSymbolInit = in.readInt();
            theSymbolMachineInit = in.readInt();
            theSymbolType = in.readInt();
            theSymbolDefault = in.readInt();
            theSymbolOperations = in.readInt();
            theSymbolParents = in.readInt();
            theSymbolName = in.readInt();
            theSymbolArity = in.readInt();
            theSymbolOwner = in.readInt();
            theSymbolContents = in.readInt();
            theSymbolInvoke = in.readInt();
            theSymbolDot = in.readInt();
            theSymbolFire = in.readInt();
            theSymbolValue = in.readInt();
            theSymbolDocumentation = in.readInt();
            theSymbolHead = in.readInt();
            theSymbolTail = in.readInt();
            theSymbolIsEmpty = in.readInt();
            theSymbolNewListener = in.readInt();
            operatorTable = in.readInt();
            constructorTable = in.readInt();
            newListenersTable = in.readInt();
            globalDynamics = in.readInt();
        } catch (IOException ioe) {
            throw new MachineError(LOADERR, ioe.getMessage());
        }
    }

    // *************************************************************************
    // * Saving and Loading the Machine State End *
    // *************************************************************************

    // ************************************************************************
    // * Error Handling *
    // ************************************************************************
    // When an error occurs in the machine, we throw an exception. To throw an
    // exception we create an instance of the machine exception class, set its
    // values and throw it to the most recently established exception handler.

    public int error(int id, String message) {
        int exception = mkException(message);
        objSetAttValue(exception, mkSymbol("id"), mkInt(id));
        valueStack.push(exception);
        throwIt();
        return exception;
    }

    public int mkException(String message) {
        System.err.println("\nXMF Exception: " + message);
        int exception = mkObj();
        objSetType(exception, theClassException);
        addSlots(exception, theClassException);
        objSetAttValue(exception, mkSymbol("backtrace"), stackFrames());
        objSetAttValue(exception, mkSymbol("message"), mkString(message));
        objSetAttValue(exception, mkSymbol("lineCount"), frameLineCount());
        objSetAttValue(exception, mkSymbol("charCount"), frameCharCount());
        objSetAttValue(exception, mkSymbol("resourceName"), codeBoxResourceName(frameCodeBox()));
        return exception;
    }

    public void install(String signalName) {
        Signal diagSignal = new Signal(signalName);
        Signal.handle(diagSignal, this);
    }

    public void handle(Signal sig) {
        setInterruptFlag();
    }

    public void setInterruptFlag() {
        interrupt = true;
    }

    public void interrupt() {
        error(INTERRUPT, "Execution interrupted.");
        interrupt = false;
    }

    // ************************************************************************
    // * End Error Handling *
    // ************************************************************************

    public static void main(String[] args) {
        String[] xosArgs = new String[args.length + 1];
        for (int i = 1; i < args.length + 1; i++)
            xosArgs[i] = args[i - 1];
        Machine machine = new Machine(null);
        machine.init(xosArgs);
    }

    public void init(String[] args) {

        // Initialise the machine from the command line arguments.
        // If the arguments specify an image then the heap, stack
        // and threads are initialised from the image. Otherwise
        // an empty heap and stack are created. The args may also
        // specify an initial object file to load as the starting
        // point for execution.

        args = ArgParser.parseArgs(XVMargSpecs, args);
        parseOpts(args);
        install("INT");
        init();
        loadImage();
        loadInit();
    }

    public void parseOpts(String[] args) {

        // Parse the command line options. Must be done before any
        // memory structures are initialised. The VM is created and
        // initialised from XOS. The first command line argument to
        // XOS is te port number used to connect clients. All VM
        // args start at index 1.

        int index = 0;
        while (index < args.length) {
            if (args[index].equals("-instr"))
                traceInstr = true;
            else if (args[index].equals("-frames"))
                traceFrames = true;
            else if (args[index].equals("-stats"))
                stats = true;
            else if (args[index].equals("-heapSize"))
                heapSize = Integer.parseInt(args[++index]) * K;
            else if (args[index].equals("-stackSize"))
                stackSize = Integer.parseInt(args[++index]) * K;
            else if (args[index].equals("-initFile"))
                initFile = args[++index];
            else if (args[index].equals("-freeHeap"))
                freeHeap = Integer.parseInt(args[++index]) * K;
            else if (args[index].equals("-stackDump"))
                stackDump = true;
            else if (args[index].equals("-image"))
                imageFile = args[++index];
            else if (args[index].equals("-arg"))
                parseImageArg(args[++index]);
            else {
                System.out.println("Unknown option: " + args[index]);
                displayOptions();
                exitAbnormal();
            }
            index++;
        }
    }

    public boolean checkoutLicense() {
    	return true;
    }

    public void displayOptions() {
        System.out.println("Options: ");
        System.out.println("  -heapSize <SIZE IN K UNITS>");
        System.out.println("  -stackSize <SIZE IN K UNITS>");
        System.out.println("  -freeHeap <SIZE IN K UNITS>");
        System.out.println("  -image <IMAGE FILE>");
        System.out.println("  -arg <NAME>:<VALUE>");
    }

    public void parseImageArg(String argSpec) {
        int separator = argSpec.indexOf(':');
        if (separator == -1) {
            System.out.println("An arg should take the form <KEY>:<VALUE> " + argSpec);
            System.exit(1);
        }
        String key = argSpec.substring(0, separator);
        String value = argSpec.substring(separator + 1, argSpec.length());
        imageArgs.addElement(key);
        imageArgs.addElement(value);
    }

    public String elapsedTime() {
        return elapsedTime(time);
    }

    public String elapsedTime(long time) {

        // Calculate the time duration, as a string, since the last point
        // at which the time was reset.

        long elapsedTimeMillis = System.currentTimeMillis() - time;
        int mins = (int) (elapsedTimeMillis / (60 * 1000F));
        int secs = (int) ((elapsedTimeMillis - (mins * 60 * 1000F)) / 1000F);
        int millis = (int) (elapsedTimeMillis - ((mins * 60 * 1000F) + (secs * 1000F)));
        return mins + " minutes " + secs + " seconds " + millis + " milliseconds";
    }

    public void printValueStack(PrintStream output) {
        for (int i = 0; i < valueStack.index; i++) {
            output.print(i + " : ");
            int value = valueStack.elements[i];
            if (!(value == -1))
                output.println(valueToString(value));
            else
                output.println("-1");
        }
    }

    public String valueStackAt(int index) {
        String output = index + " : ";
        int value = valueStack.elements[index];
        if (!(value == -1))
            output = output + valueToString(value);
        else
            output = output + "-1";
        return output;
    }

    public boolean stackFrameAvailable() {
        return valueStack.index != -1;
    }

    public int nonReferencedElements(int elements, int nameSpaces) {
        int newElements = mkNil();
        while (elements != nilValue) {
            int head = consHead(elements);
            if (!isReference(nameSpaces, head))
                newElements = including(newElements, head);
            elements = consTail(elements);
        }
        return newElements;
    }

    public boolean isReference(int nameSpaces, int element) {

        // A named element is a reference when it occurs in one of
        // the ref tables. It may be referenced because all entries
        // in the ref tables are assumed to persist across different
        // instantiations of XMF.

        if (!(isObj(element) && objGetName(element) != -1 && objGetOwner(element) != -1))
            return false;
        return isNamedElementReference(nameSpaces, element);
    }

    public boolean isNamedElementReference(int nameSpaces, int namedElement) {
        boolean isReference = false;
        int ns = nameSpaces;
        while (ns != Machine.nilValue && !isReference) {
            int nameSpace = consHead(ns);
            ns = consTail(ns);
            int table = objGetContents(nameSpace);
            if (table != -1 && hashTableHasValue(table, namedElement))
                isReference = true;
        }
        return isReference;
    }

    public void setConstructorArgs(int klass, int args) {
        hashTablePut(constructorTable, klass, args);
    }

}