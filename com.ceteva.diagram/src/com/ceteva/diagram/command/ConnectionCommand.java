package com.ceteva.diagram.command;

import org.eclipse.gef.commands.Command;

import XOS.Message;
import XOS.Value;

import com.ceteva.diagram.model.CommandEvent;
import com.ceteva.diagram.model.Edge;

public class ConnectionCommand extends Command {
  private String toolIdentity = "";
  private CommandEvent parent = null;
  private Edge edge = null;	
  private String source = null; 
  private String target = null;

  public ConnectionCommand() {
  }

  public boolean canExecute(){
	return true;
  }

  public void execute() {
    if(edge==null) {
      	Message m = parent.handler.newMessage("newEdge",3);
    	Value v1 = new Value(toolIdentity);
    	Value v2 = new Value(source);
    	Value v3 = new Value(target);
    	m.args[0] = v1;
    	m.args[1] = v2;
    	m.args[2] = v3;
    	parent.handler.raiseEvent(m);	
    }	
    else if(source != null && !edge.getSourcePort().equals(source)) {
      	Message m = parent.handler.newMessage("edgeSourceReconnected",2);
    	Value v1 = new Value(edge.getIdentity());
    	Value v2 = new Value(source);
    	m.args[0] = v1;
    	m.args[1] = v2;
    	parent.handler.raiseEvent(m);
    }
    else if(target != null && !edge.getTargetPort().equals(target)) {
    	Message m = parent.handler.newMessage("edgeTargetReconnected",2);
    	Value v1 = new Value(edge.getIdentity());
    	Value v2 = new Value(target);
    	m.args[0] = v1;
    	m.args[1] = v2;
    	parent.handler.raiseEvent(m);
    }
  }
  
  public void setSource(String source) {
  	this.source = source;
  }
  
  public void setTarget(String target) {
	this.target = target;
  }
  
  public void setEdge(Edge edge) {
    this.edge = edge;	
  }
  
  public void setParent(CommandEvent parent) {
  	this.parent = parent;
  }
  
  public void setToolIdentity(String toolIdentity) {
  	this.toolIdentity = toolIdentity;
  }
}