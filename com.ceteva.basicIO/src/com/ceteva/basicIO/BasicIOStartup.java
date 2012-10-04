package com.ceteva.basicIO;

import org.eclipse.ui.IStartup;

public class BasicIOStartup implements IStartup{
	public void earlyStartup() {
		XOS.Client.setIOHandler(new BasicIOHandler());
	}
}
