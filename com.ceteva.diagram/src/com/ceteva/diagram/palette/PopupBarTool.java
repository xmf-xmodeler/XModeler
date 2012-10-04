package com.ceteva.diagram.palette;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.CreateRequest;

import com.ceteva.diagram.ToolFactory;

public class PopupBarTool extends AbstractPopupBarTool implements DragTracker {

	/**
	 * When creating shapes on a dgrm using the abar, we do not want to cover
	 * the new shape with the abar, so we offset the creation pnt by a y-offset
	 * 32 is not not arbitrary it is 2x16 which is the height of an icon. and 2
	 * pixels bigger than the height of an action-bar row.
	 */
	static private int Y_OFFSET = 32;

	/**
	 * constructor
	 * 
	 * @param epHost
	 *            the host edit part for this tool
	 * @param elementType
	 */
	public PopupBarTool(EditPart epHost, String elementType) {
		super(epHost, elementType);
	}

	/**
	 * constructor
	 * 
	 * @param epHost
	 * @param theRequest
	 *            the create request to be used
	 */
	public PopupBarTool(EditPart epHost, CreateRequest theRequest) {
		super(epHost, theRequest);
	}

	/**
	 * @see org.eclipse.gef.tools.TargetingTool#createTargetRequest()
	 */
	protected Request createTargetRequest() {

		/* if we have a request, use it */
		if (getCreateRequest() != null) {
			return getCreateRequest();
		}

		/* return CreateViewRequestFactory.getCreateShapeRequest(getElementType(),
				getPreferencesHint()); */
		
		CreateRequest r = new CreateRequest();
		String toolIdentity = (String)getElementType();
		r.setType(REQ_CREATE);
		r.setFactory(new ToolFactory(toolIdentity));
		return r;
	}

	/**
	 * First tries to get a command based on the target request (a create view
	 * and element request). If this fails, tries to get a command with a
	 * request to create an element only.
	 * 
	 * @see org.eclipse.gef.tools.TargetingTool#getCommand()
	 */
	protected Command getCommand() {
		
		
		Request theRequest = this.getTargetRequest();

		if (theRequest instanceof CreateRequest) {
			Point thePoint = this.getCurrentInput().getMouseLocation().getCopy();
			thePoint.y += Y_OFFSET;
			((GraphicalEditPart)getHost()).getFigure().translateToAbsolute(thePoint);
			((GraphicalEditPart)getHost()).getFigure().translateToRelative(thePoint);
			((CreateRequest) theRequest).setLocation(thePoint);
		}

		Command theCmd = getHost().getCommand(theRequest);
		// if we return a cmd that cannot execute then later downstream an
		// NPE can be generated.
		if (theCmd != null && theCmd.canExecute()) {
			return theCmd;
		}

		return getCommandToCreateElementOnly();
	}

	/**
	 * Tries to get a command to create a new semantic element only.
	 * 
	 * @return the command if valid; null otherwise
	 */
	private Command getCommandToCreateElementOnly() {
		return UnexecutableCommand.INSTANCE;
	}

	/**
	 * Asks the target editpart to show target feedback and sets the target
	 * feedback flag.
	 */
	protected void showTargetFeedback() {
		// After adding items to the popup bar, the targeting tool sends
		// createViewRequests
		// potentially causing incorrect feedback updates. We must prevent these
		// from
		// getting though.
	}
}
