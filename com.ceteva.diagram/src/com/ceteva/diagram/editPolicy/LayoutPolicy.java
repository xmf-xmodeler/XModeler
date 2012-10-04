package com.ceteva.diagram.editPolicy;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import com.ceteva.diagram.command.CreateNodeCommand;
import com.ceteva.diagram.command.DropCommand;
import com.ceteva.diagram.command.MoveResizeCommand;
import com.ceteva.diagram.dnd.DropRequest;
import com.ceteva.diagram.editPart.CommandEventEditPart;
import com.ceteva.diagram.editPart.GroupEditPart;
import com.ceteva.diagram.editPart.NodeEditPart;
import com.ceteva.diagram.figure.GroupFigure;
import com.ceteva.diagram.model.AbstractDiagram;
import com.ceteva.diagram.model.Node;

public class LayoutPolicy extends XYLayoutEditPolicy {

  public Command getCommand(Request request) {
	 if(DropRequest.ID.equals(request.getType())) {
		 DropRequest req = (DropRequest)request;
		 String type = req.getDropType();
		 String source = req.getSource();
		 CommandEventEditPart dep = (CommandEventEditPart)getHost();
		 AbstractDiagram diagram = (AbstractDiagram)dep.getModel();
		 Point location = req.getLocation();
		 translateToRelativeLocation(dep,location);
		 return new DropCommand(type,source,diagram,location);
	 }
	 return super.getCommand(request);
  }

  protected Command getMoveChildrenCommand(Request request) {
	//By default, move and resize are treated the same for constrained layouts.
  	return getResizeChildrenCommand((ChangeBoundsRequest)request);
  }	

  protected Command getResizeChildrenCommand(ChangeBoundsRequest request) {
	CompoundCommand resize = new CompoundCommand();
	Command c;
	GraphicalEditPart child;
	List children = request.getEditParts();
	for (int i = 0; i < children.size(); i++) {
		child = (GraphicalEditPart)children.get(i);
		c = createChangeConstraintCommand(request, child,
			translateToModelConstraint(
				getConstraintFor(request, child)));
		resize.add(c);
	}
	return resize.unwrap();
  }
  
  public EditPart getTargetEditPart(Request request) {
	if(DropRequest.ID.equals(request.getType()))
		return getHost();
	return super.getTargetEditPart(request);
  }
  
  // normal
  
  protected Command createAddCommand(EditPart child, Object constraint) {
	return null;
  }

  protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
	Rectangle rectangle = (Rectangle)constraint;
	Node model = (Node)child.getModel();
	return new MoveResizeCommand(model,rectangle.getLocation(),rectangle.getSize());
  }

  protected Command getCreateCommand(CreateRequest request) {
	CommandEventEditPart parent = (CommandEventEditPart)this.getHost();
	String toolIdentity = (String)request.getNewObject();
	Point location = request.getLocation();
	translateToRelativeLocation(parent,location);
	return new CreateNodeCommand(parent,toolIdentity,location);
  }

  protected EditPolicy createChildEditPolicy(EditPart child) {
	//return new ResizableEditPolicy();
  	if(child instanceof NodeEditPart) {
  		if(child.isSelectable())
  			return new NodeHandlesEditPolicy();
  	    else
  	    	return null;
  	}
  	else
  	  return new NodeHandlesEditPolicy();
  }

  protected Command getDeleteDependantCommand(Request request) {
	return null;
  }
  
  private void translateToRelativeLocation(CommandEventEditPart parent, Point location) {
	  if(parent instanceof GroupEditPart) {
		  GroupFigure groupFigure = (GroupFigure)parent.getFigure();
		  groupFigure.getFigure().translateToRelative(location);
		  groupFigure.getFigure().translateFromParent(location);
	  }
	  else
		  parent.getFigure().translateToRelative(location);
  }
  
}