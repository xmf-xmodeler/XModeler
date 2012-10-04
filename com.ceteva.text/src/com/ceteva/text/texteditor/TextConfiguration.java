package com.ceteva.text.texteditor;

import java.util.Vector;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import com.ceteva.text.TextPlugin;
import com.ceteva.text.highlighting.ScannerTokens;
import com.ceteva.text.highlighting.SinglelineScanner;

class TextConfiguration extends SourceViewerConfiguration {

  private PresentationReconciler reconciler = new PresentationReconciler();
  private Vector partitionTypes = new Vector();	
  private SinglelineScanner scanner = null;
  private String identity = "";
  // private CustomUndoManager undoManager;
  
	
  static class SingleTokenScanner extends BufferedRuleBasedScanner {
    public SingleTokenScanner(TextAttribute attribute) {
	  setDefaultReturnToken(new Token(attribute));
	}
  }

  public TextConfiguration(String identity) {
  	partitionTypes.add(IDocument.DEFAULT_CONTENT_TYPE);
  	this.identity = identity;
  }
  
  protected SinglelineScanner getTagScanner() {
  	if(scanner == null)
  	  scanner = new SinglelineScanner();
  	return scanner;
  }
  
  public void addPartition(IDocument document,String id,String start,String end,String color) {
  	if(reconciler!=null) {
  	  partitionTypes.addElement(id);
  	  DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(ScannerTokens.getColour(color))));
  	  dr.setDocument(document);
  	  reconciler.setDamager(dr,id);
      reconciler.setRepairer(dr,id);
  	}
  }
  
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceView) {
  	reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceView));
  	DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getTagScanner());
  	reconciler.setDamager(dr,IDocument.DEFAULT_CONTENT_TYPE);
  	reconciler.setRepairer(dr,IDocument.DEFAULT_CONTENT_TYPE);
  	return reconciler;
  }
  
  public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
    return TextPlugin.PARTITIONER;
  }
  
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
  	String[] s = new String[partitionTypes.size()];
  	for(int i=0;i<partitionTypes.size();i++) {
  	  String t = (String)partitionTypes.elementAt(i);
  	  s[i] = t;
  	}
  	return s;
  }
  
  /* public void setEventHandler(EventHandler handler) {
  	if(undoManager==null)
  		undoManager = new CustomUndoManager(10,identity);
  	undoManager.setEventHandler(handler);
  } */
  
  /* public IUndoManager getUndoManager(ISourceViewer sourceViewer) {
  	if(undoManager==null) {
  		undoManager = new CustomUndoManager(10,identity);
  	}
  	return undoManager;
  } */
}
