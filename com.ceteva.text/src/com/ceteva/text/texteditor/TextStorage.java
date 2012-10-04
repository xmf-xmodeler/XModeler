package com.ceteva.text.texteditor;
 
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;

public class TextStorage implements IStorage {
  private InputStream is;
 
  public TextStorage(String buffer) {
    this.is = new ByteArrayInputStream(buffer.getBytes());
  }
 
  public InputStream getContents() throws CoreException {
   	try {
	  return this.is;
    } catch (Exception e) {
	  throw new CoreException(new Status(Status.ERROR,"com.ceteva.text.TextEditor", Status.OK, "", e));
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