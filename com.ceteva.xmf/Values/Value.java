package Values;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Stack;

public abstract class Value implements Serializable {

    // The root class of the XMF Java export value hierarchy. Generic operations
    // are defined on this class. Every value in Java code generated from XMF
    // XOCL source must be an instance of this class (or a suitable sub-class).
    // Value is a wrapper class that ensures a standard interface and type for
    // all the application values.

    // Java code runs by emulating the XMF VM. It implements a small sub-set
    // of the machine instructions as static operations defined by Value.
    // The machine uses a stack to provide storage for arguments.

    private static Stack stack = new Stack();
    
    // The running system need to know the name of the package that has been
    // deployed.
    
    private static String packageName;

    public static void add() {
        // Instruction to add the top two elements on the stack.
        Value v1 = popStack();
        Value v2 = popStack();
        if (v1 instanceof Int && v2 instanceof Int) {
            Int i1 = (Int) v1;
            Int i2 = (Int) v2;
            pushStack(i1.add(i2));
        } 
        else if (v1 instanceof Str || v2 instanceof Str)
            pushStack(new Str(v2.toString() + v1.toString()));
        else
            throw new Error("Cannot add " + v1 + " and " + v2);
    }

    public static void apply(int args) {
        // An instruction to apply the top of the stack to
        // the arguments below it.
        Value op = popStack();
        pushStack(op.apply(popArgs(args)));
    }

    public Value apply(Value[] args) {
        // Guarantees a value can be applicable, but should be
        // implemented in only those sub-classes where it makes
        // sense.
        throw new Error("Cannot apply " + this);
    }

    public static void boolAnd() {
        // Instruction to and the top two elements on the stack.
        Value v1 = popStack();
        Value v2 = popStack();
        if (v1 instanceof Bool && v2 instanceof Bool) {
            Bool b1 = (Bool) v1;
            Bool b2 = (Bool) v2;
            pushStack(b1.boolAnd(b2));
        } else
            throw new Error("Cannot and " + v1 + " and " + v2);
    }

    public static void boolNot() {
        // Instruction to negate the top element on the stack.
        Value v = popStack();
        if (v instanceof Bool) {
            Bool b = (Bool) v;
            pushStack(b.not());
        } else
            throw new Error("Cannot not " + v);
    }

    public static void boolOr() {
        // Instruction to and the top two elements on the stack.
        Value v1 = popStack();
        Value v2 = popStack();
        if (v1 instanceof Bool && v2 instanceof Bool) {
            Bool b1 = (Bool) v1;
            Bool b2 = (Bool) v2;
            pushStack(b1.boolOr(b2));
        } else
            throw new Error("Cannot or " + v1 + " and " + v2);
    }

    public static void call(String message, int args) {
        Value target = popStack();
        pushStack(target.send(message, args));
    }

    public Value copy() {
        return this;
    }

    public Values emptyCopy() {
        if (this instanceof Values) {
            Values values = (Values) this;
            return values.emptyCopy();
        } else
            throw new Error("Empty copy: " + this + " is not a collection");
    }

    public void eql() {
        // Pushes true when the top two elements are equal.
        Value v1 = popStack();
        Value v2 = popStack();
        pushStack(new Bool(v1.equals(v2)));
    }

    public static void instanceOf(String name) {
        Value value = popStack();
        global(name);
        JClass c = (JClass) popStack();
        pushStack(c.isClassFor(value) ? new Bool(true) : new Bool(false));
    }

    public boolean isTrue() {
        if (this instanceof Bool) {
            Bool b = (Bool) this;
            return b.value();
        } else
            return false;
    }

    public boolean isFalse() {
        if (this instanceof Bool) {
            Bool b = (Bool) this;
            return !b.value();
        } else
            return false;
    }

    public static void fieldRef(String name) {
        Value object = popStack();
        pushStack(object.fieldValue(name));
    }
    
    public Value fieldValue(String name) {
        try {
            Class c = getClass();
            Field field = c.getField(name);
            Value value = (Value) field.get(this);
            return value;
        } catch (Exception e) {
            System.out.println("FieldRef: " + e);
            return Null.nullValue;
        }
    }

    public static void fieldUpdate(String name) {
        Value object = popStack();
        Value value = popStack();
        try {
            Class c = object.getClass();
            Field field = c.getField(name);
            field.set(object, value);
            pushStack(object);
        } catch (Exception e) {
            System.out.println("fieldUpdate: " + object + "." + name + " " + e);
            pushStack(Null.nullValue);
        }
    }

