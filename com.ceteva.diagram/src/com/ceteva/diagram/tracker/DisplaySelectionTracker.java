package com.ceteva.diagram.tracker;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;

import com.ceteva.diagram.editPart.NodeEditPart;
import com.ceteva.diagram.editPart.TextEditPart;

public class DisplaySelectionTracker extends NodeSelectionTracker {

  // a tracker that selects its parent node as well as the display
	
  EditPart owner;
  boolean debug = false;
  
  public DisplaySelectionTracker(EditPart owner) {
  	super(owner);
  	findOwner(owner);
  }
  
  public void findOwner(EditPart owner) {
  	// if(owner!=null) {
	  if(owner instanceof NodeEditPart)
  	    this.owner = owner;
  	  else
  	    findOwner(owner.getParent());
  	// }
  }
  
  protected void performSelection() {
	  if (hasSelectionOccurred())
	    return;
	  setFlag(FLAG_SELECTION_PERFORMED, true);
	  EditPartViewer viewer = getCurrentViewer();
	  List selectedObjects = viewer.getSelectedEditParts();
	  
	  if (getCurrentInput().isControlKeyDown()) {
		  if (selectedObjects.contains(getSourceEditPart()))
		    viewer.deselect(getSourceEditPart());
		  else {
			viewer.appendSelection(getSourceEditPart());
			viewer.appendSelection(owner);
		  }
	  } 
	  else if (getCurrentInput().isShiftKeyDown()) {
		  viewer.appendSelection(getSourceEditPart());
	      viewer.appendSelection(owner);
	  }
	  else {
		viewer.select(getSourceEditPart());
	  	viewer.appendSelection(owner);
	  }
  }
  
  public boolean handleDragStarted() {
  	if(owner!=null)
  	  setSourceEditPart(owner);
  	return super.handleDragStarted();
  }
  
  protected void performDirectEdit() {
	EditPartViewer viewer = getCurrentViewer();
	List selectedObjects = viewer.getSelectedEditParts();
	boolean hasTextEditPart = false;
	for(int i=0;i<selectedObjects.size();i++) {
	  EditPart ep = (EditPart)selectedObjects.get(i);
	  if(ep instanceof TextEditPart) {
		 TextEditPart tep = (TextEditPart)ep;
		 if(tep.isSelectable())
	       hasTextEditPart = true;
	  }
	}
	if(!hasTextEditPart) {
	  for(int i=0;i<selectedObjects.size();i++) {
		EditPart ep = (EditPart)selectedObjects.get(i);
		if(ep instanceof NodeEditPart)
		  setSourceEditPart(ep);
	  }
	}
	super.performDirectEdit();
  }
  
  protected void performOpen() {
	SelectionRequest request = new SelectionRequest();
	request.setLocation(getLocation());
	request.setType(RequestConstants.REQ_OPEN);
	EditPartViewer viewer = getCurrentViewer();
	List selectedObjects = viewer.getSelectedEditParts();
	for(int i=0;i<selectedObjects.size();i++) {
	  EditPart ep = (EditPart)selectedObjects.get(i);
	  ep.performRequest(request);
	}
  }
}