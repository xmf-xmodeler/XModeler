package com.ceteva.client.xml;

import java.util.Vector;

public abstract class XML {
	
	public abstract void addChild(XML element);
	
	public abstract void addChildren(Vector elements);
	
	public abstract Vector getChildren();
	
	public void printString() {
		printString(0);
	}
	
	public abstract void printString(int indent);
}
