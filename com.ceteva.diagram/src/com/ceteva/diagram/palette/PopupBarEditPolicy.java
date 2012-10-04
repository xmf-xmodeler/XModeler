package com.ceteva.diagram.palette;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Tool;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

import com.ceteva.diagram.DiagramPlugin;
import com.ceteva.diagram.model.ImageManager;

public class PopupBarEditPolicy extends DiagramAssistantEditPolicy {
	
	private class RoundedRectangleWithTail extends RoundedRectangle {
		
		private Image IMAGE_POPUPBAR_PLUS = ImageManager.getImage(DiagramPlugin.getDefault(),"popupbar_plus.gif");
		private Image IMAGE_POPUPBAR = ImageManager.getImage(DiagramPlugin.getDefault(),"popupbar.gif");

		private Image myTailImage = null;

		private boolean bIsInit = false;

		private int myCornerDimension = 6;

		/**
		 * constructor
		 */
		public RoundedRectangleWithTail() {
			// we do not make the myActionTailFigue opaque because it
			// doesn't look good when magnification is set.
			this.setFill(true);
			this.setBackgroundColor(ColorConstants.buttonLightest);
			this.setForegroundColor(ColorConstants.lightGray);
			this.setVisible(true);
			this.setEnabled(true);
			this.setOpaque(true);
		}

		/**
		 * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
		 */
		public void paintFigure(Graphics graphics) {
			
			int shiftWidth = 3;
			Image theTail = getTail();
			Rectangle theBounds = this.getBounds().getCopy();
			theBounds.height -= theTail.getBounds().height;
			theBounds.height -= shiftWidth;// shift slight above cursor
			theBounds.x += shiftWidth; // shift slight to right of cursor
			theBounds.width -= (shiftWidth + 1); // otherwise rhs is clipped

			// fill the round rectangle first since it is opaque
			graphics.fillRoundRectangle(
				theBounds,
				myCornerDimension,
				myCornerDimension);
			graphics.drawRoundRectangle(
				theBounds,
				myCornerDimension,
				myCornerDimension);
			graphics.drawImage(
				theTail,
				theBounds.x + 6,
				theBounds.y + theBounds.height - 1);

		}
		
