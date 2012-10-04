package com.ceteva.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.runtime.Platform;

public class FileRedirector {

	// Some early versions of tools in XMF-Mosaic hard coded file paths to graphics
	// which compromised portability of the tools across systems.  The FileRedirector
	// is a hack which allows XMF-Mosaic to program path redirects.
	
	// Maps old paths to new paths
	
	private static Hashtable redirects = new Hashtable();
	
	private static boolean gotDir = false;
	private static String modelBrowserDir = "";
	private static String diagramDir = "";
	
	public static void addRedirect(String source,String target) {
		if(!gotDir)
		  getDirs();	
		redirects.put(source,target);
	}
	
	public static String getRedirect(String source) {
		
		// return the redirect if it exists, else return
		// the given path
		
		Enumeration e = redirects.keys();
		while(e.hasMoreElements()) {
		  String oldpath = (String)e.nextElement();
		  String newpath = (String)redirects.get(oldpath);
		  source = source.replaceAll(oldpath,newpath);
		}
		
		// replace the modelBrowser/diagram reference so that
		// it corresponds to the installed directory
		
		if(!modelBrowserDir.equals(""))
		  source = source.replaceAll("com.ceteva.modelBrowser/",modelBrowserDir + "/");
		if(!diagramDir.equals(""))
		  source = source.replaceAll("com.ceteva.diagram/",diagramDir + "/");
		return source;
	}
	
	public static void getDirs() {
	  URL installUrl = null;
	  try {
	    installUrl = Platform.resolve(ClientPlugin.getDefault().getBundle().getEntry("/"));
	  } catch (IOException iox) {
	    System.out.println(iox);
	  }
	  File file = new File(installUrl.getFile().substring(1) + "../");
	  if (file.isDirectory()) {
	    File[] files = file.listFiles();
	    for(int i=0;i<files.length;i++) {
	      File child = files[i];
	      if(child.getName().startsWith("com.ceteva.modelBrowse"))
	        modelBrowserDir = child.getName();	  
	      if(child.getName().startsWith("com.ceteva.diagram"))
	        diagramDir = child.getName();
	    }
	  };
	  gotDir = true;
	}
	
}
