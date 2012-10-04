package com.ceteva.diagram.editPolicy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import com.ceteva.diagram.command.DeleteCommand;
import com.ceteva.diagram.model.CommandEvent;

public class NodeEditPolicy extends ComponentEditPolicy {

  protected Command createDeleteCommand(GroupRequest request) {
	CommandEvent model = (CommandEvent)getHost().getModel();
	String identity = model.getIdentity();
	return new DeleteCommand(identity,model);
  }
}