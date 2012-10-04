package Values;

import java.lang.reflect.Field;

public class JClass extends Value {
    
    private Class c;
    
    public JClass(Class c) {
        this.c = c;
    }
    
    public Value apply(Value[] args) {
        return sendNew(args);
    }
    
    public Value fieldValue(String name) {
        try {
            Field field = c.getField(name);
            Value value = (Value) field.get(null);
            return value;
        } catch (Exception e) {
            System.out.println("FieldRef: " + e);
            return Null.nullValue;
        }
    }
    
    public boolean isClassFor(Value value) {
    	Class vc = value.getClass();
    	return vc.getName().equals(c.getName());
    }
    
    public String toString() {
        return "Class(" + c + ")";
    }
    
    public Value send(String message,int args) {
        if(message.equals("new"))
            return sendNew(popArgs(args));
        else throw new Error("Class cannot handle message " + message);
    }
    
    public Value sendNew(Value[] args) {
        try {
            return (Value)c.newInstance();
        } catch(Exception e) {
            throw new Error(e.getMessage());
        }
    }

    public void parse(String s) {
    }

}
