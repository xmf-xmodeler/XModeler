package com.ceteva.forms.views;

import java.util.EventListener;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
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
import com.ceteva.forms.FormsPlugin;
import com.ceteva.forms.XMLBindings;
import com.ceteva.forms.actions.BrowseHistory;
import com.ceteva.forms.actions.ClearHistory;
import com.ceteva.forms.actions.LockForm;
import com.ceteva.forms.actions.NextInHistory;
import com.ceteva.forms.actions.PreviousInHistory;
import com.ceteva.menus.MenuBuilder;
import com.ceteva.menus.MenuListener;
import com.ceteva.menus.MenuManager;

public class FormView extends ViewPart implements IPartListener2, MenuListener {

	String type = "";
	String identity = "";
	String tabName = "";
	ScrolledComposite scrollable;
	Composite formContentsHolder;
    Composite canvas;
	Vector components = new Vector();
	Vector textBoxes = new Vector();
	EventHandler handler = null;
	boolean changesPending = false;
	boolean locked = false;
	LockForm lockAction = null;
	IToolBarManager toolBarManager = null;
    Vector listeners = new Vector();
    boolean xmfclose = false;
    
    public boolean addComponent(FormElement component) {
      if(component != null) {
        components.add(component);
        formContentsHolder.setSize(formContentsHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return true;
      }
      return false;
    }
	
	public void registerAsListener() {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      if(page!=null)
        page.addPartListener(this);
      MenuManager.addMenuListener(this);
	}
	
	public void unregisterAsListener() {
	  IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	  if(page!=null)
	    page.removePartListener(this);
      MenuManager.removeMenuListener(this);
    }
	
	public void createPartControl(Composite parent) {
      canvas = parent;
	  scrollable = new ScrolledComposite(parent,SWT.H_SCROLL | SWT.V_SCROLL);
	  scrollable.setAlwaysShowScrollBars(false);
	  IActionBars bars = getViewSite().getActionBars();
	  toolBarManager = bars.getToolBarManager();
	  init();
	  registerAsListener();
	}
	
	public FormElement getComponent(String identity) {
	  for(int i=0;i<components.size();i++) {
	    FormElement element = (FormElement)components.elementAt(i);
	    if(element.getIdentity().equals(identity))
	      return element;
	  }
	  return null;
	}

	private void setLockTool(boolean isLocked) {
		if (toolBarManager.find("com.ceteva.forms.actions.LockForm") == null) {
			lockAction = new LockForm(this);
			toolBarManager.add(lockAction);
			toolBarManager.update(false);
			this.setLocked(isLocked);
		}
		else {
			ActionContributionItem aci = (ActionContributionItem)toolBarManager.find("com.ceteva.forms.actions.LockForm");
			LockForm lockForm = (LockForm)aci.getAction();
			this.setLocked(isLocked);
			lockForm.update();
		}
	}

	private void setHistoryTools() {
   	  if (toolBarManager.find("com.ceteva.forms.actions.ClearHistory") == null) {
   		ClearHistory clearHistory = new ClearHistory(this);
   		toolBarManager.add(clearHistory);
   		toolBarManager.update(false);
   	  }
      if (toolBarManager.find("com.ceteva.forms.actions.BrowseHistory") == null) {
   		BrowseHistory browseHistory = new BrowseHistory(this);
   		toolBarManager.add(browseHistory);
   		toolBarManager.update(false);
   	  }
	}

	private void setNextInHistoryTool(boolean enabled) {
		if (toolBarManager.find("com.ceteva.forms.actions.NextInHistory") == null) {
			NextInHistory nextInHistory = new NextInHistory(this,enabled);
			toolBarManager.add(nextInHistory);
			toolBarManager.update(false);
		}
		else {
			ActionContributionItem aci = (ActionContributionItem)toolBarManager.find("com.ceteva.forms.actions.NextInHistory");
			NextInHistory nextInHistory = (NextInHistory)aci.getAction();
			nextInHistory.enabled = enabled;
			nextInHistory.update();
		}
	}
	
	private void setPreviousInHistoryTool(boolean enabled) {
		if (toolBarManager.find("com.ceteva.forms.actions.PreviousInHistory") == null) {
			PreviousInHistory previousInHistory = new PreviousInHistory(this,enabled);
			toolBarManager.add(previousInHistory);
			toolBarManager.update(false);
		}
		else {
			ActionContributionItem aci = (ActionContributionItem)toolBarManager.find("com.ceteva.forms.actions.PreviousInHistory");
			PreviousInHistory previousInHistory = (PreviousInHistory)aci.getAction();
			previousInHistory.enabled = enabled;
			previousInHistory.update();
		}
	}

	private void removeCanvasEventListeners() {
	   Iterator ls = listeners.iterator();
	   while (ls.hasNext()) {
	      Object l = ls.next();
	      if (l instanceof ControlListener)
	         canvas.removeControlListener((ControlListener)l);
	   }
	}
    
	public void init() {
      removeCanvasEventListeners();
      components = new Vector();
      textBoxes = new Vector();
      if (formContentsHolder!=null)
         formContentsHolder.dispose();
	  formContentsHolder = new Composite(scrollable,SWT.NONE);
	  scrollable.setContent(formContentsHolder);
	  buildMenu();
	}
	
	public void buildMenu() {
	  Menu m = formContentsHolder.getMenu();
	  if(m != null)
	    m.dispose();
	  MenuBuilder.resetKeyBindings(getSite());	
	  org.eclipse.jface.action.MenuManager menu = new org.eclipse.jface.action.MenuManager();
	  MenuBuilder.calculateMenu(identity,menu,getSite());
	  formContentsHolder.setMenu(menu.createContextMenu(formContentsHolder));
	}
	
	public void clearForm() {
      for(int i=0;i<components.size();i++) {
         FormElement component = (FormElement)components.elementAt(i);
         component.disableEvents();
      }
      init();
	}
	
	public boolean delete(Message message) {
	  String id = message.args[0].strValue();
	  for(int i=0;i<components.size();i++) {
        FormElement component = (FormElement)components.elementAt(i);
        if(component.getIdentity().equals(id))
          components.remove(component);
          return true;
	  }
	  return false;
	}
	
	public EventHandler getHandler() {
	   return handler;
	}
	
	public String getType() {
	   return type;
	}
	
	public void setName(String name) {
      this.tabName = name;
	  this.setPartName(name);
	  this.setTitleToolTip(name);
	}
	
	public void setIdentity(String identity) {
	  this.identity = identity;	
	}
	
	public void setType(String type) {
	  this.type = type;
	}
	
	public void registerEventHandler(EventHandler handler) {
	  this.handler = handler;	
	}

	public void setFocus() {
	  formContentsHolder.setFocus();
	}
	
	// Currently not supported - unsaved changes are lost when a form is closed
	
	/* public boolean closeForm() {
		MessageDialog dialog = new MessageDialog(
				Display.getCurrent().getActiveShell(),
				"Uncommited Text Changes",
				null,
				"You have uncommitted text changes that will be lost if "
						+ "you close this form.\n\nDo you still want to close the form?",
				MessageDialog.QUESTION, new String[] { "OK", "Cancel" }, 1);
		if (dialog.open() == 1)
			return false;
		else
			return true;
	} */

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

	public void formClosed() {
		if(handler!=null) {
		  Message m = handler.newMessage("formClosed",1);
	      Value v = new Value(getIdentity());
	      m.args[0] = v;
	      handler.raiseEvent(m);
		}
	}

	public boolean changesPending() {
		return changesPending;
	}
   
	/*
	 * public boolean isVisible() { CTabFolder parent = getParent(); Form form =
	 * (Form)parent.getSelection(); if(form == this) return true; return false; }
	 */

   public void enableRender() {
   	  // scrollable.setVisible(true);
   }
   
   public void disableRender() {
   	  // scrollable.setVisible(false);
   }
   
   public String getIdentity() {
      return identity;
   }

   public Value processCall(Message message) {
	   return broadcastCall(message);
   }
   
   public boolean processMessage(Message message) {
		boolean processed = false;
		if (message.args[0].hasStrValue(identity)) {
			if (message.hasName("clearForm") && message.arity == 1) {
				clearForm();
				processed = true;
			} else if (message.hasName("closeForm") && message.arity == 1) {
				closeForm(true);
				processed = true;
			} else if (message.hasName("enableRender")
					&& message.arity == 1) {
				enableRender();
				processed = true;
			} else if (message.hasName("disableRender")
					&& message.arity == 1) {
				disableRender();
				processed = true;
			} 
			else if (message.hasName("newButton") && message.arity == 7)
				return newButton(message);
			else if (message.hasName("newText") && message.arity == 5)
				return newText(message);
			else if (message.hasName("newTextField") && (message.arity == 7 || message.arity == 8))
				return newTextField(message);
			else if (message.hasName("newComboBox") && message.arity == 6)
				return newComboBox(message);
			else if (message.hasName("newList") && message.arity == 6)
				return newList(message);
			else if (message.hasName("newCheckBox") && message.arity == 5)
				return newCheckBox(message);
			else if (message.hasName("newTree") && message.arity == 7) {
				return newTree(message);
			}
		    else if (message.hasName("newTextBox") && message.arity == 7)
				return newTextBox(message);
			else if (message.hasName("newTable") && message.arity == 8)
				return newTable(message);
			else if (message.hasName("setText") && message.arity == 2)
			    return setText(message);
		    else if (message.hasName("setTool") && message.arity == 3)
			    return setTool(message);
			else if(message.hasName("delete") && message.arity == 1)
				return delete(message);
		}
		else if(broadcastCommand(message)) {
	       processed = true;
	    }
		return processed;
	}
   
   public boolean broadcastCommand(Message message) {
      for(int i=0;i<components.size();i++) {
         FormElement component = (FormElement)components.elementAt(i);
         if(component.processMessage(message))
            return true;
      }
      return false;
   }
   
   public Value broadcastCall(Message message) {
	  for(int i=0;i<components.size();i++) {
	    FormElement component = (FormElement)components.elementAt(i);
	    Value v = component.processCall(message);
	    if(v != null)
	      return v;	
	  }
	  return null;
   }
   
   public boolean newButton(Message message) {
      String identity = message.args[1].strValue();    
      String text = message.args[2].strValue();
      int x = message.args[3].intValue;
      int y = message.args[4].intValue;
      int width = message.args[5].intValue;
      int height = message.args[6].intValue;
      FormButton button = newButton(identity,text,x,y,width,height);
      return addComponent(button);  
   }
   
   public FormButton newButton(String identity,String text,int x,int y,int width,int height) {
     FormButton button = new FormButton(formContentsHolder,identity,handler);	 	
	 button.setText(text);
	 button.setBounds(x,y,width,height);
	 return button;
   }
   
   public boolean newText(Message message) {
      String identity = message.args[1].strValue();    
      String label = message.args[2].strValue();
      int x = message.args[3].intValue;
      int y = message.args[4].intValue;
      FormText text = newText(identity,label,x,y);
      return addComponent(text);
   }
   
   public FormText newText(String identity,String label,int x,int y) {
	  FormText text = new FormText(formContentsHolder,identity,handler);
	  text.setText(label);
	  text.setLocation(new Point(x,y));
	  text.calculateSize();
	  return text; 
   }
   
   public boolean newTextField(Message message) {
      String identity = message.args[1].strValue();
      String text = "";
      int x;
      int y;
      int width;
      int height;
      boolean editable;
      if(message.arity==7) {
         x = message.args[2].intValue; 
         y = message.args[3].intValue; 
         width = message.args[4].intValue; 
         height = message.args[5].intValue;
         editable = message.args[6].boolValue;
      }
      else {
         text = message.args[2].strValue();
         x = message.args[3].intValue;
         y = message.args[4].intValue;
         width = message.args[5].intValue;
         height = message.args[6].intValue;
         editable = message.args[7].boolValue;		 	
      }
      FormTextField textField = newTextField(identity,text,x,y,width,height,editable);
      return addComponent(textField);
   }
   
   public FormTextField newTextField(String identity,String text,int x,int y,int width,int height,boolean editable) {
	   FormTextField field = new FormTextField(formContentsHolder,identity,handler,getViewSite());
	   field.setBounds(x,y,width,height);
	   field.setText(text);
	   field.setEditable(editable);
	   return field;
   }
   
   public boolean newComboBox(Message message) {
      String identity = message.args[1].strValue();
      int x = message.args[2].intValue;
      int y = message.args[3].intValue;
      int width = message.args[4].intValue;;
      int height = message.args[5].intValue;
      FormComboBox comboBox = newComboBox(identity,x,y,width,height);
      return addComponent(comboBox);
   }
   
   public FormComboBox newComboBox(String identity,int x,int y,int width,int height) {
	  FormComboBox combo = new FormComboBox(formContentsHolder,identity,handler);
	  combo.setBounds(x,y,width,height);
	  return combo;
   }
   
   public boolean newList(Message message) {
      String identity = message.args[1].strValue();
      int x = message.args[2].intValue;
      int y = message.args[3].intValue;;
      int width = message.args[4].intValue;
      int height = message.args[5].intValue;
      FormList list = newList(identity,x,y,width,height);
      return addComponent(list);
   }
   
   public FormList newList(String identity,int x,int y,int width,int height) {
	  FormList list = new FormList(formContentsHolder,identity,handler);
	  list.setBounds(x,y,width,height);
	  return list; 
   }
   
   public boolean newCheckBox(Message message) {
      String identity = message.args[1].strValue();
      int x = message.args[2].intValue;
      int y = message.args[3].intValue;
      boolean value = message.args[4].boolValue;
      FormCheckBox checkBox = newCheckBox(identity,x,y,value);
      return addComponent(checkBox);
   }
   
   public FormCheckBox newCheckBox(String identity,int x,int y,boolean value) {
	  FormCheckBox checkbox = new FormCheckBox(formContentsHolder,identity,handler);
	  checkbox.setLocation(new Point(x,y));
	  checkbox.setSelected(value);
	  return checkbox;
   }
   
   public boolean newTree(Message message) {
      String identity = message.args[1].strValue();
      int x = message.args[2].intValue;
      int y = message.args[3].intValue;
      int width = message.args[4].intValue;
      int height = message.args[5].intValue;
      boolean editable = message.args[6].boolValue;
      FormTree tree = newTree(identity,x,y,width,height,editable);
      return addComponent(tree);
   }
   
   public FormTree newTree(String identity,int x,int y,int width,int height,boolean editable) {
	  FormTree tree = new FormTree(formContentsHolder,identity,handler,getViewSite(),editable,false);
	  tree.setBounds(x,y,width,height);
	  return tree;
   }   
   
   public boolean newTextBox(Message message) {
      String identity = message.args[1].strValue();;
      int x = message.args[2].intValue;
      int y = message.args[3].intValue;
      int width = message.args[4].intValue;
      int height = message.args[5].intValue;
      boolean editable = message.args[6].boolValue;
      FormTextBox textBox = newTextBox(identity,"",x,y,width,height,editable);
      return addComponent(textBox);
   }
   
   public FormTextBox newTextBox(String identity,String text,int x,int y,int width,int height,boolean editable) {
	  FormTextBox box = new FormTextBox(formContentsHolder,identity,handler,this);
	  textBoxes.add(box);
	  box.setBounds(x,y,width,height);
	  box.setEditable(editable);
	  box.setText(text);
	  return box;
   }

   public boolean newTable(Message message) {
	   String tableID = message.args[1].strValue();
	   int x = message.args[2].intValue;
	   int y = message.args[3].intValue;;
	   int width = message.args[4].intValue;
	   int height = message.args[5].intValue;
	   int cols = message.args[6].intValue;
	   int rows = message.args[7].intValue;
	   FormTable table = new FormTable(formContentsHolder,tableID,handler,getViewSite(),cols,rows);
	   table.setBounds(x,y,width,height);
	   return addComponent(table);
   }
   
   public String getToolTipText() {
      return getTitle();
   }
   
   public boolean setText(Message message) {
     String text = message.args[1].strValue();
     changeText(text);
     return true;
   }

   public void maximiseToCanvas(Message message) {
     String componentId = message.args[1].strValue();
	 for(int i=0;i<components.size();i++) {
	     FormElement component = (FormElement)components.elementAt(i);
	     // this should work with all components, but for the time being it
	     // only supports FormTextBox
	     if(component.getIdentity().equals(componentId) && component instanceof FormTextBox) 
	        ((FormTextBox)component).maximiseToCanvas(scrollable,formContentsHolder);
	 }
   }
   
   public boolean setTool(Message message) {
      String toolText = message.args[1].strValue();
      boolean value = message.args[2].boolValue;
      if (toolText.equals("lock")) {
         setLockTool(value);
         return true;
      }
      if (toolText.equals("browseAndClearHistory")) {
         setHistoryTools();
         return true;
      }
      if (toolText.equals("nextInHistory")) {
         setNextInHistoryTool(value);
         return true;
      }
      if (toolText.equals("previousInHistory")) {
         setPreviousInHistoryTool(value);
         return true;
      }
      return false;
    }
   
   public void changeText(String s) {
      tabName = s;
      this.setPartName(s);
   }

   public void setChangesPending(boolean b, FormTextBox targetBox) {
      changesPending = b;
      if (b) {
         this.setPartName("*"+tabName);
         Iterator i = textBoxes.iterator();
         while (i.hasNext()) {
            FormTextBox box = (FormTextBox)i.next();
            if (box!=targetBox)
               box.setEnabled(false);
         }
      }
      else {
         this.setPartName(tabName);
         Iterator i = textBoxes.iterator();
         while (i.hasNext()) {
            ((FormTextBox)i.next()).setEnabled(true);
         }
      }
   }
   
   public void setLocked(boolean locked) {
   	 this.locked = locked;
   	 lockAction.update();
   }
   
   public void toggleLock() {
   	setLocked(!locked);
      Message m = handler.newMessage("lockForm",2);
		m.args[0] = new Value(identity);
		m.args[1] = new Value(locked);
		handler.raiseEvent(m);	
   }
   
   public boolean isLocked() {
   	 return locked;
   }
   
   public void dispose() {
     super.dispose();
   	 unregisterAsListener();
   	 formContentsHolder.dispose();
   	 scrollable.dispose();
	 MenuBuilder.dispose(getSite());
   }
   
   public void closeForm(boolean xmfclose) {
	   this.xmfclose = xmfclose;
  	   IWorkbenchPage page = FormsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
  	   page.hideView(this);
   }
   
   public void propertyChange(PropertyChangeEvent event) {
   }

   public void partActivated(IWorkbenchPartReference partRef) {
     if(partRef.getPart(false).equals(this)  && handler != null)
       focusGained();
   }

   public void partBroughtToTop(IWorkbenchPartReference partRef) {
   }

   public void partClosed(IWorkbenchPartReference partRef) {
     if(partRef.getPart(false).equals(this) && handler != null)
       if(!xmfclose)
         formClosed();
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

   public void newMenuAdded() {
     buildMenu();
   }
   
   public void addCanvasEventListener(EventListener listener) {
      listeners.add(listener);
   }
   
   public void synchronise(Element xml) {
	  
	  // Check that all elements in the document are represented on the form
	   
	  setName(xml.getString("name"));
	  for(int i=0;i<xml.childrenSize();i++) {  
		Element child = xml.getChild(i);
	    if(child.hasName(XMLBindings.button))
	      synchroniseButton(child);
	    if(child.hasName(XMLBindings.checkbox))
	      synchroniseCheckbox(child);
	    if(child.hasName(XMLBindings.combobox))
		  synchroniseCombobox(child);
	    if(child.hasName(XMLBindings.list))
		  synchroniseList(child);
	    if(child.hasName(XMLBindings.label))
		  synchroniseLabel(child);
	    if(child.hasName(XMLBindings.textbox))
		  synchroniseTextbox(child);
	    if(child.hasName(XMLBindings.textfield))
		  synchroniseTextfield(child);
	    if(child.hasName(XMLBindings.tree))
		  synchroniseTree(child);
	  }
	  
	  // Check that all element on the form are in the document
	  // todo
	  
   }
   
   public void synchroniseButton(Element xml) {
	   String identity = xml.getString("identity");
       String label = xml.getString("label");
	   int x = xml.getInteger("x");
       int y = xml.getInteger("y");
       int width = xml.getInteger("width");
       int height = xml.getInteger("height");
       
	   FormElement element = getComponent(identity);
	   if(element != null && element instanceof FormButton) {
	     FormButton button = (FormButton)element;
	     button.setText(label);
	   }
	   else
	     addComponent(newButton(identity,label,x,y,width,height));
   }
   
   public void synchroniseCheckbox(Element xml) {
	   String identity = xml.getString("identity");
       boolean value = xml.getBoolean("value");
	   int x = xml.getInteger("x");
       int y = xml.getInteger("y");
       int width = xml.getInteger("width");
       int height = xml.getInteger("height");
       
	   FormElement element = getComponent(identity);
	   if(element != null && element instanceof FormCheckBox) {
		 FormCheckBox checkbox = (FormCheckBox)element;
	     checkbox.setSelected(value);
	   }
	   else
	     addComponent(newCheckBox(identity,x,y,value));	   
   }
   
   public void synchroniseCombobox(Element xml) {
	   String identity = xml.getString("identity");
	   int x = xml.getInteger("x");
       int y = xml.getInteger("y");
       int width = xml.getInteger("width");
       int height = xml.getInteger("height");
       int selected = xml.getInteger("selected");
       
	   FormElement element = getComponent(identity);
	   if(element != null && element instanceof FormComboBox) {
		 FormComboBox combobox = (FormComboBox)element;
		 combobox.removeAllItems();
		 synchroniseComboboxValues(xml,combobox);
		 combobox.selectItem(selected);
	   }
	   else {
	     FormComboBox combobox = newComboBox(identity,x,y,width,height);
		 addComponent(combobox);
		 synchroniseComboboxValues(xml,combobox);
		 combobox.selectItem(selected);
	   }
   }
   
   public void synchroniseComboboxValues(Element xml,FormComboBox combobox) {
	   for(int i=0;i<xml.childrenSize();i++) {
	     Element child = xml.getChild(i);
		 if(child.getName().equals(XMLBindings.value)) {
		   String value = child.getString("value");
		   combobox.addItem(value,false);
		 }
	   }   
   }
   
   public void synchroniseLabel(Element xml) {
	   String identity = xml.getString("identity");
	   int x = xml.getInteger("x");
       int y = xml.getInteger("y");
       String text = xml.getString("text");
	   
       FormElement element = getComponent(identity);
	   if(element != null && element instanceof FormText) {
		 FormText label = (FormText)element;
		 // label.setText(text);
	   }
	   else
		 addComponent(newText(identity,text,x,y));
   }
   
   public void synchroniseList(Element xml) {
	   String identity = xml.getString("identity");
	   int x = xml.getInteger("x");
       int y = xml.getInteger("y");
       int width = xml.getInteger("width");
       int height = xml.getInteger("height");
	   FormElement element = getComponent(identity);
	   if(element != null && element instanceof FormList) {
		 FormList listbox = (FormList)element;
		 listbox.removeAllItems();
		 synchroniseListValues(xml,listbox);
	   }
	   else {
		 FormList listbox = newList(identity,x,y,width,height);
		 addComponent(listbox);
		 synchroniseListValues(xml,listbox);
	   }
   }
   
   public void synchroniseListValues(Element xml,FormList listbox) {
	   for(int i=0;i<xml.childrenSize();i++) {
	     Element child = xml.getChild(i);
		 if(child.getName().equals(XMLBindings.value)) {
		   String identity = child.getString("identity");
		   String text = child.getString("text");
		   listbox.addItem(identity,text);
		 }
	   }	   
   }

   public void synchroniseTextbox(Element xml) {
	   String identity = xml.getString("identity");
	   int x = xml.getInteger("x");
       int y = xml.getInteger("y");
       int width = xml.getInteger("width");
       int height = xml.getInteger("height");
       String text = xml.getString("text");
       boolean editable = xml.getBoolean("editable");
       
       FormElement element = getComponent(identity);
	   if(element != null && element instanceof FormTextBox) {
		 FormTextBox textbox = (FormTextBox)element;
		 textbox.setText(text);
		 textbox.setEditable(editable);
	   }
	   else
		 addComponent(newTextBox(identity,text,x,y,width,height,editable));   
   }

   public void synchroniseTextfield(Element xml) {
	   String identity = xml.getString("identity");
	   int x = xml.getInteger("x");
       int y = xml.getInteger("y");
       int width = xml.getInteger("width");
       int height = xml.getInteger("height");
       String text = xml.getString("text");
       boolean editable = xml.getBoolean("editable");
       
       FormElement element = getComponent(identity);
	   if(element != null && element instanceof FormTextField) {
	     FormTextField textfield = (FormTextField)element;
		 textfield.setText(text);
		 textfield.setEditable(editable);
	   }
	   else
	     addComponent(newTextField(identity,text,x,y,width,height,editable));
   }

   public void synchroniseTree(Element xml) {
	   String identity = xml.getString("identity");
	   int x = xml.getInteger("x");
       int y = xml.getInteger("y");
       int width = xml.getInteger("width");
       int height = xml.getInteger("height");
       boolean editable = xml.getBoolean("editable");
       
       FormElement element = getComponent(identity);
	   if(element != null && element instanceof FormTree) {
	     FormTree tree = (FormTree)element;
	     tree.synchronise(xml);
	   }
	   else {
		 FormTree tree = newTree(identity,x,y,width,height,editable);
	     addComponent(tree);
		 tree.synchronise(xml);
	   }
   }

   public void updateComponent(Element xml,FormElement element) {
   }
}