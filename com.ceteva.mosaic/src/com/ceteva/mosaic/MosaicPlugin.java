package com.ceteva.mosaic;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import Mosaic.XmfPlugin;

public class MosaicPlugin extends AbstractUIPlugin implements org.eclipse.ui.IStartup {
	
	private static MosaicPlugin plugin;
	
	public MosaicPlugin() {
		plugin = this;
	}
	
	public static MosaicPlugin getDefault() {
		return plugin;
	}
	
	public void earlyStartup() {
	   WorkbenchClient client = new WorkbenchClient();
	   XmfPlugin.XOS.newMessageClient("com.ceteva.mosaic",client);
	}
}