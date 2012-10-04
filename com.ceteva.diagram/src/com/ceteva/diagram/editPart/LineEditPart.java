package com.ceteva.diagram.editPart;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.figure.LineFigure;
import com.ceteva.diagram.model.Line;
import com.ceteva.diagram.preferences.IPreferenceConstants;

public class LineEditPart extends DisplayEditPart {
	
	Line model;
	LineFigure line;
	
	protected IFigure createFigure() {	
	  model = (Line)getModel();
	  Point start = model.getStart();
	  Point end = model.getEnd();
	  line = new LineFigure(start,end);
	  return line;
	}
	
	protected void createEditPolicies() {
	}
	
	public RGB getColor() {
	   RGB lineColor = ((Line)getModel()).getColor();
	   if(lineColor != null)
	     return lineColor;
	   IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
	   return PreferenceConverter.getColor(preferences,IPreferenceConstants.EDGE_COLOR);
	}
	
	public boolean isSelectable() {
	   return false;
	}
	
	public void propertyChange(PropertyChangeEvent evt)  {
      String prop = evt.getPropertyName();
      if(prop.equals("startRender"))
        this.refresh();
	  if(prop.equals("locationSize"))
	    refreshVisuals();
	  if(prop.equals("color"))
	  	refreshColor();
	  if (prop.equals("visibilityChange")) {
        this.getFigure().setVisible(!((com.ceteva.diagram.model.Display)getModel()).hidden());
        this.getViewer().deselectAll();
      }
	}
	
	protected void refreshVisuals() {
	  Point start = ((Line)getModel()).getStart();
      Point end = ((Line)getModel()).getEnd();
      line.setStart(start);
      line.setEnd(end);
      refreshColor();
	}
	
    public void refreshColor() {
      getFigure().setForegroundColor(ColorManager.getColor(getColor()));
    }
}