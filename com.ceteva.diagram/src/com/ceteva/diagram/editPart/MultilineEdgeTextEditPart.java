package com.ceteva.diagram.editPart;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.editPolicy.MultilineEdgeTextEditPolicy;
import com.ceteva.diagram.editPolicy.MultilineEdgeTextMovePolicy;
import com.ceteva.diagram.editPolicy.MultilineEdgeTextSelectionPolicy;
import com.ceteva.diagram.figure.EdgeFigure;
import com.ceteva.diagram.figure.MultilineEdgeTextFigure;
import com.ceteva.diagram.model.MultilineEdgeText;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.diagram.tracker.EdgeTextDragTracker;

public class MultilineEdgeTextEditPart extends CommandEventEditPart {
	
	private DirectEditManager manager = null;
	private MultilineEdgeText model = null;
	
	protected IFigure createFigure() {
		model = (MultilineEdgeText)getModel();
		String text = model.getText();
		Point position = model.getLocation();
		RGB forecolor = getForeColor();
		MultilineEdgeTextFigure mulilinetext = new MultilineEdgeTextFigure(position,forecolor);
		mulilinetext.setText(text);
		return mulilinetext;
	}
	
	public RGB getForeColor() {
		RGB foreColor = model.getColor();
		if(foreColor != null)
		  return foreColor;
		IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
		return PreferenceConverter.getColor(preferences,IPreferenceConstants.UNSELECTED_FONT_COLOR);
	}
	
	public void propertyChange(PropertyChangeEvent evt)  {
	  String prop = evt.getPropertyName();
	  if(prop.equals("startRender"))
	  	 refresh();
	  if(prop.equals("locationSize"))
	     refreshVisuals();
	  if(prop.equals("textChanged"))
	     refreshVisuals();
	  if(prop.equals("color"))
	  	 refreshColor();
      if (prop.equals("visibilityChange")) {
    	 refreshVisuals();
         this.getViewer().deselectAll();
      }
	}
	
	public void refreshColor() {
	  getFigure().setForegroundColor(ColorManager.getColor(getForeColor()));
	}
	
	protected void refreshVisuals() {
	  MultilineEdgeTextFigure figure = (MultilineEdgeTextFigure)getFigure();
	  String string = model.getText();
	  Point offset = model.getLocation();
	  String position = model.getPosition();
	  figure.setText(string);
	  figure.setFont(model.getFont());
	  figure.setVisible(!model.hidden());
	  EdgeEditPart parent = (EdgeEditPart)getParent();	  
	  MultilineEdgeTextConstraint constraint = new MultilineEdgeTextConstraint(this,string,parent.getFigure(),parent,position,offset);
	  parent.setLayoutConstraint(this,getFigure(),constraint);
	  refreshColor();
	}
	
	protected void createEditPolicies() {
	  installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new MultilineEdgeTextMovePolicy());
	  installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new MultilineEdgeTextSelectionPolicy());
	  if(model.isEditable())
	    installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new MultilineEdgeTextEditPolicy());
	}
	
	public void performRequest(Request request){
	  Object type = request.getType();
	  if(type == RequestConstants.REQ_DIRECT_EDIT || type == RequestConstants.REQ_OPEN) {
	    if (model.isEditable())
	      performDirectEdit();
	  }
	}
	
	public DragTracker getDragTracker(Request request) {
	  return new EdgeTextDragTracker(this,(EdgeEditPart)getParent());
	}
	
	public EdgeEditPart getEdgeEditPart() {
	  return (EdgeEditPart)getParent();	
	}
	
	public Point getEdgePosition() {
	  EdgeEditPart parent = (EdgeEditPart)getParent();
	  EdgeFigure edgeFigure = (EdgeFigure)parent.getFigure();
	  String position = model.getPosition();
	  if(position.equals("start"))
	    return edgeFigure.getStart();
	  else if(position.equals("end"))
	    return edgeFigure.getEnd();
	  else
		return edgeFigure.getPoints().getMidpoint();
	}
	
	private void performDirectEdit(){
		if(manager == null)
			manager = new MultilineEdgeEditManager(this, 
				TextCellEditor.class, new MultilineEdgeCellEditorLocator((MultilineEdgeTextFigure)getFigure()));
		manager.show();
	}
	
	public void preferenceUpdate() {
	  refreshColor();
	  MultilineEdgeTextFigure figure = (MultilineEdgeTextFigure)getFigure();
	  figure.preferenceUpdate();
	  figure.repaint();
	}
}
