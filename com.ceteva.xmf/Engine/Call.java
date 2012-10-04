package Engine;

import java.io.PrintStream;

public class Call {
    
    public static final int MAXARGS = 48;
    
    public int target;
    public int message;
    public int arity;
    public int[] args = new int[MAXARGS];
    
    public Call() {}
    
    public void gc(Machine machine) {
        target = machine.gcCopy(target);
        message = machine.gcCopy(message);
        for(int i = 0; i < arity; i++)
            args[i] = machine.gcCopy(args[i]);
    }
    
    public int getMessage() {
        return message;
    }
    
    public int getTarget() {
        return target;
    }
    
    public int getArity() {
        return arity;
    }
    
    public int getArg(int a) {
        return args[a];
    }
    
    public void print(PrintStream out,Machine machine) {
        String tgtString = machine.valueToString(target);
        String opName = machine.valueToString(message);
        out.print(tgtString);
        out.print(".");
        out.print(opName);
        printArgs(out,machine);
        out.println();
    }
    
    public void printArgs(PrintStream out,Machine machine) {
        out.print("(");
        for(int i = 0; i < arity; i++) {
            String arg = machine.valueToString(args[i]);
            out.print(arg);
            if(i + 1 < arity)
                out.print(",");
        }
        out.print(")");
    }
    
    public void setTarget(int target) {
        this.target = target;
    }
    
    public void setMessage(int name) {
        this.message = name;
    }
    
    public void setArity(int arity) {
        this.arity = arity;
    }
    
    public void setArg(int a,int value) {
        args[a] = value;
    }

}
