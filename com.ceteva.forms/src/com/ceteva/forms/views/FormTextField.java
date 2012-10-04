package com.ceteva.forms.views;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ColorManager;
import com.ceteva.client.EventHandler;
import com.ceteva.forms.FormsPlugin;
import com.ceteva.menus.MenuBuilder;
import com.ceteva.menus.MenuListener;
import com.ceteva.menus.MenuManager;

class FormTextField extends FormElement implements MenuListener, DisposeListener {

	IWorkbenchPartSite site;
	Hashtable    menuItems   = new Hashtable();
    Text         text        = null;
    boolean      changesMade = false;
    Menu         menu        = null;
    Composite    owningForm;

    public FormTextField(Composite parent, String identity, EventHandler handler, IWorkbenchPartSite site) {
    	super(identity);
    	this.owningForm = parent;
        this.handler = handler;
        initText(parent);
        // setMenuListener();
        calculateMenu();
        MenuManager.addMenuListener(this);
        text.addDisposeListener(this);
    }

    public Control getControl() {
        return text;
    }

    public void calculateMenu() {
    	Menu m = text.getMenu();
    	if(m != null)
    	  m.dispose();
    	MenuBuilder.resetKeyBindings(site);
    	org.eclipse.jface.action.MenuManager menu = new org.eclipse.jface.action.MenuManager();
        MenuBuilder.calculateMenu(getIdentity(),menu,site);
        text.setMenu(menu.createContextMenu(text));
    }  
    
    public void setBounds(int x, int y, int width, int height) {
        text.setBounds(x, y, width, height);
    }
    
    /* public void setMenuListener() {
    	menu = new Menu(text);
    	text.setMenu(menu);
    	menu.addMenuListener(new MenuAdapter() {
    	  public void menuShown(MenuEvent e) {
    	  	MenuBuilder.calculateMenu(identity,menu);
    	  }
    	});
    } */

    public void setText(String textString) {
        text.setText(textString);
        text.setToolTipText(textString);
        changesMade(false);
    }

    public void changesMade(boolean b) {
        changesMade = b;
    }

    public void initText(Composite parent) {
        text = new Text(parent, SWT.SINGLE);
        Menu menu = new Menu(text);
        text.setMenu(menu);
        menuItems.put(this.getIdentity(), menu);
        addClickListener();
        changesMade(false);
    }

    public void setEditable(boolean editable) {
        text.setEditable(editable);
        if (editable) {
           text.setBackground(FormsPlugin.normalBackgroundColor);
           addChangeListener();
        }
        else {
           text.setBackground(FormsPlugin.disabledBackgroundColor);
        }
    }

    public void setBackground(int red, int green, int blue) {
        text.setBackground(ColorManager.getColor(new RGB(red,green,blue)));
    }

    public void addChangeListener() {
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                changesMade(true);
            }
        });

        text.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                getEditableText();
            }

            public void focusLost(FocusEvent e) {
                if (changesMade) {
                    Message m = handler.newMessage("textChanged", 2);
                    Value v1 = new Value(getIdentity());
                    Value v2 = new Value(text.getText());
                    m.args[0] = v1;
                    m.args[1] = v2;
                    raiseEvent(m);
                } else {
                    Message m = handler.newMessage("resetText", 1);
                    Value v1 = new Value(getIdentity());
                    m.args[0] = v1;
                    raiseEvent(m);
                }
                changesMade(false);
            }
        });

        text.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.CR && changesMade) {
                    Message m = handler.newMessage("textChanged", 2);
                    Value v1 = new Value(getIdentity());
                    Value v2 = new Value(text.getText());
                    m.args[0] = v1;
                    m.args[1] = v2;
                    raiseEvent(m);
                    changesMade(false);
                } else if (e.keyCode == SWT.CR || e.keyCode == SWT.ESC) {
                    Message m = handler.newMessage("resetText", 1);
                    Value v1 = new Value(getIdentity());
                    m.args[0] = v1;
                    raiseEvent(m);
                    changesMade = false;
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        });

    }

    public void addClickListener() {
        text.addListener(SWT.MouseDoubleClick, new Listener() {
            public void handleEvent(Event e) {
                Message m = handler.newMessage("doubleSelected", 1);
                Value v1 = new Value(getIdentity());
                m.args[0] = v1;
                raiseEvent(m);
            }
        });
        text.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {

                // should the following event not be selected?

                Message m = handler.newMessage("doubleSelected", 1);
                Value v1 = new Value(getIdentity());
                m.args[0] = v1;
                raiseEvent(m);
            }
        });
    }
    
	public Value processCall(Message message) {
		return null;
	}    

    public boolean processMessage(Message message) {
        if (message.arity >= 1) {
            /* if (message.hasName("addMenuItem") && message.arity == 3) {
                if (menuItems.containsKey(message.args[0].strValue())) {
                    // addMenuItem(message);
                    return true;
                }
            } else */ 
            	
            if (message.args[0].hasStrValue(getIdentity())) {
                if (message.hasName("getText") && message.arity == 1) {
                    getText();
                    return true;
                } else if (message.hasName("setText") && message.arity == 2) {
                    setText(message);
                    //owningForm.forceFocus();
                    return true;
                } else if (message.hasName("setBackground") && message.arity == 4) {
                	setBackground(message);
                    return true;
                } else if (message.hasName("setEditableText") && message.arity == 2) {
                    int caretpos = text.getCaretPosition();
                    setText(message);
                    text.setSelection(caretpos, caretpos);
                    return true;
                } else if (ComponentCommandHandler.processMessage(text, message))
                    return true;
            }
        }
        return super.processMessage(message);
    }

    public void getText() {
        Message m = handler.newMessage("text", 2);
        Value v1 = new Value(getIdentity());
        Value v2 = new Value(text.getText());
        m.args[0] = v1;
        m.args[1] = v2;
        raiseEvent(m);
    }

    public void getEditableText() {
        Message m = handler.newMessage("getEditableText", 1);
        Value v1 = new Value(getIdentity());
        m.args[0] = v1;
        raiseEvent(m);
    }

    public void setText(Message message) {
        String textString = message.args[1].strValue();
        setText(textString);
    }

    public void setBackground(Message message) {
        int red = message.args[1].intValue;
        int green = message.args[2].intValue;
        int blue = message.args[3].intValue;
        setBackground(red,green,blue);
    }

    public void newMenuAdded() {
    	calculateMenu();
    }

	public void widgetDisposed(DisposeEvent e) {
        MenuManager.removeMenuListener(this);
   	    MenuBuilder.dispose(site);
	}

}