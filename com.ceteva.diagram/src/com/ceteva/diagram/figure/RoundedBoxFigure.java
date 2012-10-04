package com.ceteva.diagram.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.preferences.IPreferenceConstants;

public class RoundedBoxFigure extends RoundedRectangle {
	
	boolean gradient = true;
	
	public RoundedBoxFigure(Point position,Dimension size,int curve) {
	  this.setLocation(position);
	  this.setSize(size);
	  this.setCornerDimensions(new Dimension(curve,curve));
	  preferenceUpdate();
	}
	
	protected void fillShape(Graphics graphics) {
	  graphics.fillRoundRectangle(getBounds(), corner.width, corner.height);
	}
	
	protected boolean useLocalCoordinates() {
	  return true;
	}
	
	public void preferenceUpdate() {
	  getPreferences();
	}
    
	public void getPreferences() {
	  IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
	  RGB color = PreferenceConverter.getColor(preferences,IPreferenceConstants.FILL_COLOR);
	  setBackgroundColor(ColorManager.getColor(color));    	
	}
	
	public void setGradientFill(boolean b) {
	  this.gradient = b;
	}
}