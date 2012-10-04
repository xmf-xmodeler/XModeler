package com.ceteva.forms;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import Mosaic.XmfPlugin;

import com.ceteva.client.ColorManager;
import com.ceteva.client.FontManager;

public class FormsPlugin extends AbstractUIPlugin implements org.eclipse.ui.IStartup {
	
	private static FormsPlugin plugin;
	// font
	public static FontData defaultFont; 
	public static Font font;
	// font colours
	public static Color normalBackgroundColor;
	public static Color disabledBackgroundColor;
	public static Color modifiedBackgroundColor;
	// syntax highlighting colours
	public static Color RED;
	public static Color GREEN;
	public static Color BLUE;
	public static Color BLACK;
	
	public FormsPlugin() {
		defaultFont = new FontData("1|Courier New|9|0|WINDOWS|1|-13|0|0|0|400|0|0|0|0|3|2|1|49|Courier New");
		// font = new Font(Display.getCurrent(),defaultFont);
		font = FontManager.getFont(defaultFont);
		normalBackgroundColor = ColorManager.getColor(new RGB(255,255,255));
		disabledBackgroundColor = ColorManager.getColor(new RGB(222,221,220));
		modifiedBackgroundColor = ColorManager.getColor(new RGB(221,171,160));
		RED = ColorManager.getColor(new RGB(255,0,0));
		GREEN = ColorManager.getColor(new RGB(0,255,0));
		BLUE = ColorManager.getColor(new RGB(0,0,255));
		BLACK = ColorManager.getColor(new RGB(0,0,0));
		plugin = this;
	}
	
	public static FormsPlugin getDefault() {
		return plugin;
	}
	
	public void stop(BundleContext context) throws Exception {
		modifiedBackgroundColor.dispose();
		font.dispose();
		RED.dispose();
		GREEN.dispose();
		BLUE.dispose();
		BLACK.dispose();
	}
	
	public void earlyStartup() {
		FormsClient client = new FormsClient();
		XmfPlugin.XOS.newMessageClient("com.ceteva.forms",client);
	}	
}
