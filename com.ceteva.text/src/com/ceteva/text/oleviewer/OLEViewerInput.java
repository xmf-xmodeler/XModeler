package com.ceteva.text.oleviewer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class OLEViewerInput implements IEditorInput {
	
	private String identity;
	private String type;
	private String file;
	
	public OLEViewerInput(String identity,String type,String file) {
		this.identity = identity;
		this.type = type;
		this.file = file;
	}

	public boolean exists() {
		return true;
	}
	
	public String getFile() {
		return file;
	}
	
	public String getIdentity() {
		return identity;
	}
	
	public String getType() {
		return type;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return "";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "";
	}

	public Object getAdapter(Class arg0) {
		return null;
	}
	
}