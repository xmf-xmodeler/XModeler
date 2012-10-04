package com.ceteva.forms.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Control;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;

class FormCheckBox extends FormElement {

  Button check = null;
  
  public FormCheckBox(Composite parent,String identity,EventHandler handler) {
	super(identity);
  	check = new Button(parent,SWT.CHECK);
  	check.setSize(20,20);
  	check.setAlignment(SWT.CENTER);
  	this.handler = handler;
  	addListener();
  }

  public Control getControl() {
	return check;
  }
  
  public void addListener() {
	check.addListener(SWT.Selection, new Listener() {
		public void handleEvent(Event e) {
		  Message m = handler.newMessage("setBoolean",2);
		  Value v1 = new Value(getIdentity());
		  Value v2 = new Value(check.getSelection());
		  m.args[0] = v1;
		  m.args[1] = v2;
		  check.setSelection(!check.getSelection());
		  raiseEvent(m);
		}
	 });	
  }
  
  public void setLocation(Point position) {
  	check.setLocation(position);
  }
  
  public void setSelected(boolean selected) {
  	check.setSelection(selected);
  }	
  
  public Value processCall(Message message) {
		return null;
  }
  
  public boolean processMessage(Message message) {
	if(message.arity>=1) {
	  if(message.args[0].hasStrValue(getIdentity())) {
		if(message.hasName("check") && message.arity==1) {
		  check.setSelection(true);
		  return true;
		}
		if(message.hasName("uncheck") && message.arity==1) {
		  check.setSelection(false);
		  return true;
		}
		else {
		  if(ComponentCommandHandler.processMessage(check,message))
		    return true;
		}	
	  }	
	}
	return super.processMessage(message);
  }
}