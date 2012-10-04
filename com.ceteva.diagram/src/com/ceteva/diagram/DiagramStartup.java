package com.ceteva.diagram;

import org.eclipse.ui.IStartup;

import Mosaic.XmfPlugin;

public class DiagramStartup implements IStartup{
	public void earlyStartup() {
	      DiagramClient client = new DiagramClient();
		  XmfPlugin.XOS.newMessageClient("com.ceteva.diagram",client);
		}
}
