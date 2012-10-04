package com.ceteva.diagram.palette;

import org.eclipse.gef.DragTracker;
import org.eclipse.swt.graphics.Image;

public class PopupBarDescriptor {

	/** The action button tooltip */
	private String _tooltip = new String();

	/** The image for the button */
	private Image _icon = null;

	/** The typeinfo used to create the Request for the command */
	private String _identity;

	/** The DracgTracker / Tool associatd with the popup bar button */
	private DragTracker _dragTracker = null;

	/**
	 * constructor
	 * @param s
	 * @param i
	 * @param elementType
	 * @param theTracker
	 */
	public PopupBarDescriptor(String s, Image i, String identity,
			DragTracker theTracker) {
		_tooltip = s;
		_icon = i;
		_dragTracker = theTracker;
		_identity = identity;

	}

	/**
	 * gets the element type associated with this Descriptor
	 * @return element type
	 */
	public final String getIdentity() {
		return _identity;
	}

	/**
	 * gets the icon associated with this Descriptor
	 * @return Image
	 */
	public final Image getIcon() {
		return _icon;
	}

	/**
	 * gets the drag tracker associated with this Descriptor
	 * @return drag tracker
	 */
	public final DragTracker getDragTracker() {
		return _dragTracker;
	}

	/**
	 * gets the tool tip associated with this Descriptor
	 * @return string
	 */
	public final String getToolTip() {
		return _tooltip;
	}

} // end PopupBarDescriptor
