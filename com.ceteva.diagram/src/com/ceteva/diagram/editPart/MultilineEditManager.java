package com.ceteva.diagram.editPart;

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
import com.ceteva.diagram.figure.MultilineTextFigure;

public class MultilineEditManager extends DirectEditManager {

	Font scaledFont;

	public MultilineEditManager(GraphicalEditPart source, Class editorType,
			CellEditorLocator locator) {
		super(source, editorType, locator);
	}

	public void cancel() {
		bringDown();
	}

	protected void initCellEditor() {
		Text text = (Text) getCellEditor().getControl();
		MultilineTextFigure stickyNote = (MultilineTextFigure) getEditPart()
				.getFigure();
		String initialLabelText = stickyNote.getText();
		getCellEditor().setValue(initialLabelText);
		scaledFont = stickyNote.getFigure().getFont();
		FontData data = scaledFont.getFontData()[0];
		Dimension fontSize = new Dimension(0, data.getHeight());
		stickyNote.translateToAbsolute(fontSize);
		data.setHeight(fontSize.height);
		scaledFont = FontManager.getFont(data);

		text.setFont(scaledFont);
		text.selectAll();
	}

	protected CellEditor createCellEditorOn(Composite composite) {
		return new TextCellEditor(composite, SWT.MULTI | SWT.WRAP);
	}

}