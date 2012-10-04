package com.ceteva.forms.views;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;

class FormText extends FormElement {
	
	Label text = null;
	
	public FormText(Composite parent,String identity,EventHandler handler) {
		super(identity);
		text = new Label(parent,SWT.NONE);
		this.handler = handler;
	}

    public Control getControl() {
        return text;
    }

	public void setText(String textString) {
		text.setText(textString);
	}
	
	public void setLocation(Point location) {
		text.setLocation(location);
	}
	
	public void calculateSize() {
		Dimension minimum = FigureUtilities.getTextExtents(text.getText(),text.getFont());
		text.setSize(minimum.width,minimum.height);
	}
	
	public Value processCall(Message message) {
		return null;
	}	
	
	public boolean processMessage(Message message) {
	  if(message.args[0].strValue().equals(getIdentity())) {	
	    if(message.hasName("setText")) {
	  	  text.setText(message.args[1].strValue());
	  	  return true;
	    }
	    else {
	      if(ComponentCommandHandler.processMessage(text,message))
	      	return true;
	    }
	  }
	  return super.processMessage(message);
	}
}