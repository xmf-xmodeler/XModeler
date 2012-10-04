package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

public class Box extends Container {
  
  int cornerCurve;					// the extent to which the border is curved (0 = none)
  boolean top;
  boolean left;
  boolean right;
  boolean bottom;
  
  public Box(ClientElement parent,EventHandler handler,String identity,Point location,Dimension size,int borderCurve,boolean top,boolean right,boolean bottom,boolean left,RGB lineColor,RGB fillColor) {
  	super(parent,handler,identity,location,size,lineColor,fillColor);
    this.cornerCurve = borderCurve;
    this.top = top;
    this.left = left;
	this.bottom = bottom;
    this.right = right;
  }
  
  public Box(ClientElement parent,EventHandler handler,String identity,int x,int y,int width,int height,int borderCurve,boolean top,boolean right,boolean left,boolean bottom,RGB lineColor,RGB fillColor) {
  	this(parent,handler,identity,new Point(x,y),new Dimension(width,height),borderCurve,top,right,left,bottom,lineColor,fillColor);
  }
  
  public Box(ClientElement parent,EventHandler handler,String identity,int x,int y,int width,int height,int borderCurve,boolean top,boolean right,boolean left,boolean bottom) {
	  this(parent,handler,identity,new Point(x,y),new Dimension(width,height),borderCurve,top,right,left,bottom,null,null);
  }
  
  public void delete() {
  	super.delete();
  	((Container)parent).removeDisplay(this);
  }
  
  public int getBorderCurve() {
  	return cornerCurve;
  }
  
  public boolean processMessage(Message message) {
  	if(message.hasName("showEdges") && message.args[0].hasStrValue(identity)) {
      this.top = message.args[1].boolValue;
  	  this.bottom = message.args[2].boolValue;
  	  this.left = message.args[3].boolValue;
  	  this.right = message.args[4].boolValue;
  	  if(isRendering())
 	    firePropertyChange("containerSize",null,null);
  	  return true;
  	}
  	return super.processMessage(message);
  }
  
  public boolean showTop() {
  	return top;
  }
  
  public boolean showLeft() {
  	return left;
  }
  
  public boolean showRight() {
  	return right;
  }
  
  public boolean showBottom() {
  	return bottom;
  }
  
  public void synchronise(Element box) {
	cornerCurve = box.getInteger("cornerCurve");
	top = box.getBoolean("showTop");
	bottom = box.getBoolean("showBottom");
	left = box.getBoolean("showLeft");
	right = box.getBoolean("showRight");
	super.synchronise(box);
  }
}