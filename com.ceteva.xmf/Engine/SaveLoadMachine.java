package Engine;

public class SaveLoadMachine implements Value {

    // A save and load machine defines a common language and
    // caching structure for saving and rebuilding the values
    // that can be found in the heap.

    // The following two values are used to keep track ot the
    // version of XAR data files. They will be incremented as
    // new versions of XAR are introduces. When a file is saved
    // the version numbers are saved in the file. This allows
    // future versions of the loader to upgrade the saved format
    // where this is possible.

    public static final int  VERSION_MAJOR    = 1;

    public static final int  VERSION_MINOR    = 6;

    public int               loadMajorVersion;

    public int               loadMinorVersion;

    // The following are 8-bit machine instructions.

    public static final int  MKARRAY          = 1;

    public static final int  SETARRAY         = 2;

    public static final int  MKCODE           = 3;

    public static final int  MKINT            = 4;

    public static final int  MKSTRING         = 5;

    public static final int  MKCODEBOX        = 7;

    public static final int  SETCODEBOX       = 8;

    public static final int  MKTRUE           = 9;

    public static final int  MKFALSE          = 10;

    public static final int  MKFUN            = 11;

    public static final int  SETFUN           = 12;

    public static final int  MKUNDEF          = 13;

    public static final int  MKOBJ            = 14;

    public static final int  SETOBJ           = 15;

    public static final int  MKCONS           = 16;

    public static final int  SETCONS          = 17;

    public static final int  MKNIL            = 18;

    public static final int  MKSYMBOL         = 19;

    public static final int  MKSET            = 20;

    public static final int  TABLE            = 21;

    public static final int  PUT              = 22;

    public static final int  LOOKUP           = 23;

    public static final int  MKFOREIGNFUN     = 24;

    public static final int  GLOBAL_DYNAMICS  = 25;

    public static final int  MKDYNAMIC_TABLE  = 26;

    public static final int  MKNEGINT         = 27;

    public static final int  MKFLOAT          = 28;

    public static final int  REFARRAY         = 29;

    public static final int  REFSTRING        = 30;

    public static final int  REFOBJ           = 31;

    public static final int  REFFUN           = 32;

    public static final int  REFCONS          = 33;

    public static final int  REFSYMBOL        = 34;

    public static final int  REFVALUE         = 35;

    public static final int  MKCONS_NONSHARE  = 36;

    public static final int  MKARRAY_NONSHARE = 37;

    public static final int  MKNEWLISTENER    = 38;

    public static final int  HOTLOAD          = 39;

    public static final int  MKSYMBOL2        = 40;

    public static final int  MKLIST           = 41;

    public static final int  SETOBJ2          = 42;

    public static final int  MKSET2           = 43;

    public static final int  LISTCONS         = 44;

    public static final int  MKSIG            = 45;

    public static final int  MKARG            = 46;

    public static final int  MK_ELEMENT_TYPE  = 47;

    public static final int  NULLGLOBS        = 48;

    public static final int  MKGLOBALS        = 49;

    public static final int  MKDAEMON         = 50;

    public static final int  LOOKUP2          = 51;

    public static final int  MKNULLSLOT       = 52;

    public static final int  MKNILSLOT        = 53;

    public static final int  MKEMPTYSLOT      = 54;

    public static final int  MKZEROSLOT       = 55;

    public static final int  MKTRUESLOT       = 56;

    public static final int  MKFALSESLOT      = 57;

    public static final int  LOOKUPFUN        = 58;

    public static final int  SETDAEMON        = 59;

    public static final int  VERSION          = 60;

    public static final int  MKBUFFER         = 61;

    public static final int  SETBUFFER        = 62;

    public static final int  MKCLOSURE        = 63;

    public static final int  SETCLOSURE       = 64;

    public static final int  END              = 255;

    // The save and load mechanism must deal with sharing. This is
    // achieved by saving values in stacks as they are saved and loaded
    // and serializing subsequent occurrences of values as references
    // via stack offsets. The stacks are organised by value type
    // to reduce the amount of time that is spent sweeping the stack
    // in order to search for the index of a previously saved value.

    public static final int  ARRAYSTACK       = 0;

    public static final int  STRINGSTACK      = 1;

    public static final int  OBJSTACK         = 2;

    public static final int  FUNSTACK         = 3;

    public static final int  CONSSTACK        = 4;

    public static final int  SYMBOLSTACK      = 5;

    public static final int  VALUESTACK       = 6;

