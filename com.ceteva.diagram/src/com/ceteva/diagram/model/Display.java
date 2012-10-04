package com.ceteva.diagram.model;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;

public abstract class Display extends CommandEvent {
	
  private boolean hidden = false;	

  public Display(ClientElement parent,EventHandler handler,String identity) {
  	super(parent,handler,identity);	
  }
  
  public void close() {
  }
  
  public boolean hidden() {
      return hidden;
  }
  
  public void hide() {
      hidden = true;
      if (isRendering())
        firePropertyChange("visibilityChange", null, null);
  }
  
  public boolean processDelete(String identity) {
  	return false;
  }
  
  public boolean processMessage(Message message) {
    if (message.hasName("hide") && message.args[0].hasStrValue(identity) && message.arity == 1) {
      hide();
      return true;
    }
    if (message.hasName("show") && message.args[0].hasStrValue(identity) && message.arity == 1) {
      show();
      return true;
    }
    return super.processMessage(message);
  }
  
  public void refreshZoom() {
  }
  
  public void selected(int clicks) {
	  Message m = handler.newMessage("selected", 2);
      Value v1 = new Value(identity);
      Value v2 = new Value(clicks);
      m.args[0] = v1;
      m.args[1] = v2;
      handler.raiseEvent(m);
  }
  
  public void show() {
    hidden = false;
    if (isRendering())
      firePropertyChange("visibilityChange", null, null);
  }
}