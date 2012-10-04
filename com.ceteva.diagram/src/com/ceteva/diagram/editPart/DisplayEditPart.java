package com.ceteva.diagram.editPart;

import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

import com.ceteva.diagram.model.Display;

public abstract class DisplayEditPart extends CommandEventEditPart {
	
	public void performRequest(Request req){
		Display display = (Display)getModel();
		Object request = req.getType();
		if(request == RequestConstants.REQ_DIRECT_EDIT)
		  display.selected(1);	
		else if(request == RequestConstants.REQ_OPEN)
		  display.selected(2);
   }
}