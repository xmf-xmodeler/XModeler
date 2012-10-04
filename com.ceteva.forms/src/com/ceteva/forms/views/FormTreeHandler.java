package com.ceteva.forms.views;

import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public interface FormTreeHandler {
	
	public void doubleSelected(String identity);
	
	public void deselected(String identity);
	
	public void enableDrag();
	
	public void enableDrop();
	
	public void getEditableText(String identity);
	
	public AbstractUIPlugin getPlugin();
	
	public IWorkbenchPartSite getSite();
	
	public void selected(String identity);
	
	public void treeExpanded(String identity);
	
	public void textChanged(String identity,String text);

}
