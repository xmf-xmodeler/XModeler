package com.ceteva.diagram.dnd;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;

public class DropRequest extends Request {
	
	public static final String ID = "Drop Request";
	public static final String COPY = "copy";
	public static final String MOVE = "move";
	
	private String source;
	private Point location;
	private String type;
	
	public DropRequest(String source,String type) {
		super(ID);
		this.source = source;
		this.type = type;
	}
	
	public Point getLocation() {
		return location;
	}
	
	public String getDropType() {
		return type;
	}
	
	public String getSource() {
		return source;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

}