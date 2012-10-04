package XOS;

import java.util.LinkedList;


public class MessageQueue extends LinkedList {
    
    // Used to implement scheduled messages in XOS.
    
    public void insert(Message thread) {
        addLast(thread);
    }
    
    public Message next() {
        return (Message)removeFirst();
    }

}
