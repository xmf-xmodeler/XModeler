package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

public abstract class DisplayWithDimension extends DisplayWithPosition {
	
	boolean fill = true;
	RGB fillColor = null;
	RGB foregroundColor = null;
	Dimension size;
	
	public DisplayWithDimension(ClientElement parent,EventHandler handler,String identity,Point location,Dimension size,RGB foregroundColor,RGB fillColor) {
		super(parent,handler,identity,location);
		this.size = size;
		this.foregroundColor = foregroundColor;
		this.fillColor = fillColor;
	}
	
	public RGB getFillColor() {
	    return fillColor;
	}
	
	public RGB getForegroundColor() {
	    return foregroundColor;
	}
	
	public Dimension getSize() {
		return size;
	}
	
	public boolean getfill() {
		return fill;
	}
	
	public void resize(Dimension size) {
		this.size = size;
		if(isRendering())
		  firePropertyChange("locationSize",null, null);
	}
	
	public boolean processMessage(Message message) {
	   if(message.hasName("resize") && message.args[0].hasStrValue(identity) && message.arity == 3) {
		 int width = message.args[1].intValue;
		 int height = message.args[2].intValue;
         resize(new Dimension(width,height));
		 return true;	
	   }
	   if(message.hasName("setFill") && message.args[0].hasStrValue(identity) && message.arity == 2) {
	   	 setFill(message.args[1].boolValue);
		 return true;	
	   }
	   if(message.hasName("setFillColor") && message.args[0].hasStrValue(identity) && message.arity == 4) {
		 int red = message.args[1].intValue;
	     int green = message.args[2].intValue;
	     int blue = message.args[3].intValue;
	     setFillColor(red,green,blue);
	     return true;
	   }
	   if(message.hasName("setLineColor") && message.args[0].hasStrValue(identity) && message.arity == 4) {
		 int red = message.args[1].intValue;
		 int green = message.args[2].intValue;
		 int blue = message.args[3].intValue;
		 setForegroundColor(red,green,blue);
		 return true;
	   }
	   return super.processMessage(message);
	}
	
	public void synchronise(Element element) {
	   size.width = element.getInteger("width");
	   size.height = element.getInteger("height");
	   super.synchronise(element);
	}
	
	public void setFill(boolean fill) {
		this.fill = fill;
		if(isRendering())
	      firePropertyChange("locationSize",null, null);
	}
	
	public void setFillColor(int red,int green,int blue) {
	    fillColor = ModelFactory.getColor(red,green,blue);
	    if(isRendering())
	      firePropertyChange("color",null, null); 
	}
	
	public void setForegroundColor(int red,int green,int blue) {
	    foregroundColor = ModelFactory.getColor(red,green,blue);
	    if(isRendering())
	      firePropertyChange("color",null, null); 
	}
}