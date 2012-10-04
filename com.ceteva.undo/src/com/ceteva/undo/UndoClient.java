package com.ceteva.undo;

import XOS.Message;

import com.ceteva.client.Client;
import com.ceteva.client.EventHandler;
import com.ceteva.undo.actions.RedoAction;
import com.ceteva.undo.actions.UndoAction;

public class UndoClient extends Client {

	public static UndoAction undo = new UndoAction();
	public static RedoAction redo = new RedoAction();
	
	public UndoClient() {
	  super("com.ceteva.undo");	
	}
	
	public void setEventHandler(EventHandler handler) {
	  undo.registerEventHandler(handler);
	  redo.registerEventHandler(handler);
	}
	
	public boolean processMessage(Message message) {
	  if(message.hasName("enableUndo")) {
	  	undo.setEnabled(true);
	    return true;
	  }
	  if(message.hasName("disableUndo")) {
	  	undo.setEnabled(false);
	    return true;
	  }
      if(message.hasName("enableRedo")) {
  	    redo.setEnabled(true);
        return true;
      }
      if(message.hasName("disableRedo")) {
  	    redo.setEnabled(false);
        return true;
      }
      return false;
	}
}