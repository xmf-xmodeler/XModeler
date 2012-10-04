package com.ceteva.diagram.command;

import org.eclipse.draw2d.geometry.Point;

import XOS.Message;
import XOS.Value;

import com.ceteva.diagram.model.AbstractDiagram;

public class DropCommand extends org.eclipse.gef.commands.Command {

	String type;
	String source;
	AbstractDiagram diagram;
	Point location;
	
	public DropCommand(String type,String source,AbstractDiagram diagram,Point location) {
		this.type = type;
		this.source = source;
		this.diagram = diagram;
		this.location = location;
	}
	
	public void execute() {
		// System.out.println("Drop Command(" + type + "," + source + "," + diagram.identity + "," + location + ")");
		Message m = diagram.handler.newMessage("dragAndDrop",5);
		Value v1 = new Value(diagram.getIdentity());
		Value v2 = new Value(type);
		Value v3 = new Value(source);
		Value v4 = new Value(location.x);
		Value v5 = new Value(location.y);
		m.args[0] = v1;
		m.args[1] = v2;
		m.args[2] = v3;
		m.args[3] = v4;
		m.args[4] = v5;
		diagram.handler.raiseEvent(m);
	}
}
