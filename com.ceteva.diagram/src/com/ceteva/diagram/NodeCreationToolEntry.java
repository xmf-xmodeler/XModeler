package com.ceteva.diagram;

import org.eclipse.gef.Tool;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.jface.resource.ImageDescriptor;

public class NodeCreationToolEntry extends CombinedTemplateCreationEntry {
	
	private boolean unloadWhenFinished = true;
	
	public NodeCreationToolEntry(
			  String label,
			  String shortDesc,
			  Object template,
			  CreationFactory factory,
			  ImageDescriptor iconSmall,
			  ImageDescriptor iconLarge) {
		
		super(label,shortDesc,template,factory,iconSmall,iconLarge);
	}
	
	public Tool createTool() {
	  AbstractTool t = (AbstractTool)super.createTool();
	  t.setUnloadWhenFinished(unloadWhenFinished);
	  return t;
	}
	
	public void unloadWhenFinished(boolean unload) {
	  this.unloadWhenFinished = unload;
	}
}