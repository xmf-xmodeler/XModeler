package com.ceteva.dialogs.progress.model;

import org.eclipse.core.runtime.IProgressMonitor;

public class Job {
	
	private String identity;
	private String name;
	private String tooltip;
	private boolean background;
	private int minimum = 0;
	private int maximum = 100;
	private int current = IProgressMonitor.UNKNOWN;
	
	public Job(String identity,String name,String tooltip,int minimum,int maximum,boolean background) {
		this.identity = identity;
		this.name = name;
		this.tooltip = tooltip;
		this.minimum = minimum;
		this.maximum = maximum;
		this.background = background;
	}
	
	public Job(String identity,String name,String tooltip,boolean background) {
		this.identity = identity;
		this.name = name;
		this.tooltip = tooltip;
		this.background = background;
	}
	
	public boolean isBackground() {
		return background;
	}
	
	public int getCurrent() {
		return current;
	}
	
	public String getIdentity() {
		return identity;
	}
	
	public int getMinimum() {
		return minimum;
	}
	
	public int getMaximum() {
		return maximum;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTooltip() {
		return tooltip;
	}
	

}
