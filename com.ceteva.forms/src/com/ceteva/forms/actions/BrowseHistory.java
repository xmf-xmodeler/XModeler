package com.ceteva.forms.actions;

import org.eclipse.jface.action.Action;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.client.IconManager;
import com.ceteva.forms.FormsPlugin;
import com.ceteva.forms.views.FormView;

public class BrowseHistory extends Action {
	
	FormView form = null;
	
	public BrowseHistory(FormView form) {
	  setId("com.ceteva.forms.actions.BrowseHistory");
	  setText("Browse History");
	  setToolTipText("Select a form from the history");
	  setImageDescriptor(IconManager.getImageDescriptor(FormsPlugin.getDefault(),"History.gif"));
     this.form = form;
	}
	
	public void run() {
	   EventHandler handler = form.getHandler();
		if(handler!=null) {
			  Message m = handler.newMessage("browseHistory",1);
		      Value v = new Value(form.getIdentity());
		      m.args[0] = v;
		      handler.raiseEvent(m);
			}
	}
	
	public void update() {
	}
}