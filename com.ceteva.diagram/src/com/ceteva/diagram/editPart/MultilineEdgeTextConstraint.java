package com.ceteva.diagram.editPart;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

import com.ceteva.diagram.figure.EdgeFigure;

public class MultilineEdgeTextConstraint implements Locator {
	
	String text;
	IFigure parent;
	EdgeEditPart edgeEditPart;
	MultilineEdgeTextEditPart edgeTextEditPart;
	EdgeFigure edgeFigure;
	String position;
	Point offset;
	
	public MultilineEdgeTextConstraint(MultilineEdgeTextEditPart edgeTextEditPart,String text,IFigure parent,EdgeEditPart edgeEditPart,String position,Point offset) {
		this.edgeTextEditPart = edgeTextEditPart;
		this.text = text;
		this.parent = parent;
		this.edgeEditPart = edgeEditPart;
		this.position = position;
		this.offset = offset;
		edgeFigure = edgeEditPart.getEdgeFigure();
	}
	
	public void relocate(IFigure figure) {
		// figure.setSize(edgeTextEditPart.getSize());
		Dimension minimum = FigureUtilities.getTextExtents(text,figure.getFont());
		figure.setSize(minimum);
		Point endLocation;
		if(position.equals("start"))
		  endLocation = edgeFigure.getStart();
		else if(position.equals("end"))
		  endLocation = edgeFigure.getEnd();
		else
		  endLocation = getMiddle();
		Point offsetCopy = offset.getCopy();
		offsetCopy.translate(endLocation);
		figure.setLocation(offsetCopy);
	}
	
	public Point getMiddle() {
		PointList points = edgeFigure.getPoints();
		return points.getMidpoint();
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setOffset(Point offset) {
		this.offset = offset;
	}
}