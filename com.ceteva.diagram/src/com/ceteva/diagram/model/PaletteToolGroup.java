package com.ceteva.diagram.model;

import java.util.Vector;

public class PaletteToolGroup {

	public String name;
	public Vector tools = new Vector();
	
	public PaletteToolGroup(String name) {
		this.name = name;
	}
	
	public void add(String parent,PaletteTool tool) {
	  if(parent.equals(name))
	    tools.addElement(tool);
	}
	
}
