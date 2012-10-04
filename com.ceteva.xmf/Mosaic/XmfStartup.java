package Mosaic;

import org.eclipse.ui.IStartup;

public class XmfStartup implements IStartup{
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
	
}
