package com.ceteva.console;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.ceteva.console.preferences.IPreferenceConstants;

public class ConsolePlugin extends AbstractUIPlugin {

	private static ConsolePlugin plugin;
	private FontData defaultFont = new FontData("1|Courier New|10|0|WINDOWS|1|-13|0|0|0|400|0|0|0|0|3|2|1|49|Courier New");
	private static BufferedWriter out;
	private static boolean writeToFile = false;
	
	public ConsolePlugin() {
		plugin = this;
		if(writeToFile) {
		  try {
		    out = new BufferedWriter(new FileWriter("c:\\temp\\xmflog.txt"));
			Date currentDate = new java.util.Date();
			out.write("###" + currentDate.toString() + "\n");
		  } catch (IOException e) {
		    System.out.println(e);
		  }
		}
	}

	public static ConsolePlugin getDefault() {
		return plugin;
	}
	
	protected void initializeDefaultPreferences(IPreferenceStore store) {
	    PreferenceConverter.setDefault(store, IPreferenceConstants.CONSOLE_BACKGROUND,new RGB(0,0,128));
		PreferenceConverter.setDefault(store, IPreferenceConstants.CONSOLE_FONT_COLOUR,new RGB(255,255,255));
		PreferenceConverter.setDefault(store, IPreferenceConstants.CONSOLE_FONT,defaultFont);
		store.setDefault(IPreferenceConstants.LINE_LIMIT, 5000);
		store.setDefault(IPreferenceConstants.COMMAND_HISTORY_LIMIT, 10);
	}
	
	public void stop(BundleContext context) throws Exception {
		   super.stop(context);
		   if(writeToFile)
		     out.close();
	}
	
	public static void writeToFile(String string) {
		if(writeToFile) {
		  try {
			out.write(string + "\n");
			out.flush();
	      } catch (IOException e) {
	    	  System.out.println(e);
	      }
	    }
	}
}
