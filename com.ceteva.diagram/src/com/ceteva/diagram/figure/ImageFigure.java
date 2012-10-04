package com.ceteva.diagram.figure;

import org.eclipse.swt.graphics.Image;

import com.ceteva.diagram.model.ImageManager;

public class ImageFigure extends org.eclipse.draw2d.ImageFigure {
	
  public ImageFigure(Image image) {
  	super(image);
  }
	
  protected boolean useLocalCoordinates() {
    return false;
  }
  
  public void setSize(int w, int h) {
	super.setSize(w,h);
  	this.setImage(ImageManager.resizeImage(this.getImage(),w,h));
  }
}