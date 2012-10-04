package com.ceteva.forms.views;

import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

public class CustomSourceViewer extends SourceViewer {
	
	public CustomSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		super(parent, ruler, styles);
	}
	
}
