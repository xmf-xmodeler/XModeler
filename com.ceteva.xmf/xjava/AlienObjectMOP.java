package xjava;

import Engine.Machine;

public interface AlienObjectMOP {
	
	// One of the object types the XMF VM can deal with is AlienObjects.  An AlienObject
	// is a Java object where MOP level calls to the object are handled natively in Java.
	// It is the responsibility of AlienObjectMOP implementations to handle MOP level calls
	// on behalf of Java objects.  An AlienObjectHandler should be registered with the 
	// AlienObject so that MOP calls are appropriately directed.
	
	public int canSend(Machine machine, Object object, String message, int arity);
	
	public String getError();
	
	public int getSlot(Machine machine,Object object,String name);
	
	public int hasSlot(Object object,String name);
	
	public int send(Machine machine, Object target, String message, int args);
	
	public void setSlot(Machine machine, Object object, String name, int value);

}
