package com.ceteva.client;


public interface Droppable extends ComponentWithIdentity {

	public boolean dropEnabled();
	
	// Check if drop has already been enabled (otherwise SWTError is thrown). If not,
	// call setDroppable
	public void enableDrop();
	
	public void setDroppable();
	
}

