package Engine;

public interface Errors {

    // This file defines the error codes used by the machine when it
    // raises exceptions. The codes are set in an instance of MachineException
    // at the user level and can be used to take appropriate action when the
    // exception is handled.

    public final static int ERROR            = 0; // General error.

    public final int        TYPE             = 1; // Something was of the wrong type.

    public final int        UNBOUNDVAR       = 2; // Trying to reference an undefined dynamic variable.

    public final int        INSTR            = 3; // Unknown instruction.

    public final int        MISSINGSLOT      = 4; // Object has no slot.

    public final int        NAMESPACEERR     = 5; // Name space does not define a name.

    public final int        ARGCOUNT         = 6; // Illegal number of supplied args.

    public final int        GCERROR          = 7; // An error during garbage collection.

    public final int        SAVEERR          = 8; // An error during save.

    public final int        LOADERR          = 9; // An error during loading.

    public final int        NOKEY            = 10; // Missing key in table.

    public final int        NOOPERATIONFOUND = 11; // Message passing failed.

    public final int        FOREIGNFUNERR    = 12; // Something happened during a foreign call.

    public final int        INTERRUPT        = 13; // Interrupt pressed.

    public final int        HASHTABLEGET     = 14; // Key does not exist.

    public final int        ARRAYACCESS      = 15; // Index out of range.
    
    public final int        ALLOCERROR       = 16; // Allocation error.

}