package com.ceteva.forms.views;

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPartSite;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;

class FormTable extends FormElement {
	
	static final int ROW_HEIGHT = 15;
	
	IWorkbenchPartSite site;
	Table table;
	int cols;
	int rows;
	Vector components = new Vector();
	Hashtable editors = new Hashtable();
	
	public FormTable(Composite parent,String identity,EventHandler handler,IWorkbenchPartSite site,int cols,int rows) {
		super(identity);
		this.handler = handler;
		this.cols = cols;
		this.rows = rows;
		createTable(parent);
	}
	
	public Control getControl() {
		return table;
	}
	
	public void createTable(Composite parent) {
		
		table = new Table (parent, SWT.BORDER | SWT.SINGLE);
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		
		// initialise columns...
		for (int i=0; i<cols; i++) {
			new TableColumn(table, SWT.NONE);
		}
		
		// initialise rows...
		for (int i=0; i<rows; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			TableEditor[] editorArray = new TableEditor[cols];
			editors.put(item,editorArray);
		}

		// hack to force a change to the default row height...
		if (table.getItemCount()>0)
			initRowHeight();
	}
	
	private void initRowHeight() {
		TableItem item = table.getItem(0);
		Image image = new Image(null, 1, ROW_HEIGHT);
		item.setImage(image); 
	}
	
	private TableEditor getEditor(int col, int row) {
		TableItem item = table.getItem(row);
		TableEditor[] editorArray = (TableEditor[])editors.get(item);
		TableEditor editor = (TableEditor)editorArray[col];
		if (editor == null) {
			editor = new TableEditor(table);
			editorArray[col] = editor;
		}
		return editor;
	}
	
	public void setBounds(int x,int y,int width,int height) {
		table.setBounds(x,y,width,height);
	}
	
	public Value processCall(Message message) {
		return null;
	}
	
	public void delete(Message message) {
		String id = message.args[0].strValue();
		for(int i=0;i<components.size();i++) {
			FormElement component = (FormElement)components.elementAt(i);
			if(component.getIdentity().equals(id))
				components.remove(component);
		}
	}

	public void addRow(Message message) {
		int row = message.args[1].intValue;
		TableItem item = new TableItem(table, SWT.NONE, row);
		TableEditor[] editorArray = new TableEditor[cols];
		
		// Need to reset the row height it the table was previously empty...
		if (rows == 0)
			initRowHeight();
		
		// Pack the table, and reset the bounds afterwards...
		table.setFocus();
		Rectangle bounds = table.getBounds();
		bounds.height = bounds.height + ROW_HEIGHT + 1;
		table.setBounds(bounds);
		
		// Update internal data structure...
		editors.put(item,editorArray);
		this.rows = rows+1;
	}
	
	public void deleteRow(Message message) {
		int row = message.args[1].intValue;
		TableItem item = table.getItem(row);
		
		// Dispose of editors, controls and the item itself in order to remove the row...
		for (int col=0; col<cols; col++) {
			TableEditor editor = this.getEditor(col,row);
			editor.getEditor().dispose();
			editor.dispose();			
		}
		item.dispose();
		
		// Pack the table, and reset the bounds afterwards
		table.setFocus();
		Rectangle bounds = table.getBounds();
		bounds.height = bounds.height - (ROW_HEIGHT + 1);
		table.setBounds(bounds);
		
		// Update internal data structure...
		editors.remove(item);
		this.rows = rows-1;
	}
	
	public boolean processMessage(Message message) {
		boolean processed = false;
		if (message.args[0].hasStrValue(getIdentity())) {
			if(message.hasName("setTableItem") && message.arity == 4) {
				String itemId = message.args[1].strValue();
				for(int i=0;i<components.size();i++) {
					FormElement component = (FormElement)components.elementAt(i);
					if(component.getIdentity().equals(itemId)) {
						setTableItem(component,message);
						processed = true;
					}
				}
			} else if (message.hasName("setTableColumnWidth") && message.arity == 3) {
				setTableColumnWidth(message);
				processed = true;
			} else if (message.hasName("setTableColumnTitle") && message.arity == 3) {
				setTableColumnTitle(message);
				processed = true;
			} else if (message.hasName("newButton") && message.arity == 7) {
				components.addElement(newButton(message));
				processed = true;
			} else if (message.hasName("newText") && message.arity == 5) {
				components.addElement(newText(message));
				processed = true;
			} else if (message.hasName("newTextField")
					&& (message.arity == 7 || message.arity == 8)) {
				components.addElement(newTextField(message));
				processed = true;
			} else if (message.hasName("newComboBox") && message.arity == 6) {
				components.addElement(newComboBox(message));
				processed = true;
			//} else if (message.hasName("newList") && message.arity == 6) {
			//	components.addElement(newList(message));
			//	processed = true;
			} else if (message.hasName("newCheckBox") && message.arity == 5) {
				components.addElement(newCheckBox(message));
				processed = true;
			//} else if (message.hasName("newTree") && message.arity == 7) {
			//	components.addElement(newTree(message));
			//	processed = true;
			//} else if (message.hasName("newTable") && message.arity == 8) {
			//	components.addElement(newTable(message));
			//	processed = true;
			} else if(message.hasName("addTableRow") && message.arity == 2) {
				addRow(message);
				processed = true;
			} else if(message.hasName("deleteTableRow") && message.arity == 2) {
				deleteRow(message);
				processed = true;
			} else if(message.hasName("delete") && message.arity == 1) {
				delete(message);
				processed = true;
			} else if(ComponentCommandHandler.processMessage(table,message))
			    return true;
		}
		else if(broadcastCommand(message)) {
			processed = true;
		}
		if (processed)
			return true;
		return super.processMessage(message);
	}
	
