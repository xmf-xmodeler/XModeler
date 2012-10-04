package Engine.Undo;

import java.util.Stack;

import Engine.Machine;

public class Commands extends Command {
    
    Stack<Command> commands = new Stack<Command>();
    
    public void gc(Machine machine) {
        for(int i = 0; i < commands.size(); i++) 
            commands.elementAt(i).gc(machine);
    }
    
    public void push(Command command) {
        commands.push(command);
    }
    
    public void redo(Machine machine) {
        for(int i = commands.size() - 1; i >= 0; i--) 
            commands.elementAt(i).redo(machine);
    }
    
    public int size() {
        int size = 0;
        for(int i = 0; i < commands.size(); i++) 
            size = size + commands.elementAt(i).size();
        return size;
    }
    
    public String toString() {
    	String s = super.toString();
    	for(Command c : commands) {
    		s = s + "\n  " + c.toString();
    	}
    	return s + "\n***";
    }
    
    public String toString(Machine machine) {
    	String s = "Commands(";
    	for(Command c : commands) {
    		s = s + "\n  " + c.toString(machine);
    	}
    	return s + ")";
    }
    
    public void undo(Machine machine) {
        for(int i = 0; i < commands.size(); i++) 
            commands.elementAt(i).undo(machine);
    }

}
