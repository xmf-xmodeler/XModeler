package com.ceteva.forms.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;

class FormComboBox extends FormElement {
	
	private Combo combo = null;
	
	public FormComboBox(Composite parent,String identity,EventHandler handler) {
		super(identity);
		combo = new Combo(parent,SWT.DROP_DOWN | SWT.READ_ONLY);
		this.handler = handler;
		addListener();
	}
	
	public void addItem(String item,boolean select) {
		combo.add(item);
		if(select)
		  combo.select(combo.getItemCount()-1);
	}

	public Control getControl() {
		return combo;
	}

	public void addListener() {
		combo.addSelectionListener(new SelectionListener () {
		  public void widgetSelected (SelectionEvent e) {
		  	Message m = handler.newMessage("comboBoxSelection",2);
			Value v1 = new Value(getIdentity());
			Value v2 = new Value(combo.getItem(combo.getSelectionIndex()));
			m.args[0] = v1;
			m.args[1] = v2;
			raiseEvent(m);
		  }
		  public void widgetDefaultSelected(SelectionEvent e) {
		  }
		});
	}
	
	public void setBounds(int x,int y,int width,int height) {
	  	combo.setBounds(x,y,width,height);	
	}
	
	public Value processCall(Message message) {
		return null;
	}	
	
	public boolean processMessage(Message message) {
		if(message.arity >= 1) {
		  if(message.args[0].hasStrValue(getIdentity())) {
		  	if(message.hasName("addItem") && message.arity == 2) { 
		      String value = message.args[1].strValue();
		      addItem(value,false);
		      return true;
		  	}
		  	if(message.hasName("setSelection") && message.arity == 2) { 
		      int index = message.args[1].intValue;
		  	  selectItem(index);
		      return true;
		  	}
		    else if(message.hasName("clear") && message.arity == 1) {
		      removeAllItems();
		      return true;
		    }
		    else 
		      if(ComponentCommandHandler.processMessage(combo,message))
		        return true;
		  }
	    }
		return super.processMessage(message);
	}
	
	public void removeAllItems() {
		combo.removeAll();
	}
	
	public void selectItem(int index) {
		combo.select(index);
	}
	
}