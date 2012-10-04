package com.ceteva.diagram.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.IdManager;
import com.ceteva.client.xml.Element;

public class Edge extends CommandEvent {

    private Node    sourceNode;
    private Node    targetNode;
    private String  sourcePort;
    private String  targetPort;
    private int     sourceHead;
    private int     targetHead;
    private int     style;
    private String  type  = "normal";
    private int		width = 1;
    private Point   refPoint  = new Point();
    private List    waypoints = new ArrayList();
    private Vector  labels    = new Vector();
    private boolean hidden    = false;
    private RGB color;

    public Edge(ClientElement parent,EventHandler handler, String identity, Node sourceNode, Node targetNode, String sourcePort, String targetPort, int xRef,
            int yRef, int sourceHead, int targetHead, int dotStyle,RGB color) {
        super(parent,handler, identity);
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.targetHead = targetHead;
        this.sourceHead = sourceHead;
        this.color = color;
        this.refPoint = new Point(xRef, yRef);
        if (dotStyle != 0)
            this.style = dotStyle;
        else
            this.style = 1;
        sourceNode.addSourceEdge(this);
        targetNode.addTargetEdge(this);
    }
    
    public void dispose() {
    	super.dispose();
    	for(int i=0;i<waypoints.size();i++) {
    	  Waypoint w = (Waypoint)waypoints.get(i);
    	  w.dispose();
    	}
    	for(int i=0;i<labels.size();i++)  {
    	  if(labels.elementAt(i) instanceof EdgeText) {
    	    EdgeText l = (EdgeText)labels.get(i);
    	    l.dispose();
    	  }
    	  else {
    		MultilineEdgeText l = (MultilineEdgeText)labels.get(i);
    		l.dispose();
    	  }
    	}
    }
    
    public RGB getColor() {
        return color;
    }

    public void stopRender() {
    	setRender(false);
        render(false);
    }

    public boolean identityExists(String identity) {
        if (this.identity.equals(identity))
            return true;
        for (int i = 0; i < labels.size(); i++) {
            CommandEvent et = (CommandEvent)labels.elementAt(i);
            if (et.identityExists(identity))
                return true;
        }
        return false;
    }

    public void startRender() {
    	setRender(true);
        render(true);
    }

