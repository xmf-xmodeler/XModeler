package com.ceteva.forms.views;

import java.util.Vector;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.Commandable;
import com.ceteva.client.ComponentWithControl;
import com.ceteva.client.Draggable;
import com.ceteva.client.Droppable;
import com.ceteva.client.EventHandler;

abstract class FormElement implements Commandable, ComponentWithControl, Draggable, Droppable {

	private String identity = "";
	public EventHandler handler = null;
	boolean eventsEnabled = true;
	boolean dragEnabled = false;
	boolean dropEnabled = false;
	
	public FormElement(String identity) {
	  setIdentity(identity);
	}
	
	public String getIdentity() {
		return identity;	
	}

	public void disableEvents() {
		eventsEnabled = false;
	}

	public boolean processMessage(Message message) {
		if(message.args[0].hasStrValue(identity)) {
			if (message.hasName("enableDrag") && message.arity==1) {
				enableDrag();
				return true;
			}
		}
		if(message.args[0].hasStrValue(identity)) {
			if (message.hasName("enableDrop") && message.arity==1) {
				enableDrop();
				return true;
			}
		}
		return false;
	}
	
	void raiseEvent(Message m) {
		if (eventsEnabled)
			handler.raiseEvent(m);
	}
	
	// DRAG

	public boolean dragEnabled() {
		return dragEnabled;
	}

	public void enableDrag() {
		if (!dragEnabled()) {
			setDraggable();
		}
	}

	public void setDraggable() {
		dragEnabled = true;
		final FormElement formElement = this;
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		DragSource source = new DragSource(this.getControl(), operations);
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);

		source.addDragListener(new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				formElement.dragStart(event);
			}

			public void dragSetData(DragSourceEvent event) {
				formElement.dragSetData(event);
			}

			public void dragFinished(DragSourceEvent event) {
				formElement.dragFinished(event);
			}
		});
	}

	public void dragStart(DragSourceEvent event) {
        // System.out.println("dragStart: '" + getIdentity() + "'");
	}

	public void dragSetData(DragSourceEvent event) {
        // System.out.println("dragSetData: '" + getIdentity() + "'");
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			Vector dragIds = new Vector();
			dragIds.add(getIdentity());
			String dragIdsString = dragIds.toString();
			event.data = dragIdsString;
		}
	}

	public void dragFinished(DragSourceEvent event) {
		// System.out.println("dragFinished: '" + getIdentity() + "'");
	}

	// DROP

	public boolean dropEnabled() {
		return dropEnabled;
	}

	public void enableDrop() {
		if (!dropEnabled()) {
			setDroppable();
		}
	}

	public void setDroppable() {
		dropEnabled = true;
		final FormElement formElement = this;
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		DropTarget target = new DropTarget(this.getControl(), operations);
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		target.setTransfer(types);

		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				formElement.dragEnter(event);
			}
			
			public void dragOver(DropTargetEvent event) {
				formElement.dragOver(event);
			}
			
			public void dragOperationChanged(DropTargetEvent event) {
				formElement.dragOperationChanged(event);
			}
			
			public void dragLeave(DropTargetEvent event) {
				formElement.dragLeave(event);
			}
			
			public void dropAccept(DropTargetEvent event) {
				formElement.dropAccept(event);
			}
			
			public void drop(DropTargetEvent event) {
				formElement.drop(event);
			}
		});
	}

	public void dragEnter(DropTargetEvent event) {
		//System.out.println("dragEnter: '" + getIdentity() + "'");
	}
	
	public void dragOver(DropTargetEvent event) {
		//System.out.println("dragOver: '" + getIdentity() + "'");
	}
	
	public void dragOperationChanged(DropTargetEvent event) {
		//System.out.println("dragOperationChanged: '" + getIdentity() + "'");
	}
	
	public void dragLeave(DropTargetEvent event) {
		//System.out.println("dragLeave: '" + getIdentity() + "'");
	}
	
	public void dropAccept(DropTargetEvent event) {
		//System.out.println("dropAccept: '" + getIdentity() + "'");
	}
	
	public void drop(DropTargetEvent event) {
		//System.out.println("drop: '" + getIdentity() + "'");
		if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
			String dragIdsString = (String)event.data;
			String dropId = getIdentity();
			String operation = getDropOperation(event.detail);
			//System.out.println("drop: '" + dragIdsString + "->" + dropId + "'");
			raiseDragAndDropEvent(dropId,operation,dragIdsString);
		}
	}
	
	public String getDropOperation(int eventDetail) {
		if (eventDetail == DND.DROP_COPY)
			return "copy";
		if (eventDetail == DND.DROP_MOVE)
			return "move";
		return "default";
	}

	public void raiseDragAndDropEvent(String dropId, String operation, String dragIdsString) {
		Message m = handler.newMessage("dragAndDrop",3);
		Value v1 = new Value(dropId);
		Value v2 = new Value(operation);
		Value v3 = new Value(dragIdsString);
		m.args[0] = v1;
		m.args[1] = v2;
		m.args[2] = v3;
		raiseEvent(m);
	}

	public void raiseAcceptDropEvent(String dropId, String operation, String dragIdsString) {
		Message m = handler.newMessage("acceptDrop",3);
		Value v1 = new Value(dropId);
		Value v2 = new Value(operation);
		Value v3 = new Value(dragIdsString);
		m.args[0] = v1;
		m.args[1] = v2;
		m.args[2] = v3;
		raiseEvent(m);
	}
	
	public void setIdentity(String identity) {
		this.identity = identity;
	}

}