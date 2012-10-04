package com.ceteva.diagram.command;

import org.eclipse.draw2d.geometry.Point;

import XOS.Message;
import XOS.Value;

import com.ceteva.diagram.model.Edge;

public class WaypointDeleteCommand extends org.eclipse.gef.commands.Command {

  Edge edge = null;
  String identity;
  Point location;
  
  public WaypointDeleteCommand(Edge edge,String identity,Point location) {
	this.edge = edge;
	this.identity = identity;
	this.location = location;
  }

  public void execute() {
  	Message m = edge.handler.newMessage("deleteWaypoint",1);
	Value v1 = new Value(identity);
	m.args[0] = v1;
	edge.setRefPoint(location);
	edge.handler.raiseEvent(m);
  }

  public void redo() {
  }
  
  public void undo() {
  }
}