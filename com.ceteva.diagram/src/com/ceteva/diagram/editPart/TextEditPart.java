package com.ceteva.diagram.editPart;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.editPolicy.TextEditPolicy;
import com.ceteva.diagram.editPolicy.TextSelectionPolicy;
import com.ceteva.diagram.figure.LabelFigure;
import com.ceteva.diagram.model.Text;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.diagram.tracker.DisplaySelectionTracker;

public class TextEditPart extends DisplayEditPart {
	
	static private TextEditManager manager = null;
	private Text model = null;
	private TextSelectionPolicy textSelectionPolicy = null;
	
	public void activate() {	
		super.activate();
		Text model = (Text)getModel();
		if(model.edit())
		  this.editText();
    }
	
	protected IFigure createFigure() {
		model = (Text)getModel();
		String text = model.getText();
		Point position = model.getLocation();
		boolean underline = model.getUnderline();
		boolean italicise = model.getItalicise();
		LabelFigure label = new LabelFigure(position,text,underline,italicise);
		return label;
	}
	
	public void editText() {
		if (model.isEditable())
		  performDirectEdit();
	}
	
	public RGB getColor() {
		RGB lineColor = ((Text)getModel()).getColor();
		if(lineColor != null)
		  return lineColor;
		IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
		return PreferenceConverter.getColor(preferences,IPreferenceConstants.UNSELECTED_FONT_COLOR);
	}
	
	public void propertyChange(PropertyChangeEvent evt)  {
	  String prop = evt.getPropertyName();
	  if(prop.equals("editText"))
		  editText();
	  if(prop.equals("startRender"))
		  this.refresh();
	  if(prop.equals("locationSize"))
	     refreshVisuals();
	  if(prop.equals("textChanged"))
	     refreshVisuals();
	  if(prop.equals("color"))
	  	 refreshColor();
      if (prop.equals("visibilityChange")) {
          this.getFigure().setVisible(!((com.ceteva.diagram.model.Display)getModel()).hidden());
          this.getViewer().deselectAll();
      }
	}
	
	public void refreshColor() {
	  getFigure().setForegroundColor(ColorManager.getColor(getColor()));
	}
	
	protected void refreshVisuals() {
	  LabelFigure figure = (LabelFigure)getFigure();
	  Text text = (Text)getModel();
	  String string = text.getText();
	  figure.setText(string);
	  figure.setFont(text.getFont());
	  figure.setItalicise(text.getItalicise());
	  figure.setUnderline(text.getUnderline());
	  figure.setVisible(!text.hidden());
	  Point loc = text.getLocation();
	  Rectangle r = new Rectangle(loc,new Dimension(-1,-1));
	  ((GraphicalEditPart) getParent()).setLayoutConstraint(this,getFigure(),r);
	  refreshColor();
	}
	
	protected void createEditPolicies() {
	  textSelectionPolicy = new TextSelectionPolicy();	
	  installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, textSelectionPolicy);
	  if(model.isEditable())
	    installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new TextEditPolicy());
	}
	
	public void performRequest(Request request){
	  Object type = request.getType();
	  if(type == RequestConstants.REQ_DIRECT_EDIT || type == RequestConstants.REQ_OPEN) {
	    if (model.isEditable()) {
	      performDirectEdit();
	      return;
	    }
	  }
	  super.performRequest(request);
	  
	}
	
	public DragTracker getDragTracker(Request request) {
	  return new DisplaySelectionTracker(this);
	}
	
	private void performDirectEdit(){
		if(manager != null) {
		  manager.cancel();
		  manager = null;
		}
		TextCellEditorLocator tcel = new TextCellEditorLocator((LabelFigure)getFigure());
		manager = new TextEditManager(this,TextCellEditor.class, tcel);
		manager.show();
	}
	
	public void preferenceUpdate() {
	  
	  // preference changes are only observed if the font
	  // was programmatically set
		
	  if(model.getFont().equals("")) {	
	    refreshColor();
	    LabelFigure figure = (LabelFigure)getFigure();
	    figure.preferenceUpdate();
	    figure.repaint();
	  }
	  textSelectionPolicy.preferenceUpdate();
	}
}
