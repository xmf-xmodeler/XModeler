package com.ceteva.help;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class HelpPlugin extends AbstractUIPlugin {
	
	private static HelpPlugin plugin;
	
	public HelpPlugin() {
		plugin = this;
	}

	public static HelpPlugin getDefault() {
		return plugin;
	}
	
	public String getWelcomeFilePath() {
		URL helpLocation = null;
		try {
			helpLocation = Platform.resolve(this.getBundle().getEntry("/"));
		} catch (IOException iox) {
			System.out.println(iox);
		}
		return "file://"+helpLocation.getFile().toString() + "Welcome/welcome.html";
	}
	
    public String getHelpPath() {
        URL installUrl = null;
        try {
            installUrl = Platform.resolve(this.getBundle().getEntry("/"));
        } catch (IOException iox) {
            System.out.println(iox);
        }
        return installUrl.getRef();
    }
}
