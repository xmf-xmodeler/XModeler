package com.ceteva.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ceteva.client.FontManager;

public class TextAreaDialog extends MessageDialog {
	
	String displayText;
	static Color backgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	static FontData defaultFont = new FontData("1|Courier New|9|0|WINDOWS|1|-13|0|0|0|400|0|0|0|0|3|2|1|49|Courier New");
	
	public TextAreaDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String message, String displayText, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, message,
				dialogImageType, dialogButtonLabels, defaultIndex);
		this.displayText = displayText;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	public Point getInitialSize() {
		return new Point(450,300);
	}
	
	protected Control createCustomArea(Composite parent) {
	  Text text = new Text(parent,SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
	  text.setFont(FontManager.getFont(defaultFont));
	  text.setBackground(backgroundColor);
	  text.setText(displayText);
	  parent.setLayout(new FillLayout());
	  return text;
	}
}