package com.ceteva.dialogs.progress.ui;

import java.util.Vector;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.ceteva.dialogs.progress.model.Job;

public class DetailedProgressViewer {

	Composite control;
	private ScrolledComposite scrolled;
	private Composite noEntryArea;
	private Vector progressbars = new Vector();

	public DetailedProgressViewer(Composite parent, int style) {
		scrolled = new ScrolledComposite(parent, SWT.V_SCROLL | style);
		int height = JFaceResources.getDefaultFont().getFontData()[0]
				.getHeight();
		scrolled.getVerticalBar().setIncrement(height * 2);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);

		control = new Composite(scrolled, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		control.setLayout(layout);
		control.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		scrolled.setContent(control);
		noEntryArea = new Composite(scrolled, SWT.NONE);
		noEntryArea.setLayout(new GridLayout());
		Text noEntryLabel = new Text(noEntryArea, SWT.SINGLE);
		noEntryLabel.setBackground(noEntryArea.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		GridData textData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		noEntryLabel.setLayoutData(textData);
		noEntryLabel.setEditable(false);
	}
	
	public Control getControl() {
		return scrolled;
	}
	
	public boolean existsProgressForJob(Job job) {
		return getProgressForJob(job) != null;
	}
	
	public ProgressInfoItem getProgressForJob(Job job) {
		for(int i=0;i<progressbars.size();i++) {
			ProgressInfoItem progress = (ProgressInfoItem)progressbars.elementAt(i);
			if(progress.getJob() == job)
			  return progress;
		}
		return null;
	}
	
	public void addProgress(Job job) {
		ProgressInfoItem progress = new ProgressInfoItem(job,control, SWT.NONE);
		progressbars.add(progress);
	}
	
	public void refresh(Vector jobs) {
		for(int i=0;i<progressbars.size();i++) {
			ProgressInfoItem progress = (ProgressInfoItem)progressbars.elementAt(i);
			if(!jobs.contains(progress.getJob())) {
				progress.delay();
				removeProgress(progress);
			}
		}
		for(int i=0;i<jobs.size();i++) {
			Job job = (Job)jobs.elementAt(i);
			if(!existsProgressForJob(job))
				addProgress(job);
		}
		if(!control.isDisposed())
		  control.layout(true);
	}
	
	public void removeProgress(ProgressInfoItem progress) {
		progressbars.remove(progress);
		progress.dispose();
	}


}