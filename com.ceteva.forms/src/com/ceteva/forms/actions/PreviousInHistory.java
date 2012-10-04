package com.ceteva.forms.actions;

import org.eclipse.jface.action.Action;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.client.IconManager;
import com.ceteva.forms.FormsPlugin;
import com.ceteva.forms.views.FormView;

public class PreviousInHistory extends Action {
	
	FormView form = null;
	public boolean enabled = false;
	
	public PreviousInHistory(FormView form,boolean enabled) {
		setId("com.ceteva.forms.actions.PreviousInHistory");
		setText("Previous in History");
		setToolTipText("Selects the previous form in the history");
		setImageDescriptor(IconManager.getImageDescriptor(FormsPlugin.getDefault(),"Back.gif"));
		this.form = form;
		this.enabled = enabled;
		update();
	}
	
	public void run() {
	   EventHandler handler = form.getHandler();
	   if(enabled && handler!=null) {
	      Message m = handler.newMessage("previousInHistory",1);
	      Value v = new Value(form.getIdentity());
	      m.args[0] = v;
	      handler.raiseEvent(m);
	   }
	}

    public void update() {
    	String icon = enabled ? "Back.gif" : "BackDisabled.gif";
    	setImageDescriptor(IconManager.getImageDescriptor(FormsPlugin.getDefault(),icon));
    }
}