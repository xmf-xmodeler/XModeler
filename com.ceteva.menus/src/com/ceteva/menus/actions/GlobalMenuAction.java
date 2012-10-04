package com.ceteva.menus.actions;

import java.util.Vector;

import org.eclipse.jface.action.Action;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.menus.MenusClient;

public class GlobalMenuAction extends Action {
	
	private Vector menuIdentities;
	private Vector identities;
	
	public GlobalMenuAction(String text,Vector menuIdentities,Vector identities,boolean enabled) {
		super(text);
		this.menuIdentities = menuIdentities;
		this.identities = identities;
		this.setEnabled(enabled);
		
		// Need to set this for key bindings
		
		//if (identities.size()>1)
		//	System.out.println(menuIdentities + " -> " + identities);
		
		if(!identities.isEmpty()) {
			
			// We assume that the keybinding for the first element
			// is the same as for all the elements bound within this
			// action
			
			String identity = (String)menuIdentities.elementAt(0);
			this.setId(identity);
		}
	}

	public void run() {
		EventHandler handler = MenusClient.handler;
		int size = identities.size();
		Message m = handler.newMessage("rightClickMenuSelected",1);
		Value[] ids = new Value[size * 2];
		for(int i=0;i<size;i++) {
			String menuIdentity = (String)menuIdentities.elementAt(i);
			ids[i*2] = new Value(menuIdentity);
			String identity = (String)identities.elementAt(i);
			ids[i*2+1] = new Value(identity);
		}
		m.args[0] = new Value(ids);
		handler.raiseEvent(m);
	}
	
	/* public void run() {
		EventHandler handler = MenusClient.handler;
		int numberOfIdentities = identities.size();
		Message m = handler.newMessage("rightClickMenuSelected",numberOfIdentities*2);
		for(int i=0;i<numberOfIdentities;i++) {
			String menuIdentity = (String)menuIdentities.elementAt(i);
			m.args[i*2] = new Value(menuIdentity);
			String identity = (String)identities.elementAt(i);
			m.args[i*2+1] = new Value(identity);
		}
		handler.raiseEvent(m);
	} */
	
}