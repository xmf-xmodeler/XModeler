package com.ceteva.diagram;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPartSite;

import com.ceteva.diagram.editPart.CommandEventEditPart;
import com.ceteva.diagram.editPart.DisplayEditPart;
import com.ceteva.diagram.editPart.EdgeEditPart;
import com.ceteva.diagram.editPart.NodeEditPart;
import com.ceteva.menus.MenuBuilder;

public class DiagramMenuBuilder implements ISelectionChangedListener {
	
	GraphicalViewer viewer;
	IWorkbenchPartSite site;
	
	public DiagramMenuBuilder(GraphicalViewer viewer,IWorkbenchPartSite site) {
	  this.viewer = viewer;
	  this.site = site;
	}

	public void selectionChanged(SelectionChangedEvent event) {
	  StructuredSelection selection = (StructuredSelection)event.getSelection();
	  MenuBuilder.resetKeyBindings(site);
	  MenuManager manager = new MenuManager();
      MenuBuilder.calculateMenu(getSelectedIdentity(selection),manager,site);
      viewer.setContextMenu(manager);
	}
	
	public Vector getSelectedIdentity(StructuredSelection selection) {

		  Vector selected = new Vector();
		
		  // if zero parts selected then no identity is returned 
		  
		  if(selection.size()==0)
		    return selected;
		    
		  // we know there is at least one part selected
		  
		  Iterator selections = selection.iterator();
		  
		  if(selection.size()==1) {
			EditPart ep = (EditPart)selections.next(); 
		    String id = identityForEditPart(ep);
		    if(MenuBuilder.hasMenu(id)) 
		      selected.addElement(id);
		  }
		  else if(selection.size()==2) {
			
		    // if the selected item is the node and a display edit part
			  
			EditPart part1 = (EditPart)selections.next();
			EditPart part2 = (EditPart)selections.next();
			String id1 = identityForEditPart(part1);
			String id2 = identityForEditPart(part2);
			
			if(part1 instanceof NodeEditPart && part2 instanceof DisplayEditPart) {
			  if(MenuBuilder.hasMenu(id2)) 
			    selected.addElement(id2);
			  else
				selected.addElement(id1);
			}
			else if(part2 instanceof NodeEditPart && part1 instanceof DisplayEditPart) {
			  if(MenuBuilder.hasMenu(id1)) 
				selected.addElement(id1);
			  else
				selected.addElement(id2);
			}
		  }
		  else {
		    
			while(selections.hasNext()) {
			  EditPart ep = (EditPart)selections.next();
			
			  // Only include nodes and edges
			
			  if(ep instanceof NodeEditPart || ep instanceof EdgeEditPart) {
			    String id = identityForEditPart(ep);
			    if(MenuBuilder.hasMenu(id))
			      selected.add(id);
			  }
			}
		  }
		  return selected;
	}
	
	public String identityForEditPart(EditPart ep) {
		if(ep instanceof CommandEventEditPart) {
		  CommandEventEditPart cep = (CommandEventEditPart)ep;
		  return cep.getModelIdentity();
		}
		if(ep instanceof EdgeEditPart) {
		  EdgeEditPart cep = (EdgeEditPart)ep;
		  return cep.getModelIdentity();
		}
		return "";
	}
	
}