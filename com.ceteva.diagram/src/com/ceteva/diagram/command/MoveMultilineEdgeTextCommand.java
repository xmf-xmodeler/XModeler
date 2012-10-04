package com.ceteva.diagram.command;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import com.ceteva.diagram.model.MultilineEdgeText;

public class MoveMultilineEdgeTextCommand extends Command
{
  MultilineEdgeText model = null;
  Point delta = null;
  Figure parent = null;
  
  public MoveMultilineEdgeTextCommand(MultilineEdgeText model,Figure parent,Point delta) {
	this.model = model;
	this.parent = parent;
	this.delta = delta;
  }
 
  public void execute() {
  	Point newLocation = model.getLocation().getCopy();
	parent.translateToAbsolute(newLocation);
	newLocation.translate(delta);
	parent.translateToRelative(newLocation);
	model.raiseMoveEvent(newLocation);
  }
}