package com.ceteva.forms.views;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Control;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;

class FormList extends FormElement {
   
   Hashtable idBindings = new Hashtable();
   Hashtable bindingIds = new Hashtable();
   
   private List list = null;
   
   public FormList(Composite parent,String identity,EventHandler handler) {
	  super(identity);
      list = new List(parent,SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
      this.handler = handler;
      addListener();
   }

   public void addItem(String identity,String text) {
      idBindings.put(new Integer(list.getItemCount()),identity);
      bindingIds.put(identity,text);
      list.add(text);
   }

   public Control getControl() {
      return list;
   }

   public void addListener() {
      list.addListener(SWT.MouseDoubleClick, new Listener() {
         public void handleEvent(Event e) {
            int count = list.getSelectionIndex();
            if(count!=-1) {
               Message m = handler.newMessage("doubleSelected",2);
    		   Value v1 = new Value((String)idBindings.get(new Integer(count)));
    		   Value v2 = new Value(getIdentity());
    		   m.args[0] = v1;
    		   m.args[1] = v2;
    		   raiseEvent(m);
            }
         }
      });
      list.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event e) {
            int count = list.getSelectionIndex();
            if(count!=-1) {
               Message m = handler.newMessage("selected",2);
     		   Value v1 = new Value((String)idBindings.get(new Integer(count)));
     		   Value v2 = new Value(getIdentity());
     		   m.args[0] = v1;
     		   m.args[1] = v2;
     		   raiseEvent(m);
            }
         }
      }); 	
      list.addListener(SWT.FocusOut, new Listener() {
         public void handleEvent(Event e) {
            list.deselectAll();
         }
      });
   } 
   
   public void setBounds(int x,int y,int width,int height) {
      list.setBounds(x,y,width,height);	
   }
   
   public Value processCall(Message message) {
		return null;
   }   
   
   public boolean processMessage(Message message) {
      if(message.arity >= 1) {
         if(message.args[0].hasStrValue(getIdentity())) {
            if(message.hasName("addItem") && message.arity == 3) {
               String identity = message.args[1].strValue();
               String text = message.args[2].strValue();
               addItem(identity,text);
               return true;
            }
         	if(message.hasName("clear") && message.arity == 1) {
         	   removeAllItems();
			   return true;
			}
            else
               if(ComponentCommandHandler.processMessage(list,message))
                  return true;
         }
         else
            if(checkListItems(message))
               return true;
      }
      return super.processMessage(message);
   }
   
   public boolean checkListItems(Message message) {
	   if(message.hasName("removeItem") && message.arity == 1) {
		   String target = message.args[0].strValue();
		   if (idBindings.contains(target)) {
			   String item = (String)bindingIds.get(target);
			   int count = list.indexOf(item);
			   list.remove(item);
			   refreshIdBindings(count);
			   bindingIds.remove(target);
			   return true;
		   }
	   }
	   if(message.hasName("enableDrag") && message.arity == 1) {
		   String target = message.args[0].strValue();
		   if (idBindings.contains(target)) {
			   enableDrag();
			   return true;
		   }
	   }
	   if(message.hasName("enableDrop") && message.arity == 1) {
		   String target = message.args[0].strValue();
		   if (idBindings.contains(target)) {
			   enableDrop();
			   return true;
		   }
	   }
	   return false;
   }
   
   void refreshIdBindings(int removedIndex) {
      idBindings.remove(new Integer(removedIndex));
      Hashtable dummyTable = new Hashtable();
      Enumeration e = idBindings.keys();
      while (e.hasMoreElements()) {
         Integer key = (Integer)e.nextElement();
         String item = (String)idBindings.get(key);
         int index = key.intValue();
         if (index>=removedIndex) {
            key = new Integer(index-1);
         }
         dummyTable.put(key,item);
      }
      idBindings = dummyTable;
   }
	
   public void removeAllItems() {
	   list.removeAll();
	   idBindings.clear();
	   bindingIds.clear();
   }
	
   public void dragSetData(DragSourceEvent event) {
	   if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
		   int count = list.getSelectionIndex();
		   if(count!=-1) {
			   String id = (String)idBindings.get(new Integer(count));
			   //event.data = id;
			   Vector dragIds = new Vector();
			   dragIds.add(id);
			   String dragIdsString = dragIds.toString();
			   event.data = dragIdsString;
		   }
	   }
   }
   
   public void drop(DropTargetEvent event) {
	   if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
		   int count = list.getSelectionIndex();
		   if(count!=-1) {
			   String dragId = (String)event.data;
			   String dropId = (String)idBindings.get(new Integer(count));
			   System.out.println("drop: '" + dragId + "->" + dropId + "'");
		   }
	   }
   }
   
}