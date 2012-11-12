package Engine;

public interface Value {

    // The machine implements a tagged architecture. All values
    // are machine words consisting of a tag and a value. The tag
    // encodes the type of the value. The value can be an immediate
    // (such as an integer or a boolean) or a pointer into the
    // heap (such as an array).

    public static final int UNDEFINED       = 0;              // Memory is initialised to undefined.

    public static final int ARRAY           = 1;              // A fixed size indexed sequence of values.

    public static final int ARRAY_HEADER    = 3;

    public static final int CODE            = 2;              // A fixed size indexed sequence of instructions.

    public static final int CODE_HEADER     = 1;

    public static final int INT             = 3;              // A positive integer.

    public static final int INT_MASK        = INT << 24;

    public static final int STRING          = 4;              // A fixed size indexed sequence of char-codes.

    public static final int STRING_HEADER   = 1;

    public static final int CODEBOX         = 5;              // Code + constants + globals.

    public static final int CODEBOX_SIZE    = 6;

    public static final int BOOL            = 6;              // Either true or false.

    public static final int BOOL_MASK       = BOOL << 24;

    public static final int OBJ             = 7;              // An object with fields.

    public static final int OBJ_SIZE        = 4;

    public static final int ALIENOBJ        = 29;             // An Alien object.

    public static final int FUN             = 8;              // A function.

    public static final int FUN_SIZE        = 9;

    public static final int FOREIGNFUN      = 9;              // A Java function.

    public static final int FOREIGNOBJ      = 10;             // A Java object.

    public static final int FORWARD         = 11;             // Used by the garbage collector.

    public static final int CONT            = 12;             // A continutation (stack copy).

    public static final int CONT_HEADER     = 3;

    public static final int STRINGLENGTH    = 13;             // Used by the garbage collector.

    public static final int CODELENGTH      = 14;             // Used by the garbage collector.

    public static final int CONS            = 15;             // A cons pair.

    public static final int CONS_SIZE       = 2;

    public static final int NIL             = 16;             // The empty list.

    public static final int SYMBOL          = 17;             // Symbolic names.

    public static final int SYMBOL_SIZE     = 2;

    public static final int SET             = 18;             // A set of elements.

    public static final int SET_SIZE        = 1;

    public static final int INPUT_CHANNEL   = 19;             // A channel of input data.

    public static final int OUTPUT_CHANNEL  = 20;             // A channel for output data.

    public static final int HASHTABLE       = 21;             // A table associating keys with values.

    public static final int NEGINT          = 22;             // Negative integers.

    public static final int NEGINT_MASK     = NEGINT << 24;

    public static final int FLOAT           = 23;             // Floating point numbers.

    public static final int FLOAT_SIZE      = 1;

    public static final int CLIENT          = 24;             // Clients.

    public static final int THREAD          = 26;             // Threads.

    public static final int DAEMON          = 27;             // Daemons.

    public static final int DAEMON_SIZE     = 7;

    public static final int BUFFER          = 28;             // Dynamic arrays.

    public static final int BUFFER_SIZE     = 6;

    public static final int SAVEINDEX       = 30;             // Tags an index into the save table.

    public static final int DATABASE        = 31;             // A channel to a database

    public static final int QUERYRESULT     = 32;             // A channel to a query result in a database

    public static final int FORWARDREF      = 33;             // A forward reference with path.

    public static final int FORWARDREF_SIZE = 3;              // The path, value and listeners.

    public static final int LASTVALUE       = 34;             // The first unused type tag.

    public static final int ILLEGAL         = 255;            // Corresponds to -1.

    // Dynamic variables are one of the following:

    public static final int DYNAMIC_VALUE   = (INT << 24) | 1;

    public static final int DYNAMIC_TABLE   = (INT << 24) | 2;
}