package com.ceteva.diagram.editPolicy;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;

import com.ceteva.diagram.editPart.NodeEditPart;
import com.ceteva.diagram.model.Node;

public class NodeHandlesEditPolicy extends ResizableEditPolicy {

   protected void hideSelection() {
	  EditPart ep = this.getHost();
	  if(ep instanceof NodeEditPart) {
 	    NodeEditPart nep = (NodeEditPart)ep;
	    Node node = (Node)nep.getModel();
	    node.deselectNode();
	    super.hideSelection();
	  }
   }
   
   protected void showSelection() {
	  EditPart ep = this.getHost();
	  if(ep instanceof NodeEditPart) {
	    NodeEditPart nep = (NodeEditPart)ep;
	    Node node = (Node)nep.getModel();
	    node.selectNode();
	    super.showSelection();
	  }
	}

}