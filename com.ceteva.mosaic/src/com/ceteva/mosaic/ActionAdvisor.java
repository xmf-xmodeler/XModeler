package com.ceteva.mosaic;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.ceteva.mosaic.actions.Exit;
import com.ceteva.undo.UndoClient;

public class ActionAdvisor extends ActionBarAdvisor {
	
	static IWorkbenchAction print;
	static IWorkbenchAction help;
	static IWorkbenchAction preferences;
	static IWorkbenchAction about;
	//static IWorkbenchAction move;
	//static IWorkbenchAction view;
	
	static Exit exit;
	
    public ActionAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
	}
	
    protected void fillMenuBar(IMenuManager menuBar) {
    	addFileMenus(menuBar);
    	// addEditMenus(menuBar);
    	addWindowMenus(menuBar);
    	addHelpMenus(menuBar);
    }
    
	public void addFileMenus(IMenuManager menuBar) {
		MenuManager filemenu = new MenuManager("&File",IWorkbenchActionConstants.M_FILE);
		menuBar.add(filemenu);
		filemenu.add(new Separator("print"));
		filemenu.add(new Separator("exit"));
		filemenu.appendToGroup("print",print);
		filemenu.appendToGroup("exit",exit);
		
	}

	public void addEditMenus(IMenuManager menuBar) {	
		MenuManager editmenu = new MenuManager("&Edit",IWorkbenchActionConstants.M_EDIT);
		menuBar.add(editmenu);
		editmenu.add(UndoClient.undo);
		editmenu.add(UndoClient.redo);
		
	}

	public void addWindowMenus(IMenuManager menuBar) {
		MenuManager winmenu = new MenuManager("&Window",IWorkbenchActionConstants.M_WINDOW);
		menuBar.add(winmenu);
		winmenu.add(preferences);
		
	}
	
	public void addHelpMenus(IMenuManager menuBar) {
		MenuManager helpmenu = new MenuManager("&Help",IWorkbenchActionConstants.M_HELP);
		//helpmenu.appendToGroup(IWorkbenchActionConstants.HELP_START,help);
		//helpmenu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS,about);
		menuBar.add(helpmenu);
		helpmenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        helpmenu.add(new Separator());
        helpmenu.add(help);
		helpmenu.add(about);
		//helpmenu.add(move);
		//helpmenu.add(view);
		
	}
	
	protected void fillCoolBar(ICoolBarManager coolBar) {
		//IToolBarManager editToolBar = new ToolBarManager(coolBar.getStyle());
		IToolBarManager editToolBar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(editToolBar, "main")); 
      //coolBar.add(new ToolBarContributionItem(editToolBar,IWorkbenchActionConstants.M_EDIT));
        editToolBar.add(print);
		editToolBar.add(UndoClient.undo);
		editToolBar.add(UndoClient.redo);
		
	}
	

    protected void makeActions(final IWorkbenchWindow window) {
    	print = ActionFactory.PRINT.create(window);
    	//Don't register exit, never
    	exit = new Exit();
    	preferences = ActionFactory.PREFERENCES.create(window);
    	help = ActionFactory.HELP_CONTENTS.create(window);
    	about = ActionFactory.ABOUT.create(window);
    	//move = ActionFactory.MOVE.create(window);
    	//view = ActionFactory.SHOW_VIEW_MENU.create(window);
    	// note that the following were previously configurer.registerGlobalAction
    	register(print);
    	register(preferences);
    	register(help);
    	register(about);
    	//register(move);
    	register(UndoClient.undo);
		register(UndoClient.redo);
		//register(view);
    }
}