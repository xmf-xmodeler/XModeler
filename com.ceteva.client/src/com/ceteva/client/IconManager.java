package com.ceteva.client;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class IconManager {
	
  private static String graphicsDir = "icons";

  private static Hashtable iconCache = new Hashtable();

  // An image can either have an absolute path or one
  // relative to the plugin's directory
  
  public static Image getImage(Plugin plugin,String filename) {
	
	// apply any redirects to the filename
	  
	filename = FileRedirector.getRedirect(filename);  
	  
  	// test to see whether the name is absolute or relative
  	
  	File file = new File(filename);
  	if(file.exists())
  	  return getImageFromFileAbsolute(filename);
  	else
  	  return getImageFromFileRelative(plugin,filename);
  }
  
  private static Image getImageFromFileAbsolute(String filename) {
  	if(iconCache.containsKey(filename))
  	  return (Image)iconCache.get(filename);
  	else {
  	  return ImageDescriptor.createFromFile(null,filename).createImage();
  	}
  }
  
  private static Image getImageFromFileRelative(Plugin plugin,String filename) {
  	String iconPath = graphicsDir + "/";
	try {
	  URL installURL = plugin.getBundle().getEntry("/");
	  URL url = new URL(installURL, iconPath + filename);
	  if(iconCache.containsKey(url)) 
	    return (Image)iconCache.get(url);
	  else {
	  	Image image = ImageDescriptor.createFromURL(url).createImage(true);
	  	iconCache.put(url,image);
	  	return image;
	  }
	}
	catch (MalformedURLException e) {
	  return ImageDescriptor.getMissingImageDescriptor().createImage(true);
	}  	
  }
  
  public static ImageDescriptor getImageDescriptor(Plugin plugin,String filename) {
     
  	//  test to see whether the name is absolute or relative
  	
  	File file = new File(filename);
  	if(file.exists())
  	  return getImageDescriptorAbsolute(filename);
  	else
  	  return getImageDescriptorRelative(plugin,filename);
  }
  
  public static ImageDescriptor getImageDescriptorAbsolute(String filename) {
    return ImageDescriptor.createFromFile(null,filename);
  }
  
  public static ImageDescriptor getImageDescriptorRelative(Plugin plugin,String filename) {
	String iconPath = graphicsDir + "/";
  	try {
  	  URL installURL = plugin.getBundle().getEntry("/");
	  URL url = new URL(installURL, iconPath + filename);
	  return ImageDescriptor.createFromURL(url);
  	}
  	catch(MalformedURLException e) {
  	  return ImageDescriptor.getMissingImageDescriptor();	
  	}
  }
  
  public static Image getIcon(Plugin plugin,String name) {
  	return getImage(plugin,name);
  }
  
  public static void dispose() {
  	Enumeration e = iconCache.elements();
  	while(e.hasMoreElements()) {
  	  Image i = (Image)e.nextElement();
  	  i.dispose();
  	}
  }
}