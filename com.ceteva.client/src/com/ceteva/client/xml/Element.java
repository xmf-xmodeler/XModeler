package com.ceteva.client.xml;

import java.util.Vector;

public class Element extends XML { // implements IElement {
	
	private String name;
	private Vector attributes = new Vector();
	private Vector elements = new Vector();
	
	public void addAttribute(Attribute att) {
		attributes.add(att);
	}
	
	public void addChild(int index,XML element) {
		if(element instanceof Element)
			elements.add(index,element);
	}
	
	public void addChild(XML element) {
		if(element instanceof Element)
			elements.add(element);
	}
	
	public void addChildren(Vector elements) {
		this.elements = elements;
	}
	
	public int childrenSize() {
		return elements.size();
	}
	
	public Vector getAttributes() {
		return attributes;
	}
	
	public boolean getBoolean(String name) {
		String value = getString(name);
		return value.toLowerCase().equals("true");
	}
	
	public Element getChild(int index) {
		return (Element)elements.get(index);
	}
	
	public Vector getChildren() {
		return elements;
	}
	
	public int getInteger(String name) {
		String value = getString(name);
		return Integer.parseInt(value);
	}
	
	public String getName() {
		return name;
	}
	
	public String getString(String name) {
		for(int i=0;i<attributes.size();i++) {
		  Attribute att = (Attribute)attributes.elementAt(i);
		  if(att.getName().equals(name))
		    return att.getValue();
		}
		System.err.println("Warning - attribute '" + name + "' cannot be found in element '" + this.name + "'");
		return "";
	}
	
	public boolean hasChildren() {
		return !elements.isEmpty();
	}
	
	public boolean hasName(String name) {
		return getName().equals(name);
	}
	
	public void print(int indent,String string) {
		for(int i=0;i<indent;i++)
		  System.err.print(" ");
		System.err.print(string + "\n");
	}
	
	public void printString(int indent) {
		String start = "<" + getName();
		for(int i=0;i<attributes.size();i++) {
		  Attribute att = (Attribute)attributes.elementAt(i);
		  start = start + " " + att.getName() + " = " + att.getValue();	
		}
		start = start + ">";
		print(indent,start);
		for(int i=0;i<elements.size();i++) {
		  XML element = (XML)elements.elementAt(i);
		  element.printString(indent + 2);
		}
		print(indent,"</" + getName() + ">");
	}
	
	public void removeChild(int index) {
		elements.remove(index);
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
