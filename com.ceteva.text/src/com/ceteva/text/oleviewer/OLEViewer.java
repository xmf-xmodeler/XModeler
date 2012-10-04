package com.ceteva.text.oleviewer;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import com.ceteva.text.stubs.OLE;
import com.ceteva.text.stubs.OleClientSite;
import com.ceteva.text.stubs.OleFrame;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.menus.MenuBuilder;

public class OLEViewer extends EditorPart {
	
	String identity = "";
	String file = "";
	String type = "";
	OleClientSite site = null;
	OLEViewerModel model = null;
	EventHandler handler = null;
	
	public void dispose() {
		Message m = handler.newMessage("oleClosed",1);
		Value v1 = new Value(identity);
		m.args[0] = v1;
		handler.raiseEvent(m);
		MenuBuilder.dispose(getSite());
		model.dispose();
		super.dispose();
	}

	public void doSave(IProgressMonitor arg0) {
		System.out.println(file);
		File f = new File(file);
		site.save(f,true);
	}

	public void doSaveAs() {
		System.out.println(file);
		site.save(new File(file),true);
	}
		  
	public void init(IEditorSite iSite, IEditorInput iInput) throws PartInitException {
		setSite(iSite);
		setInput(iInput);
		if (iInput instanceof OLEViewerInput) {
			OLEViewerInput input = (OLEViewerInput)iInput;
			this.identity = input.getIdentity();
			this.file = input.getFile();
			this.type = input.getType();
		}
		model = new OLEViewerModel(identity,null,this);
	}

	public boolean isDirty() {
		return site.isDirty();
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void createPartControl(Composite parent) {
		parent.setLayout (new org.eclipse.swt.layout.FillLayout ());
		OleFrame frame = new OleFrame (parent, SWT.NONE);
		if(!file.equals("")) {
		  File f = new File(file);
		  site = new OleClientSite(frame, org.eclipse.swt.SWT.NONE, type, f);
		}
		else
		  site = new OleClientSite(frame, org.eclipse.swt.SWT.NONE, type);
		site.doVerb(OLE.OLEIVERB_SHOW);
	}
	
	public void saveAs(String filename) {
		site.save(new File(filename),true);
	}
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}

	public void setFocus() {
	}
		
}