package com.ceteva.text;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import Mosaic.XmfPlugin;

import com.ceteva.client.ColorManager;
import com.ceteva.text.preferences.IPreferenceConstants;
;

public class TextPlugin extends AbstractUIPlugin implements IStartup {
	
	private static TextPlugin plugin;
	
	public static String PARTITIONER = "default_partitioner";
	
	public static Color RED = ColorManager.getColor(new RGB(255,0,0));
	public static Color GREEN = ColorManager.getColor(new RGB(63,127,95));
	public static Color BLUE = ColorManager.getColor(new RGB(0,0,255));
	public static Color BLACK = ColorManager.getColor(new RGB(0,0,0));
	
	public TextPlugin() {
		plugin = this;
	}

	public static TextPlugin getDefault() {
		return plugin;
	}
	
	public void earlyStartup() {
	    EditorClient client = new EditorClient();
		XmfPlugin.XOS.newMessageClient("com.ceteva.text",client);
	}
	
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		// font
		PreferenceConverter.setDefault(store, JFaceResources.TEXT_FONT, new FontData("Courier New",10,SWT.NORMAL));
		
		// colors
		PreferenceConverter.setDefault(store, IPreferenceConstants.CURRENT_LINE_COLOR,new RGB(232,242,254));
		PreferenceConverter.setDefault(store, IPreferenceConstants.HIGHLIGHT_LINE_COLOR,new RGB(221,171,160));
		
		// line numbers
		store.setDefault(IPreferenceConstants.LINE_NUMBERS, false);	
	}
	
	public void stop(BundleContext context) throws Exception {
		RED.dispose();;
		GREEN.dispose();
		BLUE.dispose();
		BLACK.dispose();
		super.stop(context);	
	}
	
	/* public String getHelpDirectory() {
		return HelpPlugin.getDefault().getHelpPath();
	} */
}
