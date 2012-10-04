package com.ceteva.dialogs;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import Mosaic.XmfPlugin;

public class DialogsPlugin extends AbstractUIPlugin implements org.eclipse.ui.IStartup {
	private static DialogsPlugin plugin;
	
	public DialogsPlugin() {
  	  plugin = this;
	}
	
	public static DialogsPlugin getDefault() {
	  return plugin;
	}
	
	public void earlyStartup() {
	  DialogsClient client = new DialogsClient();
	  XmfPlugin.XOS.newMessageClient("com.ceteva.dialogs",client);
	}
}
