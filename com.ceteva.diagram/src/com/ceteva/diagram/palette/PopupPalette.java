package com.ceteva.diagram.palette;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteListener;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.gef.ui.palette.PaletteViewer;

import com.ceteva.client.IconManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.ToolFactory;

public class PopupPalette extends PopupBarEditPolicy implements PaletteListener {

	/**
	 * Holds the last active palette tool.
	 */
	public ToolEntry theLastTool = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPolicy#activate()
	 */
	public void activate() {
		super.activate();
		addPaletteListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPolicy#deactivate()
	 */
	public void deactivate() {
		removePaletteListener();
		super.deactivate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.PopupBarEditPolicy#fillActionDescriptors()
	 */
	protected void fillPopupBarDescriptors() {
		fillBasedOnLastActivePaletteTool();
		if (getPopupBarDescriptors().isEmpty()) {
			fillBasedOnOpenPaletteDrawer();
			if (getPopupBarDescriptors().isEmpty()) {
				fillWithDefaults();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.palette.PaletteListener#activeToolChanged(org.eclipse.gef.ui.palette.PaletteViewer,
	 *      org.eclipse.gef.palette.ToolEntry)
	 */
	public void activeToolChanged(PaletteViewer palette, ToolEntry tool) {
		if (!(tool instanceof SelectionToolEntry)) {
			theLastTool = tool;
		}
	}

	/**
	 * Adds this edit policy as a palette listener.
	 */
	private void addPaletteListener() {
		PaletteViewer paletteViewer = getHost().getViewer().getEditDomain()
				.getPaletteViewer();
		if (paletteViewer != null) {
			paletteViewer.addPaletteListener(this);
		}
	}

	/**
	 * Removes this edit policy as a palette listener.
	 */
	private void removePaletteListener() {
		PaletteViewer paletteViewer = getHost().getViewer().getEditDomain()
				.getPaletteViewer();
		if (paletteViewer != null) {
			paletteViewer.removePaletteListener(this);
		}
		theLastTool = null;
	}

	/**
	 * Adds popup bar descriptors for all the shape tools in the palette
	 * container of the last active palette tool.
	 */
	private void fillBasedOnLastActivePaletteTool() {
		if (theLastTool == null)
			return;

		PaletteContainer palContainer = theLastTool.getParent();
		fillWithPaletteToolsInContainer(palContainer);
	}

	/**
	 * Adds popup bar descriptors for all the shape tools in the given palette
	 * container.
	 * 
	 * @param palContainer
	 *            the <code>PaletteContainer</code>
	 */
	private void fillWithPaletteToolsInContainer(PaletteContainer palContainer) {
		if (palContainer != null) {
			List theEntries = palContainer.getChildren();
			int isz = theEntries.size();
			for (int i = 0; i < isz; i++) {
				PaletteEntry theEntry = (PaletteEntry) theEntries.get(i);

				if (theEntry != null) {
					if(theEntry instanceof CombinedTemplateCreationEntry) {
					  CreationToolEntry cte = (CreationToolEntry)theEntry;
					  ToolFactory factory = (ToolFactory)cte.getToolProperty(CreationTool.PROPERTY_CREATION_FACTORY);
					  String toolIdentity = (String)factory.getObjectType();
					  String toolName = theEntry.getLabel();
					  String icon = factory.getIconFilename();
					  addPopupBarDescriptor(toolIdentity, IconManager.getImage(DiagramPlugin.getDefault(),icon), toolName);
					}
				}
			}
		}
	}

	/**
	 * Adds popup bar descriptors for all the shape tools in the palette drawer
	 * that is initially open.
	 */
	private void fillBasedOnOpenPaletteDrawer() {
		PaletteViewer paletteViewer = getHost().getViewer().getEditDomain()
				.getPaletteViewer();
		for (Iterator iter = paletteViewer.getPaletteRoot().getChildren()
				.iterator(); iter.hasNext();) {
			Object child = iter.next();
			if (child instanceof PaletteDrawer) {
				PaletteDrawer drawer = (PaletteDrawer) child;
				if (drawer.isInitiallyOpen()) {
					fillWithPaletteToolsInContainer(drawer);
					break;
				}
			}
		}
	}

	/**
	 * Subclasses can override to provide default tools if the popup bar cannot
	 * be populated based on the state of the palette.
	 */
	protected void fillWithDefaults() {
		// by default, add no popup bar descriptors.
		fillBasedOnOpenPaletteDrawer();
	}

}
