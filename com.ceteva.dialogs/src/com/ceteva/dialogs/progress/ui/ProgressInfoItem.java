package com.ceteva.dialogs.progress.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.ceteva.dialogs.progress.model.Job;

class ProgressInfoItem extends Composite {
	
	ProgressIndicator progressBar;
	Label progressLabel;
	ToolBar actionBar;
	ToolItem actionButton;
	Job job;
	long startTime;
	long upTime = 800;
	
	static final int MAX_PROGRESS_HEIGHT = 12;

	public ProgressInfoItem(Job job,Composite parent, int style) {
		super(parent, style);
		this.job = job;
		createChildren();
		setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		this.createProgressBar(style);
		startTime = System.currentTimeMillis();
	}
	
	public void delay() {
		long endTime = System.currentTimeMillis();
	    while(endTime-startTime < upTime) {
	  	  try {
	  	  	endTime = System.currentTimeMillis();
			Thread.sleep(10);
		  } catch (InterruptedException e) {
			e.printStackTrace();
		  }
	    }
	}
	
	protected void createChildren() {
		FormLayout layout = new FormLayout();
		setLayout(layout);
		FormData imageData = new FormData();
		imageData.top = new FormAttachment(IDialogConstants.VERTICAL_SPACING);
		imageData.left = new FormAttachment(IDialogConstants.HORIZONTAL_SPACING / 2);
		progressLabel = new Label(this, SWT.NONE);
		progressLabel.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		progressLabel.setText(job.getName());
		progressLabel.setToolTipText(job.getName());
		actionBar = new ToolBar(this, SWT.FLAT);
		actionBar.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		/* actionButton = new ToolItem(actionBar, SWT.NONE);
		actionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				actionButton.setEnabled(false);
			}
		}); */

		FormData progressData = new FormData();
		progressData.top = new FormAttachment(IDialogConstants.VERTICAL_SPACING);
		progressData.left = new FormAttachment(new ToolBar(this,SWT.FLAT),IDialogConstants.HORIZONTAL_SPACING / 2);
		// progressData.right = new FormAttachment(actionBar,IDialogConstants.HORIZONTAL_SPACING);
		progressLabel.setLayoutData(progressData);
	}
	
	void createProgressBar(int style) {
		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(progressLabel, 0);
		buttonData.right = new FormAttachment(100,IDialogConstants.HORIZONTAL_SPACING * -1);
		actionBar.setLayoutData(buttonData);
		progressBar = new ProgressIndicator(this);
		FormData barData = new FormData();
		barData.top = new FormAttachment(actionBar,
				IDialogConstants.VERTICAL_SPACING, SWT.TOP);
		barData.left = new FormAttachment(progressLabel, 0, SWT.LEFT);
		barData.right = new FormAttachment(actionBar,
				IDialogConstants.HORIZONTAL_SPACING * -1);
		barData.height = MAX_PROGRESS_HEIGHT;
		barData.width = 0;// default is too large
		progressBar.setLayoutData(barData);
		progressBar.beginTask(IProgressMonitor.UNKNOWN);
		progressBar.beginAnimatedTask();
	}
	
	public Job getJob() {
		return job;
	}
}
