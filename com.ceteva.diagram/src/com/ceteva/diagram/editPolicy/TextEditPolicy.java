package com.ceteva.diagram.editPolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

import com.ceteva.diagram.command.TextChangeCommand;
import com.ceteva.diagram.editPart.TextEditPart;
import com.ceteva.diagram.model.Text;

public class TextEditPolicy extends DirectEditPolicy {

  protected Command getDirectEditCommand(DirectEditRequest edit) {
	String labelText = (String)edit.getCellEditor().getValue();
	TextEditPart label = (TextEditPart)getHost();
	TextChangeCommand command = new TextChangeCommand((Text)label.getModel(),labelText);
	return command;
  }

  protected void showCurrentEditValue(DirectEditRequest request) {
  }

}