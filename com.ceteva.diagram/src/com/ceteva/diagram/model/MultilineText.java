package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

public class MultilineText extends DisplayWithDimension {
	
	private String text;
	private boolean editable;
	private boolean edit = false;
	private String font;
	
	public MultilineText(ClientElement parent,EventHandler handler,String identity,String text,Point location,Dimension size,boolean editable,RGB lineColor,RGB fillColor,String font) {
		super(parent,handler,identity,location,size,lineColor,fillColor);
		this.text = text;
		this.editable = editable;
		this.font = font;
	}
	
	public MultilineText(ClientElement parent,EventHandler handler,String identity,String text,int x,int y,int width,int height,boolean editable,RGB lineColor,RGB fillColor,String font) {
		this(parent,handler,identity,text,new Point(x,y),new Dimension(width,height),editable,lineColor,fillColor,font);
	}
	
	public MultilineText(ClientElement parent,EventHandler handler,String identity,String text,int x,int y,int width,int height,boolean editable,String font) {
		this(parent,handler,identity,text,new Point(x,y),new Dimension(width,height),editable,null,null,font);
	}	
	
	public void changeText(String text) {
	  	Message m = handler.newMessage("textChanged",2);
		Value v1 = new Value(identity);
		Value v2 = new Value(text);
		m.args[0] = v1;
		m.args[1] = v2;
		handler.raiseEvent(m);
	}
	
	public boolean edit() {
		return edit;
	}
	
	public String getFont() {
		return font;
	}
	
	public String getText() {
		return text;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public boolean processMessage(Message message) {	
		if(message.hasName("editText") && message.args[0].hasStrValue(identity) && message.arity == 1 ) {
		  
		  // the edit may happen before the text is constructed.  In which case
		  // store the edit and the text's editpart will check after it has
		  // constructed
			
		  if(canFire())
		    firePropertyChange("editText",null, null);
		  else
			edit = true;  
		}
		if(message.hasName("setText") && message.args[0].hasStrValue(identity) && message.arity == 2) {
		  text = message.args[1].strValue();
		  if(isRendering())
		    firePropertyChange("textChanged",null, null);
		  return true;
		}
		if(message.hasName("setFont") && message.args[0].hasStrValue(identity) && message.arity == 2) {
		  font = message.args[1].strValue();
		  if(isRendering())
			firePropertyChange("textChanged",null, null);
		  return true;
		}
		return super.processMessage(message);
	}  
	
	public void setEdit(boolean edit) {
		this.edit = edit;	   
	}
	
	public void synchronise(Element multilineText) {
		text = multilineText.getString("text");
		font = multilineText.getString("font");
		editable = multilineText.getBoolean("editable");
		super.synchronise(multilineText);
	}
}