    // Sizes and offsets that are used to partition the integer array
    // that is used to implement the stacks. reference indices are
    // relative to the stack type: the sizes of reference stacks can be
    // increased and previously serialized values will continue to load.

    private static final int K                = 1024;

    public static final int  ARRAYSTACKSIZE   = 30 * K;

    public static final int  ARRAYSTACKBASE   = 0;

    public static final int  STRINGSTACKSIZE  = 25 * K;

    public static final int  STRINGSTACKBASE  = ARRAYSTACKBASE + ARRAYSTACKSIZE;

    public static final int  OBJSTACKSIZE     = 50 * K;

    public static final int  OBJSTACKBASE     = STRINGSTACKBASE + STRINGSTACKSIZE;

    public static final int  FUNSTACKSIZE     = 20 * K;

    public static final int  FUNSTACKBASE     = OBJSTACKBASE + OBJSTACKSIZE;

    public static final int  CONSSTACKSIZE    = 100 * K;

    public static final int  CONSSTACKBASE    = FUNSTACKBASE + FUNSTACKSIZE;

    public static final int  SYMBOLSTACKSIZE  = 5 * K;

    public static final int  SYMBOLSTACKBASE  = CONSSTACKBASE + CONSSTACKSIZE;

    public static final int  VALUESTACKSIZE   = 30 * K;

    public static final int  VALUESTACKBASE   = SYMBOLSTACKBASE + SYMBOLSTACKSIZE;

    public static final int  VALUESTACKLIMIT  = VALUESTACKBASE + VALUESTACKSIZE;

    public static final int  SIZE             = ARRAYSTACKSIZE + STRINGSTACKSIZE + OBJSTACKSIZE + FUNSTACKSIZE + CONSSTACKSIZE + SYMBOLSTACKSIZE
                                                      + VALUESTACKSIZE;

    // Serialized values are saved and referenced via the integer array 'values'.
    // The indices aray contains the current top of stack for each of the
    // type arrays.

    private int[]            values           = new int[SIZE];

    private int[]            indices          = new int[VALUESTACK + 1];

    // Containing elements that persist across XMF instantations:

    private int              nameSpaces       = Machine.nilValue;

    public void setNameSpaces(int nameSpaces) {
        this.nameSpaces = nameSpaces;
    }

    public boolean isReference(Machine machine, int namedElement) {

        // A named element is a reference when it occurs in one of
        // the ref tables. It may be referenced because all entries
        // in the ref tables are assumed to persist across different
        // instantiations of XMF.

        if (!isNamedElement(machine, namedElement))
            return false;
        return isNamedElementReference(machine, namedElement);
    }

