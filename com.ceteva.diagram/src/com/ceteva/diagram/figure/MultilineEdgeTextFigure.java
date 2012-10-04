package com.ceteva.diagram.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.ParagraphTextLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.FontManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.preferences.IPreferenceConstants;

public class MultilineEdgeTextFigure extends Figure {
	
	public TextFlow textFlow;
	
	public MultilineEdgeTextFigure(Point position,RGB forecolor) {
		this.setBounds(new Rectangle(position,new Dimension(-1,-1)));
		buildFlowpage(0);
		textFlow.setBackgroundColor(ColorConstants.black);
	}
	
	public void buildFlowpage(int border) {
		setBorder(new MarginBorder(border));
		FlowPage flowPage = new FlowPage();
		textFlow = new TextFlow();
		textFlow.setLayoutManager(new ParagraphTextLayout(textFlow,ParagraphTextLayout.WORD_WRAP_SOFT));
		flowPage.add(textFlow);
		setLayoutManager(new StackLayout());
		add(flowPage);
	}
	
    public void getPreferences() {
		IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
		FontData fontData = PreferenceConverter.getFontData(preferences,IPreferenceConstants.FONT);
		setFont(FontManager.getFont(fontData));
    }
    
    public String getText() {
    	return textFlow.getText();
    }

	public void preferenceUpdate() {
		getPreferences();
	}
	
	public void setFont(String font) {
		if(!font.equals("")) {
		  FontData fd = new FontData(font);
		  this.setFont(FontManager.getFont(fd));
		}
	}
	
	public void setText(String text) {
		textFlow.setText(text);
	}
	
	protected boolean useLocalCoordinates() {
		return true;
	}
    
}