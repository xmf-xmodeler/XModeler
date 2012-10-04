package com.ceteva.modelBrowser.views;

import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;
import com.ceteva.menus.MenuBuilder;
import com.ceteva.modelBrowser.ModelBrowserClient;
import com.ceteva.modelBrowser.ModelBrowserPlugin;
import com.ceteva.modelBrowser.actions.CollapseNodes;
import com.ceteva.modelBrowser.preferences.IPreferenceConstants;

public class ModelBrowserView extends ViewPart implements IPartListener2 {
	
	public static final String ID = "com.ceteva.modelBrowser.view";
	
	String type = "";
	String identity = "";
	Composite treeHolder;
	com.ceteva.modelBrowser.views.ModelBrowserTree tree;
	ModelBrowserClient client = null;
	EventHandler handler = null;
	boolean closable = true;

	public ModelBrowserView() {
	   registerAsListener();
	}

	public void registerAsListener() {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      if(page!=null)
        page.addPartListener(this);
	}
	
	public void unregisterAsListener() {
	  IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	  if(page!=null)
	    page.removePartListener(this);
    }

   public void dispose() {
      super.dispose();
      unregisterAsListener();
      tree.getControl().dispose();
	  MenuBuilder.dispose(getSite());
    } 
	
	public void setFocus() {
	   tree.setFocus();	
	}
	
	public void setName(String name) {
	   this.setPartName(name);
	   this.setTitleToolTip(name);
	}
	
	public void setType(String type) {
	   this.type = type;
	}
	
	public void setIdentity(String identity) {
	   this.identity = identity;
	   if(tree!=null)
	     tree.setIdentity(identity);    
	}

	public void collapseAllNodes() {
	   TreeItem[] items = ((Tree)tree.getControl()).getItems();
	   for (int i=0; i<items.length; i++) {
	      TreeItem item = items[i];
	      collapseNodes(item);
	   }
	}

	public void collapseNodes(TreeItem item) {
	   item.setExpanded(false);
	   TreeItem[] items = item.getItems();
	   for (int i=0; i<items.length; i++) {
	      TreeItem child = items[i];
	      collapseNodes(child);
	   }
	}

	public void focusGained() {
		if(handler!=null) {
		  Message m = handler.newMessage("focusGained",1);
	      Value v = new Value(getIdentity());
	      m.args[0] = v;
	      handler.raiseEvent(m);
		}
	}
	
	public void focusLost() {
		if(handler!=null) {
		  Message m = handler.newMessage("focusLost",1);
	      Value v = new Value(getIdentity());
	      m.args[0] = v;
	      handler.raiseEvent(m);
		}
	}
	
	public void treeClosed() {
	    client.browserClosed(getIdentity(),this);
		Message m = handler.newMessage("modelBrowserClosed",1);
		Value v = new Value(getIdentity());
		m.args[0] = v;
		handler.raiseEvent(m);
	}

   public void closeModelBrowser() {
  	   IWorkbenchPage page = ModelBrowserPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
  	   page.hideView(this);
   }
	
	public String getIdentity() {
	  return identity;
	}
	
	public String getType() {
	  return type;
	}

   /* public String getToolTipText() {
      return this.getPartName();
   } */

	
	private void fillLocalToolBar(IToolBarManager manager) {
		CollapseNodes collapse = new CollapseNodes(this);
		manager.add(collapse);
	}
	
	public void createPartControl(Composite parent) {
	  parent.setLayout(new FillLayout());
	  tree = new ModelBrowserTree(parent,identity,handler,this.getSite(),true);
	  IActionBars bars = getViewSite().getActionBars();
	  fillLocalToolBar(bars.getToolBarManager());
	}
	
	public void registerEventHandler(EventHandler handler) {
      this.handler = handler;
      if(tree!=null)
        tree.setEventHandler(handler);    
	}

   public Value processCall(Message message) {
		return new Value(false);
   }
	
	public boolean processMessage(Message message) {
		if (message.args[0].hasStrValue(identity)) {
			if (message.hasName("setName") && message.arity == 2) {
				String name = message.args[1].strValue();
				setName(name);
				handler.setCommandMode(false);
				return true;
			} else if (message.hasName("closeModelBrowser") && message.arity == 1) {
				closeModelBrowser();
				return true;
			}
			else
				return broadcastCommand(message); 
		}
		else
	      return broadcastCommand(message); 
	}
	
	public boolean broadcastCommand(Message message) {
      return tree.processMessage(message);
	}
	
	public void propertyChange(PropertyChangeEvent e) {
		String preference = e.getProperty();
	 	if (preference.equals(IPreferenceConstants.INVOKE_PROPERTY_EDITOR) || preference.equals(IPreferenceConstants.INVOKE_DIAGRAM_EDITOR)) {
	 		Message m = handler.newMessage(preference + "ActionChange",1);
			Value v1 = new Value((String)e.getNewValue());
			m.args[0] = v1;
			handler.raiseEvent(m);
		}
	}

	/* public void init() {
	   ModelBrowserPlugin
		   .getDefault()
		   .getPreferenceStore()
		   .addPropertyChangeListener(this);
	} */

    public void setClient(ModelBrowserClient client) {
	    this.client = client;
    }	

    public ModelBrowserClient getClient() {
	    return client;
    }	
    
    public void partActivated(IWorkbenchPartReference partRef) {
        if(partRef.getPart(false).equals(this)  && handler != null)
          focusGained();
      }

    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    public void partClosed(IWorkbenchPartReference partRef) {
      if(partRef.getPart(false).equals(this) && handler != null)  
        treeClosed();
    }

    public void partDeactivated(IWorkbenchPartReference partRef) {
        if(partRef.getPart(false) != null) { 
      	   if(partRef.getPart(false).equals(this) && handler != null)
            focusLost();
        }
      }

    public void partOpened(IWorkbenchPartReference partRef) {
    }

    public void partHidden(IWorkbenchPartReference partRef) {
    }

    public void partVisible(IWorkbenchPartReference partRef) {
    }

    public void partInputChanged(IWorkbenchPartReference partRef) {
    }
    
    public void synchronise(Element xml) {
    	tree.synchronise(xml);
    }

}