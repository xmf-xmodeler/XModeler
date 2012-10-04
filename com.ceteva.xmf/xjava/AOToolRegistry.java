package xjava;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.runtime.Plugin;

public class AOToolRegistry {
	
	private static Hashtable tools = new Hashtable();
	
	public static void registerTool(AlienObjectTool tool) {
	  String identity = tool.getPlugin().getBundle().getSymbolicName();
	  
	  // for classpath lookups
	  System.out.println("Putting tool: " + identity);
	  
	  tools.put(identity,tool);
	}
	
	public static Class getClass(String plugin,String classname) throws ClassNotFoundException {
	  Enumeration e = tools.keys();

	  System.out.println("Looking for tool: " + plugin);
	  if(tools.containsKey(plugin)) {
	    AlienObjectTool tool = (AlienObjectTool)tools.get(plugin);
	    Plugin tp = tool.getPlugin();
	    System.out.println("Tool found loading class");
	    return tp.getBundle().loadClass(classname);
	  }
	  System.out.println("Not found tool");
	  throw new ClassNotFoundException(classname);
	}
}
