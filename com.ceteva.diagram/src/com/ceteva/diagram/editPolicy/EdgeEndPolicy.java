package com.ceteva.diagram.editPolicy;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.GraphicalEditPart;

import com.ceteva.diagram.editPart.EdgeEditPart;
import com.ceteva.diagram.model.Edge;

public class EdgeEndPolicy extends org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy  {

  protected void addSelectionHandles(){
	super.addSelectionHandles();
	Edge edge = (Edge)getConnectionEditPart().getModel();
	getConnectionFigure().setLineWidth(edge.getWidth() + 2);
	edge.edgeSelected();
  }

  protected PolylineConnection getConnectionFigure(){
	return (PolylineConnection)((GraphicalEditPart)getHost()).getFigure();
  }

  protected void removeSelectionHandles(){
	super.removeSelectionHandles();
	Edge edge = (Edge)getConnectionEditPart().getModel();
	getConnectionFigure().setLineWidth(edge.getWidth());
	edge.edgeDeselected();
  }
  
  public EdgeEditPart getConnectionEditPart() {
	return (EdgeEditPart)getHost();
  }
}