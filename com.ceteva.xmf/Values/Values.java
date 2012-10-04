package Values;

import java.util.Vector;
//import java.util.Hashtable;
//import java.util.Iterator;

public class Values extends Value {
    
    // Implements a sequence of values.
    
    private Vector values = new Vector();
    
    public Values() {}
    
    public Values(String[] args) {
        for(int i = 0; i < args.length; i++)
            addElement(new Str(args[i]));
    }
    
    public void addElement(Value value) {
        values.addElement(value);
    }
    
    public Value at(Value index) {
        if(index instanceof Int) {
            Int i = (Int)index;
            return (Value)values.elementAt(i.value());
        } else throw new Error("Index out of range: " + index + " in " + this);
    }
    
    public Value copy() {
        Values v = new Values();
        for(int i = 0; i < values.size(); i++)
            v.addElement((Value)values.elementAt(i));
        return v;
    }
    
    public Values emptyCopy() {
        return new Values();
    }
    
    public Values excluding(Value element) {
        Values values = (Values)copy();
        values.values.remove(element);
        return values;
    }
    
    public boolean isEmpty() {
        return values.isEmpty();
    }
    
    public Value ref(int index) {
        return (Value)values.elementAt(index);
    }
    
    public void remove(Value value) {
        values.removeElement(value); 
    }
    
    public Value sel() {
        return select();
    }
    
    public Value select() {
        if(isEmpty())
            throw new Error("Cannot select from an empty collection.");
        else return (Value)values.elementAt(0);
    }
    
    public Value send(String message,int args) {
        if(message.equals("including"))
            return sendIncluding(popArgs(args));
        else return super.send(message,args);
    }
    
    public Value sendIncluding(Value[] args) {
        Values values = (Values)this.copy();
        values.addElement(args[0]);
        return values;
    }
    
    public void set(int index,Value value) {
        if(values.size() < index + 1)
            values.setSize(index + 1);
        values.setElementAt(value,index);
    }
    
    public Value size() {
        return new Int(values.size());
    }
    
    public String toString() {
        return values.toString();
    }

    public void parse(String s) {
    }

    public Vector getValues() {
       return values;
    }
    
/*    public String[] toStringArray() {
       Object[] valueArray = values.toArray();
       int count = valueArray.length;
       String[] strings = new String[count];
       for (int i=0; i<count; i++) {
          strings[i] = valueArray[i].toString();
       }
       return strings;
    }
    
    public Hashtable toStringTable() {
       Hashtable table = new Hashtable();
       Iterator i = values.iterator();
       while (i.hasNext()) {
          Value v = (Value)i.next();
          table.put(v.toString(),v);
       }
       return table;
    }
*/
}
