package com.ceteva.diagram.figure;

import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.RGB;

import com.ceteva.client.ColorManager;

class HeadFactory {

    public static RotatableDecoration getHead(int head) {
        switch (head) {
        case 1:
            return arrow();
        case 2:
            return blackDiamond();
        case 3:
            return whiteDiamond();
        case 4:
            return blackInheritance();
        case 5:
            return whiteInheritance();
        case 6:
            return blackBox();
        case 7:
            return whiteBox();
        case 8:
            return claw();
        case 9:
        	return ball();
        }
        return null;
    }

    private static RotatableDecoration arrow() {
        PolylineDecoration decoration = new PolylineDecoration();
        PointList decorationPointList = new PointList();
        decorationPointList.addPoint(0, 0);
        decorationPointList.addPoint(-2, 2);
        decorationPointList.addPoint(0, 0);
        decorationPointList.addPoint(-2, -2);
        decoration.setTemplate(decorationPointList);
        return decoration;
    }
    
    private static RotatableDecoration ball() {
    	PolygonDecoration decoration = new PolygonDecoration();
    	PointList decorationPointList = new PointList();
    	int radius = 100;
    	double DEG2RAD = 3.14159/180;
    	for (int i=0; i < 360; i++) {
    	  double degInRad = i*DEG2RAD;
    	  Point p = new Point(Math.cos(degInRad)*radius,Math.sin(degInRad)*radius);
    	  p.translate(150,0);
    	  decorationPointList.addPoint(p);
    	}
        decoration.setTemplate(decorationPointList);
        decoration.setScale(0.05,0.05);
    	return decoration;
    }

    private static RotatableDecoration blackDiamond() {
        PolygonDecoration decoration = new PolygonDecoration();
        PointList decorationPointList = new PointList();
        decorationPointList.addPoint(0, 0);
        decorationPointList.addPoint(-2, 2);
        decorationPointList.addPoint(-4, 0);
        decorationPointList.addPoint(-2, -2);
        decoration.setTemplate(decorationPointList);
        return decoration;
    }

    private static RotatableDecoration whiteDiamond() {
        PolygonDecoration decoration = (PolygonDecoration) blackDiamond();
        RGB color = new RGB(255, 255, 255);
        decoration.setBackgroundColor(ColorManager.getColor(color));
        return decoration;
    }

    public static RotatableDecoration blackInheritance() {
        PolygonDecoration decoration = new PolygonDecoration();
        PointList decorationPointList = new PointList();
        decorationPointList.addPoint(0, 0);
        decorationPointList.addPoint(-2, 2);
        decorationPointList.addPoint(-2, -2);
        decoration.setTemplate(decorationPointList);
        return decoration;
    }

    public static RotatableDecoration whiteInheritance() {
        PolygonDecoration decoration = (PolygonDecoration) blackInheritance();
        RGB color = new RGB(255, 255, 255);
        decoration.setBackgroundColor(ColorManager.getColor(color));
        return decoration;
    }

    private static RotatableDecoration blackBox() {
        PolygonDecoration decoration = new PolygonDecoration();
        PointList decorationPointList = new PointList();
        decorationPointList.addPoint(0, 0);
        decorationPointList.addPoint(0, -2);
        decorationPointList.addPoint(2, -2);
        decorationPointList.addPoint(2, 2);
        decorationPointList.addPoint(0, 2);
        decoration.setTemplate(decorationPointList);
        return decoration;
    }

    private static RotatableDecoration whiteBox() {
        PolygonDecoration decoration = (PolygonDecoration) blackBox();
        RGB color = new RGB(255, 255, 255);
        decoration.setBackgroundColor(ColorManager.getColor(color));
        return decoration;
    }

    private static RotatableDecoration claw() {
        /* PolylineDecoration decoration = new PolylineDecoration();
        PointList decorationPointList = new PointList();
        decorationPointList.addPoint(0, 0);
        decorationPointList.addPoint(0, -2);
        decorationPointList.addPoint(1, -2);
        decorationPointList.addPoint(0, -2);
        decorationPointList.addPoint(0, 2);
        decorationPointList.addPoint(1, 2);
        decoration.setTemplate(decorationPointList);
        return decoration; */
    	
    	PolylineDecoration decoration = new PolylineDecoration();
     	PointList decorationPointList = new PointList();
     	int radius = 150;
     	double DEG2RAD = 3.14159/180;
     	for (int i=30; i < 330; i++) {
     	  double degInRad = i*DEG2RAD;
     	  Point p = new Point(Math.cos(degInRad)*radius,Math.sin(degInRad)*radius);
     	  p.translate(150,0);
     	  decorationPointList.addPoint(p);
     	}
        decoration.setTemplate(decorationPointList);
        decoration.setScale(0.05,0.05);
     	return decoration;
    }
}