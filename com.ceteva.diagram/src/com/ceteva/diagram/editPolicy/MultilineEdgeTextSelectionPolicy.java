package com.ceteva.diagram.editPolicy; 

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.LayerConstants;

import com.ceteva.diagram.editPart.MultilineEdgeTextEditPart;

public class MultilineEdgeTextSelectionPolicy extends org.eclipse.gef.editpolicies.SelectionEditPolicy {
	
	public static Polyline line = null;
	
	public void showSelection() {
	  if(line == null) {
	    IFigure layer = getLayer(LayerConstants.PRIMARY_LAYER);
	    addFeedback(layer);
	  }
	}
	
	public void hideSelection() {
	  if(line != null) {
	    IFigure layer = getLayer(LayerConstants.PRIMARY_LAYER);
	    removeFeedback(layer);
	  }
	}
	
	protected void addFeedback(IFigure figure) {
      MultilineEdgeTextEditPart etep = (MultilineEdgeTextEditPart)this.getHost();
      Point start = etep.getEdgePosition();
      Point end = getEndPosition(etep);
      line = new Polyline();
      line.setLineStyle(Graphics.LINE_DOT);
      line.setStart(start);
      line.setEnd(end);
      figure.add(line);
	}
	
	public Point getEndPosition(MultilineEdgeTextEditPart etep) {
	  return etep.getFigure().getBounds().getTopLeft();	
	}
	
	protected void removeFeedback(IFigure figure) {
	  if(line!=null) {
	    line.getParent().remove(line);  // null pointer exception here
	  }
	  line = null;
    }
}