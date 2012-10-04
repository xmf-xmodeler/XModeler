package com.ceteva.dialogs;

import java.util.Vector;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

class MultiSelectionTreeDialog extends CheckedTreeSelectionDialog {
	
	public MultiSelectionTreeDialog(Shell shell,ILabelProvider labelProvider,ITreeContentProvider content) {
		super(shell,labelProvider,content);
	}
	
    protected void updateOKStatus() {
    	super.updateOKStatus();
    	CheckboxTreeViewer viewer = getTreeViewer();
		Object[] selected = viewer.getCheckedElements();
		for(int i=0;i<selected.length;i++) {
	      Object o = selected[i];
		  if(viewer.getGrayed(o))
			viewer.setChecked(o,false);
		}
    }
	
	public void disableNodes(Vector nodes) {
		for(int i=0;i<nodes.size();i++) {
			TreeElement te = (TreeElement)nodes.elementAt(i);
			getTreeViewer().setGrayed(te,true);
		}
		CheckStateListener csl = new CheckStateListener(nodes);
		getTreeViewer().addCheckStateListener(csl);
	}
	
	public void expandTree(Vector nodes) {
		for(int i=0;i<nodes.size();i++) {
		   TreeElement te = (TreeElement)nodes.elementAt(i);
		   getTreeViewer().setExpandedState(te,true);
		}
	}
	
	public void selectNodes(Vector nodes) {
		for(int i=0;i<nodes.size();i++) {
		  TreeElement te = (TreeElement)nodes.elementAt(i);
		  getTreeViewer().setChecked(te,true);
		}
	}
}

