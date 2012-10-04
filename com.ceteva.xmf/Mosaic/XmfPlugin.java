package Mosaic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Hashtable;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import XOS.OperatingSystem;

import com.ceteva.consoleInterface.EscapeHandler;

public class XmfPlugin extends Plugin implements org.eclipse.ui.IStartup {

    public static final String    COMMAND_LINE_ARGS = "startup.txt";

    public static XmfPlugin       plugin            = null;

    public static OperatingSystem XOS               = new OperatingSystem();

    public static String          version           = "undefined";

    public static String          imageFile         = null;


    public XmfPlugin() {
        plugin = this;
        MosaicRun.runningMosaic();
    }

    public static XmfPlugin getDefault() {
        return plugin;
    }

    public String getInstallDirectory() {
        URL installUrl = Platform.getInstallLocation().getURL();
        return installUrl.getFile().toString();
    }

    public static String getEnvVar(String name) {

    	String value = "";
    	
    	// Only works on the windows platform
    	
    	if (System.getProperty("os.name").toLowerCase().startsWith("win"))
    	{
    		// Accesses the Windows environment variable with the given
    		// name. Returns the value of the variable or "" if it is not
    		// defined.
    		
    		try {
    			String var = "%" + name + "%";
    			Process p = Runtime.getRuntime().exec("cmd.exe /c echo " + var);
    			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
    			String myvar = br.readLine();
    			p.destroy();
    			br.close();
    			value = myvar.equals(var) ? "" : myvar;
    		} catch (IOException ioe) 
    		{
    		}    		
    	}
    	return value;
    }

    public String getRuntimeDirectory() {
        URL installUrl = null;
        try {
        	installUrl = FileLocator.resolve(this.getBundle().getEntry("/"));
        } catch (IOException iox) {
            System.out.println(iox);
        }

        // Platform gets the path with a leading space i.e \C:\eclipse\workspace\...
        // since the path is often used as the basis of comparison to test when a
        // file is being edited, the leading "\" must be removed. Note this may
        // be an issue when Mosaic is used on other platforms than windows

        // return installUrl.getFile().substring(1);
        return installUrl.getFile();
    }

    public void earlyStartup() {
        System.out.println("[ Early Startup XmfPlugin ]");
    	Thread t = new Thread() {
            public void run() {
                String projDir = getEnvVar("XMFPROJECTS");
                String demoDir = getEnvVar("MOSAICDEMOS");
                String initFile = getEnvVar("MOSAICINIT");
                String imagesDir = getEnvVar("XMFIMAGES");
                String dirSlash = getRuntimeDirectory();
                String dir = dirSlash.substring(0, dirSlash.length() - 1);
                String user = System.getProperties().getProperty("user.name").replaceAll(" ", "\\\\ ");
                String[] args = readStartupArgs(dir, user, projDir, demoDir, imagesDir, initFile, XmfPlugin.imageFile);
                try {
                    registerEscapeHandler();
                    XOS.init(args);
                    
                } catch (Throwable t) {
                    System.out.println(t);
                    t.printStackTrace();
                }
            }
        };
        t.start();
    }

    public String[] readStartupArgs(String home, String user, String projDir, String demoDir, String imagesDir, String initFile, String image) {
        String[] args = null;
        try {
            String argFile = home + "/Mosaic/" + COMMAND_LINE_ARGS;
            FileInputStream fin = new FileInputStream(argFile);
            BufferedReader bin = new BufferedReader(new InputStreamReader(fin));
            String argLines = "-arg user:" + user;
            argLines = argLines + " -arg home:" + home.replaceAll(" ", "\\\\ ");
            argLines = argLines + " -image " + image.replaceAll(" ", "\\\\ ");
            argLines = initFile.equals("") ? argLines : argLines + " -arg initFile:" + initFile.replaceAll(" ", "\\\\ ");
            if (projDir != null)
                argLines = argLines + " -arg projects:" + projDir.replaceAll(" ", "\\\\ ");
            if (demoDir != null)
                argLines = argLines + " -arg demos:" + demoDir.replaceAll(" ", "\\\\ ");
            if (imagesDir != null)
                argLines = argLines + " -arg images:" + imagesDir.replaceAll(" ", "\\\\ ");
            String line = "";
            String prefString = "-arg prefs:";
            while ((line = bin.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("//"))
                    if (line.startsWith("-pref"))
                        prefString = prefString + line.substring(5).replace(':', '=').trim() + ",";
                    else
                        argLines = argLines + " " + line;
            }
            if (!prefString.equals("-arg prefs:"))
                argLines = argLines + " " + prefString;
            argLines = argLines.replaceAll("\\\\ ", "SPACE");
            args = argLines.split(" ");
            for (int i = 0; i < args.length; i++) {
                args[i] = args[i].replaceAll("SPACE", " ");
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
            System.exit(0);
        }

        // store the version number to be referenced from other places

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("version:"))
                version = arg.replaceAll("version:", "");
        }
        return args;
    }
    
    public void registerEscapeHandler() {
    	try {
            
            // This is done using reflection since the console plugin might not exist
            // in a given install of an XMF-Mosaic based tool
            
            Class c = XmfPlugin.getDefault().getBundle().loadClass("com.ceteva.console.views.ConsoleView");
            
            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = EscapeHandler.class;
            Method setEscapeHandler = c.getMethod("setEscapeHandler",parameterTypes);
            
            Object[] parameters = new Object[1];
    		InterruptHandler handler = new InterruptHandler(XOS);
            parameters[0] = handler;
            setEscapeHandler.invoke(null,parameters);
            
		} catch (ClassNotFoundException cnf) {
		  cnf.printStackTrace();
		} catch (NoSuchMethodException nsm) {
		  nsm.printStackTrace();
		} catch (IllegalAccessException iax) {
		  iax.printStackTrace();
		} catch (InvocationTargetException ite) {
		  ite.printStackTrace();
		}
    }
    
    public String getDefaultImage() {
        String dirSlash = getRuntimeDirectory();
        String dir = dirSlash.substring(0, dirSlash.length() - 1);
        return dir + "/Images/mosaic.img";
    }
    
    public Hashtable getImages() {
    	String imagesDir = getEnvVar("XMFIMAGES");
        final Hashtable fileTable = new Hashtable();
    	FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return file.getName().endsWith(".img");
            }
        };
        if (imagesDir != null && !imagesDir.equals("")) {
            File dir = new File(imagesDir);
            if(dir.exists()) {
              File[] files = dir.listFiles(filter);
              if (files.length > 0) {
                final String[] imageFiles = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                	String fileName = files[i].getName();
                	String filePath = files[i].toString();
                    imageFiles[i] = fileName;
                    fileTable.put(fileName,filePath);
                }
              }
            }
        }
        return fileTable;
    }
    
    public void setImage(String image) {
    	imageFile = image;
    }

    public void stop(BundleContext context) throws Exception {
        if (XOS != null)
            XOS.closeAll();
        super.stop(context);
    }

}