    public boolean isNamedElementReference(Machine machine, int namedElement) {
        boolean isReference = false;
        int ns = nameSpaces;
        while (ns != Machine.nilValue && !isReference) {
            int nameSpace = machine.consHead(ns);
            ns = machine.consTail(ns);
            int table = machine.objGetContents(nameSpace);
            if (table != -1 && machine.hashTableHasValue(table, namedElement))
                isReference = true;
        }
        return isReference;
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
            // System.out.println("found = " + found);
        }
        if (found)
            return nameSpace;
        else
            return -1;
    }

    public boolean isNamedElement(Machine machine, int element) {
        return Machine.isObj(element) && machine.objGetName(element) != -1 && machine.objGetOwner(element) != -1;
    }

    public void reset() {

        // A machine must be reset when saving a new collection of data since the
        // indices for the data types will be different. These are used to hold
        // saved elements so that sharing is faithfully reproduced when data is
        // re-loaded.

        for (int i = 0; i < indices.length; i++)
            indices[i] = 0;
        setMajorVersion(-1);
        setMinorVersion(-1);
        
    }

    // A value must be saved into the appropriate stack:

    public int saveValue(int word) {
        switch (Machine.tag(word)) {
        case ARRAY:
            return saveValue(word, ARRAYSTACK, ARRAYSTACKBASE, STRINGSTACKBASE);
        case STRING:
            return saveValue(word, STRINGSTACK, STRINGSTACKBASE, OBJSTACKBASE);
        case OBJ:
            return saveValue(word, OBJSTACK, OBJSTACKBASE, FUNSTACKBASE);
        case FUN:
            return saveValue(word, FUNSTACK, FUNSTACKBASE, CONSSTACKBASE);
        case CONS:
            return saveValue(word, CONSSTACK, CONSSTACKBASE, SYMBOLSTACKBASE);
        case SYMBOL:
            return saveValue(word, SYMBOLSTACK, SYMBOLSTACKBASE, VALUESTACKBASE);
        default:
            return saveValue(word, VALUESTACK, VALUESTACKBASE, VALUESTACKLIMIT);
        }
    }

    public int saveValue(int value, int stack, int lower, int upper) {
        if ((lower + indices[stack]) >= upper)
            throw new Error("Stack exhausted for " + Machine.tag(value));
        values[lower + indices[stack]] = value;
        int index = indices[stack];
        indices[stack]++;
        return index;
    }

    // Find the index of a value, relativized to the appropriate stack.

    public int index(int word) {
        // Returns the index of the word or -1 if not present.
        switch (Machine.tag(word)) {
        case ARRAY:
            return index(word, ARRAYSTACK, ARRAYSTACKBASE);
        case STRING:
            return index(word, STRINGSTACK, STRINGSTACKBASE);
        case OBJ:
            System.out.println("Warning Index(Object) should not be called.");
            return index(word, OBJSTACK, OBJSTACKBASE);
        case FUN:
            return index(word, FUNSTACK, FUNSTACKBASE);
        case CONS:
            return index(word, CONSSTACK, CONSSTACKBASE);
        case SYMBOL:
            return index(word, SYMBOLSTACK, SYMBOLSTACKBASE);
        default:
            return index(word, VALUESTACK, VALUESTACKBASE);
        }
    }

    public int index(int word, int stack, int start) {

        // Returns -1 if the word is not found in the supplied
        // stack. Otherwise returns the index into the stack.
        // NB the index returned is NOT an absolute index into
        // the values array. You *must* know the type of the
        // reference when loading in the serialized value.

        int limit = indices[stack];
        for (int i = start; i < (start + limit); i++)
            if (values[i] == word)
                return i - start;
        return -1;
    }

    public int ref(int index) {
        return values[index];
    }

    public String instrToString(int instr) {
        switch (instr) {
        case MKARRAY:
            return "MKARRAY";
        case SETARRAY:
            return "SETARRAY";
        case MKCODE:
            return "MKCODE";
        case MKINT:
            return "MKINT";
        case MKSTRING:
            return "MKSTRING";
        case MKCODEBOX:
            return "MKCODEBOX";
        case SETCODEBOX:
            return "SETCODEBOX";
        case MKTRUE:
            return "MKTRUE";
        case MKFALSE:
            return "MKFALSE";
        case MKFUN:
            return "MKFUN";
        case SETFUN:
            return "SETFUN";
        case MKUNDEF:
            return "MKUNDEF";
        case MKOBJ:
            return "MKOBJ";
        case SETOBJ:
            return "SETOBJ";
        case MKCONS:
            return "MKCONS";
        case SETCONS:
            return "SETCONS";
        case MKBUFFER:
            return "MKBUFFER";
        case SETBUFFER:
            return "SETBUFFER";
        case MKNIL:
            return "MKNIL";
        case MKSYMBOL:
            return "MKSYMBOL";
        case MKSET:
            return "MKSET";
        case TABLE:
            return "TABLE";
        case PUT:
            return "PUT";
        case LOOKUP:
            return "LOOKUP";
        case MKFLOAT:
            return "MKFLOAT";
        case END:
            return "END";
        default:
            return "<Load " + instr + ">";
        }
    }

    public void printStats() {
        System.out.println("Save machine: ");
        System.out.println("Arrays:     " + indices[ARRAYSTACK] + " out of " + ARRAYSTACKSIZE);
        System.out.println("Strings:    " + indices[STRINGSTACK] + " out of " + STRINGSTACKSIZE);
        System.out.println("Objects:    " + indices[OBJSTACK] + " out of " + OBJSTACKSIZE);
        System.out.println("Functions:  " + indices[FUNSTACK] + " out of " + FUNSTACKSIZE);
        System.out.println("Cons Pairs: " + indices[CONSSTACK] + " out of " + CONSSTACKSIZE);
        System.out.println("Symbols:    " + indices[SYMBOLSTACK] + " out of " + SYMBOLSTACKSIZE);
        System.out.println("Values:     " + indices[VALUESTACK] + " out of " + VALUESTACKSIZE);
    }

    public void setMajorVersion(int majorVersion) {
        loadMajorVersion = majorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        loadMinorVersion = minorVersion;
    }

    public boolean isNewSerializer() {
        return loadMajorVersion > VERSION_MAJOR || loadMinorVersion > VERSION_MINOR;
    }
}