package com.ceteva.client;


public interface Draggable extends ComponentWithIdentity {

	public boolean dragEnabled();
	
	// Check if drag has already been enabled (otherwise SWTError is thrown). If not,
	// call setDraggable
	public void enableDrag();
	
	public void setDraggable();
	
}

