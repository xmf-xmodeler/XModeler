package com.ceteva.menus;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.keys.IBindingService;

public class KeyBindingBuilder {
	
	private static String context = "org.eclipse.ui.contexts.window";
	private static int commandCount = 0;
	private static IBindingService bindingService = null;
	private static ICommandService commandService = null;
	private static Hashtable commandbindings = new Hashtable();  // binding between keybindings and commands
	private static Hashtable actionbindings = new Hashtable();	 // binding between actions and sites
	
	private static String addCommandBinding(String keybinding) {
		
		// create a command
		
		String identity = "command" + commandCount++;
		ParameterizedCommand pc = createParameterizedCommand(identity);
		try {
			
		  // create a key sequence and bind it to the command
			
		  KeySequence ks = KeySequence.getInstance(keybinding);
		  KeyBinding b = new KeyBinding(ks,pc,bindingService.getActiveScheme().getId(),context,null,null,null,Binding.SYSTEM);
			
	      // save new binding
		  
		  saveBinding(b);
		  
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		commandbindings.put(keybinding,identity);
		return identity;
	}
	
	public static void addKeyBinding(IWorkbenchPartSite site,IAction action) {
		if(KeyBindingManager.hasBinding(action.getId())) {
		  String keybinding = KeyBindingManager.getBinding(action.getId());
		  String commandIdentity = "";
		  if(commandbindings.containsKey(keybinding))
		     commandIdentity = (String)commandbindings.get(keybinding);
		  else {
			 commandIdentity = checkForPlatformBinding(keybinding);
			 if(commandIdentity.equals(""))
			   commandIdentity = addCommandBinding(keybinding);
		  }
		  action.setActionDefinitionId(commandIdentity);
		  site.getKeyBindingService().registerAction(action);
          actionbindings.put(action,site);
		}
	}
	
	private static String checkForPlatformBinding(String keybinding) {
		Binding[] bindings = getBindingService().getBindings();
		for(int i=0;i<bindings.length;i++) {
		  if(bindings[i] instanceof KeyBinding) {
			 KeyBinding kb = (KeyBinding)bindings[i];
			 if(kb.getContextId().equals(context)) {
			   try {
				 KeySequence existing = kb.getKeySequence();
				 KeySequence required = KeySequence.getInstance(keybinding);
				 if(existing.equals(required)) { 
				   Command command = kb.getParameterizedCommand().getCommand();
				   String identity = command.getId();
				   commandbindings.put(keybinding,identity);
				   return identity;
				 }
			   } catch (ParseException e) {
				e.printStackTrace();
			   }
			 }
		  }
		}
		return "";
	}
	
	/* public static void dispose(IWorkbenchPartSite site) {
		Enumeration sites = actionbindings.elements();
		while(sites.hasMoreElements()) {
		  IWorkbenchPartSite s = (IWorkbenchPartSite)sites.nextElement();	
		  if(s.equals(site)) {
			Enumeration actions = actionbindings.keys();
			while(actions.hasMoreElements()) {
			  IAction a = (IAction)actions.nextElement();
			  if(actionbindings.get(a).equals(s)) {
			    actionbindings.remove(a);
			    System.out.println("Removing site binding");
			  }
			}
		  }
		}
	} */
	
	public static void resetKeyBindings(IWorkbenchPartSite site) {
        Enumeration e = actionbindings.keys();
        while(e.hasMoreElements()) {
           IAction action = (IAction)e.nextElement();
           IWorkbenchPartSite s = (IWorkbenchPartSite)actionbindings.get(action);
           if(s.equals(site)) {
             site.getKeyBindingService().unregisterAction(action);
             actionbindings.remove(action);
           }
        }
	}
	
	private static ParameterizedCommand createParameterizedCommand(String id) {
	    Command c = getCommandService().getCommand(id);
	    Category cat = getCommandService().getCategory(context);
		c.define("XMF-Mosaic command", "Command added programmatically by XMF-Mosaic", cat, null);
		return new ParameterizedCommand(c,null);	
	}
	
	private static ICommandService getCommandService() {
		if(commandService == null)
		  commandService = (ICommandService)getWorkbench().getAdapter(ICommandService.class);
		return commandService;
	}
	
	private static IBindingService getBindingService() {
		if(bindingService == null)
    	  bindingService = (IBindingService)getWorkbench().getAdapter(IBindingService.class);
		return bindingService;
	}
	
	private static IWorkbench getWorkbench() {
		return MenusPlugin.getDefault().getWorkbench();
	}
	
	private static void saveBinding(KeyBinding binding) {
		Binding[] oldBindings = getBindingService().getBindings();
		int length = 1;
		if(oldBindings!=null)
		  length = oldBindings.length + 1;	
		Binding[] newBindings = new Binding[length];
		if(oldBindings!=null) {
		  System.arraycopy(oldBindings, 0, newBindings, 0, oldBindings.length);
		  newBindings[oldBindings.length] = binding;
		}
		else
		  newBindings[0] = binding;
		try {
		  bindingService.savePreferences(bindingService.getActiveScheme(),newBindings);
		}
		catch(IOException iox) {
		  iox.printStackTrace();
		}
	}

}
