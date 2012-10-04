package com.ceteva.diagram.figure;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.Diagram;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.preferences.IPreferenceConstants;

public class DiagramFigure extends FreeformLayer {

  boolean gridlines = false;
  int distance = 20;
  Color gridColor = ColorManager.getColor(new RGB(206,206,206));

  public DiagramFigure() {
	getPreferences();
  }
  
  org.eclipse.swt.graphics.Image image = null;
  
  /* public org.eclipse.swt.graphics.Image getImage() {
	if(image == null) {
		image = new org.eclipse.swt.graphics.Image(Display.getDefault(),1000,500);  
		GC gc = new GC(image,SWT.NONE);  
		Graphics canvas = new SWTGraphics(gc);
		canvas.setForegroundColor(gridColor);
		canvas.drawRectangle(30,30,200,200); 
	}
	return image;
  } */
	
  protected void paintFigure(Graphics g) {
	if(Diagram.antialias)
	  g.setAntialias(SWT.ON);
	super.paintFigure(g);
	// g.drawImage(getImage(),0,0);
	if(gridlines)
	  drawGridlines(distance,g);
  }
  
  public void drawGridlines(int distance,Graphics g) {
	g.setForegroundColor(gridColor);
	Rectangle region = new Rectangle();
	g.getClip(region);
	g.fillRectangle(region);
	g.setLineWidth(1);
	for (int i=region.x - 2; i<region.right()+2; i++)
	  if (i % distance == 0)
		g.drawLine(i, region.y-2, i, region.bottom()+2);
	for (int i=region.y-2; i<region.bottom()+2; i++)
	  if (i % distance == 0)
		g.drawLine(region.x-2, i, region.right()+2, i);  			
  }
  
  public void preferenceUpdate() {
    getPreferences();
  }
  
  public void getPreferences() {
	Preferences preferences = DiagramPlugin.getDefault().getPluginPreferences();
	gridlines = preferences.getBoolean(IPreferenceConstants.GRIDLINES);
	distance = preferences.getInt(IPreferenceConstants.GRIDSIZE);
	IPreferenceStore ipreferences = DiagramPlugin.getDefault().getPreferenceStore();
	RGB color = PreferenceConverter.getColor(ipreferences,IPreferenceConstants.DIAGRAM_BACKGROUND_COLOR);
	setBackgroundColor(ColorManager.getColor(color));
  }
}