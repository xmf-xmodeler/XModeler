package com.ceteva.consoleInterface;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class ConsoleInterfacePlugin extends Plugin {

	private static ConsoleInterfacePlugin plugin;
	
	public ConsoleInterfacePlugin() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	public static ConsoleInterfacePlugin getDefault() {
		return plugin;
	}
}
