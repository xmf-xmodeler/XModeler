 package com.ceteva.diagram.figure;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import com.ceteva.diagram.model.Port;

public class NodeFigure extends Figure {
	
	Hashtable fixedAnchors = new Hashtable();
	
	public NodeFigure(Point location,Dimension size,Vector ports) {
	  setLocation(location);
	  setSize(size);
	  createFixedAnchors(ports);
	}
	
	public boolean containsGroupFigure() {
	  return containsGroupFigure(this);
	}
	
	public boolean containsGroupFigure(Figure parent) {
	  List children = parent.getChildren();
	  for(int i=0;i<children.size();i++) {
		Figure child = (Figure)children.get(i);
		if(child instanceof GroupFigure)
		  return true;
		else
		  return containsGroupFigure(child);
	  }
	  return false;
	}
	
	protected void createFixedAnchors(Vector ports) {
	  for(int i=0;i<ports.size();i++) {
	  	 Port p = (Port)ports.elementAt(i);
	  	 Figure figure = new Figure();
	  	 figure.setLocation(p.getLocation());
	  	 figure.setSize(p.getSize());
	  	 figure.setParent(this);
	  	 figure.getParent().translateToRelative(figure.getBounds());
	  	 figure.getParent().translateToAbsolute(figure.getBounds());	  	 
		 FixedAnchor anchor = new FixedAnchor(figure,p.getIdentity());
	  	 fixedAnchors.put(p.getIdentity(),anchor);
	  }
	}
	
	public AbstractConnectionAnchor getConnectionAnchor(String name) {
	  return getFixedConnectionAnchor(name);
	}
	
	public FixedAnchor getFixedConnectionAnchor(String name) {
	  if(fixedAnchors.containsKey(name))
	    return (FixedAnchor)fixedAnchors.get(name);
	  else return null;	
	}
	
	public AbstractConnectionAnchor getAnchor(Point p) {
	  return getFixedAnchor(p);
	}
	
	public FixedAnchor getFixedAnchor(Point p) {
 	  Enumeration e = fixedAnchors.elements();
	  while (e.hasMoreElements()) {
	    FixedAnchor anchor = (FixedAnchor)e.nextElement();
	    if(anchor.containsPoint(p))
	      return anchor;
	  }
      return null;
	}
	
	static int count = 0;
	
	public void resetFixedPorts(Vector ports) {
	  fixedAnchors = new Hashtable();
	  createFixedAnchors(ports);
	}
	
	protected boolean useLocalCoordinates() {
	  return true;
	}

}