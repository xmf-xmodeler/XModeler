package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;

public class Port extends DisplayWithDimension {
  
  public Port(ClientElement parent,EventHandler handler,String identity,Point location,Dimension size) {
  	super(parent,handler,identity,location,size,null,null);
  	this.location = location;
  	this.size = size;
  }
  
  public void dispose() {
	super.dispose();
	PortRegistry.removePort(identity);
  }
  
  public void delete() {
  	super.delete();
  	((Node)parent).removeDisplay(this);
  }
  
  public Point getLocation() {
    return location;
  }
  
  public Dimension getSize() {
  	return size;
  }
  
  public void move(Point location) {
    super.move(location);
    Node owner = (Node)parent;
    if(owner.isRendering())
      owner.firePropertyChange("refreshPorts", null, null);
    for (int i = 0; i < owner.sourceEdges().size(); i++) {
        Edge e = (Edge) owner.sourceEdges().elementAt(i);
        e.refresh();
    }
    for (int i = 0; i < owner.targetEdges().size(); i++) {
        Edge e = (Edge) owner.targetEdges().elementAt(i);
        e.refresh();
    }
  }    

  public void resize(Dimension size) {
	super.resize(size);
    Node owner = (Node)parent;
    if(owner.isRendering())
      owner.firePropertyChange("refreshPorts", null, null);
    for (int i = 0; i < owner.sourceEdges().size(); i++) {
        Edge e = (Edge) owner.sourceEdges().elementAt(i);
        e.refresh();
    }
    for (int i = 0; i < owner.targetEdges().size(); i++) {
        Edge e = (Edge) owner.targetEdges().elementAt(i);
        e.refresh();
    }
  }
  
  public void setLocation(Point location) {
  	this.location = location;
  }
  
  public void setSize(Dimension size) {
  	this.size = size;
  }
}