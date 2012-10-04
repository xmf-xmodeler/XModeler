package com.ceteva.menus;

import java.util.Vector;

import org.eclipse.swt.graphics.Image;

public class Menu {
	
	private Vector identities;
	private String name;
	private String handlerPointId;
	private boolean enabled = true;
	private boolean supportsMulti = true;
	private Image icon = null;
	private Vector menus = new Vector();
	
	public Menu(String identity,String name) {
		Vector v = new Vector();
		v.addElement(identity);
		this.identities = v;
		this.name = name;
	}	
	
	public Menu(String identity,String name,boolean supportsMulti) {
		Vector v = new Vector();
		v.addElement(identity);
		this.identities = v;
		this.name = name;
		this.supportsMulti = supportsMulti;
	}
	
	public Menu(Vector identities,String name) {
		this.identities = identities;
		this.name = name;
	}
	
	public void addMenu(Menu menu) {
		menus.addElement(menu);
	}
	
	public Menu findMenu(String identity) {
		if(getIdentity().equals(identity))
		  return this;
		else {
		  for(int i=0;i<menus.size();i++) {
		  	Menu m = (Menu)menus.elementAt(i);
		  	Menu m2 = m.findMenu(identity);
		  	if(m2 != null)
		  	  return m2;
		  }
		}
		return null;
	}
	
	public boolean getEnabled() {
		return enabled;
	}

	public String getHandlerPointId() {
		return handlerPointId;
	}
	
	public String getIdentity() {
		return (String)identities.elementAt(0);
	}
	
	public Vector getIdentities() {
		return identities;
	}
	
	public Image getIcon() {
		return icon;
	}
	
	public Menu getMenu(String name) {
		for(int i=0;i<menus.size();i++) {
		  com.ceteva.menus.Menu m = (com.ceteva.menus.Menu)menus.elementAt(i);
		  if(name.equals(m.getName()))
		    return m;
		}
		return null;
	}
	
	public String getName() {
		return name;
	}
	
	public Vector getSubMenus() {
		return menus;
	}
	
	public boolean hasIcon() {
		return icon != null;
	}
	
	public boolean hasSubMenu(String name) {
		for(int i=0;i<menus.size();i++) {
			com.ceteva.menus.Menu menu = (com.ceteva.menus.Menu)menus.elementAt(i);
			if(name.equals(menu.getName()))
			  return true;
		}
		return false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isParent() {
		return !menus.isEmpty();
	}

    public void removeMenu(Menu menu) {
        menus.removeElement(menu);
    }
    
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setHandlerPointId(String handlerPointId) {
		this.handlerPointId = handlerPointId;
	}	
	
	public void setIcon(Image icon) {
		this.icon = icon;
	}
	
	public boolean supportsMulti() {
		return supportsMulti;
	}
	
	public String toString() {
	   String s = "Menu(" + name + ", " + identities + ")";
	   return s;
	}

}