package com.ceteva.undo;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import Mosaic.XmfPlugin;


public class UndoPlugin extends AbstractUIPlugin implements IStartup  {
	
	private static UndoPlugin plugin;
	
	public UndoPlugin() {
		plugin = this;
	}

	public static UndoPlugin getDefault() {
		return plugin;
	}
	
	public void earlyStartup() {
		UndoClient undoClient = new UndoClient();
		XmfPlugin.XOS.newMessageClient("com.ceteva.undo",undoClient);
	}
}
