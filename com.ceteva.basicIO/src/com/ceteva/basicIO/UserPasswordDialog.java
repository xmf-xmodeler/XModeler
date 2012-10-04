package com.ceteva.basicIO;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import XOS.UserPassword;

public class UserPasswordDialog extends Dialog {

	protected Text usernameField;
	protected Text passwordField;

	protected String host;
	protected String message;
	protected UserPassword userPassword = null;

	public static UserPassword getAuthentication(final String host, final String message) {

		class UIOperation implements Runnable {
			public UserPassword auth;
			public void run() {
				auth = UserPasswordDialog.askForAuthentication(
						host, message);
			}
		}

		UIOperation uio = new UIOperation();
		if (Display.getCurrent() != null) {
			uio.run();
		} else {
			Display.getDefault().syncExec(uio);
		}
		return uio.auth;
	}

	protected static UserPassword askForAuthentication(String host,
			String message) {
		UserPasswordDialog ui = new UserPasswordDialog(null, host, message); //$NON-NLS-1$
		ui.open();
		return ui.getUserPassword();
	}

	protected UserPasswordDialog(Shell parentShell, String host,
			String message) {
		super(parentShell);
		this.host = host;
		this.message = message;
		setBlockOnOpen(true);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Password Required"); //$NON-NLS-1$
	}

	public void create() {
		super.create();
		//give focus to username field
		usernameField.selectAll();
		usernameField.setFocus();
	}

	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		main.setLayout(layout);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(main, SWT.WRAP);
		String text = "Connect to:" + host; 
		text += "\n\n" + message; //$NON-NLS-1$ //$NON-NLS-2$
		label.setText(text);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		label.setLayoutData(data);

		createUsernameFields(main);
		createPasswordFields(main);
		return main;
	}

	protected void createPasswordFields(Composite parent) {
		new Label(parent, SWT.NONE).setText("Password:");

		passwordField = new Text(parent, SWT.BORDER | SWT.PASSWORD);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		passwordField.setLayoutData(data);

		new Label(parent, SWT.NONE); //spacer
	}

	protected void createUsernameFields(Composite parent) {
		new Label(parent, SWT.NONE).setText("User name:");

		usernameField = new Text(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		usernameField.setLayoutData(data);

		new Label(parent, SWT.NONE); //spacer
	}

	public UserPassword getUserPassword() {
		return userPassword;
	}

	protected void okPressed() {
		userPassword = new UserPassword(usernameField.getText(),
				passwordField.getText());
		super.okPressed();
	}

}
