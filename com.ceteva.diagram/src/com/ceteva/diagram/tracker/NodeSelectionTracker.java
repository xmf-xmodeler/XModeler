package com.ceteva.diagram.tracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.BendpointRequest;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.LocationRequest;
import org.eclipse.gef.tools.DragEditPartsTracker;

import com.ceteva.diagram.editPart.EdgeEditPart;
import com.ceteva.diagram.editPart.NodeEditPart;
import com.ceteva.diagram.editPolicy.EdgePolicy;

public class NodeSelectionTracker extends DragEditPartsTracker {
	
	public boolean debug = false;

	public NodeSelectionTracker(EditPart sourceEditPart) {
		super(sourceEditPart);
	}
	
	public Command getCommand() {
		CompoundCommand command = new CompoundCommand();
		command.setDebugLabel("Drag Object Tracker");
		Iterator iter = getOperationSet().iterator();
		Request request = getTargetRequest();
		
		if (isCloneActive())
			request.setType(REQ_CLONE);
		else if (isMove())
			request.setType(REQ_MOVE);
		else
			request.setType(REQ_ORPHAN);

		if (!isCloneActive()) {

			if (request.getType() == REQ_MOVE) {
				// get the edges whose waypoints/refpoint we are going to move	
				List edgesToDelta = calculateEdgesToDelta(getOperationSet());
				// create commands to move the node
				while (iter.hasNext()) {
					EditPart editPart = (EditPart) iter.next();
					command.add(editPart.getCommand(request));
				}
				// create commands to move the waypoints/refpoint
				checkWaypoints(request, edgesToDelta, command);
				if (debug) {
					System.out.println("*** COMMAND STACK ***");
					List commands = command.getCommands();
					for (int i = 0; i < commands.size(); i++)
						System.out.println(commands.get(i).getClass());
					System.out.println("*********************");
				}
			} else {
				while (iter.hasNext()) {
					EditPart editPart = (EditPart) iter.next();
					command.add(editPart.getCommand(request));
				}
			}
		}

		if (!isMove() || isCloneActive()) {
			if (!isCloneActive())
				request.setType(REQ_ADD);

			if (getTargetEditPart() == null)
				command.add(UnexecutableCommand.INSTANCE);
			else
				command.add(getTargetEditPart().getCommand(getTargetRequest()));
		}

		return command;
	}

	public List calculateEdgesToDelta(List selectedEditParts) {
		List edgesToDelta = new ArrayList();

		for (int i = 0; i < selectedEditParts.size(); i++) {
			GraphicalEditPart selected = (GraphicalEditPart) selectedEditParts
					.get(i);
			List sourceConnections = selected.getSourceConnections();
			for (int z = 0; z < sourceConnections.size(); z++) {
				EdgeEditPart conn = (EdgeEditPart) sourceConnections.get(z);
				NodeEditPart nep = (NodeEditPart) conn.getTarget();
				for (int j = 0; j < selectedEditParts.size(); j++) {
					NodeEditPart nep2 = (NodeEditPart) selectedEditParts.get(j);
					if (nep.getModelIdentity() == nep2.getModelIdentity())
						edgesToDelta.add(conn);
				}
			}
		}
		return edgesToDelta;
	}

	// if there is an edge between two nodes that are simultaneously been
	// moved then the following method ensures that the edge's waypoints and
	// refpoint are moved similarly

	protected void checkWaypoints(Request request, List edgesToDelta,
			CompoundCommand command) {
		for (int i = 0; i < edgesToDelta.size(); i++) {
			EdgeEditPart conn = (EdgeEditPart) edgesToDelta.get(i);
			int waypoints = conn.getWaypointCount();
			if (waypoints == 0) {

				// move ref point

				LocationRequest req = new LocationRequest();
				req.setType(EdgePolicy.MOVE_REFPOINT);
				Point location = conn.getRefPoint().getCopy();

				conn.getFigure().translateToAbsolute(location);
				location.translate(((ChangeBoundsRequest) request)
						.getMoveDelta());
				conn.getFigure().translateToRelative(location);

				req.setLocation(location);
				command.add(conn.getCommand(req));
			} else {

				// move waypoints	

				for (int y = 0; y < conn.getWaypointCount(); y++) {
					BendpointRequest req = new BendpointRequest();
					req.setType(REQ_MOVE_BENDPOINT);
					req.setSource(conn);
					req.setIndex(y);
					Point location = conn.getWaypointPosition(y);

					conn.getFigure().translateToAbsolute(location);
					location.translate(((ChangeBoundsRequest) request)
							.getMoveDelta());

					req.setLocation(location);
					command.add(conn.getCommand(req));
				}
			}
		}
	}
	
	public boolean ctrlIsPressed() {
		return isCloneActive();
	}

}
