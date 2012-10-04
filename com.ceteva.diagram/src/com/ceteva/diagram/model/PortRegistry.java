package com.ceteva.diagram.model;

import java.util.Hashtable;

// this class provides a mapping between port names
// and their owning nodes

class PortRegistry {

  private static Hashtable mappings = new Hashtable();
  
  public static void addPort(String portName,Node node) {
  	 mappings.put(portName,node);
  }
  
  public static Node getNode(String portName) {
  	 return (Node)mappings.get(portName);
  }
  
  public static void removePort(String portName) {
	 mappings.remove(portName);
  }
}