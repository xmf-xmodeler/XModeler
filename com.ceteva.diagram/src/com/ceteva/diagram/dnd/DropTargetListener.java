package com.ceteva.diagram.dnd;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;

public class DropTargetListener extends AbstractTransferDropTargetListener {
	
	private boolean enabled = false;
	
	public DropTargetListener(EditPartViewer viewer,Transfer transfer) {
		super(viewer,transfer);
	}
	
	protected Request createTargetRequest() {
		if(getCurrentEvent().detail == DND.DROP_COPY)
		  return new DropRequest((String)getCurrentEvent().data,DropRequest.COPY);
		else
		  return new DropRequest((String)getCurrentEvent().data,DropRequest.MOVE);
	}
	
	public boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled; 
	}
	
	protected void updateTargetRequest() {
		((DropRequest)getTargetRequest()).setLocation(getDropLocation());
	}
	
	public boolean isEnabled(DropTargetEvent event) {
		return enabled;
	}
}