    public static void global(String name) {
        try {
            Class c = Class.forName(packageName + "." + name);
            pushStack(new JClass(c));
        } catch (Exception e) {
            System.out.println("Global cannot find name: " + packageName + "." + name);
            pushStack(Null.nullValue);
        }
    }

    public static void greater() {
        // Instruction to compare the top two elements on the stack.
        Value v1 = popStack();
        Value v2 = popStack();
        if (v1 instanceof Int && v2 instanceof Int) {
            Int i1 = (Int) v1;
            Int i2 = (Int) v2;
            pushStack(i2.greater(i1));
        } else
            throw new Error("Cannot compare " + v1 + " and " + v2);
    }

    public boolean isNotEmpty() {
        if (this instanceof Values) {
            Values values = (Values) this;
            return !values.isEmpty();
        } else
            return false;
    }

    public static void less() {
        // Instruction to compare the top two elements on the stack.
        Value v1 = popStack();
        Value v2 = popStack();
        if (v1 instanceof Int && v2 instanceof Int) {
            Int i1 = (Int) v1;
            Int i2 = (Int) v2;
            pushStack(i2.less(i1));
        } else
            throw new Error("Cannot compare " + v1 + " and " + v2);
    }
    
    public void neql() {
        // Pushes false when the top two elements are equal.
        Value v1 = popStack();
        Value v2 = popStack();
        pushStack(new Bool(!v1.equals(v2)));
    }

    public void parse(String s) throws ValueParseException {
    }
    
    public static void pushStack(int value) {
        pushStack(new Int(value));
    }
    
    public static void pushStack(boolean value) {
        pushStack(new Bool(value));
    }
    
    public static void pushStack(String value) {
        pushStack(new Str(value));
    }

    public static void pushStack(Value value) {
        stack.push(value);
    }

    public static Value popStack() {
        return (Value) stack.pop();
    }

    public static Value peekStack() {
        return (Value) stack.peek();
    }

    public static Value[] popArgs(int n) {
        Value[] args = new Value[n];
        for (int i = n - 1; i >= 0; i--)
            args[i] = popStack();
        return args;
    }

    public void println() {
        System.out.println(this);
    }

    public void remove(Value value) {
        if (this instanceof Values) {
            Values values = (Values) this;
            values.remove(value);
        } else
            throw new Error("Cannot remove value from " + this);
    }

    public Value select() {
        if (this instanceof Values) {
            Values values = (Values) this;
            return values.select();
        } else
            throw new Error("Cannot select from " + this);
    }

    public Value send(String message, int args) {
        try {
            Class c = this.getClass();
            Method[] methods = c.getMethods();
            Method method = null;
            for (int i = 0; i < methods.length; i++)
                if (methods[i].getName().equals(message)) method = methods[i];
            if (method == null) throw new Error(this + "cannot handle message " + message);
            Object result = method.invoke(this, popArgs(args));
            if(result instanceof Value) return (Value)result;
            if(result instanceof String) return new Str((String)result);
            if(result == null) return Null.nullValue;
            System.out.println("Send " + this + " message " + message + ": illegal result " + result);
            return Null.nullValue;
        } catch (Exception e) {
            System.out.println("Send " + this + " message " + message + ": " + e);
            //e.printStackTrace();
            return Null.nullValue;
        }
    }
    
    public static void setPackageName(String name) {
    	packageName = name;
    }

    public static void sub() {
        // Instruction to subtract the top two elements on the stack.
        Value v1 = popStack();
        Value v2 = popStack();
        if (v1 instanceof Int && v2 instanceof Int) {
            Int i1 = (Int) v1;
            Int i2 = (Int) v2;
            pushStack(i2.sub(i1));
        } else
            throw new Error("Cannot subtract " + v1 + " and " + v2);
    }

    public String toString() {
        Class c = this.getClass();
        String s;
        try {
            Field[] fields = c.getFields();
            s = c.getName() + "[";
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                String name = field.getName();
                Object value = field.get(this);
                s = s + name + "=" + value;
                if (i + 1 < fields.length) s = s + ",";
            }
            return s + "]";
        } catch (Exception e) {
            return e.toString();
        }
    }

}
