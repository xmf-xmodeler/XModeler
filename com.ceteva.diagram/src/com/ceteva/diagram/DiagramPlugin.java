package com.ceteva.diagram;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import Mosaic.XmfPlugin;

import com.ceteva.diagram.model.ImageManager;
import com.ceteva.diagram.preferences.IPreferenceConstants;

public class DiagramPlugin extends AbstractUIPlugin implements org.eclipse.ui.IStartup {
	
	private static DiagramPlugin plugin;
	
	public DiagramPlugin() {
	  plugin = this;
	}

	public static DiagramPlugin getDefault() {
	  return plugin;
	}

	public static IWorkspace getWorkspace() {
	  return ResourcesPlugin.getWorkspace();
	}
	
	protected void initializeDefaultPreferences(IPreferenceStore store) {
	  // colors
	  store.setDefault(IPreferenceConstants.GRADIENT_FILL, false);	
	  PreferenceConverter.setDefault(store, IPreferenceConstants.DIAGRAM_BACKGROUND_COLOR,new RGB(255,255,255));
	  PreferenceConverter.setDefault(store, IPreferenceConstants.EDGE_COLOR,new RGB(128,128,128));
	  PreferenceConverter.setDefault(store, IPreferenceConstants.FILL_COLOR,new RGB(185,185,255));
	  PreferenceConverter.setDefault(store, IPreferenceConstants.FOREGROUND_COLOR,new RGB(0,0,0));
	  PreferenceConverter.setDefault(store, IPreferenceConstants.UNSELECTED_FONT_COLOR,new RGB(0,0,0));
	  PreferenceConverter.setDefault(store, IPreferenceConstants.SELECTED_FONT_COLOR,new RGB(0,0,160));
	  // font
	  PreferenceConverter.setDefault(store, IPreferenceConstants.FONT,  Display.getCurrent().getSystemFont().getFontData());
	  // other
	  store.setDefault(IPreferenceConstants.ANIMATEZOOM, true);
	  store.setDefault(IPreferenceConstants.GRIDLINES, false);	
	  store.setDefault(IPreferenceConstants.GRIDSIZE, 20);
	  // store.setDefault(IPreferenceConstants.SPLINED, false);
	  // palette
	  store.setDefault(IPreferenceConstants.PALETTE_STATE, 4);
	  store.setDefault(IPreferenceConstants.DOCK_LOCATION, 8);
	  store.setDefault(IPreferenceConstants.PALETTE_WIDTH, 110);
	}
	
	public void earlyStartup() {
      DiagramClient client = new DiagramClient();
	  XmfPlugin.XOS.newMessageClient("com.ceteva.diagram",client);
	}
	
	public void stop(BundleContext context) throws Exception {
	  super.stop(context);
	  ImageManager.dispose();
	}	
}
