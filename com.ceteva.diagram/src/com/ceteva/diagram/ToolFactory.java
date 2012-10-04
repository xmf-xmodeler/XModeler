package com.ceteva.diagram;

import org.eclipse.gef.requests.CreationFactory;

public class ToolFactory implements CreationFactory {

  private String identity;
  private String iconFilename = "";

  public ToolFactory(String identity) {
	this.identity = identity;
  }
  
  public ToolFactory(String identity,String iconFilename) {
	this.identity = identity;
	this.iconFilename = iconFilename;
  }
  
  public String getIconFilename() {
	return iconFilename;
  }

  public Object getNewObject() {
	return identity;
  }

  public Object getObjectType() {
	return identity;
  }
}