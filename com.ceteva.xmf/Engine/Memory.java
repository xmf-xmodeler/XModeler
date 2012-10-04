package Engine;

class Memory implements Value {

    // Each GC records the current state of memory usage as a new record
    // in a linked list of usage records.

    public static int LIMIT = 24;                // Max number of records maintained.

    private long      time;                      // Records the time at which the record was created.

    private int       totalUsed;                 // Records the total amount of memory allocated.

    private int[]     usage = new int[LASTVALUE]; // Uses for each non-immediate type.

    private Memory    prev;                      // Previous usage record or null.

    public Memory(long time, Memory prev) {
        this.time = time;
        this.prev = prev;
    }

    public void alloc(int type, int size) {
        // Records the allocation of 'size' words of memory for the supplied
        // type.
        usage[type] += size;
    }

    public int data(Machine machine) {
        
        // Returns the sequence of memory data records.
        
        int machineTime = ForeignFuns.machineTime(machine, time, machine.time);
        int record = usageRecord(machine);
        int used = Machine.mkInt(totalUsed);
        return machine.mkCons(machine.mkCons(machineTime, machine.mkCons(used, record)), prevData(machine));
    }

    public Memory dropLastRecord() {
        if (prev == null)
            return null;
        else {
            prev = prev.dropLastRecord();
            return this;
        }
    }

    public int length() {
        // Return the number of records chained together.
        int length = 0;
        Memory m = this;
        while (m != null) {
            length++;
            m = m.prev;
        }
        return length;
    }

    public Memory newRecord() {
        // Called when a new memory record is to be allocated.
        // Links the new record into the existing record.
        if (length() == LIMIT)
            dropLastRecord();
        return new Memory(System.currentTimeMillis(), this);
    }
    
    public Memory prev() {
        return prev;
    }

    public int prevData(Machine machine) {
        if (prev == null)
            return Machine.nilValue;
        else
            return prev.data(machine);
    }
    
    public void setTotalUsed(int m) {
        totalUsed = m;
    }

    public int usageRecord(Machine machine) {
        // Return the recorded memory usage as a VM array.
        int record = machine.mkArray(LASTVALUE);
        for (int i = 0; i < LASTVALUE; i++)
            machine.arraySet(record, i, Machine.mkInt(usage[i]));
        return record;
    }
}