package com.ceteva.diagram;

import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.client.FontManager;
import com.ceteva.client.IdManager;
import com.ceteva.client.XMLClient;
import com.ceteva.client.xml.Document;
import com.ceteva.client.xml.Element;
import com.ceteva.diagram.model.AbstractDiagram;
import com.ceteva.diagram.model.XMLBindings;
import com.ceteva.diagram.preferences.IPreferenceConstants;

public class DiagramClient extends XMLClient {
	
	boolean done = false;
	static private int globalRenderOff = 0;
	
    public DiagramClient() {
    	super("com.ceteva.diagram");
    }
    
    public boolean closeNoneDisplayedDiagrams() {
    	Hashtable ids = IdManager.getIds();
    	Iterator it = ids.values().iterator();
    	while(it.hasNext()) {
    		Object object = (Object)it.next();
    		if(object instanceof com.ceteva.diagram.model.Diagram) {
    			com.ceteva.diagram.model.Diagram diagram = (com.ceteva.diagram.model.Diagram)object;
    			if(!diagram.shown()) {
    			  diagram.close();	
    			}
    		}
    	}
    	return true;
    }
    
    public void displayDiagramModel(com.ceteva.diagram.model.Diagram diagram) {
    	DiagramPlugin diagramManager = DiagramPlugin.getDefault();
		IWorkbenchPage page = diagramManager.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		DiagramEditorInput input = new DiagramEditorInput(diagram);
		try {
		  if(handler != null) {
		    Diagram newDiagram = (Diagram)page.openEditor(input,"com.ceteva.diagram.Diagram");
		    newDiagram.buildPalette();
		    diagram.refreshZoom();
		    if (input.getModel().dropEnabled())
		    	diagram.setDroppable();
		  }
		} catch (PartInitException pie) {
		    System.out.println(pie);
	    }
    }
	
	public com.ceteva.diagram.model.Diagram newDiagramModel(String identity,String name,boolean show) {
	  com.ceteva.diagram.model.Diagram diagram = new com.ceteva.diagram.model.Diagram(handler, identity);
	  diagram.setName(name);
	  diagram.setEventHandler(handler);
	  diagram.open();
	  if(show)
	    displayDiagramModel(diagram);
	  return diagram;
	}
	
	public boolean setFocus(Message message) {
	  if(message.arity == 1) {
		String identity = message.args[0].strValue();
		if(IdManager.has(identity)) {
	  	  com.ceteva.diagram.model.Diagram diagram = (com.ceteva.diagram.model.Diagram)IdManager.get(identity);
	  	  if(diagram.shown()) {
	  	    DiagramPlugin diagramManager = DiagramPlugin.getDefault();
		    IWorkbenchPage page = diagramManager.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		    page.activate(diagram.getOwner());
	  	  }
	  	  else {
	  	    displayDiagramModel(diagram);
	  	  }
	      return true;
		}
	  }
	  return false;
	}
	
	public Value broadcastCall(Message message) {
		if (message.hasName("getTextDimension") && message.arity == 2) {
		  String text = message.args[0].strValue();
		  boolean italicise = message.args[1].boolValue;
		  return getTextDimension(text, italicise);
		}
		if (message.hasName("getTextDimensionWithFont") && message.arity == 2) {
		  String text =	message.args[0].strValue();
		  String font = message.args[1].strValue();
		  return getTextDimensionWithFont(text,font);
		}
		return new Value(false);
	}
	
	public Value getTextDimension(String text,boolean italicise) {
		IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
		FontData fontData = PreferenceConverter.getFontData(preferences,IPreferenceConstants.FONT);
		if(italicise)
		  fontData.setStyle(SWT.ITALIC);
		Font f = FontManager.getFont(fontData);
		Dimension d = FigureUtilities.getTextExtents(text,f);
		Value[] values = new Value[2];
		values[0] = new Value(d.width);
		values[1] = new Value(d.height);
		Value value = new Value(values);
		return value;
	}
	
	public Value getTextDimensionWithFont(String text,String font) {
		Font f = FontManager.getFont(new FontData(font));
		Dimension d = FigureUtilities.getTextExtents(text,f);
		Value[] values = new Value[2];
		values[0] = new Value(d.width);
		values[1] = new Value(d.height);
		Value value = new Value(values);
		return value;
	}
	
	public static boolean isRendering() {
		return globalRenderOff == 0;
	}
	
	public Value processCall(Message message) {
		return broadcastCall(message); 
	}

	public boolean processMessage(Message message) {
	  if(super.processMessage(message))
		return true;
	  if(message.hasName("newDiagram")) {
        String identity = message.args[0].strValue();
        String name = message.args[1].strValue();
		newDiagramModel(identity,name,false);
		return true;
	  }
	  else if(message.hasName("setFocus"))
	    return setFocus(message);
	  else if(message.hasName("closeNoneDisplayedDiagrams"))
		return closeNoneDisplayedDiagrams();
	  else if(message.hasName("globalRenderOff"))
		globalRenderOff++;
	  else if(message.hasName("globalRenderOn")) {
		globalRenderOff--;
		if(globalRenderOff == 0)
		  refreshDiagrams();
	  }
	  return IdManager.processMessage(message);
	}

	public void processXML(Document xml) {
	  // xml.printString();
	  synchronise(xml);
	}
	
	public void refreshDiagrams() {
    	Hashtable ids = IdManager.getIds();
    	Iterator it = ids.values().iterator();
    	while(it.hasNext()) {
    		Object object = (Object)it.next();
    		if(object instanceof AbstractDiagram) {
    			AbstractDiagram diagram = (AbstractDiagram)object;
    			if(diagram.isRendering())
    			  diagram.startRender();
    		}
    	}
	}

	public void synchronise(Element xml) {
		synchroniseDiagrams(xml);
	}
	
	public void synchroniseDiagrams(Element xml) {
		
		// check that there is a diagram for each of the document's diagrams
		
		for(int i=0;i<xml.childrenSize();i++) {
			Element child = xml.getChild(i);
			if(child.hasName(XMLBindings.diagram)) {
			  String identity = child.getString("identity");
			  boolean show = child.getBoolean("isOpen");
			  if(IdManager.has(identity)) {
				com.ceteva.diagram.model.Diagram diagram = (com.ceteva.diagram.model.Diagram)IdManager.get(identity);
			    diagram.synchronise(child);
			  }
			  else {
				String name = child.getString("name");
				com.ceteva.diagram.model.Diagram diagram = newDiagramModel(identity,name,show);
				diagram.synchronise(child);
			  }
			}
		}
		
		// check that there is a document diagram for each of the diagram
		
		// needs to be implemented
	}
	
	public void setEventHandler(EventHandler handler) {
	}

}