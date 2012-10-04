package com.ceteva.diagram.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.LayerConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.editPart.ConnectionLayerManager;
import com.ceteva.diagram.preferences.IPreferenceConstants;

public class GroupFigure extends Figure implements ZoomableFigure {

  private ScalableFreeformLayeredPane pane;
  private FreeformViewport viewport;

  public GroupFigure(String identity,Point position,Dimension size,ConnectionLayerManager connectionManager) {
	setLocation(position);
	setSize(size);
	
	pane = new ScalableFreeformLayeredPane();
	pane.setLayoutManager(new FreeformLayout());
	connectionManager.addLayer(identity,pane);
	
	setLayoutManager(new StackLayout());
	
	ScrollPane scrollpane = new ScrollPane();
	add(scrollpane);
	
	viewport = new FreeformViewport();

	scrollpane.setViewport(viewport);
	scrollpane.setContents(pane);
	setOpaque(true);
	
	preferenceUpdate();	
  }
  
  public IFigure getFeedbackLayer() {
	 return pane.getLayer(LayerConstants.PRIMARY_LAYER); 
  }
  
  public ScalableFigure getFigure() {
	return pane;
  }
  
  public IFigure getViewPort() {
	return viewport;
  }
  
  protected boolean useLocalCoordinates() {
	return true;
  }

  public void preferenceUpdate() {
	getPreferences();
  }
  
  public void getPreferences() {
	IPreferenceStore ipreferences = DiagramPlugin.getDefault().getPreferenceStore();
	RGB color = PreferenceConverter.getColor(ipreferences,IPreferenceConstants.DIAGRAM_BACKGROUND_COLOR);
	setBackgroundColor(ColorManager.getColor(color));
  }
}

