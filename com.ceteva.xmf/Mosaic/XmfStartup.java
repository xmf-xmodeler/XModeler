package Mosaic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ui.IStartup;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;


public class XmfStartup implements IStartup{
	
	private static final String XMF_EXTENSION_ID = "com.ceteva.xmf.extensions.BootLoader";
	
	public void earlyStartup() {
        System.out.println("[ Early Startup XmfPlugin ]");
    	Thread t = new Thread() {
            public void run() {
                
            	String projDir = XmfPlugin.getEnvVar("XMFPROJECTS");
                String demoDir = XmfPlugin.getEnvVar("MOSAICDEMOS");
                String initFile = XmfPlugin.getEnvVar("MOSAICINIT");
                String imagesDir = XmfPlugin.getEnvVar("XMFIMAGES");
                String dirSlash = XmfPlugin.getDefault().getRuntimeDirectory();
                String dir = dirSlash.substring(0, dirSlash.length() - 1);
                String user = System.getProperties().getProperty("user.name").replaceAll(" ", "\\\\ ");
                String[] args = XmfPlugin.getDefault().readStartupArgs(dir, user, projDir, demoDir, imagesDir, initFile, XmfPlugin.imageFile);
                //getting the extensions
                List<String> filenamextensions = receiveExtensions();
                
                try {
                	XmfPlugin.getDefault().registerEscapeHandler();
                	XmfPlugin.getDefault().XOS.init(args);
                    
                } catch (Throwable t) {
                    System.out.println(t);
                    t.printStackTrace();
                }
            }
        };
        t.start();
    }
	
	private List<String> receiveExtensions() {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(XMF_EXTENSION_ID);
		List<String> result = new ArrayList<String>();
			for (IConfigurationElement e : config) {
				System.out.println("Evaluating extension");
				String filename = e.getChildren("xmf")[0].getAttribute("filename");
				result.add(filename);
			}
			return result;
		
	}
	
}
