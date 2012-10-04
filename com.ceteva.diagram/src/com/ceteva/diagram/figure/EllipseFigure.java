package com.ceteva.diagram.figure;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

public class EllipseFigure extends Ellipse {
	
  boolean outline;	
	
  public EllipseFigure(Point location,Dimension size,boolean outline) {
  	setLocation(location);
    setSize(size);
    this.outline = outline;  	
  }
  
  protected boolean useLocalCoordinates() {
	return true;
  }
  
  protected void outlineShape(Graphics graphics){
    if(outline)
      super.outlineShape(graphics);	
  }	
}