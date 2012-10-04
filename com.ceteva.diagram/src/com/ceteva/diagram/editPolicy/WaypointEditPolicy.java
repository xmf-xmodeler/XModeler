package com.ceteva.diagram.editPolicy;
 
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.BendpointRequest;

import com.ceteva.diagram.command.CreateWaypointCommand;
import com.ceteva.diagram.command.MoveWaypointCommand;
import com.ceteva.diagram.command.SetRefPointCommand;
import com.ceteva.diagram.command.WaypointDeleteCommand;
import com.ceteva.diagram.model.Edge;

public class WaypointEditPolicy extends org.eclipse.gef.editpolicies.BendpointEditPolicy {	
	
  Point ref1 = new Point();
  Point ref2 = new Point();
	
  protected Command getCreateBendpointCommand(BendpointRequest request) {
  	Edge edge = (Edge)request.getSource().getModel();
  	Connection conn = getConnection();
  	Point p = ((BendpointRequest)request).getLocation();
  	conn.translateToRelative(p);
	
  	if(getPointsCount()==3) { 
      setReferencePoints((BendpointRequest)request);
  	  conn.translateToRelative(ref1);
  	  conn.translateToRelative(ref2);
      if(lineContainsPoint(ref1,ref2,p)) {
  	  	
  	  	// it is a straight edge so no waypoint is required
  	  	// consequently this must be a reference point
  	  	
  	    return new SetRefPointCommand(edge,p);
  	  }	
	}
  	
  	// otherwise it is a waypoint
  	
  	int index = request.getIndex(); 
	return new CreateWaypointCommand(edge,index,p);
  }

  protected Command getMoveBendpointCommand(BendpointRequest request) {
	Edge edge = (Edge)request.getSource().getModel();
	int index = request.getIndex();
	Point newLocation = request.getLocation();
	Connection conn = getConnection();
	conn.translateToRelative(newLocation);
	return new MoveWaypointCommand(edge,index,newLocation);
  }

  protected Command getDeleteBendpointCommand(BendpointRequest request) {
	Edge edge = (Edge)request.getSource().getModel();
	int index = request.getIndex();
	Point location = request.getLocation();
	return new WaypointDeleteCommand(edge,edge.getWaypointIdentity(index),location);
  }
  
  private int getPointsCount() {
  	PointList points = getConnection().getPoints();
  	return points.size();
  }
  
  private void setReferencePoints(BendpointRequest request) {
    PointList points = getConnection().getPoints();
	points.getPoint(ref1, 0);
	getConnection().translateToAbsolute(ref1);
	points.getPoint(ref2, 2);
	getConnection().translateToAbsolute(ref2);
  }	  
  
  private boolean lineContainsPoint(Point p1, Point p2, Point p) {
	int tolerance = 7;
	Rectangle rect = Rectangle.SINGLETON;
	rect.setSize(0, 0);
	rect.setLocation(p1.x, p1.y);
	rect.union(p2.x, p2.y);
	rect.expand(tolerance, tolerance);
	if (!rect.contains(p.x, p.y))
		return false;

	int v1x, v1y, v2x, v2y;
	int numerator, denominator;
	double result = 0.0;

	if (p1.x != p2.x && p1.y != p2.y) {
		
		v1x = p2.x - p1.x;
		v1y = p2.y - p1.y;
		v2x = p.x - p1.x;
		v2y = p.y - p1.y;
		
		numerator = v2x * v1y - v1x * v2y;
		denominator = v1x * v1x + v1y * v1y;

		result = ((numerator << 10) / denominator * numerator) >> 10;
	}
	
	// if it is the same point, and it passes the bounding box test,
	// the result is always true.
	return result <= tolerance * tolerance;
  }  
}