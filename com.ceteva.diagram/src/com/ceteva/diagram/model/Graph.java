package com.ceteva.diagram.model;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.graphics.RGB;

import XOS.Message;

import com.ceteva.client.xml.Element;

class Graph {
	
	private CommandEvent parent;
	private Vector nodes = new Vector();
	private Vector edges = new Vector();
	
	// There can be multiple calls to stop/start render, these are recorded
	// Rendering is only enabled if this has not already happened (hiddenCount = 0)
	// Rendering is only disable if there is one element on the stack (hiddenCount = 1)
	
	private int hiddenCount = 0;
	
	public Graph(CommandEvent parent) {
	  this.parent = parent;	
	}
	
	public void close() {
	  for(int i=0;i<nodes.size();i++) {
	    Node n = (Node)nodes.elementAt(i);
		n.close();
	  }
	}
	
	public void dispose() {
	  for(int i=0;i<nodes.size();i++) {
		Node n = (Node)nodes.elementAt(i);
		n.dispose();
	  }
	  for(int i=0;i<edges.size();i++) {
		Edge e = (Edge)edges.elementAt(i);
		e.dispose();
	  }
	}
	
	public Vector getNodes() {
		return nodes;
	}
	
	public void stopRender() {
		parent.setRender(false);
		render(false);
	}
		
	public void startRender() {
		parent.setRender(true);
		render(true);
		if(parent.isRendering())
		  parent.firePropertyChange("startRender",null,null);
	}
		
	public void render(boolean render) {
		for(int i=0;i<nodes.size();i++) {
		  Node node = (Node)nodes.elementAt(i);
		  if(render == false)
		    node.stopRender();
		  else 
		    node.startRender();
		}
		for(int i=0;i<edges.size();i++)  {
		  Edge edge = (Edge)edges.elementAt(i);
		  if(render == false)
		    edge.stopRender();
		  else
		    edge.startRender();
		}	
	}
	
	public boolean processMessage(Message message) {
	  if(message.args[0].hasStrValue(parent.getIdentity())) {
		if(message.hasName("newNode")) {
		  String identity = message.args[1].strValue();
		  int x = message.args[2].intValue;
		  int y = message.args[3].intValue;
		  int width = message.args[4].intValue;
		  int height = message.args[5].intValue;
		  boolean isSelectable = message.args[6].boolValue;
		  newNode(identity,x,y,width,height,isSelectable);
		  return true;
		}
		else if(message.hasName("newEdge")) {	
		  String identity = message.args[1].strValue();;
		  String sourcePort = message.args[2].strValue();;
		  String targetPort = message.args[3].strValue();;
		  int xRef = message.args[4].intValue;
		  int yRef = message.args[5].intValue;
		  int sourceHead = message.args[6].intValue;
		  int targetHead = message.args[7].intValue;
		  int dotStyle = message.args[8].intValue;
		  int red = message.args[9].intValue;
		  int green = message.args[10].intValue;
		  int blue = message.args[11].intValue;
		  newEdge(identity,
				  sourcePort,
				  targetPort,
				  xRef,
				  yRef,
				  sourceHead,
				  targetHead,
				  dotStyle,
			      ModelFactory.getColor(red,green,blue));
		  return true;
		}
		else if(message.hasName("stopRender") && message.arity == 1) {
		  if(hiddenCount == 0)
			stopRender();
		  hiddenCount++;
		  return true;	
		}
		else if(message.hasName("startRender") && message.arity == 1) {
		  if(hiddenCount == 1)
		    startRender();
		  hiddenCount--;
		  return true;	
		}
      }
	  return false;
    }
  
	public Node newNode(String identity,int x,int y,int width,int height,boolean isSelectable) {
	  Node node = new Node(this.parent,parent.handler,identity,x,y,width,height,isSelectable);
	  nodes.addElement(node);
	  if(parent.isRendering())
	    parent.firePropertyChange("newNode",null,null);
	  return node;
	}
  
	public Edge newEdge(String identity,
						String sourcePort,
						String targetPort,
						int xRef,
						int yRef,
						int sourceHead,
						int targetHead,
						int dotStyle,
						RGB color) 
	{
	  Node source = (Node)PortRegistry.getNode(sourcePort);
	  Node target = (Node)PortRegistry.getNode(targetPort); 
	  if(source != null && target != null) {	
		Edge edge = new Edge(this.parent,
				             parent.handler,
							 identity,
							 source,
							 target,
							 sourcePort,
							 targetPort,
							 xRef,
							 yRef,
							 sourceHead,
							 targetHead,
							 dotStyle,
							 color);
		edges.addElement(edge);
		if(parent.isRendering())
		  parent.firePropertyChange("newEdge",null,null);
		return edge;
	  }
	  return null;
	}
	
