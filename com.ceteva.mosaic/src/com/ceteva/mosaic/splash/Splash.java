package com.ceteva.mosaic.splash;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Splash {

	private static int imageChooserHeight = 30;
	private String choosenImage = "";
	private Image toolImage;
	private Shell shell;
	private Display display;
	private boolean getImage;

	public Splash(String toolimage, Hashtable images) {

		this.getImage = (images.size() != 0);

		// create display and shell

		display = Display.getDefault();
		shell = new Shell(display, SWT.ON_TOP | SWT.BORDER);

		shell.setLayout(new FillLayout());

		// create images

		toolImage = new Image(display, toolimage);
		Rectangle ibounds = toolImage.getBounds();

		// create components

		Composite main = new Composite(shell, SWT.NONE);
		createToolImage(main, toolImage, getImage);
		if (getImage)
			createImageChooser(main, ibounds.height, images);

		Rectangle dbounds = shell.getMonitor().getBounds();

		if (getImage)
			shell.setBounds(dbounds.x + (dbounds.width - ibounds.width) / 2,
					dbounds.y + (dbounds.height - ibounds.height) / 2,
					ibounds.width, ibounds.height + imageChooserHeight);
		else
			shell.setBounds(dbounds.x + (dbounds.width - ibounds.width) / 2,
					dbounds.y + (dbounds.height - ibounds.height) / 2,
					ibounds.width, ibounds.height);
	}

	public String choosenImage() {
		return choosenImage;
	}

	public void createImageChooser(Composite main, int y, final Hashtable images) {

		// label

		Label label = new Label(main, SWT.NONE);
		label.setText("Please choose an image:");
		label.setBounds(5, y + 5, 130, 40);

		// chooser

		final Combo combo = new Combo(main, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setBounds(140, y, 150, 30);
		Enumeration e = images.keys();
		while (e.hasMoreElements()) {
			String image = (String) e.nextElement();
			String filePath = (String) images.get(image);
			long fileSizeInMB = getFileSize(filePath);
			combo.add(image + ": " + fileSizeInMB + " mb");
		}
		combo.select(0);

		// okay

		Button okay = new Button(main, SWT.PUSH);
		okay.setText("OK");
		okay.setBounds(300, y, 90, 21);
		okay.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int selectedIndex = combo.getSelectionIndex();
				if (selectedIndex != -1) {
					String selected = combo.getItem(selectedIndex);
					String[] temp = selected.split(":");
					choosenImage = (String) images.get(temp[0]);
					shell.dispose();
				}
			}
		});

		// cancel

		Button cancel = new Button(main, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.setBounds(400, y, 90, 21);
		cancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				shell.dispose();
			}
		});

	}

	private long getFileSize(String filePath) {
		File file = new File(filePath);
		long fileSizeInBytes = file.length();
		// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
		long fileSizeInKB = fileSizeInBytes / 1024;
		// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
		long fileSizeInMB = fileSizeInKB / 1024;

		return fileSizeInMB;
	}

	public void createToolImage(Composite main, Image toolImage,
			boolean getImage) {
		Canvas c = new Canvas(main, SWT.NONE);
		c.addPaintListener(new ImageCanvas(toolImage));
		c.setBounds(toolImage.getBounds());
		if (!getImage) {
			c.addMouseListener(new MouseListener() {
				public void mouseDoubleClick(MouseEvent e) {
				}

				public void mouseDown(MouseEvent e) {
					dispose();
				}

				public void mouseUp(MouseEvent e) {
				}
			});
		}
	}

	public void show() {
		shell.open();
		if (getImage) {
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} else
			wait(15);

	}

	public void wait(final int timeout) {
		Thread t = new Thread() {
			public void run() {
				int time = 0;
				while (time != timeout) {
					time++;
					try {
						sleep(1000);
					} catch (InterruptedException ix) {
						System.out.println(ix);
					}
				}
				dispose();
			}
		};
		t.start();
	}

	public void dispose() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				shell.dispose();
				toolImage.dispose();
			}
		});
	}
}