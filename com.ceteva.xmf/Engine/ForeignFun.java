package Engine;

import java.lang.reflect.*;
import java.io.*;

public class ForeignFun implements Serializable {

	// A foreign function is implemented as a Java method. The method
	// should be defined as a static class member expecting one argument
	// that is the current machine state. The method is completely
	// responsible for dealing with the current stack frame which contains
	// its arguments.

	private static final long serialVersionUID = 1L;
    
    static Class[] types = new Class[] { Machine.class };
	static Object[] args = new Object[1];

	private String className;
	private String methodName;
	private Method method;
	private int arity;

	public ForeignFun(String className, String methodName, int arity) {
		init(className, methodName, arity);
	}

	public void init(String className, String methodName, int arity) {
		try {
			Class c = Class.forName(className);
			Method method = c.getDeclaredMethod(methodName, types);
			this.className = className;
			this.methodName = methodName;
			this.method = method;
			this.arity = arity;
		} catch (ClassNotFoundException cnf) {
			throw new Error("Cannot find class " + className);
		} catch (NoSuchMethodException nsm) {
			throw new Error("Cannot find method " + methodName + " of " + className);
		}
	}
	
	public String className() {
		return className;
	}

	public String name() {
		return methodName;
	}

	public int arity() {
		return arity;
	}

	public void invoke(Machine machine) {
		try {
			args[0] = machine;
			method.invoke(null, args);
		} catch (InvocationTargetException ite) {
			if (machine.stackDump)
				ite.printStackTrace();
			throw new Error(ite.getTargetException().getMessage());
		} catch (IllegalAccessException iae) {
			if (machine.stackDump)
				iae.printStackTrace();
			throw new Error(iae.getMessage());
		}
	}

	private void writeObject(ObjectOutputStream out) {
		try {
			out.writeObject(className);
			out.writeObject(methodName);
			out.writeInt(arity);
		} catch (IOException ioe) {
			throw new Error(ioe.getMessage());
		}
	}

	private void readObject(ObjectInputStream in) {
		try {
			String className = (String) in.readObject();
			String methodName = (String) in.readObject();
			int arity = in.readInt();
			init(className, methodName, arity);
		} catch (ClassNotFoundException cnf) {
			throw new Error(cnf.getMessage());
		} catch (IOException ioe) {
			throw new Error(ioe.getMessage());
		}
	}
}
