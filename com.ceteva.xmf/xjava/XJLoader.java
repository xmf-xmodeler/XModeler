package xjava;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class XJLoader extends ClassLoader {

    // This loader allows the user to supply an array of paths that effectively
    // dynamically extend the paths currently defined by CLASSPATH.

    private static Hashtable cache = new Hashtable(); // Loaded classes are cached for subsequent requests by name.

    private static String[] paths = new String[0];  // Set by a client to extend CLASSPATH.

    XJLoader(ClassLoader parent) {
        super(parent);
    }

    XJLoader() {
        super();
    }

    protected Class findClass(String name) throws ClassNotFoundException {

        // Called implicitly when loadClass is used as per the Java class
        // loading documentation. Uses paths to lookup a .class file and
        // return the class if found. Returns null if no class is found.

         System.out.println("Loading: " + name);
         for (int i = 0; i < paths.length; i++)
           System.out.println("WRT: " + paths[i]);
         
         if (cache.containsKey(name))
           return (Class) cache.get(name);   
         return findClassInFile(name);
    }

    private Class findClassInFile(String name) throws ClassNotFoundException {
        try {
            String path = name.replace('.', '/');
            for (int i = 0; i < paths.length; i++) {
            	if(paths[i].endsWith(".jar")) {
            	  JarFile jar = new JarFile(paths[i]);
            	  java.util.Enumeration entries = jar.entries();
                  String entryname = name.replace('.','/') + ".class";
            	  while(entries.hasMoreElements()) {
              		JarEntry entry = (JarEntry)entries.nextElement();
              		if(entry.getName().equals(entryname)) {
              		  return loadClassFromJar(jar,entry,name);  	
              		}
            	  }
            	}
            	else {
                  File file = new File(paths[i] + "/" + path + ".class");
                  if (file.exists()) {
                    FileInputStream fi = new FileInputStream(file);
                    return loadClassFromFile(fi,name);
                  }
            	}
            }
        } catch (IOException ioe) {
            throw new ClassNotFoundException(name + ":" + ioe);
        }
        throw new ClassNotFoundException(name);
    }
    
    private Class loadClassFromFile(FileInputStream fi,String name) throws ClassNotFoundException {
    	try {
    	  byte[] classBytes;
          classBytes = new byte[fi.available()];
          fi.read(classBytes);

          String packageName = name;
          int index = packageName.lastIndexOf('.');
          if (-1 != index) {
            packageName = name.substring(0, index);
          }

          if (getPackage(packageName) == null)
            definePackage(packageName, "", "", "", "", "", "", null);

          Class c = defineClass(name, classBytes, 0, classBytes.length);
          cache.put(name, c);
          resolveClass(c);
          return c;
    	}
    	catch(IOException iox) {
          throw new ClassNotFoundException(name + ":" + iox);	
    	}
    }
    
    private Class loadClassFromJar(JarFile jar,JarEntry entry,String name) throws ClassNotFoundException {
    	String entryname = parseClassName(entry.getName());
    	try {
    	  InputStream is = jar.getInputStream(entry);
    	  BufferedInputStream bis = new BufferedInputStream(is);
    	  byte[] content = new byte[(int)entry.getSize()];
    	  bis.read(content, 0, (int) entry.getSize());
    	  Class klass = defineClass(entryname, content, 0, content.length);
    	  cache.put(name,klass);
    	  return klass;
    	}
    	catch(IOException iox) {
          throw new ClassNotFoundException(entryname + ":" + iox);
    	}
    }
    
    private String parseClassName(String jarentryname) {
    	int index = jarentryname.indexOf("class");
    	String classname = jarentryname.substring(0,index-1);
    	classname = classname.replace('/','.');
    	return classname;
    }
    
    public String[] paths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

}
