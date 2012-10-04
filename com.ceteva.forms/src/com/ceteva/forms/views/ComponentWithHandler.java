package com.ceteva.forms.views;

import XOS.Message;

import com.ceteva.client.EventHandler;

class ComponentWithHandler {

	public EventHandler handler = null;
	boolean eventsEnabled = true;
   
   public void disableEvents() {
      eventsEnabled = false;
   }
   
   void raiseEvent(Message m) {
      if (eventsEnabled)
         handler.raiseEvent(m);
   }

}