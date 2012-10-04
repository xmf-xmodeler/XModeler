package com.ceteva.mosaic;



import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class WindowAdvisor extends WorkbenchWindowAdvisor {
	
    public WindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    	configurer.setShowPerspectiveBar(true);
    }
    
    public ActionBarAdvisor createActionBarAdvisor(
        IActionBarConfigurer actionBarConfigurer) {
        return new ActionAdvisor(actionBarConfigurer);
    }
    
    public boolean preWindowShellClose() {
  	  	ActionAdvisor.exit.run();
  	  	return false;
  	}
    
    public void preWindowOpen() {
        super.preWindowOpen();
    	IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(600, 400));
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
    }
}