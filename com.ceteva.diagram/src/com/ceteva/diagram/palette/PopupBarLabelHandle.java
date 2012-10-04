package com.ceteva.diagram.palette;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Handle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

class PopupBarLabelHandle extends Label implements Handle {
	/**
	 * flag to drawFocus rect around the handle when the mouse rolls over
	 * it
	 */
	private boolean myMouseOver = false;

	private Image myDisabledImage = null;

	/** The dragTracker CreationTool associated with the handle * */
	private DragTracker myDragTracker = null;

	private Image getDisabledImage()
	{
		if (myDisabledImage != null)
			return myDisabledImage;

		Image theImage = this.getIcon();
		if (theImage == null)
			return null;

		myDisabledImage = new Image(Display.getCurrent(), theImage, SWT.IMAGE_DISABLE);
		// imagesToBeDisposed.add(myDisabledImage);
		return myDisabledImage;
	}

	/**
	 * cnostructor
	 * @param tracker
	 * @param theImage
	 */
	public PopupBarLabelHandle(DragTracker tracker, Image theImage) {
		super(theImage);
		myDragTracker = tracker;
		this.setOpaque(true);
		this.setBackgroundColor(ColorConstants.buttonLightest);
        calculateEnabled();
	}

	/**
	 * @see org.eclipse.gef.Handle#getAccessibleLocation()
	 */
	public Point getAccessibleLocation() {
		return null;
	}

	/**
	 * @see org.eclipse.gef.Handle#getDragTracker()
	 */
	public DragTracker getDragTracker() {
		return myDragTracker;
	}

	/**
	 * @see org.eclipse.draw2d.Figure#paintBorder(org.eclipse.draw2d.Graphics)
	 *      paint a focus rectangle for the label if the mouse is inside
	 *      the label
	 */
	protected void paintBorder(Graphics graphics) {
		super.paintBorder(graphics);

		if (myMouseOver) {

			Rectangle area = getClientArea();
			graphics.setForegroundColor(ColorConstants.black);
			graphics.setBackgroundColor(ColorConstants.white);

			graphics.drawFocus(
				area.x,
				area.y,
				area.width - 1,
				area.height - 1);

		}

	}

	/**
	 * @see org.eclipse.draw2d.IFigure#handleMouseEntered(org.eclipse.draw2d.MouseEvent)
	 *      flip myMouseOver bit and repaint
	 */
	public void handleMouseEntered(MouseEvent event) {
        
        calculateEnabled();

		super.handleMouseEntered(event);
		myMouseOver = true;
		repaint();
	}

	/**
	 * @see org.eclipse.draw2d.IFigure#handleMouseExited(org.eclipse.draw2d.MouseEvent)
	 *      flip myMouseOver bit and repaint
	 */
	public void handleMouseExited(MouseEvent event) {

		super.handleMouseExited(event);
		myMouseOver = false;
		repaint();
	}

	/**
	 * @see org.eclipse.draw2d.IFigure#handleMousePressed(org.eclipse.draw2d.MouseEvent)
	 *      set PopupBarEditPolicy.myActionMoveFigure bit so the popup bar
	 *      is not dismissed after creating an item in the editpart
	 * 
	 */
	public void handleMousePressed(MouseEvent event) {

		if (1 == event.button) 
		{
			// this is the flag in PopupBarEditPolicy that
			// prevents the popup bar from dismissing after a new item
			// is added to a shape, which causes the editpart to be
			// resized.
			//setFlag(POPUPBAR_MOVE_FIGURE, true);
			// future: when other tools besides PopupBarTool are
			// used
			// we will need a way in which to call

		}

		super.handleMousePressed(event);
	}

    private void calculateEnabled() {
        if((myDragTracker != null) && (myDragTracker instanceof AbstractPopupBarTool))
        {
            AbstractPopupBarTool abarTool = (AbstractPopupBarTool) myDragTracker;
            setEnabled(abarTool.isCommandEnabled());
        } else {
            setEnabled(true);
        }
    }

	/**
	 * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
	 */
	protected void paintFigure(Graphics graphics) {
		if(!isEnabled())
		{
			Image theImage = getDisabledImage();
			if (theImage != null)
			{
				graphics.translate(bounds.x, bounds.y);
				graphics.drawImage(theImage, getIconLocation());
				graphics.translate(-bounds.x, -bounds.y);
				return;
			}

		}
		super.paintFigure(graphics);

	}
}
