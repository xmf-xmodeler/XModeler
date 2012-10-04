package com.ceteva.diagram.editPart;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Text;

import com.ceteva.client.FontManager;

public class TextEditManager extends DirectEditManager {

	Font scaledFont;

	public TextEditManager(GraphicalEditPart source, Class editorType,CellEditorLocator locator) {
		super(source, editorType, locator);
	}

	public void cancel() {
		bringDown();
	}

	protected void initCellEditor() {
		Text text = (Text) getCellEditor().getControl();
		Label label = (Label) getEditPart().getFigure();
		String initialLabelText = label.getText();
		getCellEditor().setValue(initialLabelText);
		scaledFont = label.getFont();
		FontData data = scaledFont.getFontData()[0];
		Dimension fontSize = new Dimension(0, data.getHeight());
		label.translateToAbsolute(fontSize);
		data.setHeight(fontSize.height);
		// scaledFont = new Font(null, data);
		scaledFont = FontManager.getFont(data);
		text.setFont(scaledFont);
		text.selectAll();
	}
}
