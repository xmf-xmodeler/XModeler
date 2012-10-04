package com.ceteva.dialogs.progress.ui;

import java.util.Vector;

import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.ceteva.dialogs.progress.model.Job;

public class ProgressDialog extends IconAndMessageDialog {
	
	class Timer extends Thread {
		
		private int maximum = 5;
		private boolean done = false;
		
		public synchronized boolean done() {
			return done;
		}
		
		public void run() {
			for(int i=0;i<maximum;i++) {
			  try {
			    Thread.sleep(100);
			  }
			  catch(InterruptedException iox) {
			    done = true;	  
			  }
			}
			done = true;
		}
	}

	private DetailedProgressViewer viewer;
	private Button cancelSelected;
	private Cursor arrowCursor;
	private Cursor waitCursor;
	private Timer timer;

	public ProgressDialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
		// no close button
		setBlockOnOpen(false);
		setMessage("Progress");
	}
	
	protected Control createDialogArea(Composite parent) {
		setMessage(message);
		createMessageArea(parent);
		showJobDetails(parent);
		startTimer();
		return parent;
	}
	
	void showJobDetails(Composite parent) {
		viewer = new DetailedProgressViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		int heightHint = convertHeightInCharsToPixels(10);
		data.heightHint = heightHint;
		viewer.getControl().setLayoutData(data);
	}


	private void clearCursors() {
		clearCursor(cancelSelected);
		clearCursor(getShell());
		if (arrowCursor != null) {
			arrowCursor.dispose();
		}
		if (waitCursor != null) {
			waitCursor.dispose();
		}
		arrowCursor = null;
		waitCursor = null;
	}

	private void clearCursor(Control control) {
		if (control != null && !control.isDisposed()) {
			control.setCursor(null);
		}
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (waitCursor == null) {
			waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		}
		shell.setCursor(waitCursor);
		shell.setText("Progress Information");
	}

	private void setMessage(String messageString) {
		message = messageString == null ? "" : messageString;
		if (messageLabel == null || messageLabel.isDisposed()) {
			return;
		}
		messageLabel.setText(message);
	}
	
	private void startTimer() {
		timer = new Timer();
		timer.start();
	}

	protected Image getImage() {
		return getInfoImage();
	}

	public boolean close() {
		while(!timer.done());
		clearCursors();
		return super.close();
	}

	protected Control createButtonBar(Composite parent) {
		return parent;
	}
	
	public void refresh(Vector jobs) {
		if(jobs.size() > 0) {
		  this.open();
		  Job latest = (Job)jobs.lastElement();
		  setMessage(latest.getName());
		}
		else
		  this.close();
		viewer.refresh(jobs);
	}
}
