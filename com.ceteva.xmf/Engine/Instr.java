package Engine;

public interface Instr {

    // The machine instructions are tagged words. The tag indicates the instruction
    // and the data part of the word contains the operands of the instruction if it
    // has any. This file defines the instruction tags and provides a brief specification
    // of each machine instruction.

    public static final int ADD          = 1;  // Pop value1, pop value2, push value1 + value2.

    public static final int AND          = 2;  // Pop value1, pop value2, push value1 & value2.

    public static final int MKCONS       = 3;  // Pop head and tail and push a cons.

    public static final int DIV          = 4;  // Pop value1, pop value2, push value1 / value2.

    public static final int DOT          = 5;  // Operand is name, pop object, push object.name.

    public static final int DYNAMIC      = 6;  // Push value of dynamic(operand).

    public static final int ENTER        = 7;  // Complete open stack frame. Pop operator

    public static final int EQL          = 8;  // Pop value1, pop value2, push value1 == value2.

    public static final int GLOBAL       = 9;  // Operands are frame and offset, push global(frame,offset).

    public static final int GRE          = 10; // Pop value1, pop value2, push value1 > value2.

    public static final int HEAD         = 11; // Pop sequence, push head.

    public static final int TAIL         = 12; // Pop sequence, push tail.

    public static final int IMPLIES      = 13; // Pop value1, pop value2, push value2 => value1.

    public static final int LESS         = 14; // Pop value1, pop value2, push value1 < value2.

    public static final int LOCAL        = 15; // Operand is offset, push local(offset).

    public static final int MKFUN        = 16; // Push(mkFun(arity,globals,code)).

    public static final int MKSEQ        = 17; // Pop elements and create a sequence.

    public static final int MKSET        = 18; // Pop elements and create a set.

    public static final int MUL          = 19; // Pop value1, pop value2, push value1 * value2.

    public static final int NAMESPACEREF = 20; // Chain back through name spaces and get named element.

    public static final int NOT          = 21; // Pop value, push !value.

    public static final int OR           = 22; // Pop value1, pop value2, push value1 || value2.

    public static final int POP          = 23; // Pop the stack

    public static final int PUSHFALSE    = 24; // Push false.

    public static final int PUSHINT      = 25; // Push operand.

    public static final int PUSHSTR      = 26; // Push operand.

    public static final int PUSHTRUE     = 27; // Push true.

    public static final int RETURN       = 28; // Pop value, pop call frame, push value.

    public static final int STARTCALL    = 29; // Set up new call stack frame.

    public static final int SELF         = 30; // Operand is frame, push(self(frame)).

    public static final int SEND         = 31; // Send a message.

    public static final int SETLOC       = 32; // Operand is offset, pop value, local(offset) := value.

    public static final int SETSLOT      = 33; // Set the slot of an object.

    public static final int SKP          = 34; // codeIndex := codeIndex + operand.

    public static final int SKPF         = 35; // Pop bool, if !bool codeIndex := codeIndex + operand.

    public static final int SUB          = 36; // Pop value1, pop value2, push value1 - value2.

    public static final int SUPER        = 37; // Pop args, continue with the current message lookup.

    public static final int TAILENTER    = 38; // Slide arguments down into current frame. Pop operator.

    public static final int TAILSEND     = 39; // Slide arguments down onto the current frame. Pop target.

    public static final int TAILSUPER    = 40; // Slide arguments down and continue with the current lookup.

    public static final int SIZE         = 41; // Produce the size of the sequence at the head of the stack.

    public static final int DROP         = 42; // Pop the sequence, pop the number of elements, push s->drop(n).

    public static final int ISEMPTY      = 43; // Pop a sequence, push a boolean S->isEmpty.

    public static final int INCLUDES     = 44; // Pop a sequence, pop an element, push S->includes(e).

    public static final int EXCLUDING    = 45; // Pop a sequence, pop an element, push S->excluding(e).

    public static final int INCLUDING    = 46; // Pop a sequence, an element, push S->including(e).

    public static final int SEL          = 47; // Pop a sequence, push a selected element.

    public static final int UNION        = 48; // Pop two sequences, push the union.

    public static final int ASSEQ        = 49; // Pop a sequence push S->asSeq.

    public static final int AT           = 50; // Pop a sequence, pop an index, push S->at(n).

    public static final int SKPBACK      = 51; // Jump back through the instruction stream.

    public static final int SEND0        = 52; // Same as SEND without args. No stack frame is open.

