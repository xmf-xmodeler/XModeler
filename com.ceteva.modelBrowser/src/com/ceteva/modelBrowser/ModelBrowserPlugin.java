package com.ceteva.modelBrowser;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import Mosaic.XmfPlugin;

import com.ceteva.modelBrowser.preferences.IPreferenceConstants;

public class ModelBrowserPlugin extends AbstractUIPlugin implements org.eclipse.ui.IStartup {

	private static ModelBrowserPlugin plugin;
	
	public ModelBrowserPlugin() {
	  plugin = this;
	}
	
	public static ModelBrowserPlugin getDefault() {
		return plugin;
	}
	
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(IPreferenceConstants.INVOKE_PROPERTY_EDITOR, "doubleClick");
		store.setDefault(IPreferenceConstants.INVOKE_DIAGRAM_EDITOR, "none");
	}
	
	public void earlyStartup() {
		ModelBrowserClient client = new ModelBrowserClient();
		XmfPlugin.XOS.newMessageClient("com.ceteva.modelBrowser",client);
	}	
}
