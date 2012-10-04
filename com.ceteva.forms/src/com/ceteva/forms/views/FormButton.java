package com.ceteva.forms.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;

class FormButton extends FormElement {
	
	Button button = null;
	
	public FormButton(Composite parent,String identity,EventHandler handler) {
	  super(identity);
	  button = new Button(parent,SWT.PUSH);
	  addEventHandler();
	  this.handler = handler;
	}
	
	public Control getControl() {
	  return button;
	}
	
	public void setText(String text) {
	  button.setText(text);	
	}
	
	public void setBounds(int x,int y,int width,int height) {
	  button.setBounds(x,y,width,height);	
	}
	
	public void addEventHandler() {
	  button.addListener(SWT.Selection, new Listener() {
	    public void handleEvent (Event e) {
	      Message m = handler.newMessage("buttonPressed",1);
		  Value v = new Value(getIdentity());
		  m.args[0] = v;
		  raiseEvent(m);				
		}});
	}
	
	public Value processCall(Message message) {
		return null;
	}
	
	public boolean processMessage(Message message) {
		if(message.arity >= 1) {
		  if(message.args[0].hasStrValue(getIdentity()))
		    return ComponentCommandHandler.processMessage(button,message);
		}
		return super.processMessage(message);
	}
}