package com.ceteva.basicIO;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class BasicIOPlugin extends AbstractUIPlugin implements org.eclipse.ui.IStartup {

	private static BasicIOPlugin plugin;

	public BasicIOPlugin() {
		plugin = this;
	}

	public void earlyStartup() {
		XOS.Client.setIOHandler(new BasicIOHandler());
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	public static BasicIOPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("com.ceteva.basicIO", path);
	}
}
