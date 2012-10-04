package com.ceteva.diagram;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class DiagramEditorInput implements IEditorInput {

	private com.ceteva.diagram.model.Diagram model;
	
	public DiagramEditorInput(com.ceteva.diagram.model.Diagram model) {
		this.model = model;
	}
	
	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return "";
	}
	
	public com.ceteva.diagram.model.Diagram getModel() {
		return model;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "";
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
	
}