package com.ceteva.diagram;
 
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;

public class DiagramStorage implements IStorage {
  private InputStream is;
 
  public DiagramStorage(String buffer) {
    this.is = new ByteArrayInputStream(buffer.getBytes());
  }
 
  public InputStream getContents() throws CoreException {
   	try {
	  return this.is;
    } catch (Exception e) {
	  throw new CoreException(new Status(Status.ERROR,"com.ceteva.diagram.Diagram", Status.OK, "", e));
    }
  }
 
  public IPath getFullPath() {
  	return null;
  }
 
  public String getName() {
  	return null;
  }
 
  public boolean isReadOnly() {
  	return false;
  }
 
  public Object getAdapter(Class adapter) {
  	return null;
  }
}