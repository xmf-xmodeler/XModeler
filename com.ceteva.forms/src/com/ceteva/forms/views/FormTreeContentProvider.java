package com.ceteva.forms.views;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jface.viewers.*;

public class FormTreeContentProvider implements ITreeContentProvider{
	
  public Object[] getChildren(Object element) {
    Object[] children = ((TreeItem)element).getItems();
    return children == null ? new Object[0] : children;
  }

  public Object[] getElements(Object element) {
    return getChildren(element);
  }

  public boolean hasChildren(Object element) {
  	return ((TreeItem)element).getItemCount() > 0;
  }

  public Object getParent(Object element) {
    return ((TreeItem)element).getParent();
  }
  
  public void dispose() {
  }

  public void inputChanged(Viewer viewer, Object old_input, Object new_input) {
  }
}
