package com.ceteva.text.texteditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.ColorManager;
import com.ceteva.client.EventHandler;
import com.ceteva.menus.MenuBuilder;
import com.ceteva.text.TextPlugin;
import com.ceteva.text.highlighting.SinglelineScanner;
import com.ceteva.text.preferences.IPreferenceConstants;

public class TextEditor extends AbstractTextEditor implements IPropertyChangeListener, IPartListener2 {

  public int partitionNumber = 0;	
	
  TextEditorModel model;
  String identity = "";
  String tooltip = "";
  boolean editable = true;
  boolean changed = false;
  String originalName = "";
  DocumentProvider dprovider;
  SourceViewer viewer;
  CursorLinePainter cursorPainter;
  TextConfiguration configuration;
  SourceViewerDecorationSupport fSourceViewerDecorationSupport;
  SinglelineScanner scanner;
  EventHandler handler;
  Vector highlights = new Vector();
  
  boolean showNumbers = false;
  Color currentLineColor = null;
  Color highlightedLineColor = null;
  
  public TextEditor() {
  	getPreferences();
  	registerAsListener();
  }
  
  public void delete() {
	TextPlugin textManager = TextPlugin.getDefault();
	IWorkbenchPage page = textManager.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	page.closeEditor(this,false);
  }
  
  public void registerAsListener() {
	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
  	page.addPartListener(this);
  	IPreferenceStore preference = TextPlugin.getDefault().getPreferenceStore();
	preference.addPropertyChangeListener(this);
  }
  
  public void removeListener() {
    getSite().getPage().removePartListener(this);
    IPreferenceStore preference = TextPlugin.getDefault().getPreferenceStore();
	preference.removePropertyChangeListener(this);
  } 
  
  public void getPreferences() {
  	IPreferenceStore preference = TextPlugin.getDefault().getPreferenceStore();
	showNumbers = preference.getBoolean(IPreferenceConstants.LINE_NUMBERS);
	IPreferenceStore ipreferences = TextPlugin.getDefault().getPreferenceStore();
	RGB color = PreferenceConverter.getColor(ipreferences,IPreferenceConstants.CURRENT_LINE_COLOR);
	currentLineColor = ColorManager.getColor(color);
	color = PreferenceConverter.getColor(ipreferences,IPreferenceConstants.HIGHLIGHT_LINE_COLOR);
	highlightedLineColor = new Color(Display.getDefault(),color);
  }
  
  public void propertyChange(PropertyChangeEvent event) {
  	getPreferences();
  	setLineNumbers();
  	setLineColor();
  	setHighlightColor();
  }

  public void init(IEditorSite iSite, IEditorInput iInput) throws PartInitException {
  	super.init(iSite,iInput);
	setSite(iSite);
	setInput(iInput);
	if(iInput instanceof TextEditorInput) {
		TextEditorInput input = (TextEditorInput)iInput;
		  try {
			TextStorage storage = (TextStorage)input.getStorage();
			BufferedReader d = new BufferedReader(new InputStreamReader(storage.getContents()));
			try {
			  identity = d.readLine();
			  model = new TextEditorModel(identity,null,this);
			} catch(IOException io) {
			  System.out.println(io);	
			}
		  } catch(CoreException cx) {
		  	System.out.println(cx);
		  }
	}
  }
  
  public EventHandler getEventHandler() {
  	return handler;
  }
  
  public void setDirty() {
  	if(!changed) {
  	  changed = true;
  	  originalName = getPartName();
  	  setPartName(" "+originalName);
     Message m = handler.newMessage("textDirty",2);
     Value v1 = new Value(getIdentity());
     Value v2 = new Value(true);
     m.args[0] = v1;
     m.args[1] = v2;
     handler.raiseEvent(m);
  	}
  }
  
  public void setClean() {
  	if(changed) {
  	  changed = false;
  	  setPartName(originalName);
  	  clearHighlights();
     Message m = handler.newMessage("textDirty",2);
     Value v1 = new Value(getIdentity());
     Value v2 = new Value(false);
     m.args[0] = v1;
     m.args[1] = v2;
     handler.raiseEvent(m);
  	}
  }
  
  public void addHighlight(int line) {
  	int lines = viewer.getTextWidget().getLineCount();
  	if(line >= 0 && line < lines) {
  	  // removeCursorPainter();
  	  HighlightLine h = new HighlightLine(viewer,line,highlightedLineColor);
  	  highlights.add(h);
  	  // addCursorPainter();
  	}
  }
  
  public void setCursorPos(int position) {
  	StyledText textWidget = viewer.getTextWidget();
  	textWidget.setCaretOffset(position);
  }
  
  public int getCursorPos() {
  	StyledText textWidget = viewer.getTextWidget();
    int pos = textWidget.getCaretOffset();
    return pos;
  }
  
