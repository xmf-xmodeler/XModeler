package com.ceteva.text.texteditor;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class TextEditorInput implements IEditorInput {
	private TextStorage storage;

	public TextEditorInput(TextStorage _storage) {
		this.storage = _storage;
	}

	public IStorage getStorage() throws CoreException {
		return this.storage;
	}

	public boolean exists() {
		return true;
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

	public Object getAdapter(Class adapter) {
		return null;
	}
}