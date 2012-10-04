package com.ceteva.mosaic;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import Mosaic.XmfPlugin;

import com.ceteva.mosaic.perspectives.PerspectiveManager;
import com.ceteva.mosaic.splash.Splash;

public class Advisor extends WorkbenchAdvisor {
	
	public static String iconPath = "icons/";
	public static String iconName = "mosiacSmall.gif";
	public static IProduct product = Platform.getProduct();
	public static Splash splash = null;
	
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new WindowAdvisor(configurer);
    }
	
	public String getInitialWindowPerspectiveId() {
		return PerspectiveManager.standardPerspective;
	}
	
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
        System.out.println("[ Application Initialize ]");
		configurer.setSaveAndRestore(false);
		setCurvedLook();
		XmfPlugin xmfplugin = XmfPlugin.getDefault();
		String imagedefault = xmfplugin.getDefaultImage();
		Hashtable imagechoices = xmfplugin.getImages();
		splash = new Splash(splashPath()+"/splash/splash.bmp",imagechoices);
		splash.show();
		String choosenimage = splash.choosenImage();
		if(choosenimage!="")
		  xmfplugin.setImage(choosenimage);
		else
		  xmfplugin.setImage(imagedefault);
	}
	
	public void setCurvedLook() {
		IPreferenceStore store = PlatformUI.getPreferenceStore();  
	    store.setValue( 
	            IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, 
	            false);
	    store.setValue(
	    		IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
	    		IWorkbenchPreferenceConstants.TOP_RIGHT);
	}
	
	public void postStartup() {
		//PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setMaximized(true);
		/* LicenseManager manager = LicenseManager.getInstance();
		try {
		  if(!manager.isValid())
		  	reportInvalidLicense();
		}
		catch(GeneralSecurityException gse) {
			reportInvalidLicense();
		} */
	}
	
    public static void reportInvalidLicense() {
    	splash.dispose();
        String text = "Mosaic License is invalid";
      	Display display = Display.getCurrent();
      	Shell shell = new Shell (display);
      	MessageDialog.openError(shell, "Mosaic Error", text);
      	display.dispose();
      	System.exit(0);
    }
    
    public Shell getShell() {
  	  return Display.getCurrent().getActiveShell();	
  	}
	
	public String splashPath() {
		URL installURL = MosaicPlugin.getDefault().getBundle().getEntry("/");;
		try {
	  	  URL installUrl = Platform.resolve(installURL);
	  	  return installUrl.getPath();
		}
		catch(IOException iox) {
		  System.out.println(iox);	
		}
	  	return null;
	}
}
