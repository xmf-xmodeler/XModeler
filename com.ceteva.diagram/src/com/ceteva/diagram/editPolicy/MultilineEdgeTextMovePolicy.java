package com.ceteva.diagram.editPolicy;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;

import com.ceteva.diagram.command.MoveMultilineEdgeTextCommand;
import com.ceteva.diagram.editPart.EdgeEditPart;
import com.ceteva.diagram.editPart.MultilineEdgeTextEditPart;
import com.ceteva.diagram.model.MultilineEdgeText;

public class MultilineEdgeTextMovePolicy extends NonResizableEditPolicy {

	public Command getMoveCommand(ChangeBoundsRequest request) {
		MultilineEdgeText model = (MultilineEdgeText)getHost().getModel();
		Point delta = request.getMoveDelta();
		MoveMultilineEdgeTextCommand command = new MoveMultilineEdgeTextCommand(model,getParentFigure(),delta);
		return command; 
	}
		
	public Figure getParentFigure() {
		EditPart edge = getHost().getParent();
		return (Figure)((EdgeEditPart)edge).getFigure();
	}
		
	public Point getEndPosition(MultilineEdgeTextEditPart etep) {
		return etep.getFigure().getBounds().getCenter();	
	}
		
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
		super.showChangeBoundsFeedback(request);
		MultilineEdgeTextEditPart etep = (MultilineEdgeTextEditPart)this.getHost();
		if(MultilineEdgeTextSelectionPolicy.line==null) {
			MultilineEdgeTextSelectionPolicy.line = new Polyline();
			MultilineEdgeTextSelectionPolicy.line.setLineStyle(Graphics.LINE_DOT);
			MultilineEdgeTextSelectionPolicy.line.setStart(etep.getEdgePosition());
			MultilineEdgeTextSelectionPolicy.line.setEnd(getEndPosition(etep));
		    addFeedback(MultilineEdgeTextSelectionPolicy.line);
		}
		Point newLocation = ((MultilineEdgeText)etep.getModel()).getLocation().getCopy();
		IFigure edge = etep.getEdgeEditPart().getFigure();

		edge.translateToAbsolute(newLocation);
		newLocation.translate(request.getMoveDelta());
		edge.translateToRelative(newLocation);

		newLocation = etep.getEdgePosition().getCopy().translate(newLocation);
		MultilineEdgeTextSelectionPolicy.line.setEnd(newLocation);
	}
}
