package com.ceteva.diagram.command;

import org.eclipse.draw2d.geometry.Point;

import XOS.Message;
import XOS.Value;

import com.ceteva.diagram.model.Edge;

public class CreateWaypointCommand extends org.eclipse.gef.commands.Command {

  Edge edge = null;
  int index;
  Point location;
  
  public CreateWaypointCommand(Edge edge,int index,Point location) {
	this.edge = edge;
	this.index = index;
	this.location = location;
  }

  public void execute() {
	edge.addDummyWaypoint(index,location);
  	Message m = edge.handler.newMessage("newWaypoint",4);
	Value v1 = new Value(edge.getIdentity());
  	Value v2 = new Value(index);
	Value v3 = new Value(location.x);
	Value v4 = new Value(location.y);
	m.args[0] = v1;
	m.args[1] = v2;
	m.args[2] = v3;
	m.args[3] = v4;
	edge.handler.raiseEvent(m);
  }

  public void redo() {
  }
  
  public void undo() {
  }
}