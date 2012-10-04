package com.ceteva.diagram.model;

import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

public class Container extends DisplayWithDimension {

  Vector displays = new Vector();

  public Container(ClientElement parent,EventHandler handler,String identity,Point location,Dimension size,RGB lineColor,RGB fillColor) {
	super(parent,handler,identity,location,size,lineColor,fillColor);
  } 
  
  public void addDisplay(Display d) {
	 displays.addElement(d);
	 if(isRendering())
	   firePropertyChange("displayChange",null,null);	
  }
  
  public void close() {
	  for(int i=0;i<displays.size();i++) {
	    Display display = (Display)displays.elementAt(i);
	    display.close();
	  }
  }
  
  public void dispose() {
	 super.dispose();
	 for(int i=0;i<displays.size();i++) {
	    Display d = (Display)displays.elementAt(i);
	    d.dispose();
	 }
  }
  
  public Vector getContents() {
	return displays;
  }
  
  public boolean identityExists(String identity) {
    if(this.identity.equals(identity))
        return true;
  	for(int i=0;i<displays.size();i++) {
  	  Display d = (Display)displays.elementAt(i);
  	  if(d.identityExists(identity))
  	  	return true;
  	}
  	return false;
  }
  
  public boolean processMessage(Message message) {
	 if(message.hasName("newBox") && message.args[0].hasStrValue(identity)) {
	   addDisplay(ModelFactory.newBox(this,handler,message));
	   return true;
	 }
	 else if(message.hasName("newText") && message.args[0].hasStrValue(identity)) {
	   addDisplay(ModelFactory.newText(this,handler,message));
	   return true;
	 }
	 else if(message.hasName("newMultilineText") && message.args[0].hasStrValue(identity)) {
	   addDisplay(ModelFactory.newMultilineText(this,handler,message));	
	   return true;
	 }
	 else if(message.hasName("newImage") && message.args[0].hasStrValue(identity)) {
	   addDisplay(ModelFactory.newImage(this,handler,message));
	   return true;
	 }
	 else if(message.hasName("newGroup") && message.args[0].hasStrValue(identity)) {
	   addDisplay(ModelFactory.newGroup(this,handler,message));
	   return true;
	 }
	 else if(message.hasName("newLine") && message.args[0].hasStrValue(identity)) {
	   addDisplay(ModelFactory.newLine(this,handler,message));
	   return true;
	 }
	 else if(message.hasName("newEllipse") && message.args[0].hasStrValue(identity)) {
	   addDisplay(ModelFactory.newEllipse(this,handler,message));
	   return true;
	 }
	 else if(message.hasName("newShape") && message.args[0].hasStrValue(identity)) {
	   addDisplay(ModelFactory.newShape(this,handler,message));
	   return true;
	 }
	 return super.processMessage(message);
   }
  
   public void removeDisplay(Display d) {
	 displays.removeElement(d);
	 if(isRendering())
	   firePropertyChange("displayChange",null,null);	
   }
   
   public void stopRender() {
	 setRender(false);
	 render(false);
   }
	
   public void startRender() {
	 setRender(true);
	 render(true);
	 if(isRendering())
	   firePropertyChange("startRender",null,null);
   }
	
   public void render(boolean render) {
	 for(int i=0;i<displays.size();i++) {
	   Display display = (Display)displays.elementAt(i);
	   if(!render)
		 display.stopRender();
	   else 
		 display.startRender();
	 }
   }
   
   public void refreshZoom() {
	 for(int i=0;i<displays.size();i++) {
		Display display = (Display)displays.elementAt(i);
		display.refreshZoom();
	 }
   }
   
   public void setEventHandler(EventHandler handler) {
	 super.setEventHandler(handler);
	 for(int i=0;i<displays.size();i++) {
	   ((Display)displays.elementAt(i)).setEventHandler(handler);	
	 }
   }
   
   public void synchronise(Element container) {
	 // System.out.println("Synchronising container");
	 ContainerSynchroniser.synchronise(this,container);  
   }
}