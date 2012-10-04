package com.ceteva.diagram.action;

import java.text.MessageFormat;

import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.LabelRetargetAction;

public class PrintRetargetAction extends LabelRetargetAction {

  public PrintRetargetAction() {
	super(ActionFactory.PRINT.getId(),
			MessageFormat.format("Print", 
									new Object[] {""}).trim());
  }
}