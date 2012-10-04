package com.ceteva.diagram.editPolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

import com.ceteva.diagram.command.MultilineEdgeTextChangeCommand;
import com.ceteva.diagram.editPart.MultilineEdgeTextEditPart;
import com.ceteva.diagram.model.MultilineEdgeText;

public class MultilineEdgeTextEditPolicy extends DirectEditPolicy {

  protected Command getDirectEditCommand(DirectEditRequest edit) {
	String labelText = (String)edit.getCellEditor().getValue();
	MultilineEdgeTextEditPart text = (MultilineEdgeTextEditPart)getHost();
	MultilineEdgeTextChangeCommand command = new MultilineEdgeTextChangeCommand((MultilineEdgeText)text.getModel(),labelText);
	return command;
  }

  protected void showCurrentEditValue(DirectEditRequest request) {
  }

}