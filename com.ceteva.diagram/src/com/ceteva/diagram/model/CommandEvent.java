package com.ceteva.diagram.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.diagram.DiagramClient;
import com.ceteva.diagram.editPart.ConnectionLayerManager;

public abstract class CommandEvent extends ClientElement {

	private boolean render = true;
	
	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	public CommandEvent(ClientElement parent,EventHandler handler,String identity) {
		super(parent,handler,identity);
		if(parent instanceof CommandEvent)
		  this.render = ((CommandEvent)parent).render;
	}

	public void addPropertyChangeListener(PropertyChangeListener l){
	  listeners.addPropertyChangeListener(l);
	}
	
	public boolean canFire() {
	  return listeners.getPropertyChangeListeners().length != 0;
	}
  
	public void removePropertyChangeListener(PropertyChangeListener l){
	  listeners.removePropertyChangeListener(l);
	}
  
	protected void firePropertyChange(String prop, Object old, Object newValue){
	  // if(!isRendering())
	  //  System.out.println("Warning: render off and firing - " + prop);
      listeners.firePropertyChange(prop, old, newValue); 
	}
	
	public ConnectionLayerManager getConnectionManager() {
	    return ((CommandEvent)parent).getConnectionManager();
	}
	
	public boolean identityExists(String identity) {
	  return this.identity.equals(identity);	
	}
	
	public boolean isRendering() {
	  return render && DiagramClient.isRendering();
	}
	
	public void fireStartRender() {
	  if(isRendering())
	    firePropertyChange("startRender",null,null);
	}
	
	public Value processCall(Message message) {
	  return null;
	}
	
	public void setRender(boolean render) {
		this.render = render;
	}
	
	public void stopRender() {
	  render = false;
	}
	
	public void startRender() {
	  render = true;
	  fireStartRender();
	}
}