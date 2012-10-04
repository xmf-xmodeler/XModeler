package Engine.Undo;

import Engine.Machine;

public class SetDaemons extends Command {
    
    int object;
    int newDaemons;
    int oldDaemons;
    
    public SetDaemons(int object,int newDaemons,int oldDaemons) {
        this.object = object;
        this.newDaemons = newDaemons;
        this.oldDaemons = oldDaemons;
    }

    public void gc(Machine machine) {
        object = machine.gcObj(object);
        newDaemons = machine.gcCopy(newDaemons);
        oldDaemons = machine.gcCopy(oldDaemons);
    }

    public void redo(Machine machine) {
        machine.objSetDaemons(object, oldDaemons);
    }

    public int size() {
        return 1;
    }

	public String toString(Machine machine) {
		return "SetDaemons(" + machine.valueToString(object) + ","
				+ machine.valueToString(newDaemons) + ","
				+ machine.valueToString(oldDaemons) + ")";
	}

    public void undo(Machine machine) {
        machine.objSetDaemons(object, oldDaemons);
    }

}
