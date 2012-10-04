package com.ceteva.diagram.editPart;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;
import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.dnd.DragRequest;
import com.ceteva.diagram.editPolicy.NodeConnectionPolicy;
import com.ceteva.diagram.editPolicy.NodeEditPolicy;
import com.ceteva.diagram.figure.NodeFigure;
import com.ceteva.diagram.model.Edge;
import com.ceteva.diagram.model.Node;
import com.ceteva.diagram.palette.PopupBarEditPolicy;
import com.ceteva.diagram.preferences.IPreferenceConstants;
import com.ceteva.diagram.tracker.NodeSelectionTracker;

public class NodeEditPart extends CommandEventEditPart implements org.eclipse.gef.NodeEditPart {
	
    public void activate() {
    	super.activate();
    	this.getViewer().addDragSourceListener(new DragRequest(getViewer(),TextTransfer.getInstance(),this));
    }
	
	protected IFigure createFigure() {
        Node node = (Node) getModel();
        NodeFigure figure = new NodeFigure(node.getLocation(), node.getSize(), node.getPorts());
        figure.setLayoutManager(new XYLayout());
        return figure;
    }

    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new NodeConnectionPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new NodeEditPolicy());
        
        // The following role is added to avoid the palette from popping up when the cursor
        // is hovering over a node.  It can be used in future to add context specific hover
        // bars on nodes if required.
        
  	    installEditPolicy("Popup", new PopupBarEditPolicy());
    }
    
	public boolean isSelectable() {
	   return ((Node)getModel()).isSelectable();
	}

    protected List getModelChildren() {
        return ((Node) getModel()).getContents();
    }

    public DragTracker getDragTracker(Request request) {
        return new NodeSelectionTracker(this);
    }
    
    public void refresh() {
    	resetFixedPorts();
    	super.refresh();
    }
    
    public void startRenderRefresh() {
    	this.refresh();
    	List sconnections = getSourceConnections();
    	List tconnections = getTargetConnections();
    	for(int i=0;i<sconnections.size();i++) {
    	  ConnectionEditPart cep = (ConnectionEditPart)sconnections.get(i);
    	  cep.refresh();
    	}
    	for(int i=0;i<tconnections.size();i++) {
      	  ConnectionEditPart cep = (ConnectionEditPart)tconnections.get(i);
      	  cep.refresh();
      	}
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
  	    if(prop.equals("startRender"))
  	    	startRenderRefresh();
        if (prop.equals("locationSize"))
            refreshVisuals();
        else if (prop.equals("color"))
            refreshColor();
        else if (prop.equals("displayChange"))
            refreshChildren();
        else if (prop.equals("refreshPorts"))
            resetFixedPorts();
        else if (prop.equals("targetEdges"))
            refreshTargetConnections();
        else if (prop.equals("sourceEdges"))
            refreshSourceConnections();
        else if (prop.equals("visibilityChange")) {
            this.getFigure().setVisible(!getNodeModel().hidden());
            this.getViewer().deselectAll();
        }
    }
    
    public RGB getFillColor() {
        IPreferenceStore preferences = DiagramPlugin.getDefault().getPreferenceStore();
        return PreferenceConverter.getColor(preferences,IPreferenceConstants.FILL_COLOR);
    }

    public Point getLocation() {
        return ((Node) getModel()).getLocation();
    }

    public Dimension getSize() {
        return ((Node) getModel()).getSize();
    }
    
    public void refreshColor() {
        this.getFigure().setBackgroundColor(ColorManager.getColor(getFillColor()));
    }

    protected void refreshVisuals() {
        Point loc = getLocation();
        Dimension size = getSize();
        Rectangle r = new Rectangle(loc, size);
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), r);
        getFigure().setVisible(!getNodeModel().hidden());
        refreshColor();
    }

    public void resetFixedPorts() {
        getNodeFigure().resetFixedPorts(getNodeModel().getPorts());
    }

    public NodeFigure getNodeFigure() {
        return (NodeFigure) getFigure();
    }

    public Node getNodeModel() {
        return (Node) getModel();
    }

    protected List getModelSourceConnections() {
        return getNodeModel().getSourceEdges();
    }

    protected List getModelTargetConnections() {
        return getNodeModel().getTargetEdges();
    }

    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connEditPart) {
        Edge edge = (Edge) connEditPart.getModel();
        return getNodeFigure().getConnectionAnchor(edge.getSourcePort());
    }

    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        Point pt = new Point(((DropRequest) request).getLocation());
        return ((NodeFigure) getFigure()).getAnchor(pt);
    }

    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connEditPart) {
        Edge edge = (Edge) connEditPart.getModel();
        return getNodeFigure().getConnectionAnchor(edge.getTargetPort());
    }

    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        Point pt = new Point(((DropRequest) request).getLocation());
        return ((NodeFigure) getFigure()).getAnchor(pt);
    }
    
    public void performRequest(Request req) {
    	Node node = (Node)getModel();
    	Object request = req.getType();
    	if(request == RequestConstants.REQ_DIRECT_EDIT)
    	  node.selected(1);	
    	else if(request == RequestConstants.REQ_OPEN)
    	  node.selected(2);   	
    }

    public void preferenceUpdate() {
        refreshColor();
        List children = getChildren();
        for(int i = 0; i < children.size(); i++) {
            CommandEventEditPart part = (CommandEventEditPart) children.get(i);
            part.preferenceUpdate();
        }
        List sourceEdges = this.getSourceConnections();
        for(int i =0; i < sourceEdges.size(); i++ ) {	
          EdgeEditPart edge = (EdgeEditPart) sourceEdges.get(i);
          edge.preferenceUpdate();
        }
    }
}