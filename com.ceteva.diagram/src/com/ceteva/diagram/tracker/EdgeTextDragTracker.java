package com.ceteva.diagram.tracker;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.tools.DragEditPartsTracker;

import com.ceteva.diagram.editPart.EdgeEditPart;

public class EdgeTextDragTracker extends DragEditPartsTracker {

  private EdgeEditPart edgeEditPart;

  public EdgeTextDragTracker(EditPart sourceEditPart,EdgeEditPart edgeEditPart)  {
  	super(sourceEditPart);
  	this.edgeEditPart = edgeEditPart;
  }

  protected EditPart getTargetEditPart(){
	return edgeEditPart;
  }	
}