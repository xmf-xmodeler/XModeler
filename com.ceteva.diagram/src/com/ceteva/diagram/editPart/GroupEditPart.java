package com.ceteva.diagram.editPart;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.ImageProducer;
import com.ceteva.diagram.editPolicy.LayoutPolicy;
import com.ceteva.diagram.figure.GroupFigure;
import com.ceteva.diagram.figure.ZoomableFigure;
import com.ceteva.diagram.model.AbstractDiagram;
import com.ceteva.diagram.model.Group;
import com.ceteva.diagram.palette.PopupPalette;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.diagram.tracker.DisplaySelectionTracker;
import com.ceteva.diagram.zoom.CustomZoomManager;

public class GroupEditPart extends ZoomableGraphicalEditPart {
	
	public GroupEditPart() {
		super();
	}
	
	protected IFigure createFigure() {
	  Group group = getGroupModel();
	  Point location = group.getLocation();
	  Dimension size = group.getSize();
	  ConnectionLayerManager connectionManager = group.getConnectionManager();
	  GroupFigure groupFigure = new GroupFigure(group.getIdentity(),location,size,connectionManager);
	  
	  // Set in case this figure is being redrawn
	  group.setQueuedZoom(true);
	  return groupFigure;
	}
	
	protected List getModelChildren() {
	  return getGroupModel().getContents();
	}
	
    public DragTracker getDragTracker(Request request) {
		return new DisplaySelectionTracker(this);
	}
	
	protected void createEditPolicies() {
	  installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutPolicy());
	  installEditPolicy("PopupPalette", new PopupPalette());
	}
	
	public Group getGroupModel() {
	  return (Group)getModel();	
	}
	
	public ZoomManager getZoomManager() {
		// nested diagrams do not animate their zoom since this is expensive 
		// when redrawing a diagram with many sub diagrams.
		
		if(zoomManager == null) {
		  ZoomableFigure figure = (ZoomableFigure)getFigure();	
		  zoomManager = new CustomZoomManager((ScalableFigure)figure.getFigure(),((Viewport)figure.getViewPort()));
		  zoomManager.setZoomAnimationStyle(ZoomManager.ANIMATE_NEVER);
		}
		return zoomManager;
	}
	
	public void propertyChange(PropertyChangeEvent evt)  {
	  String prop = evt.getPropertyName();
	  if(prop.equals("newNode"))
	    refreshChildren();
	  if(prop.equals("delete"))
		refreshChildren();
	  if(prop.equals("backgroundColor"))
		refreshBackgroundColor();
	  if(prop.equals("locationSize"))
	  	refreshVisuals();	 
	  if(prop.equals("startRender")) {
		this.refresh();
		this.refreshZoom();
	  }
	  if(prop.equals("zoom"))
		zoom();
	  if(prop.equals("exportImage")) {
		AbstractDiagram group = (AbstractDiagram)getModel();
		String filename = group.getFilename();
		String type = group.getExportType();
		GroupFigure figure = (GroupFigure)getFigure();
		ImageProducer.createImage(filename, figure.getFigure(), type);
	  }
	  if(prop.equals("copyToClipboard"))
		ImageProducer.copyToClipboard(getLayer(LayerConstants.PRINTABLE_LAYERS));
    }
	
	protected void refreshVisuals() {
      Group group = (Group)getModel();  
	  Point loc = group.getLocation();
	  Dimension size = group.getSize();
	  Rectangle r = new Rectangle(loc ,size);
	  ((GraphicalEditPart) getParent()).setLayoutConstraint(this,getFigure(),r);
	}
	
	public void refreshZoom() {
      Group group = (Group)getModel();
	  if(group.getQueuedZoom()) {  
		group.setQueuedZoom(false);
		zoom();
	  }
	}
    
	public IFigure getContentPane() {
	    return ((GroupFigure)getFigure()).getFigure();
	}

	public void preferenceUpdate() {
	  GroupFigure figure = (GroupFigure)getFigure();
	  figure.preferenceUpdate();
	  List children = getChildren();
	  for(int i=0;i<children.size();i++) {
		CommandEventEditPart part = (CommandEventEditPart)children.get(i);
		part.preferenceUpdate();
	  }
      figure.repaint();
	}
	
    public void performRequest(Request req) {
    	Group group = (Group)getModel();
    	Object request = req.getType();
    	if(request == RequestConstants.REQ_DIRECT_EDIT) {
    	  group.selected(1);
    	}
    	else if(request == RequestConstants.REQ_OPEN) {
    	  group.selected(2);
    	}
    }
    
    public void zoom() {
    	Group group = getGroupModel();
    	this.getViewer().flush();
    	getZoomManager().setZoomAsText(group.getNestedZoom());
    }
    
    public Shell getShell() {
		return Display.getCurrent().getActiveShell();
	}

    public RGB getBackgroundColor() {
    	RGB color = ((AbstractDiagram)getModel()).getColor();
    	if(color != null)
    		return color;
    	IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
    	return PreferenceConverter.getColor(preferences,IPreferenceConstants.DIAGRAM_BACKGROUND_COLOR);
    }

    public void refresh() {
    	refreshBackgroundColor();
    	refreshZoom();
    	super.refresh();	
    }
    
    public void refreshBackgroundColor() {
    	getFigure().setBackgroundColor(ColorManager.getColor(getBackgroundColor()));
    }

}