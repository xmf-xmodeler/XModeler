package com.ceteva.text.texteditor;

import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

public class HighlightLine {
	
	SourceViewer viewer;
	Color color;
	Color oldColor;
	int line;
	
	public HighlightLine(SourceViewer viewer,int line,Color color) {
	  this.viewer = viewer;
	  this.line = line;
	  this.color = color;
	  enable();
	}
	
	public void enable() {
      StyledText textWidget = viewer.getTextWidget();
      oldColor = textWidget.getLineBackground(line);
	  textWidget.setLineBackground(line,1,color);
	}
	
	public void disable() {
	  StyledText textWidget = viewer.getTextWidget();
	  textWidget.setLineBackground(line,1,oldColor);
	}
	
	public void changeColor(Color color) {
	  this.color = color;
	  this.enable();
	}
}