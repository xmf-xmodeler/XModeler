package com.ceteva.mosaic;

import org.eclipse.ui.PlatformUI;

import XOS.Message;

import com.ceteva.client.Client;
import com.ceteva.client.EventHandler;
import com.ceteva.client.FileRedirector;
import com.ceteva.mosaic.perspectives.PerspectiveManager;

public class WorkbenchClient extends Client {
	
	PerspectiveManager perspectiveManager = PerspectiveManager.getDefaultManager();
	
	public WorkbenchClient() {
	  super("com.ceteva.mosaic");
	}
	
	public boolean processMessage(Message message) {
	  if(message.hasName("shutdown")) {
	  	PlatformUI.getWorkbench().close();
	  	return true;
	  }
	  return processPerspectiveMessage(message);
	}
	
	public boolean processPerspectiveMessage(Message message) {
	  if(message.hasName("newPerspective")) {
		  String id = message.args[0].strValue();
		  String title = message.args[1].strValue();
		  String image = message.args[2].strValue();
		  perspectiveManager.addNewPerspective(id,title,image);
		  return true;
	  }
	  if(message.hasName("newPlaceHolder")) {
		  String perpId = message.args[0].strValue();
		  String phId = message.args[1].strValue();
		  String position = message.args[2].strValue();
		  String ref = message.args[3].strValue();
		  int ratio = message.args[4].intValue;
		  return perspectiveManager.addNewPlaceHolder(perpId,phId,position,ref,ratio);
	  }
	  if(message.hasName("newPlaceHolderType")) {
		  String phId = message.args[0].strValue();
		  String type = message.args[1].strValue();
		  return perspectiveManager.addNewPlaceHolderType(phId,type);
	  }
	  if(message.hasName("showPerspective")) {
		  String id = message.args[0].strValue();
		  return perspectiveManager.showPerspective(id);
	  }
	  if(message.hasName("setFilenameRedirect")) {
		  String source = message.args[0].strValue();
		  String target = message.args[1].strValue();
		  FileRedirector.addRedirect(source,target);
		  return true;
	  }
	  return false;
	}

	public void setEventHandler(EventHandler handler) {
	  if(ActionAdvisor.exit!=null)
		ActionAdvisor.exit.setEventHandler(handler);
	}
}