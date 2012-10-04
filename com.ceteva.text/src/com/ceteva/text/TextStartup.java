package com.ceteva.text;

import org.eclipse.ui.IStartup;

import Mosaic.XmfPlugin;

public class TextStartup implements IStartup {
	public void earlyStartup() {
	    System.out.println("[ register com.ceteva.text ]");
		EditorClient client = new EditorClient();
		XmfPlugin.XOS.newMessageClient("com.ceteva.text",client);
	}
}
