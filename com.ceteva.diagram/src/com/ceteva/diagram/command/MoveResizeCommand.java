package com.ceteva.diagram.command;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import com.ceteva.diagram.model.Node;

public class MoveResizeCommand extends Command
{
  Node model = null;
  Point location = null;
  Dimension size = null;

  public static int count=0;
  
  public MoveResizeCommand(Node model,Point location,Dimension size) {
  	this.model = model;
    this.location = location;
    this.size = size;
  }
 
  public void execute() {
  	model.moveResize(location,size);
  }

  public void redo() {
  }

  public void undo() {
  }
}