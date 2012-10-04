package com.ceteva.diagram.editPart;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import com.ceteva.diagram.model.Box;
import com.ceteva.diagram.model.Diagram;
import com.ceteva.diagram.model.Edge;
import com.ceteva.diagram.model.EdgeText;
import com.ceteva.diagram.model.Ellipse;
import com.ceteva.diagram.model.Group;
import com.ceteva.diagram.model.Image;
import com.ceteva.diagram.model.Line;
import com.ceteva.diagram.model.MultilineEdgeText;
import com.ceteva.diagram.model.MultilineText;
import com.ceteva.diagram.model.Node;
import com.ceteva.diagram.model.Shape;
import com.ceteva.diagram.model.Text;

public class GraphicalPartFactory implements EditPartFactory {

  public EditPart createEditPart(EditPart iContext, Object iModel) {
	EditPart editPart = null;
	if(iModel instanceof Diagram)
	  editPart = new DiagramEditPart();
	else if(iModel instanceof Node)
	  editPart = new NodeEditPart();
	else if(iModel instanceof Edge)
	  editPart = new EdgeEditPart();	
	else if(iModel instanceof Box)
	  editPart = new BoxEditPart();
	else if(iModel instanceof Text)
	  editPart = new TextEditPart();
	else if(iModel instanceof MultilineText)
	  editPart = new MultilineTextEditPart();
	else if(iModel instanceof EdgeText)
	  editPart = new EdgeTextEditPart();
	else if(iModel instanceof MultilineEdgeText)
	  editPart = new MultilineEdgeTextEditPart();
	else if(iModel instanceof Line)
	  editPart = new LineEditPart();
	else if(iModel instanceof Ellipse)
	  editPart = new EllipseEditPart();
	else if(iModel instanceof Group) {
	  Group group = (Group)iModel;
	  if(group.isTopLevel())
	  // if(DiagramFocusManager.isTopLevel(group))
	    editPart = new DiagramEditPart();
	  else
	    editPart = new GroupEditPart();
	}
	else if(iModel instanceof Image)
	  editPart = new ImageEditPart();
	else if(iModel instanceof Shape)
	  editPart = new ShapeEditPart();
	if (editPart != null)
	  editPart.setModel(iModel);
	return editPart;
  }
}