package com.ceteva.diagram;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ceteva.client.ClientElement;
import com.ceteva.diagram.clipboard.ImageDataTransfer;
import com.ceteva.diagram.editPart.GraphicalPartFactory;
import com.ceteva.diagram.editPart.RootEditPart;
import com.ceteva.diagram.figure.DiagramFigure;
import com.ceteva.diagram.model.AbstractDiagram;
import com.ceteva.diagram.model.Diagram;

public class ImageProducer {
    
    public static int borderSize = 20; // the border to add to the bottom and right of the exported figure
    public static Clipboard clipboard = null;
    public static ImageLoader imageLoader = new ImageLoader();

    public static void createImage(String filename,AbstractDiagram diagram, String format) {
    	ClientElement parent = diagram;
    	
  	    while(!(parent instanceof Diagram))
  	      parent = parent.getParent();
  	    Diagram di = (Diagram)parent;
  	    
  	    if(di.getOwner() != null) {
  	    	com.ceteva.diagram.Diagram diag = (com.ceteva.diagram.Diagram)di.getOwner();
  	    	diag.activate(false);
  	    }
  	    
  	    AbstractDiagram displayedDiagram = di.getDisplayedDiagram();
  	    di.zoomTo(diagram,false);
  	    writeImage(filename,diagram,format);
  	    di.zoomTo(displayedDiagram,false);
  	    
  	    if(di.getOwner() != null) {
	    	com.ceteva.diagram.Diagram diag = (com.ceteva.diagram.Diagram)di.getOwner();
	    	diag.activate(true);
	    }
  	    
    }
    
	public static void createImage(String filename,IFigure figure, String format) {
		int swtFormat = SWT.IMAGE_JPEG; // default
		if(format.equals("BMP") || format.equals("bmp"))
		  swtFormat = SWT.IMAGE_BMP;
		if(format.equals("GIF") || format.equals("gif"))
		  swtFormat = SWT.IMAGE_GIF;
		if(format.equals("ICO") || format.equals("ico"))
		  swtFormat = SWT.IMAGE_ICO;
		if(format.equals("PNG") || format.equals("png"))
		  swtFormat = SWT.IMAGE_PNG;
		createImage(filename,figure,swtFormat);
	}

	/**
	  * Returns the bytes of an encoded image for the specified
	  * IFigure in the specified format.
	  *
	  * @param figure the Figure to create an image for.
	  * @param format one of SWT.IMAGE_BMP, SWT.IMAGE_BMP_RLE, SWT.IMAGE_GIF
	  *          SWT.IMAGE_ICO, SWT.IMAGE_JPEG, or SWT.IMAGE_PNG
	  * @return the bytes of an encoded image for the specified Figure
	  */
	
	public static void createImage(String filename,IFigure figure, int format) {
		ImageData data = getImageData(figure,format);
        imageLoader.data = new ImageData[] {data};
        try {
          FileOutputStream fos = new FileOutputStream(filename);
          imageLoader.save(fos, format);
          fos.close();
          System.gc();
        }
        catch(FileNotFoundException fnf) {
        }
        catch(IOException iox) {
        }
	}
	
    public static void copyToClipboard(IFigure figure) {
        if(clipboard!=null)
          clipboard.dispose();
    	clipboard = new Clipboard(Display.getDefault());
        ImageData d = getImageData(figure, SWT.IMAGE_BMP);
        clipboard.setContents(new Object[] {
            d }, new Transfer[] {
            ImageDataTransfer.getInstance() });
    }
	
	public static ImageData getImageData(IFigure figure,int format) {
		 Device device = Display.getDefault();
	     Rectangle client = figure.getClientArea();
		 Point p = findFigureBounds(figure);
		 int x = client.x;
		 int y = client.y;
		 int width = -client.x + p.x;
		 int height = -client.y + p.y;
		 Rectangle r = new Rectangle(x,y,width,height);
	     Image image = null;
	     GC gc = null;
	     Graphics g = null;
	     try {
	         image = new Image(device, r.width + borderSize, r.height + borderSize);
	         gc = new GC(image);
	         g = new SWTGraphics(gc);
	         g.translate(r.x * -1, r.y * -1);
	         figure.paint(g);
	         return image.getImageData();
	         
	     } finally {
	         if (g != null) {
	             g.dispose();
	         }
	         if (gc != null) {
	             gc.dispose();
	         }
	         if (image != null) {
	             image.dispose();
	         }
	     }
	}
	
	public static Point findFigureBounds(IFigure figure) {
		Point p = new Point();
		IFigure diagramFigure = getDiagramFigure(figure);
		if(diagramFigure != null) {
		  List children = diagramFigure.getChildren();
		  for(int i=0;i<children.size();i++) {
	        IFigure child = (IFigure)children.get(i);
	        int cx = child.getBounds().getBottomRight().x;
	        int cy = child.getBounds().getBottomRight().y;
	        p.x = Math.max(p.x, cx);
	        p.y = Math.max(p.y, cy);
		  }
		}
		else
		  p.setLocation(0,0);
		return p;
	}
	
	public static IFigure getDiagramFigure(IFigure figure) {
		if(!(figure instanceof DiagramFigure)) {
			List children = figure.getChildren();
			if(children.size()>=1) {
			  IFigure child = (IFigure)children.get(0);
			  return getDiagramFigure(child);
			}
			else
			  return null;
		}
		return figure;
	}
	
	private static void writeImage(String filename,AbstractDiagram diagram, String format) {
		int style = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getStyle();
		Shell shell = new Shell((style & SWT.MIRRORED) != 0 ? SWT.RIGHT_TO_LEFT : SWT.NONE);
		GraphicalViewer viewer = new ScrollingGraphicalViewer();
		viewer.createControl(shell);
		viewer.setEditDomain(new DefaultEditDomain(null));
		viewer.setRootEditPart(new RootEditPart());
		viewer.setEditPartFactory(new GraphicalPartFactory());
		viewer.setContents(diagram);
		viewer.flush();
		diagram.refreshZoom();
		LayerManager lm = (LayerManager)viewer.getEditPartRegistry().get(LayerManager.ID);
		IFigure figure = lm.getLayer(LayerConstants.PRINTABLE_LAYERS);
		createImage(filename,figure,format);
		viewer.getRootEditPart().deactivate();
	}
}
