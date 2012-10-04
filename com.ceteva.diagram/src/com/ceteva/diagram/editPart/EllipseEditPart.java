package com.ceteva.diagram.editPart;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.figure.EllipseFigure;
import com.ceteva.diagram.model.Ellipse;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.diagram.tracker.DisplaySelectionTracker;

public class EllipseEditPart extends DisplayEditPart {
	
	protected IFigure createFigure() {
	  Ellipse ellipse = (Ellipse)getModel();
	  Point location = ellipse.getLocation();
	  Dimension size = ellipse.getSize();
	  boolean outline = ellipse.getOutline();
	  EllipseFigure ef = new EllipseFigure(location,size,outline);
	  ef.setLayoutManager(new XYLayout());
	  return ef;
	}
	
    public DragTracker getDragTracker(Request request) {
		return new DisplaySelectionTracker(this);
	}
	
    public RGB getFillColor() {
        RGB fillColor = ((Ellipse) getModel()).getFillColor();
        if(fillColor != null)
          return fillColor;
        IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
        return PreferenceConverter.getColor(preferences,IPreferenceConstants.FILL_COLOR);
    }
    
    public RGB getForegroundColor() {
        RGB lineColor = ((Ellipse) getModel()).getForegroundColor();
        if(lineColor != null)
          return lineColor;
        IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
        return PreferenceConverter.getColor(preferences,IPreferenceConstants.FOREGROUND_COLOR);
    }
	
	protected List getModelChildren() {
	  return ((Ellipse)getModel()).getContents();
	}
	
	protected void createEditPolicies() {
	}
	
	public void propertyChange(PropertyChangeEvent evt)  {
	  String prop = evt.getPropertyName();
	  if(prop.equals("startRender"))
	  	this.refresh();
	  if(prop.equals("locationSize"))
		refreshVisuals();
	  else if(prop.equals("displayChange"))
		refreshChildren();
	  else if(prop.equals("color"))
	  	refreshColor();
      else if (prop.equals("visibilityChange")) {
        this.getFigure().setVisible(!((com.ceteva.diagram.model.Display)getModel()).hidden());
        this.getViewer().deselectAll();
      }
	}
	
	public void refreshColor() {
	  getFigure().setForegroundColor(ColorManager.getColor(getForegroundColor()));
	  getFigure().setBackgroundColor(ColorManager.getColor(getFillColor()));
	}
	
	protected void refreshVisuals() {
	  Ellipse model = (Ellipse)getModel();
	  Point loc = ((Ellipse)getModel()).getLocation();
	  Dimension size = ((Ellipse)getModel()).getSize();
	  boolean fill = model.getfill();
	  EllipseFigure f = (EllipseFigure)getFigure();
	  f.setFill(fill);
	  Rectangle r = new Rectangle(loc ,size);
	  ((GraphicalEditPart) getParent()).setLayoutConstraint(this,getFigure(),r);
	  refreshColor();
	}
	
	public boolean isSelectable() {
	  return true;
	}
	
	public void preferenceUpdate() {
	  refreshColor();
	  List children = getChildren();
	  for(int i=0;i<children.size();i++) {
		CommandEventEditPart part = (CommandEventEditPart)children.get(i);
		part.preferenceUpdate();
	  }
	}
}