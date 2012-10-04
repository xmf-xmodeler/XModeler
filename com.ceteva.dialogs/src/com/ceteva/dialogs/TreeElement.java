package com.ceteva.dialogs;

import java.util.Vector;

public class TreeElement {
  
	private String name;
	private Vector children = new Vector();
	private TreeElement owner;
	
	public TreeElement(TreeElement owner,String name) {
		this.owner = owner;
		this.name = name;
	}
	
	public void addChild(TreeElement child) {
		children.addElement(child);
	}
	
	public Object[] getChildren() {
		Object[] o = children.toArray();
		return o;
	}
	
	public TreeElement getOwner() {
		return owner;
	}
	
	public String toString() {
		return name;
	}
	
	public void getPath(Vector path) {
	  path.addElement(toString());
	  if(owner.owner != null)
	    owner.getPath(path);
	}
}
