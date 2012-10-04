package com.ceteva.dialogs;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeElementProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof TreeElement) {
		  TreeElement te = (TreeElement)parentElement;
		  return te.getChildren();
		}
		return null;
	}

	public Object getParent(Object element) {
		TreeElement te = (TreeElement)element;
		return te.getOwner();
	}

	public boolean hasChildren(Object element) {
		if(element instanceof TreeElement) {
		  TreeElement te = (TreeElement)element;
		  return te.getChildren().length > 0;
		}
		return false;
	}

	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
