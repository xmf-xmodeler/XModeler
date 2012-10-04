package com.ceteva.diagram.figure;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

public class ShapeFigure extends Shape {

  Vector points;
  boolean outline;
	
  public ShapeFigure(Vector points,boolean outline) {
  	this.points = points;
  	this.outline = outline;
  }
  
  public void refresh(Vector points,boolean outline) {
  	this.points = points;
  	this.outline = outline;
    this.repaint();
  }
  
  protected void fillShape(Graphics graphics){
  	PointList list = new PointList();
  	Iterator it = points.iterator();
  	while(it.hasNext()) {
  	  list.addPoint((Point)it.next());
  	}
	graphics.fillPolygon(list);
  }
  
  protected void outlineShape(Graphics graphics){
    if(outline) {
      Point firstPoint = (Point)points.elementAt(0);
      Point lastPoint = firstPoint;
      Iterator it = points.subList(1,points.size()).iterator();
      while(it.hasNext()) {
      	Point nextPoint = (Point)it.next();
      	graphics.drawLine(lastPoint,nextPoint);
      	lastPoint = nextPoint;
      }
      graphics.drawLine(lastPoint,firstPoint);
    }
  }
}