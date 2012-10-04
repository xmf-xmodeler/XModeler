package com.ceteva.diagram.editPart;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

import com.ceteva.diagram.figure.MultilineTextFigure;

final public class MultilineCellEditorLocator implements CellEditorLocator {

	private MultilineTextFigure multilineTextFigure;

	public MultilineCellEditorLocator(MultilineTextFigure stickyNote) {
		setLabel(stickyNote);
	}

	public void relocate(CellEditor celleditor) {
		Text text = (Text) celleditor.getControl();
		Point sel = text.getSelection();
		Point pref = text.computeSize(-1, -1);
		Rectangle rect = multilineTextFigure.getBounds().getCopy();
		multilineTextFigure.translateToAbsolute(rect);
		text.setBounds(rect.x - 4, rect.y - 1, pref.x + 1, pref.y + 1);
		text.setSelection(0);
		text.setSelection(sel);
	}

	protected MultilineTextFigure getLabel() {
		return multilineTextFigure;
	}

	protected void setLabel(MultilineTextFigure stickyNote) {
		this.multilineTextFigure = stickyNote;
	}

}
