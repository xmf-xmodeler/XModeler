package com.ceteva.mosaic.perspectives;

import java.util.Vector;

import org.eclipse.ui.IPageLayout;

public class Holder {

	private String holderid;
	private String position;
	private String refid;
	private int ratio;
	private Vector viewTypes = new Vector();
	
	public Holder(String holderid,String position,String refid,int ratio) {
		this.holderid = holderid;
		this.position = position;
		this.refid = refid;
		this.ratio = ratio;
	}
	
	public void addViewType(String id) {
		viewTypes.addElement(id);
	}
	
	public String getIdentity() {
		return holderid;
	}
	
	public int getPosition() {
		if(position.equals("left"))
		  return IPageLayout.LEFT;
		else if(position.equals("right"))
		  return IPageLayout.RIGHT;
		else if(position.equals("bottom"))
		  return IPageLayout.BOTTOM;
		return IPageLayout.TOP;
	}
	
	public String getRefId() {
		return refid;
	}
	
	public float getRatio() {
		float f = ratio;
		f = f/100;
		f = Math.min(f,(float)0.95);
		f = Math.max(f,(float)0.05);
		return f;
	}
	
	public Vector getViewTypes() {
		return viewTypes;
	}
	
	public void removeViewType(String type) {
		viewTypes.remove(type);
	}
	
}
