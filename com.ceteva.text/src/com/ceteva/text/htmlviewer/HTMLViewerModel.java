package com.ceteva.text.htmlviewer;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;

public class HTMLViewerModel extends ClientElement {
	
	HTMLViewer viewer;
	
	public HTMLViewerModel(String identity, EventHandler handler,
			HTMLViewer viewer) {
		super(null,handler,identity);
		this.viewer = viewer;
	}
	
	public void delete() {
		viewer.delete();
	}
	
	public boolean processMessage(Message message) {
		if(message.arity > 0)  {
		 if(message.args[0].hasStrValue(identity)) {
			 if(message.hasName("delete") && message.arity == 1) {	
			   viewer.delete();
			   return true;
			 }
			 if(message.hasName("setUrl") && message.arity == 2) {	
		      String url = message.args[1].strValue();
		      viewer.setURL(url);
		      return true;
		    }
		    if(message.hasName("setName") && message.arity == 2) {
		      String name = message.args[1].strValue();
		      viewer.setName(name);
		      return true;
		    }
		    if(message.hasName("setTooltip") && message.arity == 2) {
		      String tooltip = message.args[1].strValue();
		      viewer.setToolTip(tooltip);
		      return true;
		    }
	        if(message.hasName("setFocus") && message.arity==1) {
	          viewer.setFocusInternal();
	          return true;
	        }
		  }
		}
	    return false;
	}
	
	
	
}