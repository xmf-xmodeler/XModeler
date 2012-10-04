package com.ceteva.mosaic.actions;

import org.eclipse.jface.action.Action;

import XOS.Message;

import com.ceteva.client.EventHandler;

public class Exit extends Action {
	
	private EventHandler handler;
    
    public Exit() {
      super("E&xit");
      this.setId("exit");
	}
	
	public void run() {
      Message m = handler.newMessage("shutdownRequest",0);
	  handler.raiseEvent(m);
	}
	
	public void setEventHandler(EventHandler handler) {
      this.handler = handler;
	}
}
