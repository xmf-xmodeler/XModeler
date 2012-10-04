package com.ceteva.diagram.editPolicy;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.requests.LocationRequest;

import com.ceteva.diagram.command.DeleteCommand;
import com.ceteva.diagram.command.MoveRefPointCommand;
import com.ceteva.diagram.model.CommandEvent;
import com.ceteva.diagram.model.Edge;

public class EdgePolicy extends org.eclipse.gef.editpolicies.ConnectionEditPolicy {

  public static String MOVE_REFPOINT = "moveRefPoint";
  
  public Command getCommand(Request request) {
	if (REQ_DELETE.equals(request.getType()))
		return getDeleteCommand((GroupRequest)request);
    if (MOVE_REFPOINT.equals(request.getType())) {
    	return getMoveRefPointCommand((LocationRequest)request);
    }
	return null;
  }
	
  protected Command getDeleteCommand(GroupRequest request) {
	CommandEvent model = (CommandEvent)getHost().getModel();
	String identity = model.getIdentity();
	return new DeleteCommand(identity,model);
  }
  
  protected Command getMoveRefPointCommand(LocationRequest request) {
  	Edge model = (Edge)getHost().getModel();
  	return new MoveRefPointCommand(model,request.getLocation());
  }
  

}