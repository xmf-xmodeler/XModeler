package com.ceteva.diagram.model;

import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;

public class Shape extends DisplayWithDimension {

    Vector  points;

    boolean outline;

    public Shape(ClientElement parent, EventHandler handler, String identity, int x, int y, int width, int height, boolean outline, Vector points,
            RGB lineColor, RGB fillColor) {
        super(parent, handler, identity, new Point(x, y), new Dimension(width, height), lineColor, fillColor);
        this.points = points;
        this.outline = outline;
    }

    public void delete() {
        super.delete();
        ((Container) parent).removeDisplay(this);
    }

    public void redrawShape(Message message) {
        int x = message.args[1].intValue;
        int y = message.args[2].intValue;
        int width = message.args[3].intValue;
        int height = message.args[4].intValue;
        location = new Point(x, y);
        size = new Dimension(width, height);
        outline = message.args[5].boolValue;
        XOS.Value[] rawPoints = message.args[6].values;
        points = new Vector();
        for (int i = 0; i < rawPoints.length; i = i + 2) {
            int xPos = rawPoints[i].intValue;
            int yPos = rawPoints[i + 1].intValue;
            points.addElement(new Point(xPos, yPos));
        }
        if (isRendering())
            firePropertyChange("redrawShape", null, null);
    }

    public int x() {
        return location.x;
    }

    public int y() {
        return location.y;
    }

    public int width() {
        return size.width;
    }

    public int height() {
        return size.height;
    }

    public Vector getPoints() {
        return points;
    }

    public boolean outline() {
        return outline;
    }

    public boolean processMessage(Message message) {
        if (super.processMessage(message))
            return true;
        if (message.hasName("redrawShape") && message.args[0].hasStrValue(identity) && message.arity == 7) {
            redrawShape(message);
            return true;
        }
        return false;
    }

    public static boolean parseBool(String value) {
        if (value.equals("true"))
            return true;
        return false;
    }

    public void setFillColor(int red, int green, int blue) {
        fillColor = ModelFactory.getColor(red, green, blue);
        if (isRendering())
            firePropertyChange("color", null, null);
    }

    public void setLineColor(int red, int green, int blue) {
        foregroundColor = ModelFactory.getColor(red, green, blue);
        if (isRendering())
            firePropertyChange("color", null, null);
    }
}