package com.ceteva.client;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class ClientPlugin extends AbstractUIPlugin {
	
	private static ClientPlugin plugin;

	public ClientPlugin() {
	   plugin = this;
	}

	public static ClientPlugin getDefault() {
	   return plugin;
	}
	
	public void stop(BundleContext context) throws Exception {
	   super.stop(context);
	   IconManager.dispose();
	   FontManager.dispose();
	   ColorManager.dispose();
	}
}
