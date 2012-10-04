package Engine.Undo;

import Engine.Machine;

public class SetTable extends Command {
    
    int table;
    int key;
    int newValue;
    int oldValue;
    
    public SetTable(int table,int key,int newValue,int oldValue) {
        this.table = table;
        this.key = key;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }
    
    public void gc(Machine machine) {
        table = machine.gcHashTable(table);
        newValue = machine.gcCopy(newValue);
        oldValue = machine.gcCopy(oldValue);
    }

    public void redo(Machine machine) {
        machine.hashTablePut(table,key,newValue);
    }

    public int size() {
        return 1;
    }

	public String toString(Machine machine) {
		return "SetTable(" + machine.valueToString(table) + ","
		+ machine.valueToString(key) + ","
				+ machine.valueToString(newValue) + ","
				+ machine.valueToString(oldValue) + ")";
	}

    public void undo(Machine machine) {
        machine.hashTablePut(table,key,oldValue);
    }

}
