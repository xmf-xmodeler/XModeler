package com.ceteva.undo.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import XOS.Message;

import com.ceteva.client.EventHandler;

public class RedoAction extends Action {
	
	EventHandler handler;
	
	public RedoAction() {
      super("&Redo");
	  setEnabled(false);
	  setImages();
	  setId("com.ceteva.xmf.redoAction");
	}
			
	public void setImages() {
      setImageDescriptor(
        PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
      setDisabledImageDescriptor(
        PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_REDO_DISABLED));
	  this.setAccelerator(SWT.CTRL | 'Y');
	}
	
	public void registerEventHandler(EventHandler handler) {
	  this.handler = handler;
	}
	
	public void run() {
	  if(handler != null) {
        Message m = handler.newMessage("redo",0);
	  	handler.raiseEvent(m);
	  }
	}
}
