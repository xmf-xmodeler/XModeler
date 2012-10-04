package com.ceteva.forms;

import java.util.Vector;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
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
import com.ceteva.forms.views.FormView;

public class FormsClient extends XMLClient {
    
    public FormsClient() {
    	super("com.ceteva.forms");
    }
    
    public FormView getForm(String identity) {
      Vector forms = getAllForms();
      for(int i=0;i<forms.size();i++) {
   	    FormView form = (FormView)forms.elementAt(i);  
    	if(form.getIdentity().equals(identity))
    	  return form;
      }
   	  return null;
    }
    
    public String getFormType(String identity) {
    	Vector forms = getAllForms();
  	  	for(int i=0;i<forms.size();i++) {
  	      FormView form = (FormView)forms.elementAt(i);  
  	  	  if(form.getIdentity().equals(identity))
  	  	    return form.getType();
  	    }
  	  	return null;
    }

    public Value getTextDimension(String text) {
    	Font f = Display.getDefault().getSystemFont();
    	Dimension d = FigureUtilities.getTextExtents(text,f);
    	Value[] values = new Value[2];
    	values[0] = new Value(d.width);
    	values[1] = new Value(d.height);
    	Value value = new Value(values);
    	return value;
    }
    
    /* public IViewReference[] getAllForms() {
        IWorkbenchPage page = FormsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
        return page.getViewReferences();
    } */
    
    public Vector getAllForms() {
      Vector forms = new Vector();
      IWorkbenchPage page = FormsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
      IViewReference[] vreferences = page.getViewReferences();
	  for(int i=0;i<vreferences.length;i++) {
	  	IViewReference reference = vreferences[i];
	  	IViewPart view = reference.getView(false);
	  	if(view instanceof FormView)
	  	  forms.add(view);
	  }
      return forms;
    } 

  	public Value broadcastCall(Message message) {
  	  Vector forms = getAllForms();
  	  for(int i=0;i<forms.size();i++) {
  		FormView form = (FormView)forms.elementAt(i);
  	    Value value = form.processCall(message);
  	    if(value != null)
  	      return value;
  	  }
 	  return new Value(false);
 	}
  	
  	public boolean newForm(Message message) {
  	  String identity = message.args[0].strValue();
  	  String type = message.args[1].strValue();
  	  String name = message.args[2].strValue();
	  return newForm(identity,type,name) != null;
  	}
  	
  	public FormView newForm(String identity,String type,String name) {
      IWorkbenchPage page = FormsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try {
  	    String id = type + "@" + identity;
  		FormView form = (FormView)page.showView("com.ceteva.forms.view",id,IWorkbenchPage.VIEW_CREATE);
  		page.showView("com.ceteva.forms.view",id,IWorkbenchPage.VIEW_ACTIVATE);
  		form.setName(name);
  		form.setType(type);
  		form.setIdentity(identity);
  		form.registerEventHandler(handler);
  		return form;
      } 
      catch (PartInitException e) {
  		e.printStackTrace();
  		return null;
      }  		
  	}

    public Value processCall(Message message) {
    	if (message.hasName("getTextDimension")) {
			String text = message.args[0].strValue();
			return getTextDimension(text);
    	}
    	return broadcastCall(message);
    }
    
    public boolean processMessage(Message message) {  	    
    	if(super.processMessage(message))
		  return true;
    	if(message.hasName("newForm"))
    	   newForm(message);
    	else if (message.hasName("setVisible") && message.arity == 1)
     	   setVisible(message);
    	else if (message.hasName("setFocus") && message.arity == 1)
     	   setFocus(message);
    	else {
    	  Vector forms = getAllForms();
    	  for(int i=0;i<forms.size();i++) {
    	    FormView form = (FormView)forms.elementAt(i);  
    	  	if(form.processMessage(message))
    	  	  return true;
    	  }
    	}
    	return false; 
	}
    
    public void processXML(Document xml) {
	    // xml.printString();
    	synchroniseForms(xml);
   }

	public void setEventHandler(EventHandler handler) {
	}
	
	public boolean setFocus(Message message) {
	   String identity = message.args[0].strValue();
   	   IWorkbenchPage page = FormsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();	
   	   try {
         String formType = getFormType(identity);
         if (formType!=null) {
            String id = formType + "@" + identity;
            page.showView("com.ceteva.forms.view",id,IWorkbenchPage.VIEW_ACTIVATE);
         }
   	   } catch (PartInitException e) {
	     e.printStackTrace();
   	   }
   	   handler.setCommandMode(false);
   	   return true;
	}
	
	public boolean setVisible(Message message) {
	   String identity = message.args[0].strValue();
   	   IWorkbenchPage page = FormsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();	
   	   try {
         String formType = getFormType(identity);
         if (formType!=null) {
            String id = formType + "@" + identity;
            IViewPart part = page.showView("com.ceteva.forms.view",id,IWorkbenchPage.VIEW_VISIBLE);
            page.bringToTop(part);
            handler.setCommandMode(false);
            return part != null;
         }
         return false;
   	   } 
   	   catch (PartInitException e) {
	     e.printStackTrace();
   	   }
	   return false;
	}
	
	public void synchroniseForms(Element xml) {
		
		// check that there is a form for each of the document's forms
		
		for(int i=0;i<xml.childrenSize();i++) {
			Element child = xml.getChild(i);
			if(child.hasName(XMLBindings.form)) {
			  boolean isOpen = child.getBoolean("isOpen");
			  if(isOpen) {
			    String identity = child.getString("identity");
			    FormView form = getForm(identity);
			    if(form == null) {
			      String type = child.getString("type");
			      String name = child.getString("name");
			      form = newForm(identity,type,name);
			    }
			    form.synchronise(child);
			  }
			}
		}
		
		// check that there is an open form in the document for each of the client's forms
		
		Vector forms = getAllForms();
		for(int i=0;i<forms.size();i++) {
		  FormView form = (FormView)forms.elementAt(i);
		  String identity = form.getIdentity();
		  boolean found = false;
		  for(int z=0;z<xml.childrenSize();z++) {
			Element child = xml.getChild(z);
			if(child.hasName(XMLBindings.form)) {
			  boolean isOpen = child.getBoolean("isOpen");
			  if(isOpen) {
				String id = child.getString("identity");	
				if(id.equals(identity))
				  found = true;
			  }
			}
		  }
		  if(!found)
			form.closeForm(true);
		}
	}	
	
}