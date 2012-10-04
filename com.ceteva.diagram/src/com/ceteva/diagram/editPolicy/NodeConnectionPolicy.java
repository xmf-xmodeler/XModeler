package com.ceteva.diagram.editPolicy; 

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import com.ceteva.diagram.command.ConnectionCommand;
import com.ceteva.diagram.editPart.NodeEditPart;
import com.ceteva.diagram.figure.FixedAnchor;
import com.ceteva.diagram.model.CommandEvent;
import com.ceteva.diagram.model.Edge;

public class NodeConnectionPolicy extends org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy {	
	
  protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {	
    ConnectionCommand command = (ConnectionCommand)request.getStartCommand();
	AbstractConnectionAnchor anchor = (AbstractConnectionAnchor)getEditPart().getTargetConnectionAnchor(request);
	if(anchor != null)  {
	  FixedAnchor targetPort = (FixedAnchor)anchor;
	  command.setTarget(targetPort.getIdentity());
	  return command;
	}
	return null;
  }

  protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
    ConnectionCommand command = new ConnectionCommand();
	command.setParent((CommandEvent)getHost().getModel());
	AbstractConnectionAnchor anchor = (AbstractConnectionAnchor)getEditPart().getSourceConnectionAnchor(request);
	if(anchor != null) {
	  FixedAnchor sourcePort = (FixedAnchor)anchor;
	  command.setSource(sourcePort.getIdentity());	
	  String toolIdentity = (String)request.getNewObject();
      command.setToolIdentity(toolIdentity);
      request.setStartCommand(command);
	  return command;
	}
	return null;
  }
  
  protected Command getReconnectTargetCommand(ReconnectRequest request) {
	ConnectionCommand command = new ConnectionCommand();
	command.setParent((CommandEvent)getHost().getModel());
	command.setEdge((Edge)request.getConnectionEditPart().getModel());
	AbstractConnectionAnchor anchor = (AbstractConnectionAnchor)getEditPart().getTargetConnectionAnchor(request);
	if(anchor != null) {
	  FixedAnchor fixedAnchor = (FixedAnchor)anchor;
	  command.setTarget(fixedAnchor.getIdentity());	
      return command;
	}
	return null;
  }

  protected Command getReconnectSourceCommand(ReconnectRequest request) {
	ConnectionCommand command = new ConnectionCommand();
	command.setParent((CommandEvent)getHost().getModel());
	command.setEdge((Edge)request.getConnectionEditPart().getModel());
	AbstractConnectionAnchor anchor = (AbstractConnectionAnchor)getEditPart().getSourceConnectionAnchor(request);
	if(anchor != null) {
	  FixedAnchor fixedAnchor = (FixedAnchor)anchor;
	  command.setSource(fixedAnchor.getIdentity());		
	  return command;
	}
	return null;	
  }
    
  public NodeEditPart getEditPart() {
	return (NodeEditPart)getHost();
  }
}