		private Image getTail()
		{
			if(!bIsInit)
			{
				if(getIsDisplayAtMouseHoverLocation() && !isHostConnection())
				{
					if(myTailImage == null)
					{
						myTailImage = IMAGE_POPUPBAR_PLUS;
						bIsInit = true;
					}
				}
				else
				{
					if(myTailImage == null)
					{
						myTailImage = IMAGE_POPUPBAR;
						bIsInit = true;
					}
				}

			}
			return myTailImage;

		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramAssistantEditPolicy#isDiagramAssistant(java.lang.Object)
	 */
	protected boolean isDiagramAssistant(Object object) {
		return object instanceof RoundedRectangleWithTail
			|| object instanceof PopupBarLabelHandle;
	}

	/**
	 * Adds the popup bar after a delay
	 */
	public void mouseHover(MouseEvent me) {
			// if the cursor is inside the popup bar
			// or the keyboar triggred activation
			// then we do not want to deactivate
			if (!isDiagramAssistant(me.getSource()))
				setAvoidHidingDiagramAssistant(false);
			setMouseLocation(me.getLocation());
			if (getIsDisplayAtMouseHoverLocation())
				showDiagramAssistantAfterDelay(getAppearanceDelayLocationSpecific()); // no
																						// delay
			else if (shouldShowDiagramAssistant()) {
				showDiagramAssistant(getMouseLocation()); // no delay
			}
	}

	/**
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseMoved(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseMoved(MouseEvent me) {

		if(getIsDisplayAtMouseHoverLocation())
			{
			Object srcObj = me.getSource();
			if ((srcObj != null) && srcObj.equals(getHostFigure())) {
				hideDiagramAssistant();
			}
		}
		setAvoidHidingDiagramAssistant(true);
		setMouseLocation(me.getLocation());

		if (!getIsDisplayAtMouseHoverLocation()) {
			// if the cursor is inside the popup bar
			// or the keyboar triggred activation
			// then we do not want to deactivate
			
			if (!isDiagramAssistant(me.getSource()))
				setAvoidHidingDiagramAssistant(false);


			// the event is consumed by the following line
			
			showDiagramAssistantAfterDelay(getAppearanceDelay());
		}
	}

	/**
	 * Listens to the owner figure being moved so the handles can be removed
	 * when this occurs.
	 * 
	 * @author affrantz@us.ibm.com
	 * 
	 */
	private class OwnerMovedListener implements FigureListener {

		private Point myPopupBarLastPosition = new Point(0, 0);

		boolean hasPositionChanged(Rectangle theBounds) {
			if (theBounds.x != myPopupBarLastPosition.x)
				return true;
			if (theBounds.y != myPopupBarLastPosition.y)
				return true;
			return false;
		}

		/**
		 * @see org.eclipse.draw2d.FigureListener#figureMoved(org.eclipse.draw2d.IFigure)
		 */
		public void figureMoved(IFigure source) {
			// for some reason we get more than one
			// figure moved call after compartment items are added
			// myActionMoveFigure handles the first one which we expect
			// hasPositionChanged handles the others caused by the selection of
			// the compartment
			// item.
			if (getFlag(POPUPBAR_MOVE_FIGURE)
				&& hasPositionChanged(source.getBounds())) {
				hideDiagramAssistant(); // without delay
			} else {
				setFlag(POPUPBAR_MOVE_FIGURE, false); // toggle flag back
				Rectangle theBounds = source.getBounds();
				myPopupBarLastPosition.setLocation(theBounds.x, theBounds.y);

			}

		}
	}

	/**
	 * Listens for mouse key presses so the popup bar can be dismissed if the context 
	 * menu is displayed
	 * 
	 * @author affrantz@us.ibm.com
	 */
	private class PopupBarMouseListener extends MouseListener.Stub {

		/**
		 * @see org.eclipse.draw2d.MouseListener#mousePressed(org.eclipse.draw2d.MouseEvent)
		 */
		public void mousePressed(MouseEvent me) {
			if (3 == me.button) // context menu, hide the popup bar
			{
				hideDiagramAssistant();
			}
			super.mousePressed(me);
			setPopupBarOnDiagramActivated(true);
		}
		public void mouseReleased(MouseEvent me)
		{
			super.mouseReleased(me);

		}
	}

	/* ************************* End nested classes ******************** */

	/** Y postion offset from shape where the balloon top begin. */
	static private int BALLOON_Y_OFFSET = 10;

	/** Y postion offset from shape where the balloon top begin. */
	static private double BALLOON_X_OFFSET_RHS = 0.65;

	static private double BALLOON_X_OFFSET_LHS = 0.25;

	/** Y postion offset from shape where the balloon top begin. */
	static private int ACTION_WIDTH_HGT = 30;

	static private int ACTION_BUTTON_START_X = 5;

	static private int ACTION_BUTTON_START_Y = 5;

	static private int ACTION_MARGIN_RIGHT = 10;

	/** popup bar bits */
	static private int POPUPBAR_ACTIVATEONHOVER				= 0x01; /* Display the action when hovering*/
	static private int POPUPBAR_MOVE_FIGURE			 		= 0x02; /* Ignore the first figureMoved event when creating elements inside a shape via a popup bar*/ 
	static private int POPUPBAR_DISPLAYATMOUSEHOVERLOCATION	= 0x04; /* Display the popup bar at the mouse location used by diagrams and machine edit parts*/
	static private int POPUPBAR_ONDIAGRAMACTIVATED				= 0x10; /* For popup bars on diagram and machine edit parts, where we POPUPBAR_DISPLAYATMOUSEHOVERLOCATION, don't display popup bar until user clicks on surface*/
	static private int POPUPBAR_HOST_IS_CONNECTION				= 0x20; /* For popup bars on connection edit parts*/

	/** Bit field for the actrionbar associated bits */
	private int myPopupBarFlags = POPUPBAR_ACTIVATEONHOVER;

	private double myBallonOffsetPercent = BALLOON_X_OFFSET_RHS;

	/** the figure used to surround the action buttons */
	private IFigure myBalloon = null;

	/** The popup bar descriptors for the popup bar buttons */
	private List myPopupBarDescriptors = new ArrayList();

	/** Images created that must be deleted when popup bar is removed */
	protected List imagesToBeDisposed = new ArrayList();

	/** mouse keys listener for the owner shape */
	private PopupBarMouseListener myMouseKeyListener = new PopupBarMouseListener();

	/** listener for owner shape movement */
	private OwnerMovedListener myOwnerMovedListener = new OwnerMovedListener();

	/** flag for whether mouse cursor within shape */

	private void setFlag(int bit, boolean b)
	{
		if (b)
			myPopupBarFlags |= bit;
		else if (getFlag(bit))
			myPopupBarFlags ^= bit;

	}

	private boolean getFlag(int bit)
	{
		return ((myPopupBarFlags & bit) > 0);
	}


	
	private void setPopupBarOnDiagramActivated(boolean bVal)
	{
		setFlag(POPUPBAR_ONDIAGRAMACTIVATED, bVal);
	}
	private boolean getPopupBarOnDiagramActivated()
	{
		return getFlag(POPUPBAR_ONDIAGRAMACTIVATED);
	}

	/**
	 * set the host is connection flag
	 * @param bVal the new value
	 */
	protected void setHostConnection(boolean bVal)
	{
		setFlag(POPUPBAR_HOST_IS_CONNECTION, bVal);
	}

	/**
	 * get the host is connection flag
	 * @return true or false
	 */
	protected boolean isHostConnection()
	{
		return getFlag(POPUPBAR_HOST_IS_CONNECTION);
	}

	/**
	 * Populates the popup bar with popup bar descriptors added by suclassing
	 * this editpolicy (i.e. <code>fillPopupBarDescriptors</code> and by
	 * querying the modeling assistant service for all types supported on the
	 * popup bar of this host. For those types added by the modeling assistant
	 * service the icons are retrieved using the Icon Service.
	 */
	protected void populatePopupBars() {
		fillPopupBarDescriptors();
	}

	/**
	 * This is the entry point that subclasses can override to fill the
	 * popup bar descrioptors if they have customized tools that cannot be done
	 * using the type along with the modeling assistant service.
	 */
	protected void fillPopupBarDescriptors() {
		// subclasses can override.
	}

	private boolean isSelectionToolActive()
	{
		// getViewer calls getParent so check for null
		if(getHost().getParent() != null && getHost().isActive() )
		{
			Tool theTool = getHost().getViewer().getEditDomain().getActiveTool();
			if((theTool != null) && theTool instanceof SelectionTool)
			{
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramAssistantEditPolicy#shouldShowDiagramAssistant()
	 */
	protected boolean shouldShowDiagramAssistant()
	{
		/* if (!super.shouldShowDiagramAssistant()) {
			return false;
		} */

		if(this.getIsDisplayAtMouseHoverLocation())
		{
			if (isHostConnection())
				return isSelectionToolActive();
			if (getPopupBarOnDiagramActivated())
				return isSelectionToolActive();
			return false;
		}
		else
			return isSelectionToolActive();

	}

	/**
	 * allows plugins to add their own popup bar tools and tips
	 * @param elementType
	 * @param theImage
	 * @param theTracker
	 * @param theTip
	 */
	protected void addPopupBarDescriptor(
			String toolIdentity,
			Image theImage,
			DragTracker theTracker,
			String theTip) {

		PopupBarDescriptor desc =
			new PopupBarDescriptor(theTip, theImage, toolIdentity, theTracker);
		myPopupBarDescriptors.add(desc);

	}

	/**
	 * adds popup bar descriptor
	 * @param elementType
	 * @param theImage
	 * @param theTracker
	 */
	protected void addPopupBarDescriptor(
		String toolIdentity,
		Image theImage,
		DragTracker theTracker) {

		// String theInputStr = DiagramUIMessages.PopupBar_AddNew;
		// String theTip = NLS.bind(theInputStr, displayName);
	
		addPopupBarDescriptor(toolIdentity, theImage, theTracker);
	}

	/**
	 * default method for plugins which passes along the PopupBarTool
	 * as the tool to be used.
	 * @param elementType
	 * @param theImage
	 */
	protected void addPopupBarDescriptor(String toolIdentity, Image theImage) {

		this.addPopupBarDescriptor(toolIdentity, theImage,
			new PopupBarTool(getHost(), toolIdentity));

	}

	/**
	 * @param elementType
	 * @param theImage
	 * @param theTip
	 */
	protected void addPopupBarDescriptor(
			String toolIdentity,
			Image theImage,
			String theTip) {

		PopupBarTool theTracker =
			new PopupBarTool(getHost(), toolIdentity);
		PopupBarDescriptor desc =
			new PopupBarDescriptor(theTip, theImage, toolIdentity, theTracker);
		myPopupBarDescriptors.add(desc);

	}

	/**
	 * method used primarily to add UnspecifiedTypeCreationTool
	 * @param elementType
	 * @param theImage
	 * @param theRequest the create request to be used
	 */
	protected void addPopupBarDescriptor(
			String toolIdentity,
			String displayName,
			Image theImage,
			CreateRequest theRequest)
	{

		PopupBarTool theTracker =
			new PopupBarTool(getHost(), theRequest);

		this.addPopupBarDescriptor(toolIdentity, theImage, theTracker, displayName);

	}

	/**
	 * gets the popup bar descriptors
	 * @return list
	 */
	protected List getPopupBarDescriptors() {
		return myPopupBarDescriptors;
	}

	/**
	 * initialize the popup bars from the list of action descriptors.
	 */
	private void initPopupBars() {
		
		List theList = getPopupBarDescriptors();
		if (theList.isEmpty())
			return;
		myBalloon = createPopupBarFigure();

		int iTotal = ACTION_WIDTH_HGT * theList.size() + ACTION_MARGIN_RIGHT;

		getBalloon().setSize(
			iTotal,
			ACTION_WIDTH_HGT + 2 * ACTION_BUTTON_START_Y);

		int xLoc = ACTION_BUTTON_START_X;
		int yLoc = ACTION_BUTTON_START_Y;

		
		for (Iterator iter = theList.iterator(); iter.hasNext();) {
			
			PopupBarDescriptor theDesc = (PopupBarDescriptor) iter.next();

			// Button b = new Button(theDesc.myButtonIcon);
			PopupBarLabelHandle b =
				new PopupBarLabelHandle(
					theDesc.getDragTracker(),
					theDesc.getIcon());
			
			Rectangle r1 = new Rectangle();
			r1.setLocation(xLoc, yLoc);
			xLoc += ACTION_WIDTH_HGT;
			r1.setSize(
				ACTION_WIDTH_HGT,
				ACTION_WIDTH_HGT - ACTION_MARGIN_RIGHT);
			
			Label l = new Label();
			l.setText(theDesc.getToolTip());
			
			b.setToolTip(l);
			b.setPreferredSize(ACTION_WIDTH_HGT, ACTION_WIDTH_HGT);
			b.setBounds(r1);
			getBalloon().add(b);

			b.addMouseMotionListener(this);
			b.addMouseListener(this.myMouseKeyListener);

		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramAssistantEditPolicy#getPreferenceName()
	 */
	String getPreferenceName() {
		// return IPreferenceConstants.PREF_SHOW_POPUP_BARS;
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramAssistantEditPolicy#isDiagramAssistantShowing()
	 */
	protected boolean isDiagramAssistantShowing() {
		return getBalloon() != null;
	}

	private IFigure getBalloon() {
		return myBalloon;
	}

	protected IFigure createPopupBarFigure() {
		return new RoundedRectangleWithTail();
	}

	protected void showDiagramAssistant(Point referencePoint) {
		
		// already have a one
		if (getBalloon() != null && getBalloon().getParent() != null) 
		{
			return;
		}

		if (this.myPopupBarDescriptors.isEmpty()) 
		{
			populatePopupBars();
			initPopupBars();

			if (myPopupBarDescriptors.isEmpty()) {
				return; // nothing to show
			}
		}
		getBalloon().addMouseMotionListener(this);
		getBalloon().addMouseListener(myMouseKeyListener);

		// the feedback layer figures do not recieve mouse events so do not use
		// it for popup bars
		IFigure layer = getLayer(LayerConstants.HANDLE_LAYER);
		
		layer.add(getBalloon());
		
		if (referencePoint == null) {
			referencePoint = getHostFigure().getBounds().getCenter();
		}

		Point thePoint = getBalloonPosition(referencePoint);
		// Point thePoint = referencePoint;
		
		// Note we are bypassing the offset
		
		getBalloon().setLocation(thePoint);

		// dismiss the popup bar after a delay
		if(!shouldAvoidHidingDiagramAssistant())
		{
			hideDiagramAssistantAfterDelay(getDisappearanceDelay());
		}
	}

	/**
	 * getter for the IsDisplayAtMouseHoverLocation flag
	 * @return true or false
	 */
	protected boolean getIsDisplayAtMouseHoverLocation()
	{
		return getFlag(POPUPBAR_DISPLAYATMOUSEHOVERLOCATION);
		// return true;
	}

	/**
	 * setter for the IsDisplayAtMouseHoverLocation
	 * @param bVal
	 */
	protected void setIsDisplayAtMouseHoverLocation(boolean bVal)
	{
		setFlag(POPUPBAR_DISPLAYATMOUSEHOVERLOCATION, bVal);
	}

	/**
	 * For editparts that consume the entire viewport, statechart, structure,
	 * communication, we want to display the popup bar at the mouse location.
	 * @param referencePoint
	 *            The reference point which may be used to determine where the
	 *            diagram assistant should be located. This is most likely the
	 *            current mouse location. 
	 *            @return Point
	 */
	private Point getBalloonPosition(Point referencePoint)
	{
		Point thePoint = new Point();
		boolean atMouse = getIsDisplayAtMouseHoverLocation();
		if (atMouse) {
			thePoint.setLocation(referencePoint);
			getHostFigure().translateToAbsolute(thePoint);
			getBalloon().translateToRelative(thePoint);

			// shift the ballon so it is above the cursor.
			thePoint.y -= ACTION_WIDTH_HGT;

			if (willBalloonBeClipped(thePoint)) {
				Rectangle rcBounds = getHostFigure().getBounds().getCopy();
				getHostFigure().translateToAbsolute(rcBounds);
				getBalloon().translateToRelative(rcBounds);
				Dimension dim = getBalloon().getSize();
				int offsetX = dim.width + ACTION_WIDTH_HGT;
				thePoint.x = rcBounds.right() - offsetX;
			}
		}
		else
		{
			Dimension theoffset = new Dimension();
			Rectangle rcBounds = getHostFigure().getBounds().getCopy();

			getHostFigure().translateToAbsolute(rcBounds);
			getBalloon().translateToRelative(rcBounds);

			theoffset.height = -(BALLOON_Y_OFFSET + ACTION_WIDTH_HGT);
			theoffset.width = (int) (rcBounds.width * myBallonOffsetPercent);

			thePoint.x = rcBounds.x + theoffset.width;
			thePoint.y = rcBounds.y + theoffset.height;
			if (isRightDisplay() && willBalloonBeClipped(thePoint)) {
				this.setLeftHandDisplay();
				theoffset.width = (int) (rcBounds.width * myBallonOffsetPercent);
				thePoint.x = rcBounds.x + theoffset.width;

			}
		}
		return thePoint;
	}

	private boolean willBalloonBeClipped(Point pnt) {
		Control ctrl1 = getHost().getViewer().getControl();
		if (ctrl1 instanceof FigureCanvas) {
			FigureCanvas figureCanvas = (FigureCanvas) ctrl1;
			Viewport vp = figureCanvas.getViewport();
			Rectangle vpRect = vp.getClientArea();
			Dimension dim = getBalloon().getSize();
			if ((pnt.x + dim.width) >= (vpRect.x + vpRect.width)) {
				return true;
			}
		}
		return false;
	}

	private void teardownPopupBar() {
		getBalloon().removeMouseMotionListener(this);
		getBalloon().removeMouseListener(myMouseKeyListener);
		// the feedback layer figures do not recieve mouse events
		IFigure layer = getLayer(LayerConstants.HANDLE_LAYER);
		if (myBalloon.getParent() != null) {
			layer.remove(myBalloon);
		}
		myBalloon = null;

		this.myPopupBarDescriptors.clear();
		setRightHandDisplay(); // set back to default

		for (Iterator iter = imagesToBeDisposed.iterator(); iter.hasNext();) {
			((Image) iter.next()).dispose();
		}
		imagesToBeDisposed.clear();

	}

	protected void hideDiagramAssistant() {
		if (getBalloon() != null) {

			teardownPopupBar();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gmf.runtime.diagram.ui.editpolicies.DiagramAssistantEditPolicy#showDiagramAssistantAfterDelay(int)
	 */
	protected void showDiagramAssistantAfterDelay(int theDelay) {
		// only show the popup bar if it isn't already showing
		if (!isDiagramAssistantShowing()) {
			super.showDiagramAssistantAfterDelay(theDelay);
		}
	}

	public void activate() {
		super.activate();
		getHostFigure().addMouseListener(this.myMouseKeyListener);
		getHostFigure().addFigureListener(this.myOwnerMovedListener);
		setIsDisplayAtMouseHoverLocation(true);
	}

	public void deactivate() {
		getHostFigure().removeMouseListener(this.myMouseKeyListener);
		getHostFigure().removeFigureListener(this.myOwnerMovedListener);
		super.deactivate();
	}

	/**
	 * This is the default which places the popup bar to favor the right side
	 * of the shape
	 * 
	 */
	protected void setRightHandDisplay() {
		this.myBallonOffsetPercent = BALLOON_X_OFFSET_RHS;
	}

	/**
	 * Place the popup bar to favor the left had side of the shape
	 * 
	 */
	protected void setLeftHandDisplay() {
		this.myBallonOffsetPercent = BALLOON_X_OFFSET_LHS;
	}

	/**
	 * check thee right display status
	 * @return true or false
	 */
	protected boolean isRightDisplay() {
		return (BALLOON_X_OFFSET_RHS == myBallonOffsetPercent);
	}

	/**
	 * Gets the amount of time to wait before showing the popup bar if the
	 * popup bar is to be shown at the mouse location
	 * {@link #getIsDisplayAtMouseHoverLocation()}.
	 * 
	 * @return the time to wait in milliseconds
	 */
	protected int getAppearanceDelayLocationSpecific() {
		return getAppearanceDelay();
	}

}
