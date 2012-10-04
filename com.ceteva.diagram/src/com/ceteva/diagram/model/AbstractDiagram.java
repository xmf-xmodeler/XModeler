package com.ceteva.diagram.model;

import java.util.Collection;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ClientElement;
import com.ceteva.client.Droppable;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;
import com.ceteva.diagram.ImageProducer;

public class AbstractDiagram extends Container implements Droppable {
	
	private Graph graph = new Graph(this);
	private String filename = "";
	private String type = "";
    private RGB color;
    private String zoom = "100";
    boolean queuedZoom = false;
    private boolean dropEnabled = false;
	
	public AbstractDiagram(ClientElement parent, EventHandler handler, String identity) {
		super(parent, handler, identity,new Point(0,0),new Dimension(0,0),null,null);
	}
	
	public AbstractDiagram(ClientElement parent, EventHandler handler,String identity,Point location,Dimension size) {
		super(parent,handler, identity, location, size, null, null);
	}
	
	public void close() {
		graph.close();
		Message m = handler.newMessage("diagramClosed", 1);
        Value v1 = new Value(getIdentity());
        m.args[0] = v1;
        handler.raiseEvent(m);
	}
	
	public void dispose() {
		super.dispose();
		graph.dispose();
	}

	public boolean dropEnabled() {
		return dropEnabled;
	}
	
	public void enableDrop() {
		//if (!dropEnabled()) {
			setDroppable();
		//}
	}

	public void setDroppable() {
		// Over-ride as necessary
		dropEnabled = true;
	}
	
	public RGB getColor() {
		return color;
	}
	
	public Vector getContents() {
		Vector contents = (Vector)super.getContents().clone();
		contents.addAll((Collection)graph.getNodes());
		return contents;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getExportType() {
		return type;
	}
	
	public boolean getQueuedZoom() {
		return queuedZoom;
	}
	
	public String getZoom() {
		return zoom;
	}
	
	public void open() {
		Message m = handler.newMessage("diagramOpen", 1);
        Value v1 = new Value(getIdentity());
        m.args[0] = v1;
        handler.raiseEvent(m);
	}
	
	public void preferenceChange() {
		firePropertyChange("preferenceChange",null,null);
		
		// if the change is to the font size then we need to raise
		// and event to the diagram client so that it can adjust the
		// diagram to take into account the font change
		
		Message m = handler.newMessage("preferenceChange",1);
		Value v = new Value(identity);
		m.args[0] = v;
		handler.raiseEvent(m);
	}	
	
	public boolean processMessage(Message message) {
		if (super.processMessage(message))
			return true;
		if (message.hasName("exportImage") && message.arity == 3 && message.args[0].hasStrValue(identity)) {
			filename = message.args[1].strValue();
			type = message.args[2].strValue();
			ImageProducer.createImage(filename,this,type);
			return true;
		}
		if(message.hasName("copyToClipboard") && message.args[0].hasStrValue(identity) && message.arity == 1) {
			firePropertyChange("copyToClipboard",null,null);
	        return true;
	    }
		if(message.hasName("setColor") && message.args[0].hasStrValue(identity) && message.arity == 4) {
			int red = message.args[1].intValue;
			int green = message.args[2].intValue;
			int blue = message.args[3].intValue;
			setBackgroundColor(red,green,blue);
			return true;
		}
		if(message.hasName("zoomTo") && message.args[0].hasStrValue(identity) && message.arity == 2) {
			int percent = message.args[1].intValue;
			zoomTo(percent);
			return true;
		}
		else if(message.args[0].hasStrValue(identity) && (message.hasName("enableDrop") && message.arity==1)) {
			enableDrop();
			return true;
		}
		return graph.processMessage(message);
	}
	
	public void removeEdge(Edge edge) {
		graph.removeEdge(edge);
		if(isRendering())
		  firePropertyChange("newEdge",null, null);
	}
		  
	public void removeNode(Node node) {
		graph.removeNode(node);
		if(isRendering())
		  firePropertyChange("newNode",null, null);
	}
	
	public void render(boolean render) {
		super.render(render);
		if(!render)
		  graph.stopRender();
		else 
		  graph.startRender();
	}
	
	public void refreshZoom() {
	  graph.refreshZoom();
	  if(isRendering())
		firePropertyChange("zoom",null, null);
	  else 
		queuedZoom = true;
	  super.refreshZoom();
	}
	
	public void setBackgroundColor(int red,int green,int blue) {
		this.color = ModelFactory.getColor(red,green,blue);
		if(isRendering())
			firePropertyChange("backgroundColor",null, null); 
	}
	
	public void setQueuedZoom(boolean queuedZoom) {
		this.queuedZoom = queuedZoom;	
	}
	  
	public void synchronise(Element element) {
		String zoom = element.getString("zoom");
		this.zoom = zoom;
		graph.synchronise(element);
		super.synchronise(element);
	}
	
	public void zoomChanged(int zoom) {
		Integer z = new Integer(zoom);
		this.zoom = z.toString();
		Message m = handler.newMessage("zoomChanged",2);
		Value v1 = new Value(identity);
		Value v2 = new Value(z.intValue()); 
		m.args[0] = v1;
		m.args[1] = v2;
		handler.raiseEvent(m);	
	}
	
	public void zoomTo(int percent) {
		zoom = new Integer(percent).toString();
		if(isRendering())
		  firePropertyChange("zoom",null, null);
		else
		  queuedZoom = true;
	}
}