package com.ceteva.dialogs;

import java.util.Vector;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;

class CheckStateListener implements ICheckStateListener {
	
	private Vector disabled;
	
	public CheckStateListener(Vector disabled) {
		this.disabled = disabled;
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		CheckboxTreeViewer viewer = (CheckboxTreeViewer)event.getSource();
		Object[] objects = viewer.getCheckedElements();
		for(int i=0;i<objects.length;i++) {
			TreeElement element = (TreeElement)objects[i];
			if(disabled.contains(element))
			  viewer.setChecked(element,false);	
		}
	}
	
}