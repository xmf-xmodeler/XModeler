package com.ceteva.text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.Client;
import com.ceteva.client.EventHandler;
import com.ceteva.client.IconManager;
import com.ceteva.client.IdManager;
import com.ceteva.text.htmlviewer.HTMLViewer;
import com.ceteva.text.oleviewer.OLEViewer;
import com.ceteva.text.oleviewer.OLEViewerInput;
import com.ceteva.text.texteditor.TextEditor;
import com.ceteva.text.texteditor.TextEditorInput;
import com.ceteva.text.texteditor.TextStorage;

public class EditorClient extends Client {
	
	public EditorClient() {
		super("com.ceteva.text");
	}

	public boolean newBrowser(Message message) {
		String identity = message.args[0].strValue();
		String name = message.args[1].strValue();
		String tooltip = message.args[2].strValue();
		String url = message.args[3].strValue();
		showBrowser(identity,name,tooltip,url);
		return true;
	}
	
	public boolean newOleEditor(Message message) {
		TextPlugin textManager = TextPlugin.getDefault();
		IWorkbenchPage page = textManager.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String identity = message.args[0].strValue();
		String type = message.args[1].strValue();
		String file = message.args[2].strValue();
		OLEViewerInput input = new OLEViewerInput(identity,type,file);
		try {
			OLEViewer viewer = (OLEViewer) page.openEditor(input,"com.ceteva.text.OLEViewer");
			viewer.setEventHandler(handler);
		} catch (PartInitException pie) {
			System.out.println(pie);
		}
		return true;
	}
		
    public void showBrowser(String identity,String title,String tooltip,String url) {
		TextPlugin textManager = TextPlugin.getDefault();
		IWorkbenchPage page = textManager.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		TextStorage storage = new TextStorage(identity);
		TextEditorInput input = new TextEditorInput(storage);
		try {
			HTMLViewer viewer = (HTMLViewer) page.openEditor(input,"com.ceteva.text.HTMLViewer");
			if (viewer != null) {
				viewer.setEventHandler(handler);
				viewer.setName(title);
				viewer.setToolTip(tooltip);
				if(!url.equals(""))
				  viewer.setURL(url);
			}
		} catch (PartInitException pie) {
			System.out.println(pie);
		}
	}

	
	public boolean newTextEditor(Message message) {
	  if(message.arity == 4 || message.arity == 5) {
	    String identity = message.args[0].strValue(); 
		String name = message.args[1].strValue();
		String tooltip = message.args[2].strValue();
		boolean editable = message.args[3].boolValue;
		TextPlugin textManager = TextPlugin.getDefault();
		IWorkbenchPage page = textManager.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		TextStorage storage = new TextStorage(identity);
		TextEditorInput input = new TextEditorInput(storage);
		try {
		  if(handler!=null) {
		    TextEditor newEditor = (TextEditor)page.openEditor(input,"com.ceteva.text.TextEditor");
		    newEditor.setName(name);
		    newEditor.setToolTip(tooltip);
		    newEditor.setEditable(editable);
		    newEditor.setEventHandler(handler);
		    if(message.arity==5) {
		      String icon = message.args[4].strValue();
		      if (!icon.endsWith(".gif"))
		     	 icon = icon + ".gif";
			  newEditor.setImage(IconManager.getIcon(TextPlugin.getDefault(),icon));	
		    }
		  }
		} catch (PartInitException pie) {
		    System.out.println(pie);
	    }
		return true;
	  }
	  return false;
	}
	
	public Value processCall(Message message) {
	  if(message.hasName("getWelcomePage")) {
		  
		  // The help plugin is optional so we load the class dynamically to show the
		  // help page if it is available.
		  
		  String path = "";
		  
		  try {
		    Class c = TextPlugin.getDefault().getBundle().loadClass("com.ceteva.help.HelpPlugin");
		    Method getDefault = c.getMethod("getDefault",new Class[0]);
		    Object def = getDefault.invoke(c,new Object[0]);
		    Method getPath = def.getClass().getMethod("getWelcomeFilePath",new Class[0]);
		    path = (String)getPath.invoke(def,new Object[0]);
		  }
		  catch(ClassNotFoundException cnf) {
		  }
		  catch(NoSuchMethodException nsm) {
		  }
		  catch(IllegalAccessException iax) {
		  }
		  catch(InvocationTargetException ite) {
		  }
		  return new Value(path);
	  	  // String path = HelpPlugin.getDefault().getWelcomeFilePath();
	  	  //    return new Value(path);
	  }
	  return IdManager.processCall(message);
	}
	
	public boolean processMessage(Message message) {
		if(message.hasName("newTextEditor"))
		    return newTextEditor(message);
		if(message.hasName("newBrowser"))
		  	return newBrowser(message);
		if(message.hasName("newOleEditor"))
			return this.newOleEditor(message);
		return IdManager.processMessage(message);
	}

	public void setEventHandler(EventHandler handler) {
	}
}