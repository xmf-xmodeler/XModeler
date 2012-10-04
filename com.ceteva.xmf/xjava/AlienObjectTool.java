package xjava;

import org.eclipse.core.runtime.Plugin;

public class AlienObjectTool {

	private Plugin plugin;
	
	public AlienObjectTool(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public Plugin getPlugin() {
		return plugin;
	}
	
}
