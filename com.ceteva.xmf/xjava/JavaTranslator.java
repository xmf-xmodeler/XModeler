package xjava;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.emf.ecore.EObject;

import Engine.Machine;

public class JavaTranslator {
	
    public static boolean isStatic(Field field) {
        return (field.getModifiers() & Modifier.STATIC) != 0;
    }

    public static boolean isStatic(Method method) {
        return (method.getModifiers() & Modifier.STATIC) != 0;
    }

    public static boolean isPublic(Field field) {
        return (field.getModifiers() & Modifier.PUBLIC) != 0;
    }

    public static boolean isPublic(Method method) {
        return (method.getModifiers() & Modifier.PUBLIC) != 0;
    }

    public static int mapJavaArray(Machine machine, Object[] values) {
        int seq = Machine.nilValue;
        for (int i = values.length - 1; i >= 0; i--)
            seq = machine.mkCons(mapJavaValue(machine, values[i]), seq);
        return seq;
    }

    public static int mapJavaArray(Machine machine, int[] values) {
        int seq = Machine.nilValue;
        for (int i = values.length - 1; i >= 0; i--)
            seq = machine.mkCons(mapJavaValue(machine, new Integer(values[i])), seq);
        return seq;
    }

    public static int mapJavaArray(Machine machine, float[] values) {
        int seq = Machine.nilValue;
        for (int i = values.length - 1; i >= 0; i--)
            seq = machine.mkCons(mapJavaValue(machine, new Float(values[i])), seq);
        return seq;
    }

    public static int mapJavaArray(Machine machine, double[] values) {
        int seq = Machine.nilValue;
        for (int i = values.length - 1; i >= 0; i--)
            seq = machine.mkCons(mapJavaValue(machine, new Double(values[i])), seq);
        return seq;
    }

    public static int mapJavaArray(Machine machine, boolean[] values) {
        int seq = Machine.nilValue;
        for (int i = values.length - 1; i >= 0; i--)
            seq = machine.mkCons(mapJavaValue(machine, new Boolean(values[i])), seq);
        return seq;
    }
    
    public static int mapJavaHashtable(Machine machine, Hashtable hashTable) {
        int table = machine.mkHashtable(100);
        Enumeration keys = hashTable.keys();
        while(keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = hashTable.get(key);
            machine.hashTablePut(table,mapJavaValue(machine,key),mapJavaValue(machine,value));
        }
        return table;
    }
    
    public static int mapJavaVector(Machine machine,Vector vector) {
        int seq = Machine.nilValue;
        for (int i = vector.size() - 1; i >= 0; i--)
            seq = machine.mkCons(mapJavaValue(machine, vector.elementAt(i)), seq);
        return seq;
    }

    public static int mapJavaValue(Machine machine, Object value) {
        if (value == null)
            return Machine.undefinedValue;
        if (value instanceof Boolean)
            return ((Boolean) value).booleanValue() ? Machine.trueValue : Machine.falseValue;
        if (value instanceof Integer)
            return Machine.mkInt(((Integer) value).intValue());
        if (value instanceof Float)
            return machine.mkFloat(((Float) value).floatValue());
        if (value instanceof Double)
            return machine.mkFloat(((Double) value).floatValue());
        if (value instanceof String)
            return machine.mkString((String) value);
        if (value instanceof Object[])
            return mapJavaArray(machine, (Object[]) value);
        if (value instanceof int[])
            return mapJavaArray(machine, (int[]) value);
        if (value instanceof boolean[])
            return mapJavaArray(machine, (boolean[]) value);
        if (value instanceof float[])
            return mapJavaArray(machine, (float[]) value);
        if (value instanceof double[])
            return mapJavaArray(machine, (double[]) value);
        if(value instanceof Vector)
            return mapJavaVector(machine,(Vector)value);
        if(value instanceof Hashtable)
            return mapJavaHashtable(machine,(Hashtable)value);
        if (value instanceof EObject)
            return machine.newAlienObj(value);
        if (value instanceof Object)
            return machine.newForeignObj(value);
        else
            return Machine.undefinedValue;
    }
    
    public static Object[] mapXMFArgs(Machine machine, Class[] types, int args) {
        Object[] values = new Object[machine.consLength(args)];
        int i = 0;
        while (machine.consLength(args) > 0) {
            Class type = types[i];
            values[i++] = mapXMFValue(machine, type, machine.consHead(args));
            args = machine.consTail(args);
        }
        return values;
    }

