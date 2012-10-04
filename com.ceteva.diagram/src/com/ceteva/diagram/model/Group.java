package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.SharedMessages;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

public class Group extends AbstractDiagram {

    String nestedZoom = "100";
    private boolean isTopLevel = false;

	public Group(ClientElement parent,EventHandler handler,String identity,Point location,Dimension size) {
		super(parent,handler, identity, location, size);
	}

	public Group(ClientElement parent,EventHandler handler,String identity,int x,int y,int width,int height) {
		this(parent,handler,identity,new Point(x, y),new Dimension(width, height));
	}
	
	public String getNestedZoom() {
		return nestedZoom;
	}
	
	public boolean isTopLevel() {
		return isTopLevel;
	}

	public boolean processMessage(Message message) {
	  if(super.processMessage(message))
	    return true;
	  if(message.hasName("zoomIn") && message.args[0].hasStrValue(identity) && message.arity == 1) {
		zoomIn();
		return true;
	  }
	  if(message.hasName("zoomOut") && message.args[0].hasStrValue(identity) && message.arity == 1) {
		zoomOut();
		return true;
	  }
	  if(message.hasName("nestedZoomTo") && message.args[0].hasStrValue(identity) && message.arity == 2) {
		int percent = message.args[1].intValue;
		if(percent == -1)
		  zoomToFit();
		else
		  nestedZoomTo(percent);
		return true;
	  }
	  return false;
	}
	
	public void synchronise(Element element) {
	  String nestedZoom = element.getString("nestedZoom");
      this.nestedZoom = nestedZoom;
      super.synchronise(element);
	}

	
	public void zoomIn() {
	  ClientElement parent = getParent();
	  while(!(parent instanceof Diagram))
	    parent = parent.getParent();
	  Diagram diagram = (Diagram)parent;
	  diagram.zoomTo(this,true);
	}
	
	public void zoomOut() {
	  ClientElement parent = getParent();
	  while(!(parent instanceof Diagram))
	    parent = parent.getParent();
	  ClientElement levelUp = getParent();
	  while(!(levelUp instanceof AbstractDiagram))
		levelUp = levelUp.getParent();
	  Diagram diagram = (Diagram)parent;
	  AbstractDiagram target = (AbstractDiagram)levelUp;
	  if(diagram.zoomTo(target,true))
	    diagram.refreshZoom();
	}
	
	public void nestedZoomTo(int percent) {	
	  nestedZoom = new Integer(percent).toString();
	  if(isRendering())
	    firePropertyChange("zoom",null, null);
	  else
	    queuedZoom = true;
	}
	
	public void setTopLevel(boolean isTopLevel) {
		this.isTopLevel = isTopLevel;
	}
	
	public void zoomToFit() {
	  nestedZoom = SharedMessages.FitAllAction_Label;
	  if(isRendering())
	    firePropertyChange("zoom",null, null);
	  else
		queuedZoom = true;
	}
}