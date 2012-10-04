package com.ceteva.diagram.action;

import org.eclipse.jface.action.Action;

import com.ceteva.diagram.dnd.DragRequest;

public class CtrlReleasedAction extends Action {

  public void run() {
	DragRequest.setCtrl(false);
  }
	
}
