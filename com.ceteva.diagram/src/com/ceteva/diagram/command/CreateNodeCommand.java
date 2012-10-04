package com.ceteva.diagram.command;

import org.eclipse.draw2d.geometry.Point;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.diagram.editPart.CommandEventEditPart;
import com.ceteva.diagram.model.CommandEvent;

public class CreateNodeCommand extends org.eclipse.gef.commands.Command {

  CommandEventEditPart parent;
  String toolIdentity;
  Point location;
  
  public CreateNodeCommand(CommandEventEditPart parent,String toolIdentity,Point location) {
    this.parent = parent;
    this.toolIdentity = toolIdentity;
    this.location = location;
  }

  public void execute() {
  	EventHandler handler = ((CommandEvent)parent.getModel()).handler;
  	Message m = handler.newMessage("newNode",4);
	Value v1 = new Value(toolIdentity);
  	Value v2 = new Value(parent.getModelIdentity());
	Value v3 = new Value(location.x);
	Value v4 = new Value(location.y);
	m.args[0] = v1;
	m.args[1] = v2;
	m.args[2] = v3;
	m.args[3] = v4;
	handler.raiseEvent(m);
  }
}