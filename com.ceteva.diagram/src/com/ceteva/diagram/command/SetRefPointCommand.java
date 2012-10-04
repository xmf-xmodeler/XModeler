package com.ceteva.diagram.command;

import org.eclipse.draw2d.geometry.Point;

import com.ceteva.diagram.model.Edge;

public class SetRefPointCommand extends org.eclipse.gef.commands.Command {

  Edge edge;
  Point position;
  
  public SetRefPointCommand(Edge edge,Point position) {
  	this.edge = edge;
  	this.position = position;
  }
  
  public void execute() {
  	edge.setRefPoint(position);
  }

  public void redo() {
  }
  
  public void undo() {
  }
}
