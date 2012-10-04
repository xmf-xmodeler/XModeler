package com.ceteva.menus.actions;

import org.eclipse.jface.action.Action;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;

public class MenuAction extends Action {
	
	EventHandler handler = null;
	String identity = null;
	
	public MenuAction(String text,String identity,EventHandler handler) {
		super(text);
		this.identity = identity;
		this.handler = handler;
		this.setId(identity);
	}
	
	public void run() {
		Message m = handler.newMessage("menuSelected",1);
		Value v1 = new Value(identity);
		m.args[0] = v1;
		handler.raiseEvent(m);
	}
}