package com.ceteva.console.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ceteva.console.ConsolePlugin;

public class ConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ConsolePreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		IPreferenceStore store = ConsolePlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	protected void createFieldEditors() {
		FontFieldEditor font = new FontFieldEditor(
			IPreferenceConstants.CONSOLE_FONT,
			"Font",
			getFieldEditorParent());
		addField(font);
			
		ColorFieldEditor fontColour = new ColorFieldEditor(
			IPreferenceConstants.CONSOLE_FONT_COLOUR,
			"Selected font colour",
			getFieldEditorParent());
		addField(fontColour);
		
		ColorFieldEditor backgroundColour = new ColorFieldEditor(
			IPreferenceConstants.CONSOLE_BACKGROUND,
			"Background colour",
			getFieldEditorParent());
		addField(backgroundColour);
		
		IntegerFieldEditor lineLimit = new IntegerFieldEditor(
		    IPreferenceConstants.LINE_LIMIT,
		    "Output history limit (chars)",
			getFieldEditorParent());
		lineLimit.setValidRange(1000,999999);
	    addField(lineLimit);
	    
		IntegerFieldEditor commandHistoryLimit = new IntegerFieldEditor(
		    IPreferenceConstants.COMMAND_HISTORY_LIMIT,
			"Command history limit",
			getFieldEditorParent());
		//lineLimit.setValidRange(5,100);
		addField(commandHistoryLimit);
	}
	
	public void init(IWorkbench workbench) {
	}
}