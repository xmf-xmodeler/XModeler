package com.ceteva.modelBrowser.preferences;

import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ceteva.modelBrowser.ModelBrowserPlugin;

public class ModelBrowserPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ModelBrowserPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		IPreferenceStore store = ModelBrowserPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	protected void createFieldEditors() {
		RadioGroupFieldEditor invokePropertyEditor = new RadioGroupFieldEditor(
			IPreferenceConstants.INVOKE_PROPERTY_EDITOR,
			"Action required to invoke property editor", 1,
			new String[][] {
				{"Double click", "doubleClick"},
				{"Single Click", "singleClick"},
				{"None", "none"},
			},
			getFieldEditorParent(),
		  	true);
			addField(invokePropertyEditor);

		RadioGroupFieldEditor invokeDiagramEditor= new RadioGroupFieldEditor(
			IPreferenceConstants.INVOKE_DIAGRAM_EDITOR,
			"Action required to view diagram (where appropriate)", 1,
			new String[][] {
				{"Double click", "doubleClick"},
				{"Single Click", "singleClick"},
				{"None", "none"},
			},
			getFieldEditorParent(),
			true);
			addField(invokeDiagramEditor);
	}
	
	public void init(IWorkbench workbench) {
	}
}