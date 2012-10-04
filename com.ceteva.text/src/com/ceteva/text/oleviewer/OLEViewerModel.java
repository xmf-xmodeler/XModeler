package com.ceteva.text.oleviewer;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;

public class OLEViewerModel extends ClientElement {

	OLEViewer viewer;
	
	public OLEViewerModel(String identity,EventHandler handler,OLEViewer viewer) {
		super(null,handler,identity);
		this.viewer = viewer;
	}
	
	public boolean processMessage(Message message) {
		if(message.args[0].hasStrValue(identity)) {
		  if(message.hasName("saveAs")) {
			String filename = message.args[1].strValue();
			viewer.saveAs(filename);
			return true;
		  }
		}
		return false;
	}

	
}
