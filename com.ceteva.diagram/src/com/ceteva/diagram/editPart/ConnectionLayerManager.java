package com.ceteva.diagram.editPart;

import java.util.Hashtable;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayeredPane;

public class ConnectionLayerManager {
	
	private Hashtable layers = new Hashtable();
	private LayeredPane printableLayers = null;
	private String diagramId = ""; 
	
	public void addLayer(String identity,IFigure layer) {
		layers.put(identity,layer);
		if(printableLayers != null)
		  printableLayers.add(layer);
	}
	
	public IFigure getLayer(String identity) {
		if(identity.equals(diagramId))
	      return printableLayers;
		return (IFigure)layers.get(identity);
	}
	
	public boolean isRenderingParent(String identity) {
		return identity.equals(diagramId) || layers.containsKey(identity);
	}
	
	public void reset() {
		resetPrintableLayers();
		layers = new Hashtable();
	}
	
	public void resetPrintableLayers() {
		printableLayers = null;
	}
	
	public void setPrintableLayers(LayeredPane pl) {
		printableLayers = pl;
	}
	
	public void setDiagramId(String identity) {
		diagramId = identity;
	}
}