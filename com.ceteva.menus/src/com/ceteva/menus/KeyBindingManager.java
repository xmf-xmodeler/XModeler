package com.ceteva.menus;

import java.util.Hashtable;

public class KeyBindingManager {
	
	private static Hashtable keybindings = new Hashtable();
	
	public static void addBinding(String identity,String keybinding) {
		keybindings.put(identity,keybinding);
	}
	
	public static String getBinding(String identity) {
		return (String)keybindings.get(identity);
	}
	
	public static boolean hasBinding(String identity) {
		return keybindings.containsKey(identity);
	}

}