    public static Object mapXMFConsToArray(Machine machine, Class type, int value) {
        if (type == Integer.TYPE)
            return mapXMFConsToIntArray(machine, value);
        if (type == Boolean.TYPE)
            return mapXMFConsToBoolArray(machine, value);
        /*
         * if (type == Float.TYPE) return mapXMFConsToFloatArray(machine, value); if (type == Double.TYPE) return mapXMFConsToDoubleArray(machine, value);
         */
        return mapXMFConsToObjectArray(machine, type, value);
    }

    public static int[] mapXMFConsToIntArray(Machine machine, int values) {
        int[] array = new int[machine.consLength(values)];
        int i = 0;
        while (values != Machine.nilValue) {
            int value = machine.consHead(values);
            if (Machine.isInt(value)) {
                array[i++] = Machine.intValue(value);
                values = machine.consTail(values);
            } else
                throw new XMFToJavaTypeError("Expecting an int", value, Integer.TYPE);
        }
        return array;
    }

    public static boolean[] mapXMFConsToBoolArray(Machine machine, int values) {
        boolean[] array = new boolean[machine.consLength(values)];
        int i = 0;
        while (values != Machine.nilValue) {
            int value = machine.consHead(values);
            if (Machine.isBool(value)) {
                array[i++] = value == Machine.trueValue ? true : false;
                values = machine.consTail(values);
            } else
                throw new XMFToJavaTypeError("Expecting a boolean", value, Boolean.TYPE);
        }
        return array;
    }

    public static Object[] mapXMFConsToObjectArray(Machine machine, Class type, int values) {
        Object[] array = new Object[machine.consLength(values)];
        int i = 0;
        while (values != Machine.nilValue) {
            int value = machine.consHead(values);
            array[i++] = mapXMFValue(machine, type, value);
            values = machine.consTail(values);
        }
        return array;
    }
    
    public static Vector mapXMFConsToVector(Machine machine,int cons) {
        Vector vector = new Vector();
        while(cons != Machine.nilValue) {
            vector.addElement(mapXMFValue(machine,Object.class,machine.consHead(cons)));
            cons = machine.consTail(cons);
        }
        return vector;
    }

    public static Object mapXMFValue(Machine machine, Class type, int value) {
        if (value == Machine.undefinedValue)
            if (type.isPrimitive() || type == java.lang.Object.class)
                throw new XMFToJavaTypeError("Primitive type cannot be null", value, type);
            else
                return null;
        if (Machine.isInt(value))
            if (type == Integer.TYPE || type == java.lang.Integer.class || type == java.lang.Object.class)
                return new Integer(Machine.intValue(value));
            else
                throw new XMFToJavaTypeError("Integer clash", value, type);
        if (Machine.isBool(value))
            if (type == Boolean.TYPE || type == Boolean.class || type == java.lang.Object.class)
                return new Boolean(value == Machine.trueValue ? true : false);
            else
                throw new XMFToJavaTypeError("Boolean clash", value, type);
        if (Machine.isFloat(value))
            if (type == Float.TYPE || type == Float.class || type == java.lang.Object.class)
                return new Float(machine.valueToString(value));
            else if(type == Double.TYPE || type == Double.class)
                return new Double(machine.valueToString(value));
            else
                throw new XMFToJavaTypeError("Float clash", value, type);
        if (Machine.isString(value))
            if (type == String.class || type == java.lang.Object.class)
                return machine.valueToString(value);
            else
                throw new XMFToJavaTypeError("Boolean clash", value, type);
        if (Machine.isCons(value))
            if (type.isArray())
                return mapXMFConsToArray(machine, type.getComponentType(), value);
            else if (type == Vector.class || type == java.lang.Object.class) 
                return mapXMFConsToVector(machine, value);
            else
                throw new XMFToJavaTypeError("Cons clash", value, type);
    	if(Machine.isAlienObj(value)) {
            Object object = machine.AlienObj(Machine.value(value));
            if(type.isInstance(object))
                return object;
            else throw new XMFToJavaTypeError("Alien obj of wrong type",value,type);
        }
        if(Machine.isForeignObj(value)) {
            Object object = machine.foreignObj(Machine.value(value));
            if(type.isInstance(object))
                return object;
            else throw new XMFToJavaTypeError("Foreign obj of wrong type",value,type);
        }
        return null;
    } 

}
