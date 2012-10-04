package com.ceteva.diagram.action;

import org.eclipse.jface.action.Action;

import com.ceteva.diagram.dnd.DragRequest;

public class CtrlPressedAction extends Action {

  public void run() {
    DragRequest.setCtrl(true);
  }
	
}
