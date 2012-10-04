package com.ceteva.forms.views;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import XOS.Message;

class ComponentCommandHandler {

  public static boolean processMessage(Control control,Message message) {
	if(message.hasName("delete") && message.arity==1) {
	  control.dispose();
	  return true;
	}
  	if(message.hasName("enable") && message.arity==2) {
	  control.setEnabled(true);
	  return true;
	}
	if(message.hasName("disable") && message.arity==2) {
	  control.setEnabled(false);
	  return true;
	}
	if(message.hasName("move") && message.arity==3) {
	  control.setLocation(message.args[1].intValue,message.args[2].intValue);
	  control.redraw();
	  return true;
	}
	if(message.hasName("setSize") && message.arity==3) {
      control.setSize(message.args[1].intValue,message.args[2].intValue);
      return true;
	}
	if(message.hasName("maximiseToCanvas") && message.arity == 1) {
	  maximiseToCanvas(control);
	  return true;
	}
	return false;
  }
  
  public static void maximiseToCanvas(final Control control) {
	  final Composite formContentsHolder = control.getParent();
	  final Composite scrollable = formContentsHolder.getParent();
	  final Composite form = scrollable.getParent();
	  Point p = form.getSize();
      scrollable.setBounds(0,0,p.x+50,p.y+50);
	  formContentsHolder.setBounds(0,0,p.x,p.y);
	  control.setBounds(0,0,p.x,p.y);
      ControlListener listener = new ControlListener() {
         public void controlMoved(ControlEvent e) {}
         public void controlResized(ControlEvent e) {
            Point p = form.getSize();
            scrollable.setBounds(0,0,p.x+50,p.y+50);
            formContentsHolder.setBounds(0,0,p.x,p.y);
            control.setBounds(0,0,p.x,p.y);
         }
      };
	  form.addControlListener(listener);
      // owningView.addCanvasEventListener(listener);
  }
}