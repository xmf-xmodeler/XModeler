// This is an SVN test

package com.ceteva.client;

import org.eclipse.swt.widgets.Display;

import XOS.Message;
import XOS.MessageHandler;
import XOS.MessagePacket;
import XOS.Value;

// This is a comment

public abstract class Client implements MessageHandler {
    
    private static MessageDisplayRunnable messageRunnable = new MessageDisplayRunnable();
    private static PacketDisplayRunnable packetRunnable = new PacketDisplayRunnable();
    private static CallDisplayRunnable callRunnable = new CallDisplayRunnable();
	
	// All events for this client are raised on a handler
	
	protected boolean debug = false;
	protected String name;
	protected EventHandler handler;
	
	public Client(String name) {
	  this.name = name;	
	}

	// When a message is received it is placed on the Mosaic
	// GUI thread synchronously.  If the command is not consumed
	// by the client then an error is reported on the GUI thread 
	
	public void sendMessage(final Message message) {
	    messageRunnable.setClient(this);
	    messageRunnable.setMessage(message);
	    Display.getDefault().syncExec(messageRunnable);
	}
	
	public void sendPacket(final MessagePacket packet) {
		packetRunnable.setClient(this);
		packetRunnable.setPacket(packet);
		Display.getDefault().syncExec(packetRunnable);
	}
	
	// When a call is received it is placed on the Mosaic GUI
	// thread synchronously since the result must be returned
	// to the caller.

	public Value callMessage(final Message message) {
		callRunnable.setClient(this);
		callRunnable.setMessage(message);
		Display.getDefault().syncExec(callRunnable);
		return callRunnable.getValue();
	}
	
	// XOS supplies each client with an event handler

	public void registerEventHandler(XOS.EventHandler handler) {
		this.handler = new EventHandler(name,handler);
		setEventHandler(this.handler);
	}
	
	// The following two methods should be overriden in concrete
	// implementations of Clients
	
	public abstract boolean processMessage(Message message);
	
	public abstract void setEventHandler(EventHandler handler);
	
	public Value processCall(Message message) {
		return null;
	}
}