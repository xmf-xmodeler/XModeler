package com.ceteva.oleBridge;

import org.eclipse.ui.IStartup;

import Mosaic.XmfPlugin;

public class OleStartup implements IStartup{
	
	public void earlyStartup() {
		System.out.println("[ register com.ceteva.oleBridge ]");
		OleBridgeClient client = new OleBridgeClient();
		XmfPlugin.XOS.newMessageClient("com.ceteva.oleBridge",client);
	}
}
