package com.ceteva.diagram.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ceteva.diagram.DiagramPlugin;

public class DiagramPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public DiagramPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}

	protected void createFieldEditors() {
		
		BooleanFieldEditor animateZoom = new BooleanFieldEditor(
				IPreferenceConstants.ANIMATEZOOM, 
				"Animate zoom", 
				getFieldEditorParent());
				addField(animateZoom);
		
		BooleanFieldEditor gridlines = new BooleanFieldEditor(
			IPreferenceConstants.GRIDLINES, 
			"Show gridlines", 
			getFieldEditorParent());
			addField(gridlines);

		IntegerFieldEditor indentSpaces = new IntegerFieldEditor(
			IPreferenceConstants.GRIDSIZE,
			"Grid size",
			getFieldEditorParent());
			indentSpaces.setValidRange(5, 200);
			addField(indentSpaces);
				
		/* BooleanFieldEditor splined = new BooleanFieldEditor(
			IPreferenceConstants.SPLINED, "Show edges splined",
			getFieldEditorParent());
		addField(splined); */
			
	    ColorFieldEditor edgeColor = new ColorFieldEditor(
		    IPreferenceConstants.EDGE_COLOR,
			"Edge colour",
			getFieldEditorParent());
			addField(edgeColor);
			
		ColorFieldEditor backgroundColor = new ColorFieldEditor(
			IPreferenceConstants.DIAGRAM_BACKGROUND_COLOR,
			"Diagram background colour",
			getFieldEditorParent());
		addField(backgroundColor);
		
		/* BooleanFieldEditor gradient = new BooleanFieldEditor(
			IPreferenceConstants.GRADIENT_FILL, 
			"Show gradient", 
			getFieldEditorParent());
			addField(gradient); */
		
		ColorFieldEditor foregroundColor = new ColorFieldEditor(
			IPreferenceConstants.FOREGROUND_COLOR,
			"Foreground colour",
			getFieldEditorParent());
		addField(foregroundColor);
		
		ColorFieldEditor fillColor = new ColorFieldEditor(
			IPreferenceConstants.FILL_COLOR,
			"Fill colour",
			getFieldEditorParent());
		addField(fillColor);
		
		ColorFieldEditor unselectedFontColor = new ColorFieldEditor(
			IPreferenceConstants.UNSELECTED_FONT_COLOR,
			"Unselected font colour",
			getFieldEditorParent());
		addField(unselectedFontColor);
				
		ColorFieldEditor selectedFontColor = new ColorFieldEditor(
		    IPreferenceConstants.SELECTED_FONT_COLOR,
			"Selected font colour",
			getFieldEditorParent());
		addField(selectedFontColor);
		
		FontFieldEditor font = new FontFieldEditor(
			IPreferenceConstants.FONT,
			"Font",
			getFieldEditorParent());
			addField(font);
	}
	
	public void init(IWorkbench workbench) {
	    setPreferenceStore(DiagramPlugin.getDefault().getPreferenceStore());
	    setDescription("Diagram Displays Preferences");
	  }
}