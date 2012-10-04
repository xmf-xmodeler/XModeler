package com.ceteva.diagram.figure;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import com.ceteva.client.FontManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.preferences.IPreferenceConstants;

public class LabelFigure extends Label {
	
	public LabelFigure(Point position,String labelText,boolean underline,boolean italicise) {
		super(labelText);
		getPreferences();
		setLocation(position);
		setPreferredSize(new Dimension(-1,-1));
		if(underline)
		  setUnderline(true);
		if(italicise)
		  setItalicise(true);
	}
	
	public void setFont(String font) {
		if(!font.equals("")) {
		  FontData fd = new FontData(font);
		  this.setFont(FontManager.getFont(fd));
		}
	}
	
	// the following method is platform dependent
	
	public void setUnderline(boolean underline) {
	    Font f = this.getFont();
		FontData fd = f.getFontData()[0];
		if(underline)
		{}//fd.data.lfUnderline = 1;
	    else
	    {}//fd.data.lfUnderline = 0;
		// this.setFont(new Font(Display.getCurrent(), fd));
		
		this.setFont(FontManager.getFont(fd));
		this.repaint();
	}
	
	public void setItalicise(boolean italicise) {
		Font f = this.getFont();
		FontData fd = f.getFontData()[0];
		if(italicise)
		  fd.setStyle(SWT.ITALIC);
	    else
	      fd.setStyle(SWT.NORMAL);
		this.setFont(FontManager.getFont(fd));
		this.repaint();
	}
	
	protected boolean useLocalCoordinates() {
		return true;
	}
	
	public void preferenceUpdate() {
		getPreferences();
	}
    
    public void getPreferences() {
		IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
		FontData fontData = PreferenceConverter.getFontData(preferences,IPreferenceConstants.FONT);
		setFont(FontManager.getFont(fontData));
		setPreferredSize(new Dimension(-1,-1));    	
    }
}