	public boolean broadcastCommand(Message message) {
		for(int i=0;i<components.size();i++) {
			FormElement component = (FormElement)components.elementAt(i);
			if(component.processMessage(message))
				return true;
		}
		return false;
	}
	
	public Value broadcastCall(Message message) {
		for(int i=0;i<components.size();i++) {
			FormElement component = (FormElement)components.elementAt(i);
			Value v = component.processCall(message);
			if(v != null)
				return v;	
		}
		return null;
	}
	
	public void setTableItem(FormElement component, Message message) {
		int col = message.args[2].intValue;
		int row = message.args[3].intValue;
		Control control = component.getControl();
		TableItem item = table.getItem(row);
		TableEditor editor = this.getEditor(col,row);
		editor.grabHorizontal = true;
		Control oldControl = editor.getEditor();
		if (oldControl != null)
			oldControl.dispose();
		editor.setEditor(control, item, col);
	}

	public void setTableColumnWidth(Message message) {
		int col = message.args[1].intValue;  
		int width = message.args[2].intValue;
		table.getColumn(col).setWidth(width);
	}

	public void setTableColumnTitle(Message message) {
		int col = message.args[1].intValue;  
		String title = message.args[2].strValue();
		table.getColumn(col).setText(title);
	}

	public FormButton newButton(Message message) {
		String buttonID = message.args[1].strValue();    
		String text = message.args[2].strValue();
		int x = message.args[3].intValue;
		int y = message.args[4].intValue;
		int width = message.args[5].intValue;
		int height = message.args[6].intValue;
		FormButton button = new FormButton(table,buttonID,handler);	 	
		button.setText(text);
		button.setBounds(x,y,width,height);
		return button;
	}
	
	public FormText newText(Message message) {
		String textID = message.args[1].strValue();    
		String textString = message.args[2].strValue();
		int x = message.args[3].intValue;
		int y = message.args[4].intValue;
		FormText text = new FormText(table,textID,handler);
		text.setText(textString);
		text.setLocation(new Point(x,y));
		text.calculateSize();
		return text;
	}

	public FormTextField newTextField(Message message) {
		String fieldID = message.args[1].strValue();
		String text = "";
		int x;
		int y;
		int width;
		int height;
		boolean editable;
		if(message.arity==7) {
			x = message.args[2].intValue; 
			y = message.args[3].intValue; 
			width = message.args[4].intValue; 
			height = message.args[5].intValue;
			editable = message.args[6].boolValue;
		}
		else {
			text = message.args[2].strValue();
			x = message.args[3].intValue;
			y = message.args[4].intValue;
			width = message.args[5].intValue;
			height = message.args[6].intValue;
			editable = message.args[7].boolValue;		 	
		}
		FormTextField field = new FormTextField(table,fieldID,handler,site);
		field.setBounds(x,y,width,height);
		field.setText(text);
		field.setEditable(editable);
		return field;
	}

	public FormTableComboBox newComboBox(Message message) {
		String comboID = message.args[1].strValue();
		int x = message.args[2].intValue;
		int y = message.args[3].intValue;
		int width = message.args[4].intValue;;
		int height = message.args[5].intValue;
		FormTableComboBox combo = new FormTableComboBox(table,comboID,handler);
		combo.setBounds(x,y,width,height);
		return combo;
	}
	
	public FormList newList(Message message) {
		String listID = message.args[1].strValue();
		int x = message.args[2].intValue;
		int y = message.args[3].intValue;;
		int width = message.args[4].intValue;
		int height = message.args[5].intValue;
		FormList list = new FormList(table,listID,handler);
		list.setBounds(x,y,width,height);
		return list;
	}
	
	public FormCheckBox newCheckBox(Message message) {
		String boxID = message.args[1].strValue();
		int x = message.args[2].intValue;
		int y = message.args[3].intValue;
		boolean editable = message.args[4].boolValue;
		FormCheckBox checkbox = new FormCheckBox(table,boxID,handler);
		checkbox.setLocation(new Point(x,y));
		checkbox.setSelected(editable);
		return checkbox;	
	}
	
	public FormTree newTree(Message message) {
		String treeID = message.args[1].strValue();
		int x = message.args[2].intValue;
		int y = message.args[3].intValue;
		int width = message.args[4].intValue;
		int height = message.args[5].intValue;
		boolean editable = message.args[6].boolValue;
		FormTree tree = new FormTree(table,treeID,handler,site,editable,false);
		tree.setBounds(x,y,width,height);
		return tree;
	}
	
	public FormTable newTable(Message message) {
		String tableID = message.args[1].strValue();
		int x = message.args[2].intValue;
		int y = message.args[3].intValue;;
		int width = message.args[4].intValue;
		int height = message.args[5].intValue;
		int cols = message.args[6].intValue;
		int rows = message.args[7].intValue;
		FormTable subTable = new FormTable(table,tableID,handler,site,cols,rows);
		subTable.setBounds(x,y,width,height);
		return subTable;
	}
	
}
