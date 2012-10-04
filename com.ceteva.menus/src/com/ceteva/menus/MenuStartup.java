package com.ceteva.menus;

import org.eclipse.ui.IStartup;

import Mosaic.XmfPlugin;

public class MenuStartup implements IStartup{
	public void earlyStartup() {
		MenusClient client = new MenusClient();
		XmfPlugin.XOS.newMessageClient("com.ceteva.menus",client);  	 
	}
}
