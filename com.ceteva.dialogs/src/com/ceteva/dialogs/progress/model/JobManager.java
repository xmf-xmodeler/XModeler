package com.ceteva.dialogs.progress.model;

import java.util.Vector;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import XOS.Message;

import com.ceteva.dialogs.progress.ui.BackgroundJobs;
import com.ceteva.dialogs.progress.ui.ProgressDialog;

public class JobManager {
	
	ProgressDialog viewer;
	BackgroundJobs bviewer;
	Vector jobs = new Vector();
	
	private Job getJobForIdentity(String identity) {
		for(int i=0;i<jobs.size();i++) {
			Job job = (Job)jobs.elementAt(i);
			if(job.getIdentity().equals(identity))
			  return job;
		}
		return null;
	}
	
	public Vector getBackgroundJobs() {
		Vector background = new Vector();
		for(int i=0;i<jobs.size();i++) {
		  Job job = (Job)jobs.elementAt(i);
		  if(job.isBackground())
		    background.addElement(job);
		}
		return background;
	}
	
	public Vector getForegroundJobs() {
		Vector foreground = new Vector();
		for(int i=0;i<jobs.size();i++) {
		  Job job = (Job)jobs.elementAt(i);
		  if(!job.isBackground())
		    foreground.addElement(job);
		}
		return foreground;
	}
	
	public Shell getShell() {
		return Display.getCurrent().getActiveShell();
	}
	
	public void newBusyJob(String identity,String name,String tooltip,boolean background) {
		Job job = new Job(identity,name,tooltip,background);
		jobs.addElement(job);
		refreshViewer();
	}
	
	public boolean processMessage(Message message) {
		if (message.hasName("newBusyDialog") && message.arity == 3) {
			String identity = message.args[0].strValue();
			String title = message.args[1].strValue();
			boolean background = message.args[2].boolValue;
			newBusyJob(identity,title,title,background);
			return true;
		}
		else if(message.hasName("noLongerBusy") && message.arity==1) {
			String identity = message.args[0].strValue();
			return removeBusyJob(identity);
		}
		return false;
	}
	
	public void refreshViewer() {
		if(viewer==null)
		  viewer = new ProgressDialog(getShell());
		if(bviewer==null)
		  bviewer = new BackgroundJobs();
		viewer.refresh(getForegroundJobs());
		bviewer.refresh(getBackgroundJobs());
	}
	
	public boolean removeBusyJob(String identity) {
		Job job = getJobForIdentity(identity);
		if(job!=null && jobs.remove(job)) {
		  refreshViewer();
		  return true;
		}
		return false;
	}

}
