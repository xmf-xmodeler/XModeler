package xjava;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import Engine.Machine;

public class XJ extends JavaTranslator {

    public static String   error  = "";            // Set if an error code is returned (-1).

    public static XJLoader loader = new XJLoader(); // Used to load classes via an augmented CLASSPATH.

    public static int forName(Machine machine, String[] paths, String name) {

        // Access the class with the supplied name and allocate
        // it as a foreign object. Return the foreign object or
        // -1 if we fail.

        try {
            String[] oldPaths = loader.paths();
            loader.setPaths(paths);
            Class c = loader.loadClass(name);
            if(oldPaths.length > 0) loader.setPaths(oldPaths);
            return machine.newForeignObj(c);
        } catch (ClassNotFoundException e) {
            error = "Class " + name + " not found";
            return -1;
        }
    }

    public static Field getField(Object object, String name) {
        Class c = object.getClass();
        Field[] fields = c.getFields();
        Field field = null;
        for (int i = 0; i < fields.length && field == null; i++) {
            field = fields[i];
            boolean matches = name.equals(field.getName()) && !isStatic(field) && isPublic(field);
            if (!matches)
                field = null;
        }
        if (object instanceof Class) {
            c = (Class) object;
            fields = c.getFields();
            for (int i = 0; i < fields.length && field == null; i++) {
                field = fields[i];
                boolean matches = name.equals(field.getName()) && isStatic(field) && isPublic(field);
                if (!matches)
                    field = null;
            }
        }
        return field;
    }

    public static Method getMethod(Object target, String message, int arity) {
        Class c = target.getClass();
        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            boolean nameMatches = message.equals(method.getName());
            boolean arityMatches = method.getParameterTypes().length == arity;
            if (nameMatches && arityMatches && !isStatic(method) && isPublic(method))
                return method;
        }
        if (target instanceof Class) {
            c = (Class) target;
            methods = c.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                boolean nameMatches = message.equals(method.getName());
                boolean arityMatches = method.getParameterTypes().length == arity;
                if (nameMatches && arityMatches && isStatic(method) && isPublic(method))
                    return method;
            }
        }
        return null;
    }

    public static int getSlot(Machine machine, Object object, String name) {
        Field field = getField(object, name);
        try {
            Object value = field.get(object);
            return mapJavaValue(machine, value);
        } catch (IllegalArgumentException e) {
            error = e.toString();
            return -1;
        } catch (IllegalAccessException e) {
            error = e.toString();
            return -1;
        }

    }

    public static boolean hasSlot(Object object, String name) {

        // Return true when the object has a field name...

        Class c = object.getClass();
        Field[] fields = c.getFields();
        boolean found = false;
        for (int i = 0; i < fields.length && !found; i++) {
            Field field = fields[i];
            boolean isLegal = !isStatic(field) && isPublic(field);
            found = name.equals(field.getName()) & isLegal;
        }
        if (object instanceof Class && !found) {
            c = (Class) object;
            fields = c.getFields();
            for (int i = 0; i < fields.length && !found; i++) {
                Field field = fields[i];
                boolean isLegal = isStatic(field) && isPublic(field);
                found = isLegal && name.equals(field.getName());
            }
        }
        return found;
    }

    public static int send(Machine machine, Object target, String message, int args) {

        // Called when XMF wants to send a message to a Java object.
        // Look up a method defined by the class of the object and
        // translate the argument values before invoking the method.

        int arity = machine.consLength(args);
        Method method = getMethod(target, message, arity);
        if (method == null) {
            error = "Cannot find method named " + message;
            return -1;
        } else
            try {
                Class[] types = method.getParameterTypes();
                Object[] values = mapXMFArgs(machine, types, args);
                return mapJavaValue(machine, method.invoke(target, values));
            } catch (IllegalArgumentException e) {
                error = e.toString();
                return -1;
            } catch (IllegalAccessException e) {
                error = e.toString();
                return -1;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                error = e.getCause().toString();
                return -1;
            } catch(XMFToJavaTypeError e) {
                error =  e.getMessage();
                return -1;
            }
    }

    public static int setSlot(Machine machine, Object object, String name, int value) {
        Field field = getField(object, name);
        try {
            field.set(object, mapXMFValue(machine, field.getType(), value));
            return 0;
        } catch (IllegalArgumentException e) {
            error = e.getCause().toString();
            return -1;
        } catch (IllegalAccessException e) {
            error = e.getCause().toString();
            return -1;
        }
    }

    public static int slotNames(Machine machine, Object object) {

        // Return a sequence of symbols for the field names...

        Class c = object.getClass();
        Field[] fields = c.getFields();
        int slotNames = Machine.nilValue;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!isStatic(field) & isPublic(field)) {
                String name = field.getName();
                int symbol = machine.mkSymbol(name);
                slotNames = machine.mkCons(symbol, slotNames);
            }
        }

        // Add in the static field names...

        if (object instanceof Class) {
            c = (Class) object;
            fields = c.getFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (isStatic(field) && isPublic(field)) {
                    String name = field.getName();
                    int symbol = machine.mkSymbol(name);
                    slotNames = machine.mkCons(symbol, slotNames);
                }
            }
        }
        return slotNames;

    }

}
