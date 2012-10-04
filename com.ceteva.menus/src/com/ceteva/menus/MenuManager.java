package com.ceteva.menus;

import java.util.Hashtable;
import java.util.Vector;

public class MenuManager {
	
	private static Vector globalMenus = new Vector();
	private static Hashtable menuBindings = new Hashtable();
	private static Vector menuListeners = new Vector();
	
	public static void addGlobalMenu(String identity) {
		GlobalMenu gm = new GlobalMenu(identity);
		globalMenus.addElement(gm);
	}
	
	public static boolean addMenu(String ownerId,String menuId,String name,String keybinding,boolean supportsMulti,String handlerPointId) {
		Menu m = new Menu(menuId,name,supportsMulti);
		Menu parent = findMenu(ownerId);
		m.setHandlerPointId(handlerPointId);
		if(parent != null) {
		  parent.addMenu(m);
		  if(!keybinding.equals(""))
		    KeyBindingManager.addBinding(menuId,keybinding);
		  return true;
		}
		return false;
	}
	
	public static void addMenuListener(MenuListener listener) {
		if(!menuListeners.contains(listener))
		  menuListeners.add(listener);
	}
	
	public static boolean bindGlobalMenu(String globalMenuId,String objectId) {
		if(findGlobalMenu(globalMenuId) != null) {
		  menuBindings.put(objectId,findMenu(globalMenuId));
		  menuAdded();
		  return true;
		}
		return false;
	}
	
	public static void menuAdded() {
		for(int i=0;i<menuListeners.size();i++) {
		  MenuListener listener = (MenuListener)menuListeners.elementAt(i);
		  listener.newMenuAdded();
		}
		
	}
	
	private static Menu findMenu(String identity) {
		for(int i=0;i<globalMenus.size();i++) {
		  Menu m = (Menu)globalMenus.elementAt(i);
		  Menu m2 = m.findMenu(identity);
		  if(m2 != null)
			return m2;
		}
		return null;
	}
	
	private static GlobalMenu findGlobalMenu(String identity) {
		for(int i=0;i<globalMenus.size();i++) {
		  GlobalMenu gm = (GlobalMenu)globalMenus.elementAt(i);
		  if(gm.getIdentity().equals(identity))
		  	 return gm;
		}
		return null;
	}
	
	public static GlobalMenu getGlobalMenu(String identity) {
		if(identity != null && menuBindings.containsKey(identity))
		  return (GlobalMenu)menuBindings.get(identity);
		//System.out.println("No binding for: " + identity);
		return null;
	}
	
	public static void removeMenuListener(MenuListener listener) {
		menuListeners.remove(listener);
	}

    public static boolean deleteGlobalMenu(String identity) {
		for(int i=0;i<globalMenus.size();i++) {
		  GlobalMenu gm = (GlobalMenu)globalMenus.elementAt(i);
		  if(gm.getIdentity().equals(identity)) {
		    globalMenus.remove(gm);
		    return true;
		  }
		}
	    return false;
    }

    public static boolean deleteMenu(String ownerId,String menuId) {
        Menu parent = findMenu(ownerId);
        Menu menu = findMenu(menuId);
        if(parent != null && menu != null) {
          parent.removeMenu(menu);
          return true;
        }
        return false;
    }
}