package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

public class MultilineEdgeText extends DisplayWithPosition {

	// This type need combining with EdgeText
	
	private String text;
	private boolean editable;
	private String position;	// the position of the text relative to the edge (start,middle,end)
	private boolean underline;
	private int truncate;
	private RGB color;
	private String font;
	
	public MultilineEdgeText(ClientElement parent,EventHandler handler,String identity,String position,Point location,String text,boolean editable,boolean underline,int condense,RGB color,String font) {
		super(parent,handler,identity,location);
		this.position = position;
		this.text = text;
		this.editable = editable;
		this.underline = underline;
		this.truncate = condense;
		this.color = color;
		this.font = font;
	}
	  
	public MultilineEdgeText(ClientElement parent,EventHandler handler,String identity,String position,int x,int y,String textString,boolean editable,boolean underline,int condense,RGB color,String font) {
		this(parent,handler,identity,position,new Point(x,y),textString,editable,underline,condense,color,font);
	}
	
	public MultilineEdgeText(ClientElement parent,EventHandler handler,String identity,String position,int x,int y,String textString,boolean editable,boolean underline,int condense,String font) {
		this(parent,handler,identity,position,x,y,textString,editable,underline,condense,null,font);
	}
	
	public void changeText(String text) {
	  	Message m = handler.newMessage("textChanged",2);
		Value v1 = new Value(identity);
		Value v2 = new Value(text);
		m.args[0] = v1;
		m.args[1] = v2;
		handler.raiseEvent(m);
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
	
	public RGB getColor() {
		return color;
	}
	
	public int getCondense() {
		return truncate;
	}
	
	public boolean getUnderline() {
		return underline;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void raiseMoveEvent(Point location) {
		Message m = handler.newMessage("move",3);
		Value v1 = new Value(getIdentity());
		Value v2 = new Value(location.x);
		Value v3 = new Value(location.y);
		m.args[0] = v1;
		m.args[1] = v2;
		m.args[2] = v3;
		handler.raiseEvent(m);
	}
	
	public void synchronise(Element edgeText) {
		text = edgeText.getString("text");
		position = edgeText.getString("attachedTo");
		editable = edgeText.getBoolean("editable");
		underline = edgeText.getBoolean("underline");
		truncate = edgeText.getInteger("truncate");
		font = edgeText.getString("font");
		super.synchronise(edgeText);
	}	
}
