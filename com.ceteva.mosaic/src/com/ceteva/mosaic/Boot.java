package com.ceteva.mosaic;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class Boot implements IApplication {

	
	public Object start(IApplicationContext arg0) throws Exception {
		System.out.println("[Application Start]");
		Display display = PlatformUI.createDisplay();
		WorkbenchAdvisor workbenchAdvisor = new Advisor();
		try {
			int code = PlatformUI.createAndRunWorkbench(display,
					workbenchAdvisor);
			// exit the application with an appropriate return code
			return code == PlatformUI.RETURN_RESTART
					? EXIT_RESTART
					: EXIT_OK;
		} finally {
			if (display != null)
				display.dispose();
		}
	
		
	}

	
	public void stop() {
		System.out.println("[ Application Stop ]");
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

	/*
	 * @Override public Object run(Object args) { WorkbenchAdvisor
	 * workbenchAdvisor = new Advisor();
	 * 
	 * // for profiling // (1) include the sleak plugin in the runtime // (2)
	 * append the following to the program arguments: // -- -debug
	 * file:c:/eclipse/.options // -- where .options includes the following
	 * lines // -- org.eclipse.ui/debug=true // --
	 * org.eclipse.ui/trace/graphics=true // (3) uncomment the profiling lines
	 * below
	 * 
	 * // uncomment to profile // DeviceData data = new DeviceData(); //
	 * data.tracking = true;
	 * 
	 * Display display = PlatformUI.createDisplay();
	 * 
	 * // uncomment to profile // display.setData(data);
	 * 
	 * // create the workbench
	 * 
	 * int returnCode = PlatformUI.createAndRunWorkbench(display,
	 * workbenchAdvisor); if (returnCode == PlatformUI.RETURN_RESTART) { return
	 * IPlatformRunnable.EXIT_RESTART; } else { return
	 * IPlatformRunnable.EXIT_OK; } }
	 */
}