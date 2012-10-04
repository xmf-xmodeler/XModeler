package com.ceteva.diagram.editPart;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.draw2d.Figure;
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
import com.ceteva.diagram.figure.BoxFigure;
import com.ceteva.diagram.figure.RoundedBoxFigure;
import com.ceteva.diagram.model.AbstractDiagram;
import com.ceteva.diagram.model.Box;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.diagram.tracker.DisplaySelectionTracker;

public class BoxEditPart extends DisplayEditPart {
	
	protected IFigure createFigure() {
		Box box = (Box)getModel();
		Figure rectangle = null;
		int borderCurve = box.getBorderCurve();
		if(borderCurve == 0)
		  rectangle = new BoxFigure(box.getLocation(),box.getSize(),box.showTop(),box.showRight(),box.showBottom(),box.showLeft());
		else
		  rectangle = new RoundedBoxFigure(box.getLocation(),box.getSize(),borderCurve);
		rectangle.setLayoutManager(new XYLayout());
		return rectangle;
	}
	
    public RGB getFillColor() {
        RGB fillColor = ((Box) getModel()).getFillColor();
        if(fillColor != null)
          return fillColor;
        IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
        return PreferenceConverter.getColor(preferences,IPreferenceConstants.FILL_COLOR);
    }
    
    public RGB getForegroundColor() {
        RGB lineColor = ((Box) getModel()).getForegroundColor();
        if(lineColor != null)
          return lineColor;
        IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
        return PreferenceConverter.getColor(preferences,IPreferenceConstants.FOREGROUND_COLOR);
    }
    
    public boolean getGradient() {
    	Preferences preferences = DiagramPlugin.getDefault().getPluginPreferences();
    	return preferences.getBoolean(IPreferenceConstants.GRADIENT_FILL);
    }
	
	protected List getModelChildren() {
		return ((Box)getModel()).getContents();
	}
	
    public DragTracker getDragTracker(Request request) {
		return new DisplaySelectionTracker(this);
	}
	
	protected void createEditPolicies() {
	}
	
	public void propertyChange(PropertyChangeEvent evt)  {
	  String prop = evt.getPropertyName();
	  if(prop.equals("startRender"))
	  	this.refresh();
	  if(prop.equals("locationSize"))
		refreshVisuals();
	  else if (prop.equals("color"))
        refreshColor();
	  else if(prop.equals("displayChange"))
		refreshChildren();
      else if (prop.equals("visibilityChange")) {
    	refreshVisuals();  
      }
	}
	
    public void refreshColor() {
      IFigure f = getFigure();
      if(f instanceof BoxFigure)
        ((BoxFigure)getFigure()).setGradientFill(getGradient());
      else
      	((RoundedBoxFigure)getFigure()).setGradientFill(getGradient());
      getFigure().setForegroundColor(ColorManager.getColor(getForegroundColor()));
      getFigure().setBackgroundColor(ColorManager.getColor(getFillColor()));
    }
	
	protected void refreshVisuals() {
	  Box model = (Box)getModel();
	  Point loc = model.getLocation();
	  Dimension size = model.getSize();
	  boolean fill = model.getfill();
	  Figure f = (Figure)getFigure();
	  
	  // if the figure is either <=1 depth or <=1 height
	  // don't display it (hack)
	  
	  if(size.height <= 1 || size.width <= 1)
		f.setVisible(false);
	  else
        f.setVisible(!model.hidden());
      this.getViewer().deselectAll();
	  if(f instanceof BoxFigure) {
	  	 BoxFigure fig = (BoxFigure)f;
		 fig.setFill(fill);
		 fig.top = model.showTop();
		 fig.bottom = model.showBottom();
		 fig.left = model.showLeft();
		 fig.right = model.showRight();
	  }
	  else
	     ((RoundedBoxFigure)f).setFill(fill);
	  Rectangle r = new Rectangle(loc ,size);
	  ((GraphicalEditPart) getParent()).setLayoutConstraint(this,getFigure(),r);
	  refreshColor();
	}
	
	public boolean isSelectable() {
	  Box box = (Box)getModel();
	  return !(box.parent instanceof AbstractDiagram);
	}
	
	public void preferenceUpdate() {
	  // IFigure figure = getFigure();
	  refreshColor();
	  /* if(figure instanceof BoxFigure) {
	    BoxFigure box = (BoxFigure)figure;
	    box.preferenceUpdate();
	  }
	  else {
	  	RoundedBoxFigure rounded = (RoundedBoxFigure)figure;
	  	rounded.preferenceUpdate();
	  } */
	  
	  List children = getChildren();
	  for(int i=0;i<children.size();i++) {
		CommandEventEditPart part = (CommandEventEditPart)children.get(i);
		part.preferenceUpdate();
	  }
	  this.deactivate();
	}
	

}