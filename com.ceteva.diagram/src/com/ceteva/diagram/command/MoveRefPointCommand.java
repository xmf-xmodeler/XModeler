package com.ceteva.diagram.command;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import com.ceteva.diagram.model.Edge;

public class MoveRefPointCommand extends Command
{
  Edge model = null;
  Point location = null;
  
  public MoveRefPointCommand(Edge model,Point location) {
	this.model = model;
	this.location = location;
  }
 
  public void execute() {
  	model.setRefPoint(location);
  }
}