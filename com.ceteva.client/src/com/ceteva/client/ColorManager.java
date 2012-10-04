package com.ceteva.client;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {
	
	// keys are the colour's RGB value
	
	private static Hashtable colorbinding = new Hashtable();
	
	public static Color getColor(RGB rgb) {
	  if(colorbinding.containsKey(rgb))
	  	return (Color)colorbinding.get(rgb);
	  else {
	  	Color c = new Color(Display.getCurrent(),rgb);
	  	colorbinding.put(rgb,c);
	  	return c;
	  }
	}
	
	public static void dispose() {
	  Enumeration e = colorbinding.elements();
	  while(e.hasMoreElements()) {
	  	Color f = (Color)e.nextElement();
	  	f.dispose();
	  }
	}
}