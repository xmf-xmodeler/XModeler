package com.ceteva.undo.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import XOS.Message;

import com.ceteva.client.EventHandler;

public class UndoAction extends Action {
	
	EventHandler handler;
	
	public UndoAction() {
	  super("&Undo");
	  setEnabled(false);
	  setImages();
	  setId("com.ceteva.xmf.undoAction");
	}
		
	public void setImages() {
	  setImageDescriptor(
		PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
	  setDisabledImageDescriptor(
		PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UNDO_DISABLED));
	  this.setAccelerator(SWT.CTRL | 'Z');
	}

	public void registerEventHandler(EventHandler handler) {
      this.handler = handler;
	}
		
	public void run() {
	  if(handler != null) {
	    Message m = handler.newMessage("undo",0);
		handler.raiseEvent(m);
	  }
    }
}
