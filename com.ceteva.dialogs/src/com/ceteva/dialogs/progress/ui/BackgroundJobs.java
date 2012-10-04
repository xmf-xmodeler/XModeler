package com.ceteva.dialogs.progress.ui;

import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

import com.ceteva.dialogs.progress.model.Job;


public class BackgroundJobs {
	
	private IProgressMonitor monitor;
	private Job currentJob = null;
		
	public void done() {
	    if(currentJob != null) {
		  currentJob = null;
		  monitor.done();
	    }
	}
	
	public Job getLatestBackgroundJob(Vector jobs) {
		Job latest = null;
		for(int i=0;i<jobs.size();i++) {
		  Job job = (Job)jobs.elementAt(i);
		  if(job.isBackground())
		    latest = job;
		}
		return latest;
	}
	
	public IProgressMonitor getProgressMonitor() {
		if(monitor == null) {
		  WorkbenchWindow window = (WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		  IStatusLineManager manager = window.getActionBars().getStatusLineManager();
		  monitor = manager.getProgressMonitor();
		}
		return monitor;
	}
	
	public void refresh(Vector jobs) {
		Job latest = getLatestBackgroundJob(jobs);
		if(latest != null) {
		  if(currentJob != latest) {
		    done();
		    show(latest);
		  }
		}
		else {
		  done();
		}
	}
	
	public void show(Job job) {
		currentJob = job;
		getProgressMonitor().beginTask(job.getName(),IProgressMonitor.UNKNOWN);
	}

}
