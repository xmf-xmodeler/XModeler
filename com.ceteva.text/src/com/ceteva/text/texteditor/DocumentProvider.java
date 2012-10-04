package com.ceteva.text.texteditor;

import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;

import com.ceteva.text.TextPlugin;
import com.ceteva.text.highlighting.PartitionScanner;

class DocumentProvider extends AbstractDocumentProvider {
	
	private FastPartitioner partitioner;
	private Document document = new Document();
	private PartitionScanner scanner = new PartitionScanner();
	private Vector partitionTypes = new Vector();
	
	public void addRule(String id,String start,String end) {
		partitionTypes.addElement(id);
		setDocumentPartitioner();
		scanner.addRule(id,start,end);
	}
	
	public void setDocumentPartitioner() {
		if (document instanceof IDocumentExtension3) {
		  IDocumentExtension3 extension3= (IDocumentExtension3) document;
		  String[] s = new String[partitionTypes.size()];
		  for(int i=0;i<partitionTypes.size();i++) {
		    String t = (String)partitionTypes.elementAt(i);
		  	s[i] = t;
		  }
		  partitioner = new FastPartitioner(scanner,s);
		  extension3.setDocumentPartitioner(TextPlugin.PARTITIONER,partitioner);
		  partitioner.connect(document);
		}
	}
	
	protected IDocument createDocument(Object element) {
		partitionTypes.add(IDocument.DEFAULT_CONTENT_TYPE);
		setDocumentPartitioner();
		return document;
	}
	
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		return null;
	}

	protected void doSaveDocument(IProgressMonitor monitor,
		Object element,
        IDocument document,
        boolean overwrite) {
	}
	
	protected IAnnotationModel createAnnotationModel(Object element) {
		AnnotationModel model = new AnnotationModel(); 
		Annotation a = new Annotation("com.ceteva.text.marker", true, "com.ceteva.text.marker");
		model.addAnnotation(a,new Position(5,10));
		return model;
	}

}