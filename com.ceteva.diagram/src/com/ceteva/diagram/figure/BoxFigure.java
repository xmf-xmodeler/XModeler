package com.ceteva.diagram.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

// public class BoxFigure extends Shape { // RectangleFigure {

public class BoxFigure extends RectangleFigure {
	
	public boolean gradient = true;
	public boolean top = true;
	public boolean left = true;
	public boolean right = true;
	public boolean bottom = true;
	
	public BoxFigure(Point position,Dimension size,boolean top,boolean right,boolean bottom,boolean left) { 
	  this.setBounds(new Rectangle(position,size));
	  this.top = top;
	  this.right = right;
	  this.bottom = bottom;
	  this.left = left;
	}
	
	protected boolean useLocalCoordinates() {
	  return true;
	}
	
	protected void fillShape(Graphics graphics){
	  if(gradient)
	    graphics.fillGradient(getBounds(),false);
	  else
	    graphics.fillRectangle(getBounds());
	}

	protected void outlineShape(Graphics graphics){
		Rectangle r = getBounds();
		int x = r.x + lineWidth/2;
		int y = r.y + lineWidth/2;
		// int w = r.width - lineWidth;
		// int h = r.height - lineWidth;
		int w = r.width - Math.max(1, lineWidth);
		int h = r.height - Math.max(1, lineWidth);
		
		if(top)
		  graphics.drawLine(x,y,x+w,y);
		if(left)
		  graphics.drawLine(x,y,x,y+h);
		if(right)
		  graphics.drawLine(x+w,y,x+w,y+h);
		if(bottom)
		  graphics.drawLine(x,y+h,x+w,y+h);
	}
	
	public void setGradientFill(boolean b) {
		this.gradient = b;
	}
}