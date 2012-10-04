package Engine;

public class Thread implements java.io.Serializable {

    // A thread consists of a machine stack and the values of the machine
    // registers that relate to the stack. A machine may have multiple
    // threads, one of which is current at any given time. Threads are
    // maintained in a cyclic doubly linked list so that new threads can
    // easily be inserted and existing threads can be easily removed.

    // A thread is in one of the following states:

    public static final int ACTIVE       = 0;

    public static final int BLOCK_READ   = 1;

    public static final int BLOCK_ACCEPT = 2;

    public static final int SLEEPING     = 3;

    public static final int DEAD         = 4;

    private String          name;                 // The name of the thread.

    private int             id;                   // A unique identifier for the thread.

    private ValueStack      stack;                // A stack of call frames.

    private int             currentFrame;         // The current call frame in stack.

    private int             openFrame;            // The current open frame in stack.

    private Thread          next;                 // The next thread (could be this);

    private Thread          prev;                 // The previous thread (could be this).

    private int             state        = ACTIVE; // The current state of the thread.

    private int             inputChannel;         // Blocking input channel.

    private String          client;               // Blocking client name.

    public Thread(String owner, ValueStack stack, int currentFrame, int openFrame) {
        this.name = owner;
        this.stack = stack;
        this.currentFrame = currentFrame;
        this.openFrame = openFrame;
        this.next = this;
        this.prev = this;
    }

    public Thread add(Thread thread) {

        // A new thread is added to an existing thread.
        // The new thread is added **after** the current
        // thread and is returned as the new thread.
        // The act of adding the thread to the current
        // thread ring causes the thread to be allocated
        // an identifier.

        thread.next = next;
        thread.prev = this;
        next.prev = thread;
        this.next = thread;
        thread.id = allocateId();
        return thread;
    }

    public int allocateId() {
        Thread t = this;
        int id = 0;
        do {
            id = Math.max(id, t.id());
            t = t.next;
        } while (t != this);
        return id + 1;
    }

    public String allToString() {
        String s = "";
        Thread thread = this;
        do {
            s = s + thread.toString();
            thread = thread.next;
            if (thread != this)
                s = s + ",";
        } while (thread != this);
        return s;
    }

    public void blockOnAccept(String client) {

        // The thread is placed in a waiting state and is woken up
        // when the named client connects to XOS.

        state = BLOCK_ACCEPT;
        this.client = client;
    }

    public void blockOnRead(int inputChannel) {

        // The thread is placed in a waiting state and is woken up
        // when the indexed input channel is ready to produce some
        // input.

        state = BLOCK_READ;
        this.client = "";
        this.inputChannel = inputChannel;
    }

    public void blockOnRead(String client) {

        state = BLOCK_READ;
        this.inputChannel = -1;
        this.client = client;
    }

    public String client() {
        return client;
    }

    public int currentFrame() {
        return currentFrame;
    }

    public int id() {
        return id;
    }

    public boolean isSleeping() {
        return state == SLEEPING;
    }

    public int inputChannel() {
        return inputChannel;
    }
    
    public void kill() {
        state = DEAD;
    }

    public int length() {
        int length = 1;
        Thread thread = this.next();
        while (thread != this) {
            length++;
            thread = thread.next();
        }
        return length;
    }

    public Thread next() {
        return next;
    }

    public int openFrame() {
        return openFrame;
    }

    public Thread remove() {
        // Remove ourself from the cycle of threads.
        // If we are the only thread then return 'null'.
        if (singleThread())
            return null;
        else {
            prev.next = next;
            next.prev = prev;
            return next;
        }
    }

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }

    public void setOpenFrame(int openFrame) {
        this.openFrame = openFrame;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean singleThread() {
        // return true when receiver is the only
        // thread in the cycle.
        return next == this;
    }

    public void sleep() {
        state = SLEEPING;
    }

    public ValueStack stack() {
        return stack;
    }

    public int state() {
        return state;
    }

    public String stateToString() {
        switch (state) {
        case ACTIVE:
            return "ACTIVE";
        case BLOCK_READ:
            return "BLOCK_READ";
        case BLOCK_ACCEPT:
            return "BLOCK_ACCEPT";
        case SLEEPING:
            return "SLEEPING";
        default:
            return "?";
        }
    }

    public String toString() {
        return "Thread(" + name + "," + id + "," + stateToString() + ")";
    }

    public void wake(int value) {
        state = ACTIVE;
        stack.push(value);
    }

}