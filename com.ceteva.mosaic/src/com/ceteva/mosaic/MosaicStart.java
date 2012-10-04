package com.ceteva.mosaic;

import org.eclipse.ui.IStartup;

import Mosaic.XmfPlugin;

public class MosaicStart implements IStartup{
	public void earlyStartup() {
		   WorkbenchClient client = new WorkbenchClient();
		   XmfPlugin.XOS.newMessageClient("com.ceteva.mosaic",client);
		}
}
