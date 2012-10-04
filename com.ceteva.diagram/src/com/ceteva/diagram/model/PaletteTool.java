package com.ceteva.diagram.model;

public class PaletteTool {

	public String name;
	public String identity;
	public boolean connection;
	public String icon;
    
    public PaletteTool(String name,String identity,boolean connection,String icon) {
	  this.name = name;
	  this.identity = identity;
	  this.connection = connection;
	  this.icon = icon;
	}
	
}
