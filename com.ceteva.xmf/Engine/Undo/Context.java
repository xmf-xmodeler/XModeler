package Engine.Undo;

import Engine.Machine;

public class Context extends Command {
    
    public void gc(Machine machine) {}
    
    public void redo(Machine machine) {}
    
    public int size() { return 0; }
    
    public String toString(Machine machine) { return "Context()"; }
    
    public void undo(Machine machine) {}

}
