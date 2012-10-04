package com.ceteva.basicIO;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import XOS.UserPassword;
import XOS.XmfIOException;
import XOS.XmfIOHandler;

public class BasicIOHandler implements XmfIOHandler {
	
	 class LicenseKeyError implements Runnable {

		private String key = "";
		private String message;
		private boolean canadd;
		
		public LicenseKeyError(String message,boolean canadd) {
			this.canadd = canadd;
			this.message = message;
		}

		public void run() {
			Shell shell = Display.getDefault().getActiveShell();
			MessageDialog dialog = null;
			if (canadd)
				dialog = new MessageDialog(shell, "License key error", null,
						message, MessageDialog.ERROR, new String[] {
								IDialogConstants.OK_LABEL, "Add License" }, 0);
			else
				dialog = new MessageDialog(shell, "License key error", null,
						message, MessageDialog.ERROR, new String[] {
								IDialogConstants.OK_LABEL}, 0);
			if (dialog.open() == 1) {
				AddLicenseKey addKey = new AddLicenseKey(shell);
				addKey.open();
				key = addKey.getKey();
			}
		}

		public String getKey() {
			return key;
		}
	}

	public Object[] dialog(String dialogType, Object[] inputData) throws XmfIOException {
		if (dialogType.equals("UserPassword"))
			return userPasswordDialog(inputData);
	    if (dialogType.equals("LicenseError"))
	    	return licenseKeyError(inputData);
		else
			throw new XmfIOException("XmfIOException: No support for dialog type '" + dialogType + "'");
	}
		
	public Object[] licenseKeyError(Object[] inputData) throws XmfIOException {
		String message = (String)inputData[0];
		final boolean canadd = ((Boolean)inputData[1]).booleanValue(); 
		message = "The following license key error occured: \n\n" + message + "\n\n";
		message = message + "Please add a license or contact support@ceteva.com.";
		LicenseKeyError error = new LicenseKeyError(message,canadd);
		Display.getDefault().syncExec(error);
		String key = error.getKey();
		if(!key.equals("")) {
			Object[] returnvalue = { key };
			return returnvalue;
		}
		return null;
	}
	
	public Object[] userPasswordDialog(Object[] inputData)  throws XmfIOException {

		// Test input data arity and argument types...
		if (inputData.length != 2)
			throw new XmfIOException("XmfIOException: 'UserPassword' dialog type requires two arguments.");
		if (!(inputData[0] instanceof String))
			throw new XmfIOException("XmfIOException: Argument[0] of 'UserPassword' dialog type must be a String.");
		if (!(inputData[1] instanceof String))
			throw new XmfIOException("XmfIOException: Argument[2] of 'UserPassword' dialog type must be a String.");

		String hostString   = (String)inputData[0];
		String promptString = (String)inputData[1];
		UserPassword auth = UserPasswordDialog.getAuthentication(hostString,promptString);
		Object[] outputData = {auth};
		return outputData;
	}
	
}
