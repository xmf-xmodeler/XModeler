package com.ceteva.menus;

import java.util.Hashtable;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.WorkbenchWindow;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.Client;
import com.ceteva.client.EventHandler;
import com.ceteva.menus.actions.MenuAction;

public class MenusClient extends Client {

	Hashtable menuIdBinding = new Hashtable(); // used for defining ids on menus
												// added by the system
	public static EventHandler handler = null;

	public MenusClient() {
		super("com.ceteva.menus");
	}

	public boolean delete(String id) {
		IContributionItem item = findParent(getMenu(), id);
		if (item != null) {
			if (item instanceof ContributionItem) {
				ContributionItem icm = (ContributionItem) item;
				icm.getParent().remove(item);
				if (menuIdBinding.contains(item))
					menuIdBinding.remove(item);
				refreshMenus();
				return true;
			}
			if (item instanceof MenuManager) {
				MenuManager mm = (MenuManager) item;
				mm.getParent().remove(item);
				if (menuIdBinding.contains(item))
					menuIdBinding.remove(item);
				refreshMenus();
				return true;
			}
		}
		return false;
	}

	public void setEventHandler(EventHandler eventsOut) {
		handler = eventsOut;
	}

	public void newMenu(String parent, String identity, String title) {
		if (parent.equals("root"))
			newRootMenu(identity, title);
		else
			newChildMenu(parent, identity, title);
	}

	public void newMenuItem(String parent, String identity, String title) {
		IContributionItem owner = findParent(getMenu(), parent);
		MenuAction action = new MenuAction(title, identity, handler);
		if (owner instanceof Separator) {
			Separator s = (Separator) owner;
			s.getParent().appendToGroup(parent, action);
			refreshMenus();
		} else if (owner instanceof MenuManager) {
			MenuManager m = (MenuManager) owner;
			m.add(action);
			refreshMenus();
		}
	}

	public void newGroupMarker(String menuId, String groupId) {
		IContributionItem menu = findParent(getMenu(), menuId);
		if (menu instanceof MenuManager) {
			MenuManager m = (MenuManager) menu;

			// The following code ensures that the "print" and "exit"
			// groups added by the system always remain at the bottom

			IContributionItem item = m.find("print");
			if (item != null) {
				m.insertBefore(item.getId(), new Separator(groupId));
				refreshMenus();
			} else {
				m.add(new Separator(groupId));
				refreshMenus();
			}
		}
	}

	public void newRootMenu(String identity, String title) {
		IMenuManager menu = getMenu();
		if (rootMenuItemExists(menu, title)) {
			MenuManager item = getRootMenuItem(menu, title);
			menuIdBinding.put(item, identity);
		} else {
			MenuManager newMenu = new MenuManager(title, identity);

			menu.insertBefore(IWorkbenchActionConstants.M_HELP, newMenu);

			newMenu.setVisible(true);
			refreshMenus();
		}
	}

	public void newChildMenu(String parent, String identity, String title) {
		IContributionItem owner = findParent(getMenu(), parent);
		MenuManager newMenu = new MenuManager(title, identity);
		if (owner instanceof Separator) {
			Separator s = (Separator) owner;
			s.getParent().appendToGroup(parent, newMenu);
			refreshMenus();
		} else if (owner instanceof MenuManager) {
			MenuManager m = (MenuManager) owner;
			m.add(newMenu);
			refreshMenus();
		}
	}

	public void refreshMenus() {
		IMenuManager menu = getMenu();
		menu.updateAll(true);
	}

	public IMenuManager getMenu() {
		WorkbenchWindow window = (WorkbenchWindow) MenusPlugin.getDefault()
				.getWorkbench().getActiveWorkbenchWindow();
		return window.getMenuBarManager();
	}

	public boolean xmfId(String id) {
		return id.length() > 0 && id.charAt(0) >= '0' && id.charAt(0) <= '9';
	}

	public IContributionItem findParent(IContributionItem it, String identity) {
		if (it instanceof MenuManager) {
			IContributionItem[] items = ((MenuManager) it).getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				String id = item.getId();
				if (id != null && !xmfId(id))
					id = (String) menuIdBinding.get(item);
				if (id != null && id.equals(identity))
					return item;
				else {
					IContributionItem ite = findParent(item, identity);
					if (ite != null)
						return ite;
				}
			}
		}
		return null;
	}

	public void addMenuListener(final String identity, MenuItem item) {
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Message m = handler.newMessage("menuSelected", 1);
				Value v1 = new Value(identity);
				m.args[0] = v1;
				handler.raiseEvent(m);
			}
		});
	}

	public boolean rootMenuItemExists(IMenuManager menu, String text) {
		return getRootMenuItem(menu, text) != null;
	}

	public MenuManager getRootMenuItem(IMenuManager menu, String text) {
		IContributionItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof MenuManager) {
				MenuManager item = (MenuManager) items[i];
				if (item.getMenuText().equals(text))
					return (MenuManager) item;
			}
		}
		return null;
	}

	public boolean processMessage(Message message) {
		if (message.name().equals("newMenu")) {
			String parent = message.args[0].strValue();
			String identity = message.args[1].strValue();
			String title = message.args[2].strValue();
			newMenu(parent, identity, title);
			return true;
		} else if (message.name().equals("newMenuItem")) {
			String parent = message.args[0].strValue();
			String identity = message.args[1].strValue();
			String title = message.args[2].strValue();
			newMenuItem(parent, identity, title);
			return true;
		} else if (message.name().equals("newGroupMarker")) {
			String menu = message.args[0].strValue();
			String identity = message.args[1].strValue();
			newGroupMarker(menu, identity);
			return true;
		} else if (message.name().equals("delete")) {
			String target = message.args[0].strValue();
			return delete(target);
		} else if (message.hasName("newGlobalMenu")) {
			String identity = message.args[0].strValue();
			com.ceteva.menus.MenuManager.addGlobalMenu(identity);
			return true;
		} else if (message.hasName("setGlobalMenu")) {
			return setGlobalMenu(message);
		} else if (message.hasName("addMenuItem")) {
			String target = message.args[0].strValue();
			String menuIdentity = message.args[1].strValue();
			String name = message.args[2].strValue();
			String keybinding = message.args[3].strValue();
			boolean supportsMulti = message.args[4].boolValue;
			String handlerPointIdentity = message.args[5].strValue();
			return com.ceteva.menus.MenuManager.addMenu(target, menuIdentity,
					name, keybinding, supportsMulti, handlerPointIdentity);
		} else if (message.name().equals("deleteMenuItem")) {
			String parent = message.args[0].strValue();
			String menuIdentity = message.args[1].strValue();
			return com.ceteva.menus.MenuManager
					.deleteMenu(parent, menuIdentity);
		} else if (message.name().equals("deleteGlobalMenu")) {
			String identity = message.args[0].strValue();
			return com.ceteva.menus.MenuManager.deleteGlobalMenu(identity);
		}
		return false;
	}

	public void reset() {
		getMenu().removeAll();
		refreshMenus();
	}

	public boolean setGlobalMenu(Message message) {
		String globalMenuId = message.args[0].strValue();
		String targetId = message.args[1].strValue();
		return com.ceteva.menus.MenuManager.bindGlobalMenu(globalMenuId,
				targetId);
	}
}