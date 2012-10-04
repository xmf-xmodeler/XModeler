package Engine.Undo;

import Engine.Machine;

public class SetSlot extends Command {
    
    int object;
    int slot;
    int newValue;
    int oldValue;
    
    public SetSlot(int object,int slot,int newValue,int oldValue) {
        this.object = object;
        this.slot = slot;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }
    
    public void gc(Machine machine) {
        object = machine.gcObj(object);
        slot = machine.gcCopy(slot);
        newValue = machine.gcCopy(newValue);
        oldValue = machine.gcCopy(oldValue);
    }

    public void redo(Machine machine) {
        machine.objSetAttValue(object,slot,newValue);
    }

    public int size() {
        return 1;
    }

	public String toString(Machine machine) {
		return "SetSlot(" + machine.valueToString(object) + ","
		+ machine.valueToString(slot) + ","
				+ machine.valueToString(newValue) + ","
				+ machine.valueToString(oldValue) + ")";
	}

    public void undo(Machine machine) {
        machine.objSetAttValue(object,slot,oldValue);
    }

}
