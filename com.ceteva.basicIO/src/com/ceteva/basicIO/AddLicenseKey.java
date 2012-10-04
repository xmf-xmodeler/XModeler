package com.ceteva.basicIO;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddLicenseKey extends MessageDialog {

	private String key = "";
	private Text text = null;
	static Color backgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	
	public AddLicenseKey(Shell parentShell) {
		super(
				parentShell,
				"Add license key",
				Display.getCurrent().getSystemImage(MessageDialog.INFORMATION),
				"Paste your license key into the area below and click store.\n" +
				"Once a license key has been added you must restart the tool",
				MessageDialog.INFORMATION,
				new String[] { "Store key", "Cancel" }, 0);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	protected void buttonPressed(int buttonId) {
	    if(buttonId == 0)
	      key = text.getText();
	    super.buttonPressed(buttonId);
	    MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Information", "Please restart the tool");
	}
	
	public Point getInitialSize() {
		Display.getCurrent().getSystemImage(MessageDialog.ERROR);
		return new Point(450,300);
	}
	
	public String getKey() {
		return key;
	}
	
	protected Control createCustomArea(Composite parent) {
	  text = new Text(parent,SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
	  text.setBackground(backgroundColor);
	  parent.setLayout(new FillLayout());
	  return text;
	}
}
