package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.diagram.DiagramPlugin;

public class Image extends DisplayWithDimension {
	
  String filename = "";
  org.eclipse.swt.graphics.Image image = null;	
	
  public Image(ClientElement parent,EventHandler handler,String identity,String filename,Point location,Dimension size) {
    super(parent,handler,identity,location,size,null,null);
    // getImage(filename);
    this.filename = filename;
  }
  
  public Image(ClientElement parent,EventHandler handler,String identity,String filename,int x,int y,int width,int height) {
  	this(parent,handler,identity,filename,new Point(x,y),new Dimension(width,height));
  }
  
  public void delete() {
  	super.delete();
  	((Container)parent).removeDisplay(this);
  }
  
  public org.eclipse.swt.graphics.Image getImage(String filename) {
  	return ImageManager.getImage(DiagramPlugin.getDefault(),filename);
  }
  
  public org.eclipse.swt.graphics.Image getImage() {
	  
	// figures dispose of their images when they are destroyed (i.e. as a result of a zoom)
	// this checks to make sure that the image has not been disposed.
	  
	if(image == null || image.isDisposed())
	  image = getImage(filename);    	
  	return image;
  }
  
  public boolean processMessage(Message message) {
  	if(message.hasName("setImage") && message.args[0].hasStrValue(identity)) {
  	  filename = message.args[1].strValue();
  	  if(isRendering())
	    firePropertyChange("imageChanged",null, null);
  	  return true;
  	}
  	else 
  	  return super.processMessage(message);
  }
}