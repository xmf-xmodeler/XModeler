package com.ceteva.diagram.figure;

import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import com.ceteva.diagram.Diagram;
import com.ceteva.diagram.geometry.PointListUtilities;

public class EdgeFigure extends PolylineConnection {

	private Point ref;

	private String type = "normal";

	private int smooth = 30; // 0 none, 15 some, 30 lots

	private static final Dimension dimCheck = new Dimension(100, 100);

	private static int TOLERANCE = 3;

	private static Rectangle LINEBOUNDS = Rectangle.SINGLETON;
	
	public EdgeFigure() {
		getPreferences();
	}

	private int calculateTolerance(boolean isFeedbackLayer) {
		Dimension absTol = new Dimension(TOLERANCE + lineWidth / 2, 0);
		return absTol.width;
	}

	public boolean containsPoint(int x, int y) {
		if (isSplined()) {
			boolean isFeedbackLayer = isFeedbackLayer();
			int calculatedTolerance = calculateTolerance(isFeedbackLayer);

			LINEBOUNDS.setBounds(getBounds());
			LINEBOUNDS.expand(calculatedTolerance, calculatedTolerance);
			if (!LINEBOUNDS.contains(x, y))
				return false;

			int ints[] = getSmoothPoints().toIntArray();
			for (int index = 0; index < ints.length - 3; index += 2) {
				if (lineContainsPoint(ints[index], ints[index + 1],
						ints[index + 2], ints[index + 3], x, y, isFeedbackLayer))
					return true;
			}
			List children = getChildren();
			for (int i = 0; i < children.size(); i++) {
				if (((IFigure) children.get(i)).containsPoint(x, y))
					return true;
			}

			return false;
		} else {
			return super.containsPoint(x, y);
		}
	}

	public Rectangle getBounds() {
		if (isSplined()) {
			if (bounds == null) {
				if (getSmoothFactor() != 0) {
					bounds = getSmoothPoints().getBounds();
					bounds.expand(lineWidth / 2, lineWidth / 2);

					for (int i = 0; i < getChildren().size(); i++) {
						IFigure child = (IFigure) getChildren().get(i);
						bounds.union(child.getBounds());
					}
				} else
					super.getBounds();

				boolean isFeedbackLayer = isFeedbackLayer();
				int calculatedTolerance = calculateTolerance(isFeedbackLayer);
				bounds.expand(calculatedTolerance, calculatedTolerance);
			}
			return bounds;
		} else {
			return super.getBounds();
		}
	}

	public int getSmoothFactor() {
		return smooth;
	}

	public PointList getSmoothPoints() {
		if (getSmoothFactor() > 0) {
			return PointListUtilities.calcSmoothPolyline(getPoints(),
					getSmoothFactor(), PointListUtilities.DEFAULT_BEZIERLINES);
		} else {
			return PointListUtilities.copyPoints(getPoints());
		}
	}

	private boolean isFeedbackLayer() {
		Dimension copied = dimCheck.getCopy();
		translateToRelative(copied);
		return dimCheck.equals(copied);
	}
	
	private boolean isSplined() {
		if(type == null)
		  return false;
		return type.equals("splined");
	}

	private boolean lineContainsPoint(int x1, int y1, int x2, int y2, int px,
			int py, boolean isFeedbackLayer) {
		LINEBOUNDS.setSize(0, 0);
		LINEBOUNDS.setLocation(x1, y1);
		LINEBOUNDS.union(x2, y2);
		int calculatedTolerance = calculateTolerance(isFeedbackLayer);
		LINEBOUNDS.expand(calculatedTolerance, calculatedTolerance);
		if (!LINEBOUNDS.contains(px, py))
			return false;

		double v1x, v1y, v2x, v2y;
		double numerator, denominator;
		double result = 0;

		if (x1 != x2 && y1 != y2) {
			v1x = (double) x2 - x1;
			v1y = (double) y2 - y1;
			v2x = (double) px - x1;
			v2y = (double) py - y1;

			numerator = v2x * v1y - v1x * v2y;

			denominator = v1x * v1x + v1y * v1y;

			result = numerator * numerator / denominator;
		}

		// if it is the same point, and it passes the bounding box test,
		// the result is always true.
		return result <= calculatedTolerance * calculatedTolerance;

	}

	public void paintFigure(Graphics g) {
		if (Diagram.antialias)
			g.setAntialias(SWT.ON);
		super.paintFigure(g);
	}

	public void setSourceHead(int sourceHead) {
		if (sourceHead != 0) {
			RotatableDecoration head = HeadFactory.getHead(sourceHead);
			setSourceDecoration(head);
		}
	}

	public void setTargetHead(int targetHead) {
		if (targetHead != 0) {
			RotatableDecoration head = HeadFactory.getHead(targetHead);
			setTargetDecoration(head);
		}
	}

	protected void outlineShape(Graphics g) {

		// the built in dashed line is not distinct enough
		// override this

		if (getLineStyle() == SWT.LINE_DASH) {
			int dash[] = { 4, 4 };
			g.setLineDash(dash);
		}
		if (isSplined()) {
			PointList displayPoints = getSmoothPoints();
			g.drawPolyline(displayPoints);
		} else {
			super.outlineShape(g);
		}
	}

	protected boolean useLocalCoordinates() {
		return false;
	}

	public void setRefPoint(Point ref) {
		this.ref = ref;
	}

	public Point getRefPoint() {
		return ref;
	}

	public void preferenceUpdate() {
		getPreferences();
	}

	public void getPreferences() {
		/* Preferences preferences = DiagramPlugin.getDefault()
				.getPluginPreferences();
		splined = preferences.getBoolean(IPreferenceConstants.SPLINED); */
	}
	
	public void setEdgeType(String type) {
		this.type = type;
	}
}