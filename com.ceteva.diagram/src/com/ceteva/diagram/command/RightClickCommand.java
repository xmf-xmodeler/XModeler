package com.ceteva.diagram.command;

import org.eclipse.gef.commands.Command;

import XOS.Message;
import XOS.Value;

import com.ceteva.diagram.model.CommandEvent;

public class RightClickCommand extends Command
{
  String identity = "";
  CommandEvent model = null;

  public RightClickCommand(CommandEvent model,String identity) {
	this.identity = identity;
	this.model = model;
  }
 
  public void execute() {
  	Message m = model.handler.newMessage("rightClickMenuSelected",1);
	Value v1 = new Value(identity);
	m.args[0] = v1;
	model.handler.raiseEvent(m);
  }

  public void redo() {
  }

  public void undo() {
  }
}