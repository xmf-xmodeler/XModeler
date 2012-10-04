package Engine.Undo;

import Engine.Machine;

public class SetArray extends Command {

	int array;

	int index;

	int newValue;

	int oldValue;

	public SetArray(int array, int index, int newValue, int oldValue) {
		this.array = array;
		this.index = index;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	public void gc(Machine machine) {
		array = machine.gcCopy(array);
		newValue = machine.gcCopy(newValue);
		oldValue = machine.gcCopy(oldValue);
	}

	public void redo(Machine machine) {
		machine.arraySet(array, index, newValue);
	}

	public int size() {
		return 1;
	}

	public String toString(Machine machine) {
		return "SetArray(" + machine.valueToString(array) + ","
				+ machine.valueToString(newValue) + ","
				+ machine.valueToString(oldValue) + ")";
	}

	public void undo(Machine machine) {
		machine.arraySet(array, index, oldValue);
	}

}
