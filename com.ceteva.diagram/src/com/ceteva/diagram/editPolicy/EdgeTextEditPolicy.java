package com.ceteva.diagram.editPolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

import com.ceteva.diagram.command.EdgeTextChangeCommand;
import com.ceteva.diagram.editPart.EdgeTextEditPart;
import com.ceteva.diagram.model.EdgeText;

public class EdgeTextEditPolicy extends DirectEditPolicy {

  protected Command getDirectEditCommand(DirectEditRequest edit) {
	String labelText = (String)edit.getCellEditor().getValue();
	EdgeTextEditPart label = (EdgeTextEditPart)getHost();
	EdgeTextChangeCommand command = new EdgeTextChangeCommand((EdgeText)label.getModel(),labelText);
	return command;
  }

  protected void showCurrentEditValue(DirectEditRequest request) {
  }

}