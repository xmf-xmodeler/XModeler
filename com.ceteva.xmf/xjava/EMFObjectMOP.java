package xjava;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import Engine.Machine;

public class EMFObjectMOP implements AlienObjectMOP  {
	
	private String error = "";
	
	public int canSend(Machine machine, Object object, String message, int arity) {
		if(message.equals("allAttributes"))
		  return 0;
    	Method method = getMethod(object, message, arity);
        if(method != null)
          return 0;
        return -1;		
	}
	
	public String getError() {
		return error;
	}
	
	public int getSlot(Machine machine,Object obj,String name) {
		EObject object = (EObject)obj;
		EStructuralFeature sf = object.eClass().getEStructuralFeature(name);
		return JavaTranslator.mapJavaValue(machine,object.eGet(sf));
	}
	
	public static int getAttributes(Machine machine, Object target, String message, int args) {
		EObject object = (EObject)target;
		EList list = object.eClass().getEAllStructuralFeatures();
		String[] s = new String[list.size()];
		for(int i=0;i<list.size();i++) {
		  EStructuralFeature a = (EStructuralFeature)list.get(i);
		  s[i] = a.getName();
		}
		return JavaTranslator.mapJavaValue(machine,s);
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
	
	public int hasSlot(Object object,String name) {
		EObject obj = (EObject)object;
		EStructuralFeature sf = obj.eClass().getEStructuralFeature(name);
		if(sf != null)
		   return 0;
		return -1;
	}
    
    public int send(Machine machine, Object target, String message, int args) {
    	int arity = machine.consLength(args);
		if (message.equals("allAttributes")) {

			// the getAttributes call is hijacked
			
			return getAttributes(machine,target,message,args);

		} else {
			Method method = getMethod(target, message, arity);
			if (method == null) {
				error = "The operation " + message + " does not exist.";
				System.out.println(error);
				return -1;
			} else
				try {
					Class[] types = method.getParameterTypes();
					Object[] values = JavaTranslator.mapXMFArgs(machine, types,
							args);
					return JavaTranslator.mapJavaValue(machine, method.invoke(
							target, values));
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
				} catch (XMFToJavaTypeError e) {
					error = e.getMessage();
					System.out.println(error);
					return -1;
				}
		}
	}
	
	public void setSlot(Machine machine, Object object, String name, int value) {
		EObject obj = (EObject)object;
		EStructuralFeature sf = obj.eClass().getEStructuralFeature(name);
		Class type = sf.getEType().getInstanceClass();
	    Object val = JavaTranslator.mapXMFValue(machine, type, value);
	    obj.eSet(sf, val);
    }
	
    public String stringToString(Machine machine,int value) {
		if (machine.isString(value))
			return machine.valueToString(value);
		if (machine.isSymbol(value))
			return machine.valueToString(machine.symbolName(value));
		return "";
    }

}
