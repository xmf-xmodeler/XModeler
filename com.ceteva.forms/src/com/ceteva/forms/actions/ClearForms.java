package com.ceteva.forms.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.CTabFolder;

import com.ceteva.client.IconManager;
import com.ceteva.forms.FormsPlugin;

public class ClearForms extends Action {
	
	CTabFolder forms;
	
	public ClearForms(CTabFolder forms) {
	  setId("com.ceteva.forms.actions.ClearForms");
	  setText("Clear Forms");
	  setToolTipText("Clear all open forms");
	  setImageDescriptor(IconManager.getImageDescriptor(FormsPlugin.getDefault(),"Clear.gif"));
	  this.forms = forms;
	}
	
	public void run() {
	  /* if(forms != null) {  
	    CTabItem[] fs = forms.getItems();
	    for(int i=0;i<fs.length;i++) {
	      Form f = (Form)fs[i];
	  	  if(f.changesPending()) {
	  	  	if(f.closeForm()) {
	  	  	  f.formClosed();
	  	  	  f.dispose();
	  	  	}
	  	  }
	  	  else {
	  	  	f.formClosed();
	  	    f.dispose();
	  	  }
	    }
	  } */
	}
	
	public void update() {
      this.setEnabled(forms.getItemCount() > 0);
	}
}