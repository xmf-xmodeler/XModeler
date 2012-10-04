package com.ceteva.mosaic.perspectives;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;

import com.ceteva.mosaic.MosaicPlugin;

public class PerspectiveTemplate {

	private String id;
	private String label;
	private String image;
	private Hashtable holders = new Hashtable();
	private PerspectiveRegistry reg;
	private org.eclipse.ui.internal.registry.PerspectiveDescriptor desc;
	private PerspectiveFactory pf;

	public PerspectiveTemplate(String id, String label, String image) {
		this.id = id;
		this.label = label;
		this.image = image;
		this.reg = (PerspectiveRegistry) MosaicPlugin.getDefault()
				.getWorkbench().getPerspectiveRegistry();
		refreshPerspective();
		PerspectiveTemplate.templates.put(getById(id), this);
	}

	public Hashtable getHolders() {
		return holders;
	}

	private static Map<IPerspectiveDescriptor, PerspectiveTemplate> templates = new HashMap<IPerspectiveDescriptor, PerspectiveTemplate>();

	public static PerspectiveTemplate getTemplate(IPerspectiveDescriptor by) {
		return PerspectiveTemplate.templates.get(by);
	}

	private IPerspectiveDescriptor getById(String id) {
		return this.reg.findPerspectiveWithId(id);
	}

	public void addHolder(String id, String position, String refid, int ratio) {
		holders.put(id, new Holder(id, position, refid, ratio));
		refreshPerspective();

	}

	public void addViewTypeToHolder(String holderid, String viewid) {
		Holder holder = (Holder) holders.get(holderid);
		holder.addViewType(viewid);
		refreshPerspective();
	}

	@Deprecated
	@SuppressWarnings("restriction")
	private void createPerspective() {
		// Creation of perspectives is currently restricted to plugin.xml
		System.err.println("--->>createPerspective");
		desc = new PerspectiveDescriptor(id, label, image, holders);
		// org.eclipse.ui.internal.registry.PerspectiveDescriptor
		// createPerspectived = reg.createPerspective(id, desc);
		System.err.println("--->>" + id + ":" + label);
		System.err.println(reg.getPerspectives().length);

		org.eclipse.ui.internal.registry.PerspectiveDescriptor createdPerspective = reg
				.createPerspective(label, desc);

		System.err.println(reg.getPerspectives().length);
	}
	
	

	private void deletePerspective() {
		if (desc != null)
			reg.deletePerspective(desc);
	}

	public boolean hasHolder(String holderId) {
		return holders.containsKey(holderId);
	}

	private void refreshPerspective() {
		System.err.println("Refreshing layout_1");
		if (PerspectiveFactory.layouts != null) {
			System.err.println("Refreshing layout_2");
			IPerspectiveDescriptor perspectiveDesc = getById(id);
			if (perspectiveDesc != null) {
				System.err.println("Refreshing layout_3");
				IPageLayout pageLayout = PerspectiveFactory.layouts
						.get(perspectiveDesc);
				if (pageLayout != null) {
					System.err.println("Refreshing layout_4");
					PerspectiveFactory.createInitialLayout(pageLayout, holders);
				}
			}
		}

	}

	public void removeHolder(String id) {
		holders.remove(id);
		refreshPerspective();
	}

	public void removeViewTypeFromHolder(String holderid, String viewid) {
		Holder holder = (Holder) holders.get(holderid);
		holder.removeViewType(viewid);
		refreshPerspective();
	}
}
