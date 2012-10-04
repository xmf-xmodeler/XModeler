package com.ceteva.diagram.editPart;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.ceteva.client.FontManager;
import com.ceteva.diagram.figure.MultilineEdgeTextFigure;

public class MultilineEdgeEditManager 
	extends DirectEditManager 
{

Font scaledFont;

public MultilineEdgeEditManager(
		GraphicalEditPart source,
		Class editorType,
		CellEditorLocator locator)
{
	super(source, editorType, locator);
}

/* protected void bringDown() {
	Font disposeFont = scaledFont;
	scaledFont = null;
	super.bringDown();
	if (disposeFont != null)
		disposeFont.dispose();	
} */

protected void initCellEditor() {
	Text text = (Text)getCellEditor().getControl();
	MultilineEdgeTextFigure stickyNote = (MultilineEdgeTextFigure)getEditPart().getFigure();
	String initialLabelText = stickyNote.getText();
	getCellEditor().setValue(initialLabelText);
	IFigure figure = getEditPart().getFigure();
	scaledFont = figure.getFont();
	FontData data = scaledFont.getFontData()[0];
	Dimension fontSize = new Dimension(0, data.getHeight());
	stickyNote.translateToAbsolute(fontSize);
	data.setHeight(fontSize.height);
	// scaledFont = new Font(null, data);
	scaledFont = FontManager.getFont(data);
	text.setFont(scaledFont);
	text.selectAll();
}

protected CellEditor createCellEditorOn(Composite composite) {
	return new TextCellEditor(composite, SWT.MULTI | SWT.WRAP);
}

} 