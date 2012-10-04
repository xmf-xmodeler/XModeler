package com.ceteva.diagram.editPolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

import com.ceteva.diagram.command.MultilineTextChangeCommand;
import com.ceteva.diagram.editPart.MultilineTextEditPart;
import com.ceteva.diagram.model.MultilineText;

public class MultilineTextEditPolicy extends DirectEditPolicy {

  protected Command getDirectEditCommand(DirectEditRequest edit) {
	String labelText = (String)edit.getCellEditor().getValue();
	MultilineTextEditPart text = (MultilineTextEditPart)getHost();
	MultilineTextChangeCommand command = new MultilineTextChangeCommand((MultilineText)text.getModel(),labelText);
	return command;
  }

  protected void showCurrentEditValue(DirectEditRequest request) {
  }

}