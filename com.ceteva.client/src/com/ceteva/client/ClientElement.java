package com.ceteva.client;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.xml.Element;

public class ClientElement implements Commandable, ComponentWithIdentity {

	public String identity = "";
	public EventHandler handler = null;
	public ClientElement parent = null;
	
	public ClientElement(ClientElement parent,EventHandler handler,String identity) {
		this.parent = parent;
		this.handler = handler;
		this.identity = identity;
		IdManager.put(identity,this);
	}
	
	public void dispose() {
		IdManager.remove(identity);
	}
	
	public void delete() {
		dispose();
	}
	
	public String getIdentity() {
		return identity;
	}
	
	public ClientElement getParent() {
		return parent;
	}
	
	public boolean processMessage(Message message) {
		if(message.arity > 0) {
		  if (message.hasName("delete") && message.args[0].hasStrValue(identity) && message.arity == 1) {
			delete();
			return true;
		  }
		}
		return false;
	}
	
	public Value processCall(Message message) {
		return null;
	}
	
	public void synchronise(Element xml) {
	}
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;	
	}
	
	public void setIdentity(String identity) {
		this.identity = identity;
	}
}