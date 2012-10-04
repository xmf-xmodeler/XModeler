package com.ceteva.diagram.figure;

import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;

public class LineFigure extends Polyline {

	public LineFigure(Point start,Point end) { 
	  this.setStart(start);
	  this.setEnd(end);
	}
}