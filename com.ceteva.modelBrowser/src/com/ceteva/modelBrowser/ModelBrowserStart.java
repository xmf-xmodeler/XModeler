package com.ceteva.modelBrowser;

import org.eclipse.ui.IStartup;

import Mosaic.XmfPlugin;

public class ModelBrowserStart implements IStartup {
	public void earlyStartup() {
		ModelBrowserClient client = new ModelBrowserClient();
		XmfPlugin.XOS.newMessageClient("com.ceteva.modelBrowser",client);
	}	
}
