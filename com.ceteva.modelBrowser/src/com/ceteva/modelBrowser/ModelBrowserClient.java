package com.ceteva.modelBrowser;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.client.XMLClient;
import com.ceteva.client.xml.Document;
import com.ceteva.client.xml.Element;
import com.ceteva.modelBrowser.views.ModelBrowserView;

public class ModelBrowserClient extends XMLClient  {
	
	ModelBrowserView view = null;
	IWorkbenchPage page = null;
	Hashtable openBrowsers = new Hashtable();
	
	public ModelBrowserClient() {
	  super("com.ceteva.modelBrowser");
	}

	public void browserAdded(String id,ModelBrowserView browser) {
	  openBrowsers.put(id,browser);
	}
	    
	public void browserClosed(String id,ModelBrowserView browser) {
	  openBrowsers.remove(id);
	}
	   
	public boolean browserExists(String id) {
	  return openBrowsers.containsKey(id);
	}

    public IViewReference[] getAllForms() {
      return getPage().getViewReferences();
    }
    
    public boolean newBrowser(Message message) {
      String identity = message.args[0].strValue();
  	  String type = message.args[1].strValue();
  	  String name = message.args[2].strValue();
  	  if(newBrowser(identity,type,name,true,true) != null)
  	    return true;
  	  else
  		return false;
    }
    
    private static final String ID_DIV = "."; 
    
    public ModelBrowserView newBrowser(String identity,String type,String name,boolean closable,boolean hasFocus) {
      try {
    	String id = type + ID_DIV + identity;
    	
    	//ModelBrowserView browser = (ModelBrowserView)getPage().showView(ModelBrowserView.modelBrowserView,id,IWorkbenchPage.VIEW_CREATE);
    	
		ModelBrowserView browser = (ModelBrowserView)getPage().showView(ModelBrowserView.ID,id,IWorkbenchPage.VIEW_ACTIVATE);
    	
    	System.err.println("Modelbrowser added with id: "+browser.getIdentity()+" id:"+id);
    	browser.setName(name);
    	browser.setType(type);
    	browser.setIdentity(identity);
    	browser.registerEventHandler(handler);
    	browser.setClient(this);
    	browserAdded(identity,browser);
    	// if(closable) // Not sure what needs to be done here
    	if(hasFocus)
    	  browser.focusGained();
    	return browser;
      } catch (PartInitException e) {
    	e.printStackTrace();
    	return null;
      }
    }

 	public Value processCall(Message message) {
 	  Enumeration e = openBrowsers.elements();
 	  while(e.hasMoreElements()) {
 	    ModelBrowserView browser = (ModelBrowserView)e.nextElement();
	    Value value = browser.processCall(message);
	    if(value != null)
	      return value;
 	  }
 	  return new Value(false);
	}
    
    public boolean processMessage(Message message) {
  	    if(super.processMessage(message))
  		  return true;
    	if(message.hasName("newModelBrowser"))
    	  return newBrowser(message);
    	else if (message.hasName("setVisible") && message.arity == 1)
    	  return setVisible(message);
    	else if (message.hasName("setFocus") && message.arity == 1)
    	  return setFocus(message);
    	else {
    	  Enumeration e = openBrowsers.elements();
    	  while(e.hasMoreElements()) {
   	       ModelBrowserView browser = (ModelBrowserView)e.nextElement();
    	   if(browser.processMessage(message))
    		 return true;
    	  }
    	}
    	return false; 
   }
   
   public void processXML(Document xml) {
	    // xml.printString();
    	synchroniseBrowsers(xml);
   }

   public void setEventHandler(EventHandler handler) {
   }
   
   public boolean setFocus(Message message) {
	   String identity = message.args[0].strValue();	
	   if(openBrowsers.containsKey(identity)) {
  	     ModelBrowserView browser = (ModelBrowserView)openBrowsers.get(identity);
		 try {
  	       String id = browser.getType() + ID_DIV + identity;
  	       IViewPart part = getPage().showView(ModelBrowserView.ID,id,IWorkbenchPage.VIEW_ACTIVATE);
           handler.setCommandMode(false);
  	       return part != null;
		     } catch (PartInitException e) {
			   e.printStackTrace();
		 }
	   }
	   return false;   
   }
   
   public boolean setVisible(Message message) {
	   String identity = message.args[0].strValue();
	   if (browserExists(identity)) {
	  	 ModelBrowserView browser = (ModelBrowserView)openBrowsers.get(identity);
	     try {
	  	   String id = browser.getType() + ID_DIV + identity;
	       IViewPart part = getPage().showView(ModelBrowserView.ID,id,IWorkbenchPage.VIEW_VISIBLE);
           page.bringToTop(part);
           handler.setCommandMode(false);
           return part != null;
	     } catch (PartInitException e) {
	       e.printStackTrace();
	     }
	   }
	   return false;
   }
   
	public void synchroniseBrowsers(Element xml) {
		
		// check that there is a browser for each of the document's browsers
		
		for(int i=0;i<xml.childrenSize();i++) {
			Element child = xml.getChild(i);
			if(child.hasName(XMLBindings.browser)) {
			  boolean isOpen = child.getBoolean("isOpen");
			  if(isOpen) {
			    String identity = child.getString("identity");
			    String type = child.getString("type");
				if(openBrowsers.containsKey(identity)) {
			      
				  // Update the existing one
					
				  ModelBrowserView browser = (ModelBrowserView)openBrowsers.get(identity);
				  browser.synchronise(child);
				}
			    else {
			    
			      // Create a new one
			    	
				  String name = child.getString("name");
				  boolean closable = child.getBoolean("closable");
				  boolean hasFocus = child.getBoolean("hasFocus");
				  ModelBrowserView browser = newBrowser(identity,type,name,closable,hasFocus);
				  browser.synchronise(child);
			    }
			  }
			}
		}
		
		// check that there is an open browser in the document for each of the client's browsers
		
		// needs to be implemented
	}
	
   private IWorkbenchPage getPage() {
  	  if (page == null)
	  	 page = ModelBrowserPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
  	  return page;
   }

}