    public static final int TAILSEND0    = 53; // Same as TAILSEND without args. No stack frame is open.

    public static final int NULL         = 54; // Push the constant NULL.

    public static final int OF           = 55; // Pop an element, push its classifier.

    public static final int SETGLOB      = 56; // Pop a value and set a global via frame,offset indices.

    public static final int THROW        = 57; // Pop a value and throw to nesrest catch.

    public static final int TRY          = 58; // Pop handler, pop free vars, call code box.

    public static final int ISKINDOF     = 59; // Pop a value, pop a type return a boolean.

    public static final int SOURCEPOS    = 60; // Registers the current line and char position.

    public static final int GETELEMENT   = 61; // Looks a name up in a name space.

    public static final int SETHEAD      = 62; // Pop seq, pop value, set head of seq.

    public static final int SETTAIL      = 63; // Pop seq, pop value, set tail of seq.

    public static final int READ         = 64; // Pop an input channel and perform non-blocking read.

    public static final int ACCEPT       = 65; // Block until a client connects.

    public static final int ARRAYREF     = 66; // Pop an array, pop an index, reference an array element.

    public static final int ARRAYSET     = 67; // Pop an array, pop an index, pop a value. Set and return the array.

    public static final int TABLEGET     = 68; // Pop a table, pop a key, reference the value.

    public static final int TABLEPUT     = 69; // Pop a table, pop a key, pop a value, set and return the table.

    public static final int MKFUNE       = 70; // Pop code box offset, push a new fun.

    public static final int NOOP         = 71; // Does nothing.

    public static final int SLEEP        = 72; // Sends current thread to sleep.

    public static final int SENDSELF     = 73; // Optimized SEND for target = self.

    public static final int SENDLOCAL    = 74; // Optimized SEND for target = local var.

    public static final int DOTSELF      = 75; // Optimized DOT for target = self.

    public static final int DOTLOCAL     = 76; // Optimized DOT for target = local var.

    public static final int SETSELFSLOT  = 77; // Optimized SETSLOT for target = self.

    public static final int SETLOCALSLOT = 78; // Optimized SETSLOT for target = local var.

    public static final int CONST        = 79; // Push a constant.

    public static final int SYMBOLVALUE  = 80; // Push the value of a symbol.

    public static final int SETLOCPOP    = 81; // Combines SETLOCAL and POP.

    public static final int DISPATCH     = 82; // Performs an indexed jump.

    public static final int INCSELFSLOT  = 83; // Increment a named slot.

    public static final int DECSELFSLOT  = 84; // Decrement a named slot.

    public static final int INCLOCAL     = 85; // Increment a local.

    public static final int DECLOCAL     = 86; // Decrement a local.

    public static final int ADDLOCAL     = 87; // Add 1 to the value of a local.

    public static final int SUBLOCAL     = 88; // Subtract 1 from the value of a local.

    public static final int PREPEND      = 89; // Like CONS except TOS MUST be a sequence.

    public static final int ENTERDYN     = 90; // Entering a global variable.

    public static final int ISNOTEMPTY   = 91; // Opposite of ISEMPTY.

    public static final int LOCALHEAD    = 92; // Take head of indexed local.

    public static final int LOCALTAIL    = 93; // Take tail of indexed local.

    public static final int LOCALASSEQ   = 94; // Push the indexed local as a sequence.

    public static final int LOCALISEMPTY = 95; // Push whether the local is empty.

    public static final int LOCALREFPOS  = 96; // Reference a local ands store the source line.

    public static final int TAILENTERDYN = 97; // Entering a global variable.

    public static final int ASSOC        = 98; // Lisp-style assoc for keys in a-lists.

    public static final int DYNREFPOS    = 99; // Reference a dynamic and store the source line.

    public static final int RETDOTSELF   = 100; // Return the value of a self slot.

    public static final int TOSTRING     = 101; // The oft called operation.

    public static final int ARITY        = 102; // Often called for operations.

    public static final int STRINGEQL    = 103; // Often called for operations.

    public static final int GET          = 104; // Tables or objects.

    public static final int PUT          = 105; // Table update.

    public static final int HASKEY       = 106; // Key checking in tables.

    // ******************************************************************************
    // The following are obsolete:

    public static final int SETDYN       = 102;

    public static final int BINDDYN      = 106;

    // Pop value, operand is name, dynamicBind(name,value).
    public static final int UNBINDDYN    = 107;

    // Operands is name, unbindDynamic(name).
    public static final int MKARRAY      = 110;
    // Length is operand, pop values into elementsin reverse.
}