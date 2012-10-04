package com.ceteva.text.texteditor;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;

public class TextEditorModel extends ClientElement {
	
	private TextEditor editor;
	
	public TextEditorModel(String identity, EventHandler handler,
			TextEditor editor) {
		super(null, handler, identity);
		this.editor = editor;
	}
	
	public void delete() {
		editor.delete();
	}

	public Value processCall(Message message) {
		if (message.arity > 0) {
			if (message.args[0].hasStrValue(getIdentity())) {
				if (message.hasName("getCursorPosition")) {
					return new Value(editor.getCursorPos());
				}
				if (message.hasName("getText")) {
					String text = editor.viewer.getDocument().get();
					return new Value(text);
				}
				if (message.hasName("getTextAt")) {
					int position = message.args[1].intValue;
					int length = message.args[2].intValue;
					String text = editor.viewer.getDocument().get();
					return new Value(text
							.substring(position, position + length));
				}
			}
		}
		return null;
	}

	public boolean processMessage(Message message) {
		if (message.arity > 0) {
			if (message.args[0].hasStrValue(getIdentity())) {
				if (message.hasName("delete") && message.arity == 1) {
				    delete();
			    } else if (message.hasName("setName") && message.arity == 2) {
					String name = message.args[1].strValue();
					editor.setName(name);
					return true;
				} else if (message.hasName("setText") && message.arity == 2) {
					String text = message.args[1].strValue();
					editor.setText(text);
					return true;
				} else if (message.hasName("setTextAt") && message.arity == 4) {
					String text = message.args[1].strValue();
					int position = message.args[2].intValue;
					int length = message.args[3].intValue;
					editor.setTextAt(text, position, length);
					return true;
				} else if (message.hasName("setTooltip") && message.arity == 2) {
					String tooltip = message.args[1].strValue();
					editor.setToolTip(tooltip);
					return true;
				} else if (message.hasName("addWordRule") && message.arity == 3) {
					String word = message.args[1].strValue();
					String color = message.args[2].strValue();
					editor.addWordRule(word, color);
					return true;
				} else if (message.hasName("addMultilineRule")
						&& message.arity == 4) {
					String start = message.args[1].strValue();
					String end = message.args[2].strValue();
					String color = message.args[3].strValue();
					editor.addMultilineRule(start, end, color);
					return true;
				} else if (message.hasName("clearRules") && message.arity == 1) {
					editor.clearRules();
					return true;
				} else if (message.hasName("setDirty") && message.arity == 1) {
					editor.setDirty();
					return true;
				} else if (message.hasName("setClean") && message.arity == 1) {
					editor.setClean();
					return true;
				} else if (message.hasName("setFocus") && message.arity == 1) {
					editor.setFocusInternal();
					return true;
				} else if (message.hasName("addLineHighlight")
						&& message.arity == 2) {
					int line = message.args[1].intValue;
					editor.addHighlight(line - 1);
					return true;
				} else if (message.hasName("clearHighlights")
						&& message.arity == 1) {
					editor.clearHighlights();
					return true;
				} else if (message.hasName("showLine") && message.arity == 2) {
					int line = message.args[1].intValue;
					if (line > 0)
						editor.showLine(line - 1);
					return true;
				} else if (message.hasName("undo") && message.arity == 1) {
					editor.undo();
					return true;
				} else if (message.hasName("redo") && message.arity == 1) {
					editor.redo();
					return true;
				} else if (message.hasName("setCursorPosition")
						&& message.arity == 2) {
					editor.setCursorPos(message.args[1].intValue);
					return true;
				}
			}
		}
		return false;
	}
	  
	
}