package com.ceteva.mosaic;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.ui.branding.IProductConstants;
import org.osgi.framework.Bundle;

import Mosaic.XmfPlugin;

public class Product implements IProduct {

  public String getApplication() {
	return "com.ceteva.mosaic";
  }

  public String getName() {
  	return "XMF-Mosaic";
  }

  public String getDescription() {
	return "";
  }

  public String getId() {
	return "com.ceteva.mosaic.Product";
  }

  public String getProperty(String key) {
  	System.out.println("Requesting: " + key);
	/* if(key.equals(IProductConstants.APP_NAME))
  	  return "XMF-Mosaic";
  	if(key.equals(IProductConstants.ABOUT_IMAGE))
  	  return iconPath() + "mosaicLogo.gif";
  	else if(key.equals(IProductConstants.ABOUT_TEXT))
  	  return aboutText();
  	else if(key.equals(IProductConstants.WINDOW_IMAGES))
  	  return iconPath() + "mosaic16.gif"; */
	return null;
  }
  
  public String iconPath() {
  	String path = "";
  	URL installURL = MosaicPlugin.getDefault().getBundle().getEntry("/");
	try {
  	  URL url = new URL(installURL, "icons/");
  	  path = url.toString();
	}
	catch(MalformedURLException ex) {
	  System.out.println(ex);	
	}
  	return path;
  }
  
  public String aboutText() {
  	/* String line1 = "XMF-Mosaic\n";
  	String line2 = "Version " + XmfPlugin.version + "\n";
  	String line3 = "\n";
  	String line4 = "(c) Ceteva Ltd. 2003 - 2005.  All rights reserved\n";
  	String line5 = "http://www.ceteva.com\n";
  	return line1+line2+line3+line4+line5; */
	return "";
  }

  public Bundle getDefiningBundle() {
	return MosaicPlugin.getDefault().getBundle();
  }
}
