package com.ceteva.diagram.editPart;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
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
import com.ceteva.diagram.editPolicy.EdgeTextEditPolicy;
import com.ceteva.diagram.editPolicy.EdgeTextMoveEditPolicy;
import com.ceteva.diagram.editPolicy.EdgeTextSelectionPolicy;
import com.ceteva.diagram.figure.EdgeFigure;
import com.ceteva.diagram.figure.EdgeLabelFigure;
import com.ceteva.diagram.model.EdgeText;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.diagram.tracker.EdgeTextDragTracker;

public class EdgeTextEditPart extends CommandEventEditPart implements MouseListener {
	
	private DirectEditManager manager = null;
	private EdgeText model = null;
	
	public DragTracker getDragTracker(Request request) {
	  return getDragTracker(); 	
	}
	
	public DragTracker getDragTracker() {
      return new EdgeTextDragTracker(this,(EdgeEditPart)getParent()); 	
	}
	
	protected IFigure createFigure() {
	  model = (EdgeText)getModel();
	  String textString = model.getText();
	  boolean underline = model.getUnderline();
	  EdgeLabelFigure label = new EdgeLabelFigure(textString,underline);
	  return label;
	}
	
	public RGB getColor() {
	  RGB color = ((EdgeText)getModel()).getColor();
	  if(color != null)
	    return color;
	  IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
	  return PreferenceConverter.getColor(preferences,IPreferenceConstants.UNSELECTED_FONT_COLOR);
	}
	
	public int getCondenseSize() {
	  return model.getCondenseSize();
	}
	
	public Point getEdgePosition() {
	  EdgeEditPart parent = (EdgeEditPart)getParent();
	  EdgeFigure edgeFigure = (EdgeFigure)parent.getFigure();
	  // EdgeText model = (EdgeText)getModel();
	  String position = model.getPosition();
	  if(position.equals("start"))
	    return edgeFigure.getStart();
	  else if(position.equals("end"))
	    return edgeFigure.getEnd();
	  else
		return edgeFigure.getPoints().getMidpoint();
	}
	
	public EdgeEditPart getEdgeEditPart() {
	  return (EdgeEditPart)getParent();	
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
	  getFigure().setForegroundColor(ColorManager.getColor(getColor()));
	}
	
	protected void refreshVisuals() {
	  EdgeLabelFigure figure = (EdgeLabelFigure)getFigure();
	  String text = model.getText();
	  figure.setText(text);
	  figure.setFont(model.getFont());
  	  figure.setVisible(!model.hidden());
	  Point offset = ((EdgeText)getModel()).getLocation();
	  EdgeEditPart parent = (EdgeEditPart)getParent();
	  EdgeTextConstraint constraint = new EdgeTextConstraint(this,model.getText(),getFigure(),parent,model.getPosition(),offset);
	  parent.setLayoutConstraint(this,getFigure(),constraint);
	  refreshColor();
	}
	
	protected void createEditPolicies() {
	  installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeTextMoveEditPolicy());
	  installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new EdgeTextSelectionPolicy());
	  if(model.isEditable())
	    installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new EdgeTextEditPolicy());
	}
	
	public void preferenceUpdate() {
	  refreshColor();
	  EdgeLabelFigure figure = (EdgeLabelFigure)getFigure();
	  figure.preferenceUpdate();
	  figure.repaint();
   }
	
	public void performRequest(Request request) {
	  Object type = request.getType();
	  if(type == RequestConstants.REQ_DIRECT_EDIT || type == RequestConstants.REQ_OPEN) {
	    if (model.isEditable())
	      performDirectEdit();
	  }
	}
	
	public void performDirectEdit(){
		if(manager == null)
		  manager = new TextEditManager(this, TextCellEditor.class, new TextCellEditorLocator((Label)getFigure()));
		manager.show();
	}
	
	public EdgeLabelFigure getEdgeFigure() {
	  return (EdgeLabelFigure)getFigure();	
	}

	public void mouseDoubleClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent me) {
		me.consume();
		performRequest(new Request(RequestConstants.REQ_DIRECT_EDIT));
	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}