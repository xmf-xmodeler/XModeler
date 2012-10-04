package Engine.Undo;

import Engine.Machine;

public abstract class Command {
    
    public abstract void gc(Machine machine);
    
    public abstract void redo(Machine machine);
    
    public abstract int size();
    
    public abstract String toString(Machine machine);
    
    public abstract void undo(Machine machine);

}
