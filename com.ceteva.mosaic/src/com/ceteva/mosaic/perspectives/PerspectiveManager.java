package com.ceteva.mosaic.perspectives;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.WorkbenchException;

import com.ceteva.mosaic.MosaicPlugin;

public class PerspectiveManager {
	public static String standardPerspective = "com.ceteva.mosaic.XmodelerPerspective";
	
	private static PerspectiveManager defaultPM = new PerspectiveManager();
	
	private PerspectiveManager(){
		
	}
	
	public static PerspectiveManager getDefaultManager(){
		return PerspectiveManager.defaultPM;
	}
	
	Hashtable templates = new Hashtable();
	
	public void addNewPerspective(String id,String title,String image) {
		PerspectiveTemplate template = new PerspectiveTemplate(id,title,image);
		templates.put(id,template);
	}
	
	public boolean addNewPlaceHolder(String perspectiveId,String placeHolderId,String position,String refid,int ratio) {
		//PerspectiveTemplate template = (PerspectiveTemplate)templates.get(perspectiveId);
		PerspectiveTemplate template = (PerspectiveTemplate)templates.get(perspectiveId);
		if(template != null) {
			template.addHolder(placeHolderId,position,refid,ratio);
			return true;
		}
		return false;
	}	
	
	public boolean addNewPlaceHolderType(String placeHolderId,String type) {
		Enumeration e = templates.elements();
		while(e.hasMoreElements()) {
		  PerspectiveTemplate template = (PerspectiveTemplate)e.nextElement();
		  if(template.hasHolder(placeHolderId)) {
		    template.addViewTypeToHolder(placeHolderId,type);
		    return true;
		  }
		}
		return false;
	}
	
	public boolean removePerspective(String id) {
		return false;
	}
	
	public boolean removePlaceHolder(String id) {
		return false;
	}
	
	public boolean removePlaceHolderType(String id) {
		return false;
	}
	
	public boolean showPerspective(String id) {
		//Uncomment this again if perspectives can be edited through xmf
		/*IWorkbench workbench = MosaicPlugin.getDefault().getWorkbench();
		try {
			workbench.showPerspective(id,workbench.getActiveWorkbenchWindow());
		}
		catch(WorkbenchException e) {
			e.printStackTrace();	
		}*/
		return true;
	}
}
