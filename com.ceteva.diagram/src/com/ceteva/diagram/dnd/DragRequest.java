package com.ceteva.diagram.dnd;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;

import com.ceteva.diagram.editPart.NodeEditPart;
import com.ceteva.diagram.model.Node;

public class DragRequest extends AbstractTransferDragSourceListener {

	private NodeEditPart nodeEditPart;
	static private boolean ctrlpressed = false;
	
	public DragRequest(EditPartViewer viewer,Transfer transfer,NodeEditPart nodeEditPart) {
		super(viewer,transfer);
		this.nodeEditPart = nodeEditPart;
	}
	
	public Node getModel() {
		return (Node)nodeEditPart.getModel();
	}
	
	public void dragStart(DragSourceEvent event) {
		event.doit = ctrlpressed && getModel().isDraggable();
		ctrlpressed = false;
	}

	public void dragSetData(DragSourceEvent event) {
		event.data = getModel().getIdentity();
	}
	
	public static void setCtrl(boolean value) {
		ctrlpressed = value;
	}
}
