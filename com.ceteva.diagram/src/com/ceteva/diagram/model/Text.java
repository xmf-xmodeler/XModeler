package com.ceteva.diagram.model;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;

public class Text extends DisplayWithPosition {
	
  private String textString;	// the text to be displayed
  private boolean editable;		// denotes whether the text can be edited
  private boolean underline;
  private boolean italicise;
  private boolean edit = false;
  private RGB color;
  private String font = "";
  
  public Text(ClientElement parent,EventHandler handler,String identity,Point location,String textString,boolean editable,boolean underline,boolean italicise,RGB color,String font) {
  	super(parent,handler,identity,location);
  	this.textString = textString;
  	this.editable = editable;
  	this.underline = underline;
  	this.italicise = italicise;
  	this.color = color;
  	this.font = font;
  }
  
  public Text(ClientElement parent,EventHandler handler,String identity,int x,int y,String textString,boolean editable,boolean underline,boolean italicise,RGB color,String font) {
  	this(parent,handler,identity,new Point(x,y),textString,editable,underline,italicise,color,font);
  }
  
  public Text(ClientElement parent,EventHandler handler,String identity,int x,int y,String textString,boolean editable,boolean underline,boolean italicise,String font) {
	this(parent,handler,identity,new Point(x,y),textString,editable,underline,italicise,null,font);  
  }
  
  public void delete() {
  	super.delete();
  	((Container)parent).removeDisplay(this);
  }
  
  public RGB getColor() {
    return color;
  }
  
  public String getFont() {
	return font;
  }
  
  public String getText() {
  	return textString;
  }
  
  public void changeText(String text) {
  	Message m = handler.newMessage("textChanged",2);
	Value v1 = new Value(identity);
	Value v2 = new Value(text);
	m.args[0] = v1;
	m.args[1] = v2;
	handler.raiseEvent(m);
  }
  
  public boolean isEditable() {
  	return editable;
  }
  
  public boolean edit() {
	return edit;
  }
  
  public boolean getUnderline() {
  	return underline;
  }
  
  public boolean getItalicise() {
  	return italicise;
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
	  textString = message.args[1].strValue();
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
	if(message.hasName("underline") && message.args[0].hasStrValue(identity) && message.arity == 2) {
	  underline = message.args[1].boolValue;
	  if(isRendering())
	  	firePropertyChange("textChanged",null,null);
	  return true;
	}
	if(message.hasName("italicise") && message.args[0].hasStrValue(identity) && message.arity == 2) {
	  italicise = message.args[1].boolValue;
	  if(isRendering())
	    firePropertyChange("textChanged",null,null);
	  return true;
	}
	if(message.hasName("setColor") && message.args[0].hasStrValue(identity) && message.arity == 4) {
	  int red = message.args[1].intValue;
	  int green = message.args[2].intValue;
	  int blue = message.args[3].intValue;
	  setColor(red,green,blue);
	  return true;
	}
	return super.processMessage(message);
   }
  
   public void setEdit(boolean edit) {
     this.edit = edit;	   
   }
  
   public void setColor(int red,int green,int blue) {
     color = ModelFactory.getColor(red,green,blue);
 	 if(isRendering())
	   firePropertyChange("color",null, null); 
   }
}