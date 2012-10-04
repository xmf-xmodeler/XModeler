package com.ceteva.diagram.command;

import XOS.Message;
import XOS.Value;

import com.ceteva.diagram.model.CommandEvent;

public class DeleteCommand extends org.eclipse.gef.commands.Command {

  String identity = "";
  CommandEvent model = null;
  
  public DeleteCommand(String identity,CommandEvent model) {
	this.identity = identity;
	this.model = model;
  }

  public void execute() {
  	Message m = model.handler.newMessage("delete",1);
	Value v1 = new Value(identity);
	m.args[0] = v1;
	model.handler.raiseEvent(m);
  }

  public void redo() {
  }
  
  public void undo() {
  }

}