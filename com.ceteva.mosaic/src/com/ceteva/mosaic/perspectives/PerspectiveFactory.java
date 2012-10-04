package com.ceteva.mosaic.perspectives;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class PerspectiveFactory implements IPerspectiveFactory {

	public PerspectiveFactory() {

	}

	protected final static Map<IPerspectiveDescriptor, IPageLayout> layouts = new HashMap<IPerspectiveDescriptor, IPageLayout>();

	public static void createInitialLayout(IPageLayout layout, Hashtable holders) {
		System.err.println("Refreshing perspective with id:"
				+ layout.getDescriptor().getId() + " with " + holders.size()
				+ " holders");
		String editorArea = layout.getEditorArea();
		Enumeration e = holders.elements();
		Vector v = new Vector();
		while (e.hasMoreElements()) {
			v.addElement(e.nextElement());
		}
		for (int z = v.size() - 1; z >= 0; z--)
		// for(int z=0;z<v.size();z++)
		{
			Holder holder = (Holder) v.elementAt(z);
			IFolderLayout folder = null;
			if (holder.getRefId().equals(""))
				
				folder = layout.createFolder(holder.getIdentity(),
						holder.getPosition(), holder.getRatio(), editorArea);
			else
				folder = layout.createFolder(holder.getIdentity(),
						holder.getPosition(), holder.getRatio(),
						holder.getRefId());
			Vector viewTypes = holder.getViewTypes();
			for (int i = 0; i < viewTypes.size(); i++) {
				String type = (String) viewTypes.elementAt(i);
				addTypeToFolder(layout, folder, type);
			}
		}
	}

	public void createInitialLayout(IPageLayout layout) {
		if (PerspectiveFactory.layouts != null)
			layouts.put(layout.getDescriptor(), layout);
		Hashtable holders = (PerspectiveTemplate.getTemplate(layout
				.getDescriptor())).getHolders();
		PerspectiveFactory.createInitialLayout(layout, holders);
	}

	public static void addTypeToFolder(IPageLayout layout,
			IFolderLayout folder, String type) {
		if (type.equals("com.ceteva.console")) {
			folder.addView("com.ceteva.console.view");
			IViewLayout out = layout.getViewLayout("com.ceteva.console.view");
			out.setCloseable(false);
		} else if (type.equals("com.ceteva.outline"))
			folder.addView("org.eclipse.ui.views.ContentOutline");
		else{
			String h = "*:" + type + "@*";
			folder.addPlaceholder(h);
		}
	}
}
