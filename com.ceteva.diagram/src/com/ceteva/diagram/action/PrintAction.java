package com.ceteva.diagram.action;

import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.print.PrintGraphicalViewerOperation;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.ceteva.diagram.Diagram;
import com.ceteva.diagram.editPart.GraphicalPartFactory;
import com.ceteva.diagram.editPart.RootEditPart;

public class PrintAction extends EditorPartAction {

  Diagram diagram = null;	
	
  public PrintAction(IEditorPart editor) {
  	super(editor);
  	diagram = (Diagram)editor;
  }
  
  public boolean calculateEnabled() {
  	return true;
  }
  
  protected void init(){
	super.init();
	setId(ActionFactory.PRINT.getId());
  }

  public void run() {
	boolean antialias = false;  
	if(Diagram.antialias) {
	  antialias = true;
	  Diagram.antialias = false;
	}
	
	int style = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getStyle();
	Shell shell = new Shell((style & SWT.MIRRORED) != 0 ? SWT.RIGHT_TO_LEFT : SWT.NONE);
	GraphicalViewer viewer = new ScrollingGraphicalViewer();
	RootEditPart rep = new RootEditPart();
	viewer.createControl(shell);
	viewer.setEditDomain(new DefaultEditDomain(null));
	viewer.setRootEditPart(rep);
	viewer.setEditPartFactory(new GraphicalPartFactory());
	viewer.setContents(diagram.getDisplayedDiagram());
	viewer.flush();
	rep.deactivate();
		
	int printMode = new PrintModeDialog(shell).open();
	if (printMode == -1)
	  return;
	PrintDialog dialog = new PrintDialog(shell, SWT.NULL);
	PrinterData data = dialog.open();
	if (data != null) {
	  PrintGraphicalViewerOperation op = 
		new PrintGraphicalViewerOperation(new Printer(data), viewer);
			op.setPrintMode(printMode);
			op.run("Printing XMF-Mosaic diagram");
	}
	if(antialias)
	  Diagram.antialias = true;
  }
}