  public void showLine(int line) {
  	StyledText textWidget = viewer.getTextWidget();
  	int lines = textWidget.getLineCount();
  	if(line >= 0 && line < lines) {
  	  int offset = textWidget.getOffsetAtLine(line);
  	  this.selectAndReveal(offset,0);
  	}
  }
  
  public void clearHighlights() {
  	// removeCursorPainter();
  	for(int i=0;i<highlights.size();i++) {
  	  HighlightLine h = (HighlightLine)highlights.elementAt(i);
  	  h.disable();
  	}
  	highlights = new Vector();
  	// addCursorPainter();
  }
  
  /* public void addCursorPainter() {
  	if(cursorPainter == null) {
  	  ITextViewerExtension2 extension= (ITextViewerExtension2)viewer;
  	  cursorPainter = new CursorLinePainter(viewer);
      cursorPainter.setHighlightColor(currentLineColor);
      extension.addPainter(cursorPainter);
  	}
  	else {
  	  cursorPainter.deactivate(false);	
  	}
  }
  
  public void removeCursorPainter() {
  	// ITextViewerExtension2 extension= (ITextViewerExtension2)viewer;
  	// cursorPainter.deactivate(true);
  } */
  
  protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
  	viewer = new SourceViewer(parent,ruler,styles);
  	configuration = new TextConfiguration(identity);
  	setSourceViewerConfiguration(configuration);
  	// addCursorPainter();
    viewer.addTextListener(new ITextListener() {
      public void textChanged(TextEvent event) {
      	String currentText = viewer.getTextWidget().getText();
      	if(event.getText()!=null) {
          if(!event.getText().equals(currentText)) {
      	  	setDirty();
      	  }
      	}
      }
    });
  	return viewer;
  }
  
  protected SourceViewerDecorationSupport getSourceViewerDecorationSupport(ISourceViewer viewer) {
	if (fSourceViewerDecorationSupport == null) {
		fSourceViewerDecorationSupport= new SourceViewerDecorationSupport(viewer, null, null, null);
		configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);
	}
	return fSourceViewerDecorationSupport;
  }
  
  protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
	  support.setSymbolicFontName(getFontPropertyPreferenceKey());
  }
  
  protected IVerticalRuler createVerticalRuler() {
  	CompositeRuler ruler = new CompositeRuler();
  	if(showNumbers)
      addLineNumberRuler(ruler);
  	else
  	  addDummyRuler(ruler);
  	return ruler;
  }
  
  public void addLineNumberRuler(CompositeRuler ruler) {
  	LineNumberRulerColumn fLineNumberRulerColumn = new LineNumberRulerColumn();
	ruler.addDecorator(1, fLineNumberRulerColumn);
  }
  
  public void addDummyRuler(CompositeRuler ruler) {
  	AnnotationRulerColumn ann = new AnnotationRulerColumn(10);
	ruler.addDecorator(1, ann);
  }
  
  private void showLineNumberRuler() {
    IVerticalRuler v = getVerticalRuler();
	if (v instanceof CompositeRuler) {
	  CompositeRuler ruler = (CompositeRuler)v;
	  ruler.removeDecorator(0);
	  addLineNumberRuler(ruler);
	}
  }
	
  private void hideLineNumberRuler() {
  	IVerticalRuler v = getVerticalRuler();
	if (v instanceof CompositeRuler) {
	  CompositeRuler ruler = (CompositeRuler)v;
	  ruler.removeDecorator(0);
	  addDummyRuler(ruler);
	}
  }
  
  public void setLineNumbers() {
  	if(showNumbers)
  	  showLineNumberRuler();
  	else
  	  hideLineNumberRuler();
  }
  
  public void setLineColor() {
  	if(cursorPainter != null) {
  	  cursorPainter.setHighlightColor(currentLineColor);
  	  cursorPainter.paint(IPainter.CONFIGURATION);
  	}
  }
  
  public void setHighlightColor() {
  	// removeCursorPainter();
    for(int i=0;i<highlights.size();i++) {
      HighlightLine h = (HighlightLine)highlights.elementAt(i);
      h.changeColor(highlightedLineColor);
    }
    // addCursorPainter();
  }
  
  public IDocumentProvider getDocumentProvider() {
  	if(dprovider == null)
  	  dprovider = new DocumentProvider();	
  	return dprovider;
  }
  
  public boolean isEditorInputModifiable() {
  	return editable;
  }
  
  public boolean isEditorInputReadOnly() {
  	return false;
  }
  
  public boolean isEditable() {
  	return editable;
  }
  
  public boolean isDirty() {
  	return changed;
  }
  
  public void setName(String name) {
  	setPartName(name);
  }
  
  public void setToolTip(String tooltip) {
  	this.tooltip = tooltip;
  	this.setTitleToolTip(tooltip);
  }
  
  public String getTitleToolTip() {
  	return tooltip;
  }
  
  public void setEditable(boolean editable) {
  	this.editable = editable;
  	this.validateEditorInputState();
  }
  
  public void setEventHandler(EventHandler handler) {
  	this.handler = handler;
  	// configuration.setEventHandler(handler); // for undo events
  	model.setEventHandler(handler);
  }
  
  public void setImage(Image icon) {
  	setTitleImage(icon);
  }
  
  public void setTextAt(String text,int cursorPosition,int length) {
  	if(viewer!=null) {
  	  Document document = (Document)viewer.getDocument();
  	  try {
  	    document.replace(cursorPosition,length,text);
  	  }
  	  catch(BadLocationException ex) {
  	  	System.out.println(ex);
  	  }
  	}
  }
  
  public void setText(String text) {
  	if(viewer!=null) {
  	  Document document = (Document)viewer.getDocument();
  	  try {
  	  	document.set(text);
  	  }
  	  catch(Exception ex) {
  	  	System.out.println(ex);
  	  }
  	  viewer.refresh();
  	} 
  }
  
  public String getIdentity() {
  	return identity;
  }
  
  public SinglelineScanner getScanner() {
  	if(scanner == null) 
      scanner = configuration.getTagScanner();
  	return scanner;
  }
  
  public void addWordRule(String word,String color) {
    getScanner().addRule(word,color);
  }
  
  public void addMultilineRule(String start,String end,String color) {
  	if(configuration!=null) {
  	  String id = "partition" + (partitionNumber++);
  	  configuration.addPartition(viewer.getDocument(),id,start,end,color);
  	  dprovider.addRule(id,start,end);
  	}
  }
 
  public void clearRules() {
    getScanner().clearRules();
  }
  
  protected void editorContextMenuAboutToShow(IMenuManager menu) {
	IWorkbenchPartSite iwps = this.getSite();
	MenuBuilder.calculateMenu(identity,menu,iwps);
  	menu.add(new Separator("DocumentManagement"));
	menu.add(new Separator(ITextEditorActionConstants.GROUP_COPY));			
	menu.add(new Separator(ITextEditorActionConstants.GROUP_FIND));	
	menu.add(new Separator(ITextEditorActionConstants.GROUP_ADD));
	menu.add(new Separator(ITextEditorActionConstants.MB_ADDITIONS));
	addAction(menu, ITextEditorActionConstants.GROUP_COPY, ITextEditorActionConstants.CUT);
	addAction(menu, ITextEditorActionConstants.GROUP_COPY, ITextEditorActionConstants.COPY);
	addAction(menu, ITextEditorActionConstants.GROUP_COPY, ITextEditorActionConstants.PASTE);
	addAction(menu, ITextEditorActionConstants.GROUP_FIND, ITextEditorActionConstants.FIND);
	addAction(menu, ITextEditorActionConstants.GROUP_FIND, ITextEditorActionConstants.FIND_NEXT);
	addAction(menu, ITextEditorActionConstants.GROUP_FIND, ITextEditorActionConstants.FIND_PREVIOUS);
  }

  public void doSave(org.eclipse.core.runtime.IProgressMonitor progressMonitor) {
  	Message m = handler.newMessage("saveText",2);
	Value v1 = new Value(getIdentity());
	Value v2 = new Value(viewer.getDocument().get());
	m.args[0] = v1;
	m.args[1] = v2;
	handler.raiseEvent(m);
  }

  public void dispose() {
  	removeListener();
	Message m = handler.newMessage("textClosed",1);
	Value v1 = new Value(getIdentity());
	m.args[0] = v1;
	handler.raiseEvent(m);
	MenuBuilder.dispose(getSite());
	model.dispose();
  	super.dispose();
  }
  
  public void undo() {
  	IAction action = getAction(ITextEditorActionConstants.UNDO);
  	action.run();
  }
  
  public void redo() {
  	IAction action = getAction(ITextEditorActionConstants.REDO);
  	action.run();
  }
  
  public void partActivated(IWorkbenchPartReference ref) {
  	if(ref.getPart(false).equals(this) && handler != null) {
  	  Message m = handler.newMessage("focusGained",1);
  	  Value v1 = new Value(getIdentity());
  	  m.args[0] = v1;
  	  handler.raiseEvent(m);
    }
  }

  public void partBroughtToTop(IWorkbenchPartReference ref) {	 
  }

  public void partClosed(IWorkbenchPartReference partRef) {	
  }

  public void partDeactivated(IWorkbenchPartReference ref) {
  	if(ref.getPart(false) != null && handler != null) {
      if(ref.getPart(false).equals(this) && handler != null) {
      	Message m = handler.newMessage("focusLost",1);
    	Value v1 = new Value(getIdentity());
    	m.args[0] = v1;
    	handler.raiseEvent(m);
      }
  	}
  }

  public void partOpened(IWorkbenchPartReference partRef) {	
  }

  public void partHidden(IWorkbenchPartReference partRef) {
  }

  public void partVisible(IWorkbenchPartReference partRef) {
  }

  public void partInputChanged(IWorkbenchPartReference partRef) {
  }
  
  public void setFocusInternal() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		  .getActivePage().activate(this);
  }
}