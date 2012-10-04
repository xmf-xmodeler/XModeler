package com.ceteva.diagram.editPolicy; 

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

import com.ceteva.diagram.editPart.EdgeEditPart;
import com.ceteva.diagram.editPart.EdgeTextEditPart;
import com.ceteva.diagram.figure.LabelLocatorFigure;

public class EdgeTextSelectionPolicy extends org.eclipse.gef.editpolicies.SelectionEditPolicy {
	
	public static LabelLocatorFigure line = null;
	
	public void showSelection() {
	  if(line == null) {
		line = getLocatorFigure();
		addFeedback(line);
	  }
	}
	
	public IFigure getFeedbackLayer() { 
	  EdgeTextEditPart edgeText = (EdgeTextEditPart)getHost();
	  EdgeEditPart edge = (EdgeEditPart)edgeText.getParent();
	  return edge.getFigure().getParent();
	}
	
	public GraphicalEditPart getParentEditPart() {
	  return (GraphicalEditPart)getHost().getParent();
	}
	
	public Figure getParentFigure() {
	  return (Figure)((EdgeEditPart)getParentEditPart()).getFigure();
	}
	
	public void hideSelection() {
	  if(line != null) {
		EdgeTextEditPart etep = (EdgeTextEditPart)this.getHost();
		line.removeMouseListener(etep);
	    removeFeedback(line);
	    line = null;
	  }
	}
	
	private LabelLocatorFigure getLocatorFigure() {
	  EdgeTextEditPart etep = (EdgeTextEditPart)this.getHost();
	  Point start = etep.getEdgePosition().getCopy();
	  Point end = getEndPosition(etep).getCopy();
	      
	  etep.getFigure().translateToRelative(start);
	  etep.getFigure().translateToAbsolute(start);
	  etep.getFigure().translateToRelative(end);
	  etep.getFigure().translateToAbsolute(end);
	      
	  LabelLocatorFigure line = new LabelLocatorFigure();
	  line.setLineStyle(Graphics.LINE_DOT);
	  line.setStart(start);
	  line.setEnd(end);
	  line.addMouseListener(etep);
	  return line;
	}
	
	public Point getEndPosition(EdgeTextEditPart etep) {
	  return etep.getFigure().getBounds().getTopLeft();	
	}

	public void mousePressed(MouseEvent me) {
	  me.consume();
	  EdgeTextEditPart etep = (EdgeTextEditPart)this.getHost();
	  etep.performRequest(new Request(RequestConstants.REQ_DIRECT_EDIT));
	}

	public void mouseReleased(MouseEvent me) {
	}

	public void mouseDoubleClicked(MouseEvent me) {
	}

	protected void removeFeedback(IFigure figure) {
		  if(line!=null) {
		    line.getParent().remove(line);  // null pointer exception here
		  }
		  line = null;
	 }
}