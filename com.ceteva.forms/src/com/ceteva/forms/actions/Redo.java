package com.ceteva.forms.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IUndoManager;

public class Redo extends Action {

	IUndoManager undoManager;
	
	public Redo(IUndoManager undoManager) {
	  this.undoManager = undoManager;
	  this.setText("Redo");
	  this.setActionDefinitionId("org.eclipse.ui.edit.redo");
	}
	
    public void run() {
      undoManager.redo(); 
    }
	
}
