package XOS;

import Engine.Thread;

import java.util.LinkedList;

public class ThreadQueue extends LinkedList {
    
    // Used to implement scheduled threads in XOS.
    
    public void insert(Thread thread) {
        addLast(thread);
    }
    
    public Thread next() {
        return (Thread)removeFirst();
    }

}
