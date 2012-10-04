package com.ceteva.diagram.editPolicy; 

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.figure.LabelFigure;
import com.ceteva.diagram.preferences.IPreferenceConstants;

public class TextSelectionPolicy extends org.eclipse.gef.editpolicies.GraphicalEditPolicy
{
  private Color revertColor;

  private Color selectedColor = null;
  
  public TextSelectionPolicy() {
	getPreferences();
  }

  public void eraseTargetFeedback(Request request){
	if (revertColor != null){
		setContainerBackground(revertColor);
		revertColor = null;
	}
  }

  private IFigure getContainerFigure(){
	return ((GraphicalEditPart)getHost()).getFigure();
  }

  public EditPart getTargetEditPart(Request request){
	return request.getType().equals(RequestConstants.REQ_SELECTION_HOVER) ?
		getHost() : null;
  }

  private void setContainerBackground(Color c){
	if(getContainerFigure() instanceof LabelFigure) {
		LabelFigure label = (LabelFigure)getContainerFigure();
		revertColor = label.getForegroundColor();
		label.setForegroundColor(c);
	}
  }

  protected void showHighlight(){
	if (revertColor == null){
		if(getContainerFigure() instanceof LabelFigure) {
		  setContainerBackground(selectedColor);
		}
	}	
  }

  public void showTargetFeedback(Request request){
	showHighlight();
  }
  
  public void preferenceUpdate() {
    getPreferences();	
  }
  
  public void getPreferences() {
	IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
	RGB fontColor = PreferenceConverter.getColor(preferences,IPreferenceConstants.SELECTED_FONT_COLOR);
	selectedColor = ColorManager.getColor(fontColor);
  }

}