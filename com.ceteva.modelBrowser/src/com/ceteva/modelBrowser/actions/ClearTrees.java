package com.ceteva.modelBrowser.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.CTabFolder;

import com.ceteva.client.IconManager;
import com.ceteva.modelBrowser.ModelBrowserPlugin;

public class ClearTrees extends Action {
	
	CTabFolder trees;
	
	public ClearTrees(CTabFolder trees) {
	  this.setId("com.ceteva.modelBrowser.actions.ClearTrees");
	  this.trees = trees;
	  setText("Clear trees");
	  setToolTipText("Clear all open trees");
	  setImageDescriptor(IconManager.getImageDescriptor(ModelBrowserPlugin.getDefault(),"Clear.gif"));
	}
	
	public void run() {
	  /* if(trees != null) {  
	    CTabItem[] fs = trees.getItems();
	    for(int i=0;i<fs.length;i++) {
	      ModelBrowser t = (ModelBrowser)fs[i];
	  	  if(t.isClosable()) {
	  	  	t.treeClosed();  
	  	  	t.dispose();
	  	  }
	  	}
	  } */
	}
	
	public void update() {
      this.setEnabled(trees.getItemCount() > 0);
	}
}