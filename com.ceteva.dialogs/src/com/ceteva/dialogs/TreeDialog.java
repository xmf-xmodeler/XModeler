package com.ceteva.dialogs;

import java.util.Vector;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

class TreeDialog extends ElementTreeSelectionDialog {
	
	public TreeDialog(Shell shell,ILabelProvider labelProvider,ITreeContentProvider content) {
		super(shell,labelProvider,content);
	}
	
	public void expandTree(Vector nodes) {
		for(int i=0;i<nodes.size();i++) {
		   TreeElement te = (TreeElement)nodes.elementAt(i);
		   getTreeViewer().setExpandedState(te,true);
		}
	}
}

