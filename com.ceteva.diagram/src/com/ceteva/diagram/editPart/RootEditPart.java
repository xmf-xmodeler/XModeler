package com.ceteva.diagram.editPart;

import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.diagram.zoom.CustomZoomManager;
import com.ceteva.diagram.zoom.ZoomableEditPart;

public class RootEditPart extends ScalableFreeformRootEditPart implements ZoomableEditPart {
	
	private CustomZoomManager zoomManager;
	
	public LayeredPane getPrintableLayers() {
		return super.getPrintableLayers();	
	}

    public ZoomManager getZoomManager() {
		if (zoomManager == null) {
			zoomManager = new CustomZoomManager((ScalableFigure)getScaledLayers(),((Viewport)getFigure()));
			refreshEnableZoomAnimation(zoomManager);
		}
		return zoomManager;
	}
	
    public void preferenceUpdate() {
		refreshEnableZoomAnimation(zoomManager);
    }
    
	private void refreshEnableZoomAnimation(ZoomManager zoomMangr) {
		IPreferenceStore preferenceStore = DiagramPlugin.getDefault().getPreferenceStore();
		boolean animatedZoom = preferenceStore.getBoolean(IPreferenceConstants.ANIMATEZOOM);
		zoomMangr.setZoomAnimationStyle(animatedZoom ? ZoomManager.ANIMATE_ZOOM_IN_OUT : ZoomManager.ANIMATE_NEVER);
	}
    
	public void zoomTo(double zoom, Point center) {
		zoomManager.zoomTo(zoom, center);
	}
	
	public void zoomTo(Rectangle rect) {
		zoomManager.zoomTo(rect);
	}
	
	public void zoomIn() {
		zoomManager.zoomIn();
	}
	
	public void zoomIn(Point center) {
		zoomManager.zoomTo(zoomManager.getNextZoomLevel(), center);
	}
	
	public void zoomOut() {
		zoomManager.zoomOut();
	}
	
	public void zoomOut(Point center) {
		zoomManager.zoomTo(zoomManager.getPreviousZoomLevel(), center);
	}
	
}
