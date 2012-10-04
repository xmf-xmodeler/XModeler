package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

public class Line extends Display {
	
  private Point start;		// start of the line
  private Point end;		// end of the line
  private RGB color;		// line colour
	
  public Line(ClientElement parent,EventHandler handler,String identity,Point start,Point end,RGB color) {
  	super(parent,handler,identity);
  	this.start = start;
  	this.end = end;
  	this.color = color;
  }
  
  public Line(ClientElement parent,EventHandler handler,String identity,int x1,int y1,int x2,int y2,RGB color) {
  	this(parent,handler,identity,new Point(x1,y1),new Point (x2,y2),color);
  }
  
  public Line(ClientElement parent,EventHandler handler,String identity,int x1,int y1,int x2,int y2) {
	this(parent,handler,identity,new Point(x1,y1),new Point (x2,y2),null);
  }
  
  public void delete() {
  	super.delete();
  	((Container)parent).removeDisplay(this);
  }
  
  public RGB getColor() {
    return color;
  }
  
  public Point getStart() {
  	return start;
  }
  
  public Point getEnd() {
  	return end;
  }
  
  public void resize(Point start,Point end) {
	this.start = start;
	this.end = end;
	if(isRendering())
	  firePropertyChange("locationSize",null, null);
  }

  public boolean processMessage(Message message) {
    if(message.hasName("resizeLine") && message.args[0].hasStrValue(identity) && message.arity == 5) {
	  int x1 = message.args[1].intValue;
	  int y1 = message.args[2].intValue;
	  int x2 = message.args[3].intValue;
	  int y2 = message.args[4].intValue;
	  resize(new Point(x1,y1),new Point(x2,y2));
	  return true;	
    }
	if(message.hasName("setColor") && message.args[0].hasStrValue(identity) && message.arity == 4) {
      int red = message.args[1].intValue;
      int green = message.args[2].intValue;
	  int blue = message.args[3].intValue;
	  setColor(red,green,blue);
	  return true;
	}
    return false;
  }
  
  public void setColor(int red,int green,int blue) {
    color = ModelFactory.getColor(red,green,blue);
	if(isRendering())
	  firePropertyChange("color",null, null); 
  }
  
  public void synchronise(Element line) {
	int x1 = line.getInteger("x1"); 
	int y1 = line.getInteger("y1");
	int x2 = line.getInteger("x2"); 
	int y2 = line.getInteger("y2");
	start = new Point(x1,y1);
	end = new Point(x2,y2);
	super.synchronise(line);
  }
}