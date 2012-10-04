package com.ceteva.forms.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IUndoManager;

public class Undo extends Action {

	IUndoManager undoManager;
	
	public Undo(IUndoManager undoManager) {
	  this.undoManager = undoManager;
	  this.setText("Undo");
	  this.setActionDefinitionId("org.eclipse.ui.edit.undo");
	}
	
    public void run() {
      undoManager.undo(); 
    }
	
}
