package com.ceteva.diagram.editPart;

import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.figure.ZoomableFigure;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.diagram.zoom.CustomZoomManager;

public abstract class ZoomableGraphicalEditPart extends DisplayEditPart {
	
	CustomZoomManager zoomManager;
	
	public ZoomManager getZoomManager() {
		if(zoomManager == null) {
		  ZoomableFigure figure = (ZoomableFigure)getFigure();	
		  zoomManager = new CustomZoomManager((ScalableFigure)figure.getFigure(),((Viewport)figure.getViewPort()));
		  zoomManager.setZoomAnimationStyle(ZoomManager.ANIMATE_ZOOM_IN_OUT);	
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
	
	protected void register() {
		super.register();
		getViewer().setProperty(ZoomManager.class.toString(), getZoomManager());
	}

	protected void unregister() {
		super.unregister();
		getViewer().setProperty(ZoomManager.class.toString(), null);
	}
}
