package xjava;

import java.util.Enumeration;
import java.util.Hashtable;

import Engine.Machine;

public class AlienObject {
	
	// The AlienObject is a singleton and is responsible for distributing MOP calls
	// on Java objects to the appropriate MOP implementation
	
	// Binds AlienObject types to an associated AlienObjectMOP
	
	private static AlienObjectMOP defaultMOP = new JavaObjectMOP(); 
	private static Hashtable registry = new Hashtable();
	
	public static int canSend(Machine machine, Object target, String message, int arity) {
		return getMOP(target).canSend(machine, target, message, arity);
	}

	public static AlienObjectMOP getMOP(Object object) {
		Enumeration k = registry.keys();
		while(k.hasMoreElements()) {
		  Class c = (Class)k.nextElement();
		  if(c.isInstance(object))
			  return (AlienObjectMOP)registry.get(c);
		}
		return defaultMOP;
	}
	
	public static int forName(Machine machine,String plugin,String name) throws ClassNotFoundException {
		Class c = AOToolRegistry.getClass(plugin, name);
		return machine.newAlienObj(c);
	}
	
	public static String getError() {
		return "";
	}
	
	public static int getSlot(Machine machine,Object object,String name) {
		return getMOP(object).getSlot(machine, object, name);
	}
	
	public static int hasSlot(Object object,String name) {
		return getMOP(object).hasSlot(object, name);
	}
	
	public static void register(Object type,AlienObjectMOP mop) {
		registry.put(type, mop);
	}
	
	public static int send(Machine machine, Object target, String message, int args) {
		return getMOP(target).send(machine, target, message, args);
	}
	
	public static void setSlot(Machine machine, Object object, String name, int value) {
		getMOP(object).setSlot(machine, object, name, value);
	}

}
