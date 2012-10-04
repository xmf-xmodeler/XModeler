package com.ceteva.mosaic.splash;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;

public class ImageCanvas implements PaintListener {

	Image image;
	
	public ImageCanvas(Image image) {
		this.image = image;
	}
	
	public void paintControl(PaintEvent e) {
		e.gc.drawImage(image, 0, 0);
	}

}
