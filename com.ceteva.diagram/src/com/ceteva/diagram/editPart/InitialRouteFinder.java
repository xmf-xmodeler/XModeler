package com.ceteva.diagram.editPart;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class InitialRouteFinder {
	
  public InitialRouteFinder() {	
  }
  
  // takes the two rectangles and finds a mutually convenient
  // position for both ports
	
  public void calculateInitialPort(Rectangle r1,Rectangle r2,Point start,Point end) {
	int orientation = getOrientation(r2,r1);
	if(orientation == 1)		// north
	  initializeNorth(r1,r2,start,end);
	else if(orientation == 2) // south
	  initializeSouth(r1,r2,start,end);  		  
	else if(orientation == 3) // west
	  initalizeWest(r1,r2,start,end);
	else if(orientation == 4) // east
	  initalizeEast(r1,r2,start,end);	  
  }
  
  public int getOrientation(Rectangle centre,Rectangle periphery) {
	if((periphery.y + periphery.height) <= centre.y)
	  return 1; // north
	else if(periphery.y >= (centre.y + centre.height))
	  return 2; // south
	else if((periphery.x + periphery.width) < centre.x)
	  return 3; // east
	else if(periphery.x > (centre.x + centre.width))
	  return 4; // west
	return 0;
  }
	
  public void initializeNorth(Rectangle r1,Rectangle r2,Point start,Point end) {
	start.y = r1.y + r1.height;
	end.y = r2.y; 
	if(xIntersects(r1,r2)) {	
	  int intersection = getXIntersection(r1,r2);
	  start.x = intersection;
	  end.x = intersection;
	}
	else if((r1.x + r1.width) < r2.x) {
	  start.x = r1.x + r1.width;
	  end.x = r2.x; 
	}
	else {
	  start.x = r1.x;
	  end.x = r2.x + r2.width;
	}		
  }
	
  public void initializeSouth(Rectangle r1,Rectangle r2,Point start,Point end) {
	start.y = r1.y;
	end.y = r2.y + r2.height;
	if(xIntersects(r1,r2)) {
	  int intersection = getXIntersection(r1,r2);
	  start.x = intersection;
	  end.x = intersection;
	}
	else if((r1.x + r1.width) < r2.x) {
	  start.x = r1.x + r1.width;
	  end.x = r2.x; 
	}
	else {
	  start.x = r1.x;
	  end.x = r2.x + r2.width;
	}
  }
	
  public void initalizeWest(Rectangle r1,Rectangle r2,Point start,Point end) {
	start.x = r1.x + r1.width;
	end.x = r2.x;
	if(yIntersects(r1,r2)) {	    	
	  int intersection = getYIntersection(r1,r2);
	  start.y = intersection;
	  end.y = intersection;
	}	    	
  }
	
  public void initalizeEast(Rectangle r1,Rectangle r2,Point start,Point end) {
	start.x = r1.x;
	end.x = r2.x + r2.width;
	if(yIntersects(r1,r2)) {	    	
	  int intersection = getYIntersection(r1,r2);
	  start.y = intersection;
	  end.y = intersection;
	}	    	
  }
	
  public boolean xIntersects(Rectangle r1,Rectangle r2) {
	if(getXIntersection(r1,r2)!=-1)
	  return true;
	else return false;	
  }
	
  public boolean yIntersects(Rectangle r1,Rectangle r2) {
	if(getYIntersection(r1,r2)!=-1)
	  return true;
	else return false;	
  }
	
  public int getXIntersection(Rectangle r1,Rectangle r2) {
	Rectangle r1copy = r1.getCopy();
	Rectangle r2copy = r2.getCopy();
	r1copy.y = r2copy.y;
	r1copy.intersect(r2copy);
	if(!r1copy.getLocation().equals(new Point(0,0)) && !r1copy.getSize().equals(new Dimension(0,0)))
	  return r1copy.getCenter().x;
	return -1;  	
  }
	
  public int getYIntersection(Rectangle r1,Rectangle r2) {
	Rectangle r1copy = r1.getCopy();
	Rectangle r2copy = r2.getCopy();
	r1copy.x = r2copy.x;
	r1copy.intersect(r2copy);
	if(!r1copy.getLocation().equals(new Point(0,0)) && !r1copy.getSize().equals(new Dimension(0,0)))
	  return r1copy.getCenter().y;
	return -1;	
  }
  
  public Point absoluteToLocal(Point p,Figure port) {
	port.getParent().translateToRelative(p);
	Rectangle r = port.getBounds().getCopy();
	Rectangle portParent = port.getParent().getBounds();
	r.translate(portParent.getLocation());
	p.translate(r.getLocation().negate());
	return p;
  }
}