package Engine.Undo;

import java.util.Stack;

import Engine.Machine;

public class UndoEngine {

	// An undo engine controls all the undoable commands in the machine.
	// Each undoable state change is sent ot the undo engine. If the
	// engine is currently recording then the state change is recorded
	// on the undo stack as a command. Each command may be undone and
	// subsequently redone. In both cases the state change does *not*
	// cause daemons to fire (since it is assumed that their state changes
	// have also been recorded). Commands recorded in a given transaction
	// are packaged up into a single group command that will be undone as
	// a single unit.
	//
	// The size of the undo stack may be limited to a supplied size. All
	// of the commands must be garbage collected as part of the usual gc
	// cycle.

	int contextNesting = 0;

	Stack<Command> undo = new Stack<Command>();

	Stack<Command> redo = new Stack<Command>();

	boolean recording = false;

	public void endContext() {
		// Pop all the commands up to the marker...

		if (contextNesting == 0) {
			recording = false;
			Commands commands = new Commands();
			Command command = undo.pop();
			while (!(command instanceof Context)) {
				commands.push(command);
				command = undo.pop();
			}
			undo.push(commands);
		} else
			contextNesting--;
	}

	public void execRedo(Machine machine) {
		if (redo.isEmpty())
			throw new RedoStackEmpty();
		else {
			Command command = redo.pop();
			undo.push(command);
			command.redo(machine);
		}
	}

	public void execUndo(Machine machine) {
		if (undo.isEmpty())
			throw new UndoStackEmpty();
		else {
			Command command = undo.pop();
			redo.push(command);
			command.undo(machine);
		}
	}

	public void gc(Machine machine) {
		gcUndo(machine);
		gcRedo(machine);
	}

	public void gcUndo(Machine machine) {
		for (int i = 0; i < undo.size(); i++) {
			Command command = undo.elementAt(i);
			command.gc(machine);
		}
	}

	public void gcRedo(Machine machine) {
		for (int i = 0; i < redo.size(); i++) {
			redo.elementAt(i).gc(machine);
		}
	}

	public int redoStackSize() {
		return redo.size();
	}

	public void setArray(int array, int index, int newValue, int oldValue) {
		if (recording)
			undo.push(new SetArray(array, index, newValue, oldValue));
	}

	public void setArrayDaemons(int array, int newDaemons, int oldDaemons) {
		if (recording)
			undo.push(new SetArrayDaemons(array, newDaemons, oldDaemons));
	}

	public void setDaemons(int obj, int newDaemons, int oldDaemons) {
		if (recording)
			undo.push(new SetDaemons(obj, newDaemons, oldDaemons));
	}

	public void setTable(int table, int key, int newValue, int oldValue) {
		if (recording)
			undo.push(new SetTable(table, key, newValue, oldValue));
	}

	public void setSlot(int object, int name, int newValue, int oldValue) {
		if (recording)
			undo.push(new SetSlot(object, name, newValue, oldValue));
	}

	public void setUndoSize(int n) {
		redo.clear();
		while (undo.size() > n)
			undo.removeElementAt(0);
	}

	public void startContext() {
		// Push a marker...
		if (!recording) {
			undo.push(new Context());
			recording = true;
		} else
			contextNesting++;
	}

	public int redoCommandSize() {
		int size = 0;
		for (int i = 0; i < redo.size(); i++)
			size = size + redo.elementAt(i).size();
		return size;
	}

	public int undoCommandSize() {
		int size = 0;
		for (int i = 0; i < undo.size(); i++)
			size = size + undo.elementAt(i).size();
		return size;
	}

	public int undoStackSize() {
		return undo.size();
	}

}
