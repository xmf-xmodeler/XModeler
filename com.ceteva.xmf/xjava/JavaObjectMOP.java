package xjava;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import Engine.Machine;

public class JavaObjectMOP implements AlienObjectMOP {
	
	String error = "";

	public int canSend(Machine machine, Object object, String message, int arity) {
    	Method method = getMethod(object, message, arity);
        if(method != null)
          return 0;
        return -1;
	}

	public String getError() {
		return error;
	}

	public int getSlot(Machine machine, Object obj, String name) {
		return -1;
	}
	
    public static Method getMethod(Object target, String message, int arity) {
        Class c = target.getClass();
        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            boolean nameMatches = message.equals(method.getName());
            boolean arityMatches = method.getParameterTypes().length == arity;
            if (nameMatches && arityMatches && !JavaTranslator.isStatic(method) && JavaTranslator.isPublic(method))
                return method;
        }
        if (target instanceof Class) {
            c = (Class) target;
            methods = c.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                boolean nameMatches = message.equals(method.getName());
                boolean arityMatches = method.getParameterTypes().length == arity;
                if (nameMatches && arityMatches && JavaTranslator.isStatic(method) && JavaTranslator.isPublic(method))
                    return method;
            }
        }
        return null;
    }

	public int hasSlot(Object object, String name) {
		
		// Currently no provision for getting slots on Java objects
		
		return -1;
	}

	public int send(Machine machine, Object target, String message, int args) {
        int arity = machine.consLength(args);
        Method method = getMethod(target, message, arity);
        
        if (method == null) {
            error = "The operation " + message + " does not exist.";
            System.out.println(error);
            return -1;
        } else
            try {
                Class[] types = method.getParameterTypes();
                Object[] values = JavaTranslator.mapXMFArgs(machine, types, args);
                return JavaTranslator.mapJavaValue(machine, method.invoke(target, values));
            } catch (IllegalArgumentException e) {
                error = e.toString();
                System.out.println(error);
                return -1;
            } catch (IllegalAccessException e) {
                error = e.toString();
                System.out.println(error);
                return -1;
            } catch (InvocationTargetException e) {
                error = e.getCause().toString();
                System.out.println(error);
                return -1;
            } catch(XMFToJavaTypeError e) {
                error =  e.getMessage();
                System.out.println(error);
                return -1;
            }
	}

	public void setSlot(Machine machine, Object object, String name, int value) {
		// TODO Auto-generated method stub
	}

}