    public void render(boolean render) {
        for (int i = 0; i < labels.size(); i++) {
            Object o = labels.elementAt(i);
            if(o instanceof EdgeText) {
        	  EdgeText label = (EdgeText)o;
              if (!render)
                label.stopRender();
              else
                label.startRender();
            }
            if(o instanceof MultilineEdgeText) {
              MultilineEdgeText label = (MultilineEdgeText)o;
              if (!render)
                label.stopRender();
              else
                label.startRender();	
            }
        }
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint waypoint = (Waypoint) waypoints.get(i);
            if (!render)
                waypoint.stopRender();
            else {
                waypoint.startRender();
            }
        }
    }

    public void delete() {
    	super.delete();
        sourceNode.removeSourceEdge(this);
        targetNode.removeTargetEdge(this);
        sourceNode = null;
        targetNode = null;
        AbstractDiagram diagram = (AbstractDiagram)parent;
        diagram.removeEdge(this);
        
        
        /* if(parent instanceof Diagram)
    	  ((Diagram)parent).removeEdge(this);
        else
          ((Group)parent).removeEdge(this);
        if (isRendering())
            firePropertyChange("newSourceTarget", null, null); */
    }

    public void refresh() {
        if (isRendering())
           firePropertyChange("newSourceTarget", null, null);
    }

    public void hide() {
        hidden = true;
        // if(render)
        firePropertyChange("visibilityChange", null, null);
    }

    public void setNewSource(Node newSource, String sourcePort) {
        sourceNode.removeSourceEdge(this);
        this.sourceNode = newSource;
        sourceNode.addSourceEdge(this);
        this.sourcePort = sourcePort;
        if (isRendering())
            firePropertyChange("newSourceTarget", null, null);
    }

    public void setNewTarget(Node newTarget, String targetPort) {
        targetNode.removeTargetEdge(this);
        this.targetNode = newTarget;
        targetNode.addTargetEdge(this);
        this.targetPort = targetPort;
        if (isRendering())
            firePropertyChange("newSourceTarget", null, null);
    }
    
    public boolean hidden() {
        return hidden;
    }

    public void show() {
        hidden = false;
        // if(render)
         firePropertyChange("visibilityChange", null, null);
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public String getTargetPort() {
        return targetPort;
    }

    public List getWaypoints() {
        return waypoints;
    }

    public int getSourceHead() {
        return sourceHead;
    }

    public int getTargetHead() {
        return targetHead;
    }
    
    public String getType() {
    	return type;
    }

    public int getStyle() {
        return style;
    }

    public boolean processMessage(Message message) {
        if (message.hasName("setEdgeSource") && message.args[0].hasStrValue(identity) && message.arity == 2) {
            String sourcePort = message.args[1].strValue();
            Node node = PortRegistry.getNode(sourcePort);
            if (node != null)
                setNewSource(node, sourcePort);
            return true;
        }
        if (message.hasName("setEdgeTarget") && message.args[0].hasStrValue(identity) && message.arity == 2) {
            String targetPort = message.args[1].strValue();
            Node node = PortRegistry.getNode(targetPort);
            if (node != null)
                setNewTarget(node, targetPort);
            return true;
        }
        if (message.hasName("setEdgeWidth") && message.args[0].hasStrValue(identity) && message.arity == 2) {
        	width = message.args[1].intValue;
        	if(isRendering())
        	  firePropertyChange("newEdgeWidth", null, null);
        	return true;
        }
        if (message.hasName("changeHeads") && message.args[0].hasStrValue(identity) && message.arity == 3) {
            int sourceHead = message.args[1].intValue;
            int targetHead = message.args[2].intValue;
            changeHeads(sourceHead, targetHead);
            return true;
        }
        if (message.hasName("newWaypoint") && message.args[0].hasStrValue(identity) && message.arity == 5) {
            String waypointID = message.args[1].strValue();
            int index = message.args[2].intValue;
            int x = message.args[3].intValue;
            int y = message.args[4].intValue;
            addNewWaypoint(waypointID, index, x, y);
            return true;
        }
        if (message.hasName("newEdgeText") && message.args[0].hasStrValue(identity)) {
            labels.addElement(ModelFactory.newEdgeText(this,handler, message));
            if (isRendering())
                firePropertyChange("newEdgeText", null, null);
            return true;
        }
        if (message.hasName("newMultilineEdgeText") && message.args[0].hasStrValue(identity)) {
        	labels.addElement(ModelFactory.newMultilineEdgeText(this,handler,message));
        	if(isRendering())
        		firePropertyChange("newEdgeText", null, null);
        	return true;
        }
        if (message.hasName("setRefPoint") && message.args[0].hasStrValue(identity) && message.arity == 3) {
            int x = message.args[1].intValue;
            int y = message.args[2].intValue;
            this.changeRefPoint(new Point(x, y));
            return true;
        }
        if (message.hasName("setEdgeStyle") && message.args[0].hasStrValue(identity) && message.arity == 2) {
        	int style = message.args[1].intValue;
        	this.setStyle(style);
        	return true;
        }
        if (message.hasName("setEdgeType") && message.args[0].hasStrValue(identity) && message.arity == 2) {
        	String type = message.args[1].strValue();
        	this.setType(type);
        	return true;
        }
        if (message.hasName("show") && message.args[0].hasStrValue(identity) && message.arity == 1) {
            show();
            return true;
        }
        if (message.hasName("hide") && message.args[0].hasStrValue(identity) && message.arity == 1) {
            hide();
            return true;
        }
    	if(message.hasName("setColor") && message.args[0].hasStrValue(identity) && message.arity == 4) {
    	    int red = message.args[1].intValue;
    	    int green = message.args[2].intValue;
    		int blue = message.args[3].intValue;
    		setColor(red,green,blue);
    		return true;
    	}
        return super.processMessage(message);
    }
    
    public void addDummyWaypoint(int index, Point location) {
    	Waypoint waypoint = new Waypoint(this, handler, "dummy", location);
    	waypoints.add(index,waypoint);
        if (isRendering())
          firePropertyChange("waypoints", null, null);
    }

    public void addNewWaypoint(String waypointID, int index, int x, int y) {
    	if(!IdManager.changeId("dummy",waypointID)) {
          Waypoint waypoint = new Waypoint(this, handler, waypointID, new Point(x,y));
          waypoints.add(index, waypoint);
          if (isRendering())
            firePropertyChange("waypoints", null, null);
    	}
    }
    
    public void removeEdgeText(EdgeText edgetext) {
        labels.remove(edgetext);
        if (isRendering())
            firePropertyChange("waypoints", null, null);
    }

    public void removeWaypoint(Waypoint waypoint) {
        waypoints.remove(waypoint);
        if (isRendering())
            firePropertyChange("waypoints", null, null);
    }

    public String getWaypointIdentity(int index) {
        Waypoint waypoint = (Waypoint) waypoints.get(index);
        return waypoint.getIdentity();
    }
    
    public int getWidth() {
    	return width;
    }

    public boolean emptyWaypoints() {
        return waypoints.size() == 0;
    }

    public void changeHeads(int sourceHead, int targetHead) {
        this.sourceHead = sourceHead;
        this.targetHead = targetHead;
        if (isRendering())
            firePropertyChange("headsChange", null, null);
    }

    public Vector getContents() {
        return labels;
    }

    public void edgeDeselected() {
      Message m = handler.newMessage("edgeDeselected", 1);
      Value v1 = new Value(identity);
      m.args[0] = v1;
      handler.raiseEvent(m);
  }
    
    public void edgeSelected() {
        Message m = handler.newMessage("edgeSelected", 1);
        Value v1 = new Value(identity);
        m.args[0] = v1;
        handler.raiseEvent(m);
    }

    public void setRefPoint(Point refPoint) {
        if (!this.refPoint.equals(refPoint)) {
            raiseRefPointEvent(refPoint);
            this.refPoint = refPoint;
            if (isRendering())
                firePropertyChange("refPoint", null, null);
        }
    }

    public void changeRefPoint(Point refPoint) {
        this.refPoint = refPoint;
        if (isRendering())
            firePropertyChange("refPoint", null, null);
    }

    public void raiseRefPointEvent(Point position) {
        Message m = handler.newMessage("setReferencePoint", 3);
        Value v1 = new Value(getIdentity());
        Value v2 = new Value(position.x);
        Value v3 = new Value(position.y);
        m.args[0] = v1;
        m.args[1] = v2;
        m.args[2] = v3;
        handler.raiseEvent(m);
    }

    public Point getRefPoint() {
        return refPoint;
    }
    
    public void selected(int clicks) {
  	  Message m = handler.newMessage("selected", 2);
      Value v1 = new Value(identity);
      Value v2 = new Value(clicks);
      m.args[0] = v1;
      m.args[1] = v2;
      handler.raiseEvent(m);
    }
    
	public void setColor(int red,int green,int blue) {
		color = ModelFactory.getColor(red,green,blue);
		if(isRendering())
		  firePropertyChange("color",null, null); 
	}
    
    public void setStyle(int style) {
    	this.style = style;
    	if (isRendering())
    	  firePropertyChange("styleChange",null,null);
    }
    
    public void setType(String type) {
    	this.type = type;
    	if (isRendering())
    	  firePropertyChange("typeChange",null,null);
    }
    
    public void synchronise(Element edge) {
    	synchroniseWaypoints(edge);
    	synchroniseLabels(edge);
    	synchroniseMultilineLabels(edge);
    	String sourcePort = edge.getString("source");
    	if(!sourcePort.equals(this.sourcePort)) {
          Node node = PortRegistry.getNode(sourcePort);
          if (node != null)
            setNewSource(node, sourcePort);		
    	}
    	String targetPort = edge.getString("target");
    	if(!targetPort.equals(this.targetPort)) {
    	  Node node = PortRegistry.getNode(targetPort);
    	  if (node != null)
    	    setNewTarget(node, targetPort);
    	}
    	sourceHead = edge.getInteger("sourceHead");
    	targetHead = edge.getInteger("targetHead");
    	style = edge.getInteger("lineStyle");
    	width = edge.getInteger("width");
    	refPoint.x = edge.getInteger("refx");
    	refPoint.y = edge.getInteger("refy");
    }
    
    public void synchroniseLabels(Element edge) {
    	
  	  // Check that there is a label for each of the model's edge labels
  	  
  	  for(int i=0;i<edge.childrenSize();i++) {
  		Element child = (Element)edge.getChild(i);
  		if(child.hasName(XMLBindings.label)) {
  	  	  String id = child.getString("identity");
  		  boolean found = false;
  		  for(int z=0;z<labels.size();z++) {
  			if(labels.elementAt(z) instanceof EdgeText) {
  			  EdgeText edgeText = (EdgeText)labels.elementAt(z);
  			  if(edgeText.getIdentity().equals(id)) {
  			    found = true;
  			    edgeText.synchronise(child);
  			  }
  			}
  		  }
  		  if(!found) {
  			String identity = child.getString("identity");
  			String text = child.getString("text");
  			String attachedTo = child.getString("attachedTo");
  			int relx = child.getInteger("x");
  			int rely = child.getInteger("y");
  			boolean editable = child.getBoolean("editable");
  			boolean underline = child.getBoolean("underline");
  			int truncate = child.getInteger("truncate");
  			String font = child.getString("font");
  			EdgeText edgeText = new EdgeText(this,handler,identity,attachedTo,relx,rely,text,editable,underline,truncate,font);
  			labels.add(edgeText);
  			edgeText.synchronise(child);
  		  }
  		}
  	  }
  				
  	  // Check that each of the edge labels is represented in the document

  	  Vector toRemove = new Vector();
  	  for(int i=0;i<labels.size();i++) {
  		 if(labels.elementAt(i) instanceof EdgeText) {
  		   EdgeText text = (EdgeText)labels.elementAt(i);
  		   String id = text.getIdentity();
  		   boolean found = false;
  		   for(int z=0;z<edge.childrenSize();z++) {
  		     Element child = (Element)edge.getChild(z);
  		     if(child.hasName(XMLBindings.label) && child.getString("identity").equals(id))
  		       found = true;
  		   }
    	   if(!found)
    		 toRemove.add(text);
  		 }
  	  }
  	  
  	  // Delete edge text
  	  
  	  for(int i=0;i<toRemove.size();i++) {
  		EdgeText text = (EdgeText)toRemove.elementAt(i);
  		text.delete();
  	  }
    }
    
    public void synchroniseMultilineLabels(Element edge) {
    	  
    	// Check that there is a multiline label for each of the model's edge multiline labels
    	  
    	  for(int i=0;i<edge.childrenSize();i++) {
    		Element child = (Element)edge.getChild(i);
    		if(child.hasName(XMLBindings.multilinetext)) {
    	  	  String id = child.getString("identity");
    		  boolean found = false;
    		  for(int z=0;z<labels.size();z++) {
    			if(labels.elementAt(z) instanceof MultilineEdgeText) {
    		      MultilineEdgeText edgeText = (MultilineEdgeText)labels.elementAt(z);
    			  if(edgeText.getIdentity().equals(id)) {
    			    found = true;
    			    edgeText.synchronise(child);
    			  }
    			}
    		  }
    		  if(!found) {
    			String identity = child.getString("id");
    			String text = child.getString("text");
    			String attachedTo = child.getString("attachedTo");
    			int relx = child.getInteger("x");
    			int rely = child.getInteger("y");
    			boolean editable = child.getBoolean("editable");
    			boolean underline = child.getBoolean("underline");
    			int truncate = child.getInteger("truncate");
    			String font = child.getString("font");
    			MultilineEdgeText edgeText = new MultilineEdgeText(this,handler,identity,attachedTo,relx,rely,text,editable,underline,truncate,font);
    			labels.add(edgeText);
      			edgeText.synchronise(child);
    		  }
    		}
    	  }
    				
    	  // Check that each of the edge labels is represented in the document
    	  
    	  Vector toRemove = new Vector();
    	  for(int i=0;i<labels.size();i++) {
    		 if(labels.elementAt(i) instanceof MultilineEdgeText) {
    		   MultilineEdgeText text = (MultilineEdgeText)labels.elementAt(i);
    		   String id = text.getIdentity();
    		   boolean found = false;
    		   for(int z=0;z<edge.childrenSize();z++) {
    		     Element child = (Element)edge.getChild(z);
    		     if(child.hasName(XMLBindings.label) && child.getString("identity").equals(id))
    		       found = true;
    		   }
      	       if(!found)
      	    	 toRemove.add(text);
    	     }
    	  }
    	  
      	  // Delete multiline edge text
      	  
      	  for(int i=0;i<toRemove.size();i++) {
      		MultilineEdgeText text = (MultilineEdgeText)toRemove.elementAt(i);
      		text.delete();
      	  }
    }
    
    public void synchroniseWaypoints(Element edge) {
    	
    	// The most efficent way of synchronising waypoints is
    	// to remove them all and reconstruct them from the document

        waypoints = new ArrayList();
        for(int i=0;i<edge.childrenSize();i++) {
            Element child = edge.getChild(i);
            if(child.getName().equals(XMLBindings.waypoint)) {

              // add in a dummy	
            
              waypoints.add("dummy");
            }
        }
        for(int i=0;i<edge.childrenSize();i++) {
          Element child = edge.getChild(i);
          if(child.getName().equals(XMLBindings.waypoint)) {
            String id = child.getString("identity");
            int index = child.getInteger("index");
            int x = child.getInteger("x");
            int y = child.getInteger("y");
            Waypoint waypoint = new Waypoint(this,handler,id,new Point(x,y));
            waypoints.set(index,waypoint);
          }
        }
    }
}