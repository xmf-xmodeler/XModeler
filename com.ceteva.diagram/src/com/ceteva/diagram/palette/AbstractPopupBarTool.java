package com.ceteva.diagram.palette;

import java.util.List;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.tools.TargetingTool;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractPopupBarTool extends TargetingTool implements
		DragTracker {

	private EditPart myHostEditPart = null;

	/** the requested element kind */
	private Object object = null;

	/** the create request to be used (optional) */
	private CreateRequest myRequest = null;

	/**
	 * @param epHost
	 *            the host editpart
	 * @param elementType
	 *            the element type to be created
	 */
	public AbstractPopupBarTool(EditPart epHost, Object object) {
		this.object = object;
		myHostEditPart = epHost;
		lockTargetEditPart(epHost);
	}

	/**
	 * @param epHost
	 *            the host editpart
	 * @param theRequest
	 *            the create request to be used
	 */
	public AbstractPopupBarTool(EditPart epHost, CreateRequest theRequest) {
		myHostEditPart = epHost;
		myRequest = theRequest;
		lockTargetEditPart(epHost);
	}

	/**
	 * @see org.eclipse.gef.tools.TargetingTool#createTargetRequest()
	 */
	abstract protected Request createTargetRequest();

	/**
	 * @see org.eclipse.gef.tools.TargetingTool#getCommand()
	 */
	abstract protected Command getCommand();

	/**
	 * Test to see if the tool's associated cmd is available to be run
	 */
	public boolean isCommandEnabled() {
		Command cmd = this.getCommand();
		if (cmd == null) {
			return false;
		}

		return cmd.canExecute();
	}

	/**
	 * @see org.eclipse.gef.DragTracker#commitDrag()
	 */
	public void commitDrag() {
		// NULL implementation
	}

	protected boolean handleButtonDown(int button) {
		// push the cmd that will be used by performCreation
		setCurrentCommand(getCommand());
		return super.handleButtonDown(button);

	}

	/**
	 * 
	 * @see org.eclipse.gef.tools.AbstractTool#handleButtonUp(int)
	 */
	protected boolean handleButtonUp(int button) {
		performCreation(button);

		return true;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#performCreation(int)
	 * @param button
	 */
	private void performCreation(int button) {
		EditPartViewer viewer = getCurrentViewer();
		Command c = getCurrentCommand();
		executeCurrentCommand();

		/*
		 * List editparts = new ArrayList(1); Collection newObjects =
		 * DiagramCommandStack.getReturnValues(c); for (Iterator i =
		 * newObjects.iterator(); i.hasNext();) { Object o = i.next(); EditPart
		 * editpart = null; if (o instanceof IAdaptable) { IAdaptable adapter =
		 * (IAdaptable) o; View view = (View) adapter.getAdapter(View.class); if
		 * (view != null) editpart = (EditPart) viewer.getEditPartRegistry()
		 * .get(view); } else if (o instanceof EObject) { IGraphicalEditPart
		 * host = (IGraphicalEditPart) myHostEditPart; editpart =
		 * host.findEditPart(host, (EObject) o); } if (editpart != null)
		 * editparts.add(editpart); } selectAddedObject(viewer, editparts);
		 */
	}

	/**
	 * Finds the newly created editpart associated with the element that has
	 * been added and then selects the editpart and requests direct editing
	 * 
	 */
	protected void selectAddedObject(final EditPartViewer viewer,
			final List editparts) {
		if (editparts.isEmpty())
			return;
		// automatically put the first shape into edit-mode
		Display.getCurrent().asyncExec(new Runnable() {

			public void run() {
				viewer.setSelection(new StructuredSelection(editparts));
				EditPart editpart = (EditPart) editparts.get(0);
				editpart.performRequest(new Request(
						RequestConstants.REQ_DIRECT_EDIT));
			}
		});
	}

	/**
	 * HACK in order to lock the editpart for the duration that this tool is
	 * active and avoid updateTargetUnderMouse, getting called we need to
	 * override this method to perform a NOP
	 * 
	 * @see org.eclipse.gef.tools.TargetingTool#unlockTargetEditPart()
	 */
	protected void unlockTargetEditPart() {
		// NULL implementation
	}

	protected Object getElementType() {
		return object;
	}

	protected void setElementKind(Object object) {
		this.object = object;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#getCommandName()
	 */
	protected String getCommandName() {
		return REQ_CREATE;

	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#getDebugName()
	 */
	protected String getDebugName() {
		return "Popup Bar Creation Tool"; //$NON-NLS-1$

	}

	protected EditPart getHost() {
		return myHostEditPart;
	}

	/**
	 * Gets the preferences hint that is to be used to find the appropriate
	 * preference store from which to retrieve diagram preference values. The
	 * preference hint is mapped to a preference store in the preference
	 * registry <@link DiagramPreferencesRegistry>.
	 * 
	 * @return the preferences hint
	 */
	/*
	 * protected PreferencesHint getPreferencesHint() { PreferencesHint
	 * preferencesHint = null; if (myHostEditPart != null) { preferencesHint =
	 * ((IGraphicalEditPart) myHostEditPart) .getDiagramPreferencesHint(); }
	 * return preferencesHint; }
	 */

	/**
	 * Gets the the create request to be used if one was specified in the
	 * constructor.
	 * 
	 * @return Returns the create request.
	 */
	protected CreateRequest getCreateRequest() {
		return myRequest;
	}
}
