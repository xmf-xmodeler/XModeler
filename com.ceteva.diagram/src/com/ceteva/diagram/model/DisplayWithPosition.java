package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Point;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

public abstract class DisplayWithPosition extends Display {
	
	Point location;
	
	public DisplayWithPosition(ClientElement parent,EventHandler handler,String identity,Point location) {
		super(parent,handler,identity);
		this.location = location;
	}
	
	public Point getLocation() {
		return location;
	}
	
	public void move(Point location) {
		this.location = location;
		if(isRendering())
		  firePropertyChange("locationSize",null, null);
	}
	
	public boolean processMessage(Message message) {
	   if(message.hasName("move") && message.args[0].hasStrValue(identity) && message.arity == 3) {
	   	 int newX = message.args[1].intValue;
	   	 int newY = message.args[2].intValue;
	   	 move(new Point(newX,newY));
	   	 return true;	
	   }
	   return super.processMessage(message);
	}
	
	public void synchronise(Element element) {
	   location.x = element.getInteger("x");
	   location.y = element.getInteger("y");
	   super.synchronise(element);
	}
}