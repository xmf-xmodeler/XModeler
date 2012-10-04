package com.ceteva.forms.views;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

class FormTextBoxConfiguration extends SourceViewerConfiguration {

  private Scanner scanner = null;

  protected Scanner getTagScanner() {
  	if(scanner == null)
  	  scanner = new Scanner();
  	return scanner;
  }
  
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceView) {
  	PresentationReconciler reconciler = new PresentationReconciler();
  	DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getTagScanner());
  	reconciler.setDamager(dr,IDocument.DEFAULT_CONTENT_TYPE);
  	reconciler.setRepairer(dr,IDocument.DEFAULT_CONTENT_TYPE);
  	return reconciler;
  }
}