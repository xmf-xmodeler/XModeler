package com.ceteva.diagram.model;

import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

public class Node extends Container {

    private boolean isDraggable = false;
	private Vector  ports       = new Vector(); // ports associated with this node
    private Vector  sourceEdges = new Vector(); // all edges for which this node is the source
    private Vector  targetEdges = new Vector(); // all edges for which this node is the target
    
    private boolean isSelectable = true;

    public Node(ClientElement parent,EventHandler handler, String identity, Point location, Dimension size, boolean isSelectable) {
        super(parent, handler, identity, location, size, null, null);
        this.isSelectable = isSelectable;
    }

    public Node(ClientElement parent,EventHandler handler, String identity, int x, int y, int width, int height, boolean isSelectable) {
        this(parent,handler, identity, new Point(x, y), new Dimension(width, height), isSelectable);
    }
    
    public void delete() {
    	super.delete();
    	if(parent instanceof Diagram)
    	  ((Diagram)parent).removeNode(this);
    	else
    	  ((Group)parent).removeNode(this);
    }
    
    public void dispose() {
    	super.dispose();
    	for(int i=0;i<ports.size();i++) {
    	  Port p = (Port)ports.elementAt(i);
    	  p.dispose();
    	}
    }

    public Vector getPorts() {
        return ports;
    }
    

    public Vector getSourceEdges() {
        return sourceEdges();
    }

    public Vector getTargetEdges() {
        return targetEdges();
    }
    
    public boolean isDraggable() {
    	return isDraggable;
    }

    public boolean isSelectable() {
    	return isSelectable;
    }
    
    public Vector sourceEdges() {
    	Vector validEdges = new Vector();
    	for(int i=0;i<sourceEdges.size();i++) {
    	  Edge e = (Edge)sourceEdges.elementAt(i);
    	  if(getConnectionManager().isRenderingParent(e.parent.identity))
    	    validEdges.addElement(e);	  
    	}
    	return validEdges;
    }
    
    public Vector targetEdges() {
    	Vector validEdges = new Vector();
    	for(int i=0;i<targetEdges.size();i++) {
    	  Edge e = (Edge)targetEdges.elementAt(i);
    	  if(getConnectionManager().isRenderingParent(e.parent.identity))
    	    validEdges.addElement(e);	  
    	}
    	return validEdges;
    }

    public boolean processMessage(Message message) {
        if (message.hasName("enableDrag") && message.args[0].hasStrValue(identity) && message.arity == 1) {
        	this.isDraggable = true;
        	return true;
        }
    	if (message.hasName("newPort") && message.args[0].hasStrValue(identity) && message.arity == 6) {
            String identity = message.args[1].strValue();
            int x = message.args[2].intValue;
            int y = message.args[3].intValue;
            int width = message.args[4].intValue;
            int height = message.args[5].intValue;
            newPort(identity, x, y, width, height);
            return true;
        }
        return super.processMessage(message);
    }

    public Port newPort(String identity, int x, int y, int width, int height) {
        Port port = new Port(this,handler,identity,new Point(x,y),new Dimension(width,height));
        ports.addElement(port);
        PortRegistry.addPort(identity, this);
        if (isRendering())
          firePropertyChange("refreshPorts", null, null);
        return port;
    }

    // invoked by changes to the diagram
    // events are raised as a result

    public void moveResize(Point location, Dimension size) {
        if (!location.equals(this.location)) {
            Message m = handler.newMessage("move", 3);
            Value v1 = new Value(identity);
            Value v2 = new Value(location.x);
            Value v3 = new Value(location.y);
            m.args[0] = v1;
            m.args[1] = v2;
            m.args[2] = v3;
            handler.raiseEvent(m);
            this.move(location);
        }
        if (!size.equals(this.size)) {
            Message m = handler.newMessage("resizeNode", 3);
            Value v1 = new Value(identity);
            Value v2 = new Value(size.width);
            Value v3 = new Value(size.height);
            m.args[0] = v1;
            m.args[1] = v2;
            m.args[2] = v3;
            handler.raiseEvent(m);
        }
    }

    public void deselectNode() {
       Message m = handler.newMessage("nodeDeselected", 1);
       Value v1 = new Value(identity);
       m.args[0] = v1;
       handler.raiseEvent(m);
    }
    
    public void selectNode() {
        Message m = handler.newMessage("nodeSelected", 1);
        Value v1 = new Value(identity);
        m.args[0] = v1;
        handler.raiseEvent(m);
    }
    
    public void selected(int clicks) {
  	    Message m = handler.newMessage("selected", 2);
        Value v1 = new Value(identity);
        Value v2 = new Value(clicks);
        m.args[0] = v1;
        m.args[1] = v2;
        handler.raiseEvent(m);
    }
    
    public void synchronise(Element node) {
    	synchronisePorts(node);
    	synchroniseDisplays(node);
    	super.synchronise(node);
    }
    
    public void synchroniseDisplays(Element node) {
    	
    }
    
    public void synchronisePorts(Element node) {
    	
    	// Check there is a port for each of the ports in the document
    	
    	for(int i=0;i<node.childrenSize();i++) {
    	  Element child = (Element)node.getChild(i);
    	  if(child.hasName(XMLBindings.port)) {
    		boolean found = false;
    	    String id = child.getString("identity");
    	    for(int z=0;z<ports.size();z++) {
    	      Port port = (Port)ports.elementAt(z);
    	      if(port.getIdentity().equals(id)) {
    	        found = true;
    	        port.synchronise(child);
    	      }
    	    };
    	    if(!found) {
    	      int x = child.getInteger("x");
    	      int y = child.getInteger("y");
    	      int width = child.getInteger("width");
    	      int height = child.getInteger("height");
    	      Port port = newPort(id,x,y,width,height);
    	      ports.addElement(port);
    	      port.synchronise(child);
    	    }
    	  }	
    	}
    	
    	// Check that each of the nodes ports has a port in the document
    	
    	Vector toRemove = new Vector();
  	    for(int i=0;i<ports.size();i++) {
  	      boolean found = false;
  		  Port port = (Port)ports.elementAt(i);
  	      for(int z=0;z<node.childrenSize();z++) {
  	        Element child = (Element)node.getChild(z);
  		    if(child.hasName(XMLBindings.port)) {
  		      String id = child.getString("identity");
  		      found = port.getIdentity().equals(id);
  		    }
  	      };
  	      if(!found)
  	    	toRemove.addElement(port);
  	    }
  	    
  	    // Delete ports
  	    
  	    for(int i=0;i<toRemove.size();i++) {
  	      Port port = (Port)toRemove.elementAt(i);
  	      port.delete();
  	    }
    }

    public void addSourceEdge(Edge edge) {
        sourceEdges.addElement(edge);
        if (isRendering())
            firePropertyChange("sourceEdges", null, null);
    }

    public void removeSourceEdge(Edge edge) {
    	if(!sourceEdges.contains(edge))
          System.out.println("Node is not source of edge: " + edge.identity);
        sourceEdges.removeElement(edge);
        if (isRendering())
            firePropertyChange("sourceEdges", null, null);
    }

    public void addTargetEdge(Edge edge) {
        targetEdges.addElement(edge);
        if (isRendering())
            firePropertyChange("targetEdges", null, null);
    }

    public void removeTargetEdge(Edge edge) {
    	if(!targetEdges.contains(edge))
        	  System.out.println("Node is not target of edge: " + edge.identity);
        targetEdges.removeElement(edge);
        if (isRendering())
            firePropertyChange("targetEdges", null, null);
    }
}