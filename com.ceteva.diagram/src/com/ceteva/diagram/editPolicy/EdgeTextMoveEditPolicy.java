package com.ceteva.diagram.editPolicy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.handles.NonResizableHandleKit;
import org.eclipse.gef.requests.ChangeBoundsRequest;

import com.ceteva.diagram.command.MoveEdgeTextCommand;
import com.ceteva.diagram.editPart.EdgeEditPart;
import com.ceteva.diagram.editPart.EdgeTextEditPart;
import com.ceteva.diagram.model.EdgeText;

public class EdgeTextMoveEditPolicy extends NonResizableEditPolicy {
	
	public List createSelectionHandles() {
	
	  // the move handle is overriden so that it uses the standard edge text
	  // drag tracker rather than the built in one
		
	  List list = new ArrayList();
	  EdgeTextEditPart etep = (EdgeTextEditPart)getHost();
	  DragTracker tracker = etep.getDragTracker();
	  list.add(NonResizableHandleKit.moveHandle(etep,tracker,SharedCursors.SIZEALL));
	  NonResizableHandleKit.addHandle(etep,list,PositionConstants.SOUTH_EAST,tracker,SharedCursors.SIZEALL);
	  NonResizableHandleKit.addHandle(etep,list,PositionConstants.SOUTH_WEST,tracker,SharedCursors.SIZEALL);
	  NonResizableHandleKit.addHandle(etep,list,PositionConstants.NORTH_WEST,tracker,SharedCursors.SIZEALL);
	  NonResizableHandleKit.addHandle(etep,list,PositionConstants.NORTH_EAST,tracker,SharedCursors.SIZEALL);
	  return list;
	}
	
	public Command getMoveCommand(ChangeBoundsRequest request) {
	  EdgeText model = (EdgeText)getHost().getModel();
	  Point delta = request.getMoveDelta();
	  MoveEdgeTextCommand command = new MoveEdgeTextCommand(model,getParentFigure(),delta);
	  return command; 
	}
	
	public Figure getParentFigure() {
	  EditPart edge = getHost().getParent();
	  return (Figure)((EdgeEditPart)edge).getFigure();
	  
	}
	
	public Point getEndPosition(EdgeTextEditPart etep) {
	  return etep.getFigure().getBounds().getCenter();	
	}
	
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
	  super.showChangeBoundsFeedback(request);
	  EdgeTextEditPart etep = (EdgeTextEditPart)this.getHost();
	  
	  /* if(EdgeTextSelectionPolicy.line==null) {
	  	EdgeTextSelectionPolicy.line = new LabelLocatorFigure();
	  	EdgeTextSelectionPolicy.line.setLineStyle(Graphics.LINE_DOT);
	  	EdgeTextSelectionPolicy.line.setStart(etep.getEdgePosition());
	  	EdgeTextSelectionPolicy.line.setEnd(getEndPosition(etep));
	  	System.out.println("Adding mouse listener 2");
	  	EdgeTextSelectionPolicy.line.addMouseListener(etep);
	    addFeedback(EdgeTextSelectionPolicy.line);
	  } */
	  
	  Point newLocation = ((EdgeText)etep.getModel()).getLocation().getCopy();
	  IFigure edge = etep.getEdgeEditPart().getFigure();

	  edge.translateToAbsolute(newLocation);
	  newLocation.translate(request.getMoveDelta());
	  edge.translateToRelative(newLocation);

	  newLocation = etep.getEdgePosition().getCopy().translate(newLocation);
	  
	  Point nl = newLocation.getCopy();
	  edge.translateToRelative(nl);
	  edge.translateToAbsolute(nl);
	  
	  EdgeTextSelectionPolicy.line.setEnd(nl);
	}
	
	/* protected void eraseChangeBoundsFeedback(ChangeBoundsRequest request) {
	  super.eraseChangeBoundsFeedback(request);
	  if (EdgeTextSelectionPolicy.line != null) {
	    removeFeedback(EdgeTextSelectionPolicy.line);
		EdgeTextSelectionPolicy.line = null;
	  }
	} */
}