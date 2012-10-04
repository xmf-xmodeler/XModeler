package com.ceteva.diagram.editPart;

import java.beans.PropertyChangeListener;

import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.ceteva.diagram.model.CommandEvent;

  public abstract class CommandEventEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {
	
	public void activate() {	
	  if (isActive()==false) {
		super.activate();
		((CommandEvent)getModel()).addPropertyChangeListener(this);
	  }
	}

	public void deactivate() {
	  if (isActive()) {
		super.deactivate();
		((CommandEvent)getModel()).removePropertyChangeListener(this);
	  }
	}
	
	public String getModelIdentity() {
	  return ((CommandEvent)getModel()).getIdentity();
	}
	
	public void preferenceUpdate() {
	}
  }