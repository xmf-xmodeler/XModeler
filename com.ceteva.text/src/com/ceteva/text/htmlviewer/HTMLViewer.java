package com.ceteva.text.htmlviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.EventHandler;
import com.ceteva.menus.MenuBuilder;
import com.ceteva.text.TextPlugin;
import com.ceteva.text.texteditor.TextEditorInput;
import com.ceteva.text.texteditor.TextStorage;

public class HTMLViewer extends EditorPart {
	
	HTMLViewerModel model;
	String identity = "";
	boolean ignoreURL = true;
	Browser browser = null;
	EventHandler handler = null;
	String tooltip = "";
	
	public void delete() {
		TextPlugin textManager = TextPlugin.getDefault();
		IWorkbenchPage page = textManager.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.closeEditor(this,false);
    }
	
	public void doSave(IProgressMonitor monitor) {		
	}

	public void doSaveAs() {
	}
	
	public boolean independentType(LocationEvent event) {
		if(event.location.endsWith("php")) {
		  ignoreURL = true;
		  return true;
		}
		return false;
	}

	public void init(IEditorSite site, IEditorInput iInput) throws PartInitException {
	  this.setSite(site);
	  this.setInput(iInput);
	  if(iInput instanceof TextEditorInput) {
		TextEditorInput input = (TextEditorInput)iInput;
		  try {
			TextStorage storage = (TextStorage)input.getStorage();
			BufferedReader d = new BufferedReader(new InputStreamReader(storage.getContents()));
			try {
			  identity = d.readLine();
			  model = new HTMLViewerModel(identity,null,this);
			} catch(IOException io) {
			  System.out.println(io);	
			}
		  } catch(CoreException cx) {
		  	System.out.println(cx);
		  }
	   }
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public void close(final boolean save) {
		Display display= getSite().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
			  getSite().getPage().closeEditor(HTMLViewer.this, false);
			}
		});
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		browser = new Browser(parent, SWT.NONE);
		LocationListener locationListener = new LocationListener() {
			public void changed(LocationEvent event) {
				ignoreURL = true;
			}

			public void changing(LocationEvent event) {
				if (ignoreURL && !independentType(event)) {
					try {
						String url = URLDecoder.decode(event.location, "UTF-8");
						Message m = handler.newMessage("urlRequest", 2);
						Value v1 = new Value(identity);
						Value v2 = new Value(url);
						m.args[0] = v1;
						m.args[1] = v2;
						handler.raiseEvent(m);
						event.doit = false;
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		browser.addLocationListener(locationListener);
	}
	
	public void dispose() {
		Message m = handler.newMessage("textClosed",1);
		Value v1 = new Value(identity);
		m.args[0] = v1;
		handler.raiseEvent(m);
		MenuBuilder.dispose(getSite());
		model.dispose();
		super.dispose();
	}
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		model.setEventHandler(handler);
	}
	
	public void setFocusInternal() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(this);
	}
	
	public void setHTML(String html) {
		ignoreURL = false;
		browser.setText(html);
	}
	
	public void setURL(String url) {
	    if(browser!=null) {
	      if(!url.startsWith("<")) {
	        if(url.startsWith("help://")) {
	          // open the URL in help system
	          url = url.replaceAll("help://","");
	          if(url.equals("/"))
	        	PlatformUI.getWorkbench().getHelpSystem().displayHelp();
	          else
                PlatformUI.getWorkbench().getHelpSystem().displayHelpResource("/com.ceteva.help/"+url);
	        }
	        else {
	    	  ignoreURL = false;
	          browser.setUrl(url);
	        }
	      }
	      else {
			ignoreURL = false;
	        setHTML(url);
	      }
	    }
	}
	
	public void setName(String title) {
		setPartName(title);
	}
	
	public void setToolTip(String tooltip) {
		this.tooltip = tooltip;
		this.setTitleToolTip(tooltip);
	}
	
	public String getTitleToolTip() {
		return tooltip;
	}

	public void setFocus() {
	}
}