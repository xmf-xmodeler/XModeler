package Engine;

public class ValueStack implements java.io.Serializable {
    // A value stack is just a fixed size array of machine words.

    protected int[] elements;

    protected int   index = 0;
    
    protected int size = 0;

    public ValueStack(int size) {
        this.size = size;
        elements = new int[size];
    }

    public void reset() {
        index = 0;
    }

    public boolean full() {
        return index >= elements.length;
    }

    public void push(int word) {
        // Will throw an exception if the end of stack is
        // reached. Use this for normal operation so that
        // an exception is raised when stack overflow
        // occurs. This is caught in the VM.
        elements[index++] = word;
    }

    public void vpush(int word) {
        // Will attempt the expand if the storage is exhausted.
        // Use this sparingly since it will not catch recursive
        // loops in the correct way.
        if (index + 1 >= size) {
            growStorage();
            vpush(word);
        } else
            elements[index++] = word;
    }
    
    public void growStorage() {
        // Attempts to expand the value stack.
        System.out.println("Expanding stack...");
        int[] newElements = new int[size * 2];
        System.arraycopy(elements,0,newElements,0,size);
        elements = newElements;
        size = size * 2;
    }

    public void pushn(int n, int word) {
        for (int i = 0; i < n; i++)
            elements[index++] = word;
    }

    public int top() {
        return elements[index - 1];
    }

    public int getTOS() {
        return index;
    }

    public int fromTop(int i) {
        return elements[index - (1 + i)];
    }

    public boolean empty() {
        return index == 0;
    }

    public int pop() {
        return elements[--index];
    }

    public int index(int word) {
        boolean found = false;
        int i = 0;
        while (!found && (i < index))
            if (elements[i] == word)
                found = true;
            else
                i++;
        if (found)
            return i;
        else
            return -1;
    }

    public int ref(int index) {
        return elements[index];
    }

    public void set(int index, int value) {
        elements[index] = value;
    }

    public void setTOS(int index) {
        this.index = index;
    }

    public int size() {
        return elements.length;
    }

    public String toString(Machine machine) {
        String s = "{";
        for (int i = 0; i < index; i++) {
            int value = elements[i];
            if (!(value == -1))
                s = s + machine.valueToString(elements[i]);
            if ((i + 1) < index)
                s = s + ",";
        }
        return s + "}";
    }

    public void copyInto(ValueStack valueStack) {
        for (int i = 0; i < elements.length; i++)
            valueStack.elements[i] = elements[i];
        valueStack.index = index;
    }
}