	public void removeEdge(Edge edge) {
	  if(!edges.contains(edge))
	    System.out.println("Edge does not exist");
	  edges.removeElement(edge);
	  if(parent.isRendering())
		parent.firePropertyChange("delete",null,null);	
	}
	
	public void removeNode(Node node) {
	  nodes.removeElement(node);
	  if(parent.isRendering())
		parent.firePropertyChange("delete",null,null);	
	}
	
	public void refreshZoom() {
	  for(int i=0;i<nodes.size();i++) {
	    Node node = (Node)nodes.elementAt(i);
	    node.refreshZoom();
	  }
	}
	
	public void synchronise(Element diagram) {
	  stopRender();
	  synchroniseNodes(diagram);
	  synchroniseEdges(diagram);
	  startRender();
	}
	
	public void synchroniseEdges(Element diagram) {

	  // Check that there is a edge for each of the diagram edges
	  
	  for(int i=0;i<diagram.childrenSize();i++) {
		Element child = (Element)diagram.getChild(i);
		String id = child.getString("identity");
		if(child.hasName(XMLBindings.edge)) {
		  boolean found = false;
		  for(int z=0;z<edges.size();z++) {
			Edge edge = (Edge)edges.elementAt(z);
			if(edge.getIdentity().equals(id)) {
			  found = true;
			  edge.synchronise(child);
			}
		  }
		  if(!found) {
			String source = child.getString("source");
			String target = child.getString("target");
			int refx = child.getInteger("refx");
			int refy = child.getInteger("refy");
			int sourceHead = child.getInteger("sourceHead");
			int targetHead = child.getInteger("targetHead");
			int lineStyle = child.getInteger("lineStyle");
			Edge edge = newEdge(id,source,target,refx,refy,sourceHead,targetHead,lineStyle,null);
			edge.synchronise(child);
		  }
		}
	  }
				
	  // Check that each of the diagram edge has a edge in the document
	
	  Vector toRemove = new Vector();
	  for(int i=0;i<edges.size();i++) {
		boolean found = false;
		Edge edge = (Edge)edges.elementAt(i);
		for(int z=0;z<diagram.childrenSize();z++) {
		  Element child = (Element)diagram.getChild(z);
		  String id = child.getString("identity");
			if(child.hasName(XMLBindings.edge)) {
			  if(edge.getIdentity().equals(id))
				found = true;
		    }
		};
		if(!found)
		  toRemove.addElement(edge);
	  }
	  
	  // Delete edges
	  
	  for(int i=0;i<toRemove.size();i++) {
	    Edge edge = (Edge)toRemove.elementAt(i);
	    edge.delete();
	  }
	}
	
	public void synchroniseNodes(Element diagram) {
		  
	  // Check that there is a node for each of the diagram nodes
		
	  for(int i=0;i<diagram.childrenSize();i++) {
		Element child = (Element)diagram.getChild(i);
		String id = child.getString("identity");
		if(child.hasName(XMLBindings.node)) {
		  boolean found = false;
		  for(int z=0;z<nodes.size();z++) {
		    Node node = (Node)nodes.elementAt(z);
		    if(node.getIdentity().equals(id)) {
		      found = true;
		      node.synchronise(child);
		    }
		  }
		  if(!found) {
			int x = child.getInteger("x");
			int y = child.getInteger("y");
			int width = child.getInteger("width");
			int height = child.getInteger("height");
			boolean selectable = child.getBoolean("selectable");
			Node node = newNode(id,x,y,width,height,selectable);
			node.synchronise(child);
		  }
		}
	  }
			
	  // Check that each of the diagram nodes has a node in the document
	  
	  Vector toRemove = new Vector();
	  for(int i=0;i<nodes.size();i++) {
		boolean found = false;
		Node node = (Node)nodes.elementAt(i);
		for(int z=0;z<diagram.childrenSize();z++) {
		  Element child = (Element)diagram.getChild(z);
		  String id = child.getString("identity");
		  if(child.hasName(XMLBindings.node)) {
			if(node.getIdentity().equals(id))
			  found = true;
		  }
		};
		if(!found)
		  toRemove.addElement(node);
	  }
	  
	  // Remove nodes
	  
	  for(int i=0;i<toRemove.size();i++) {
		Node node = (Node)toRemove.elementAt(i);
		node.delete();
	  }
	}
}