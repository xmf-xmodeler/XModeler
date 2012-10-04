package com.ceteva.text.actions;

import org.eclipse.jface.action.Action;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.text.texteditor.TextEditor;

public class MenuAction extends Action {
	
	TextEditor textEditor = null;
	String parentId;
	String identity;
	String label;
	
	public MenuAction(TextEditor textEditor,String parentId,String identity,String label) {
	  super(label);
	  this.textEditor = textEditor;
	  this.parentId = parentId;
	  this.identity = identity;
	  this.label = label;
	  this.setId(identity);
	}
	
	public void run() {
	  if(textEditor.getEventHandler()!=null) {
        EventHandler handler = textEditor.getEventHandler();
        Message m = handler.newMessage("rightClickMenuSelected",1);
		Value v = new Value(identity);
		m.args[0] = v;
		handler.raiseEvent(m);
	  }
	}
	
	public String getIdentity() {
	  return identity;
	}
	
	public String getParentId() {
	  return parentId;
	}
}
