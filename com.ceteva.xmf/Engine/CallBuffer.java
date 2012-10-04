package Engine;

import java.io.PrintStream;
import java.io.PrintWriter;

public class CallBuffer {
    
    // A call buffer is used to retain the last number of calls.
    
    private Call[] buffer;
    private int head = 0;
    
    public CallBuffer(int size) {
        buffer = new Call[size];
    }
    
    public void gc(Machine machine) {
        for(int i = 0; i < buffer.length; i++) {
            Call call = buffer[i];
            if(call != null)
                call.gc(machine);
        }
    }
    
    public Call nextCall(int target,int arity,int message) {
        if(buffer[head] == null)
            buffer[head] = new Call();
        Call call = buffer[head++];
        if(head == buffer.length)
            head = 0;
        call.target = target;
        call.arity = arity;
        call.message = message;
        return call;
    }
    
    public void printBacktrace(Machine machine) {
        printBacktrace(System.err,machine);
    }
    
    public void printBacktrace(PrintStream out,Machine machine){
        for(int i = head; i < buffer.length;i++) {
            Call call = buffer[i];
            if(call != null)
                call.print(out,machine);
        }
        for(int i = 0; i < head;i++) {
            Call call = buffer[i];
            call.print(out,machine);
        }
    }

}
