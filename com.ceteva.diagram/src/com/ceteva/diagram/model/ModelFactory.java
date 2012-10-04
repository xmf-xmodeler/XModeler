package com.ceteva.diagram.model;

import java.util.Vector;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;

import XOS.Message;

import com.ceteva.client.ClientElement;
import com.ceteva.client.EventHandler;
import com.ceteva.client.xml.Element;

// this class is intended to be used as a utility for existing
// model elements to create new model elements

public class ModelFactory {
    
    public static RGB getColor(int red,int green,int blue) {
      if((red >= 0 && red <= 255) &&
         (green >= 0 && green <= 255) &&
         (blue >= 0 && blue <= 255))
        return new RGB(red,green,blue);
      else
        return null;
    }
	
	public static Box newBox(ClientElement parent,EventHandler handler, Message message) {
	  String identity = message.args[1].strValue();
	  int x = message.args[2].intValue;
	  int y = message.args[3].intValue;
	  int width = message.args[4].intValue;
	  int height = message.args[5].intValue;
	  int curve = message.args[6].intValue;
	  boolean top = message.args[7].boolValue;
	  boolean right = message.args[8].boolValue;
	  boolean bottom = message.args[9].boolValue;
	  boolean left = message.args[10].boolValue;
	  int lineRed = message.args[11].intValue;
	  int lineGreen = message.args[12].intValue;
      int lineBlue = message.args[13].intValue;
	  int fillRed = message.args[14].intValue;
	  int fillGreen = message.args[15].intValue;
      int fillBlue = message.args[16].intValue;
	  return new Box(parent,handler,identity,x,y,width,height,curve,top,right,bottom,left,getColor(lineRed,lineGreen,lineBlue),getColor(fillRed,fillGreen,fillBlue));		
	}
	
	public static void newBox(Container parent,Element box) {
	  String identity = box.getString("identity");
	  int x = box.getInteger("x");
  	  int y = box.getInteger("y");
  	  int width = box.getInteger("width");
  	  int height = box.getInteger("height");
  	  int curve = box.getInteger("cornerCurve");
  	  boolean top = box.getBoolean("showTop");
  	  boolean right = box.getBoolean("showRight");
  	  boolean bottom = box.getBoolean("showBottom");
  	  boolean left = box.getBoolean("showLeft");
  	  Box b = new Box(parent,parent.handler,identity,x,y,width,height,curve,top,right,bottom,left);
  	  parent.addDisplay(b);
  	  b.synchronise(box);
	}
  
	public static Text newText(ClientElement parent,EventHandler handler,Message message) {
	  String identity = message.args[1].strValue();
	  String text = message.args[2].strValue();
	  int x = message.args[3].intValue;
	  int y = message.args[4].intValue;
	  boolean editable = message.args[5].boolValue;
	  boolean underline = message.args[6].boolValue;
	  boolean italicise = message.args[7].boolValue;
	  int red = message.args[8].intValue;
	  int green = message.args[9].intValue;
	  int blue = message.args[10].intValue;
	  String font = message.args[11].strValue();
	  return new Text(parent,handler,identity,x,y,text,editable,underline,italicise,getColor(red,green,blue),font);	
	}
	
	public static void newText(Container parent,Element text) {
	  String id = text.getString("identity");
	  String label = text.getString("text");
	  int x = text.getInteger("x");
	  int y = text.getInteger("y");
	  boolean editable = text.getBoolean("editable");
	  boolean underline = text.getBoolean("underline");
	  boolean italicise = text.getBoolean("italicise");
	  String font = text.getString("font");
	  Text t = new Text(parent,parent.handler,id,x,y,label,editable,underline,italicise,font);
	  parent.addDisplay(t);
	  t.synchronise(text);
	}
	
	public static MultilineText newMultilineText(ClientElement parent,EventHandler handler,Message message)  {
	  String identity = message.args[1].strValue();
	  String text = message.args[2].strValue();
	  int x = message.args[3].intValue;
	  int y = message.args[4].intValue;
	  int width = message.args[5].intValue;
	  int height = message.args[6].intValue;
	  boolean editable = message.args[7].boolValue;
	  int foreRed = message.args[8].intValue;
	  int foreGreen = message.args[9].intValue;
      int foreBlue = message.args[10].intValue;
	  int fillRed = message.args[11].intValue;
	  int fillGreen = message.args[12].intValue;
      int fillBlue = message.args[13].intValue;
      String font = message.args[14].strValue();
      return new MultilineText(parent,handler,identity,text,x,y,width,height,editable,getColor(foreRed,foreGreen,foreBlue),getColor(fillRed,fillGreen,fillBlue),font);
	}
	
	public static void newMultilineText(Container parent,Element multiline) {
	  String id = multiline.getString("identity");
	  String label = multiline.getString("text");
	  int x = multiline.getInteger("x");
	  int y = multiline.getInteger("y");
	  int width = multiline.getInteger("width");
	  int height = multiline.getInteger("height");
	  boolean editable = multiline.getBoolean("editable");
	  String font = multiline.getString("font");
	  MultilineText t = new MultilineText(parent,parent.handler,id,label,x,y,width,height,editable,font);
	  parent.addDisplay(t);
	  t.synchronise(multiline);
	}
	
	public static MultilineEdgeText newMultilineEdgeText(ClientElement parent,EventHandler handler,Message message)  {
	  String identity = message.args[1].strValue();
	  String text = message.args[2].strValue();
	  String position = message.args[3].strValue();
	  int x = message.args[4].intValue;
	  int y = message.args[5].intValue;
	  boolean editable = message.args[6].boolValue;
	  boolean underline = message.args[7].boolValue;
	  int condense = message.args[8].intValue;
	  int red = message.args[9].intValue;
	  int green = message.args[10].intValue;
	  int blue = message.args[11].intValue;
	  String font = message.args[12].strValue();
	  return new MultilineEdgeText(parent,handler,identity,position,x,y,text,editable,underline,condense,getColor(red,green,blue),font);
	}
	
	public static EdgeText newEdgeText(ClientElement parent,EventHandler handler,Message message) {
	  String identity = message.args[1].strValue();
	  String text = message.args[2].strValue();
	  String position = message.args[3].strValue();
	  int x = message.args[4].intValue;
	  int y = message.args[5].intValue;
	  boolean editable = message.args[6].boolValue;
	  boolean underline = message.args[7].boolValue;
	  int condense = message.args[8].intValue;
	  int red = message.args[9].intValue;
	  int green = message.args[10].intValue;
	  int blue = message.args[11].intValue;
	  String font = message.args[12].strValue();
	  return new EdgeText(parent,handler,identity,position,x,y,text,editable,underline,condense,getColor(red,green,blue),font);	
	}
  
	public static Group newGroup(ClientElement parent,EventHandler handler,Message message) {
	  String identity = message.args[1].strValue();
	  int x = message.args[2].intValue;
	  int y = message.args[3].intValue;
	  int width = message.args[4].intValue;
	  int height = message.args[5].intValue;
	  return new Group(parent,handler,identity,x,y,width,height);
	}
	
	public static void newGroup(Container parent,Element group) {
	  String id = group.getString("identity");
	  int x = group.getInteger("x");
	  int y = group.getInteger("y");
	  int width = group.getInteger("width");
	  int height = group.getInteger("height");
	  Group g = new Group(parent,parent.handler,id,x,y,width,height);
	  parent.addDisplay(g);
	  g.synchronise(group);
	}
  
	public static Line newLine(ClientElement parent,EventHandler handler,Message message) {
	  String identity = message.args[1].strValue();
	  int x1 = message.args[2].intValue;
	  int y1 = message.args[3].intValue;
	  int x2 = message.args[4].intValue;
	  int y2 = message.args[5].intValue;
	  int red = message.args[6].intValue;
	  int green = message.args[7].intValue;
	  int blue = message.args[8].intValue;
	  return new Line(parent,handler,identity,x1,y1,x2,y2,getColor(red,green,blue));
	}
	
	public static void newLine(Container parent,Element line) {
	  String id = line.getString("identity");	
	  int x1 = line.getInteger("x1");
	  int y1 = line.getInteger("y1");
	  int x2 = line.getInteger("x2");
	  int y2 = line.getInteger("y2");
	  Line l = new Line(parent,parent.handler,id,x1,y1,x2,y2);
	  parent.addDisplay(l);
	  l.synchronise(line);
	}
	
	public static Ellipse newEllipse(ClientElement parent,EventHandler handler,Message message) {
	  String identity = message.args[1].strValue();
	  int x = message.args[2].intValue;
	  int y = message.args[3].intValue;
	  int width = message.args[4].intValue;
	  int height = message.args[5].intValue;
	  boolean outline = message.args[6].boolValue;
	  int lineRed = message.args[7].intValue;
	  int lineGreen = message.args[8].intValue;
      int lineBlue = message.args[9].intValue;
	  int fillRed = message.args[10].intValue;
	  int fillGreen = message.args[11].intValue;
      int fillBlue = message.args[12].intValue;
      RGB lineColor = getColor(lineRed,lineGreen,lineBlue);
      RGB fillColor = getColor(fillRed,fillGreen,fillBlue);
	  return new Ellipse(parent,handler,identity,x,y,width,height,outline,lineColor,fillColor);
	}
	
	public static void newEllipse(Container parent,Element ellipse) {
	  String id = ellipse.getString("identity");
	  int x = ellipse.getInteger("x");
	  int y = ellipse.getInteger("y");
	  int width = ellipse.getInteger("width");
	  int height = ellipse.getInteger("height");
	  boolean outline = ellipse.getBoolean("showOutline");
	  Ellipse e = new Ellipse(parent,parent.handler,id,x,y,width,height,outline);
  	  parent.addDisplay(e);
  	  e.synchronise(ellipse);
	}
	
	public static Image newImage(ClientElement parent,EventHandler handler,Message message) {
	  String identity = message.args[1].strValue();
	  String filename = message.args[2].strValue();
	  int x = message.args[3].intValue;
	  int y = message.args[4].intValue;
	  int width = message.args[5].intValue;
	  int height = message.args[6].intValue;
	  return new Image(parent,handler,identity,filename,x,y,width,height);
	}
	
	public static Shape newShape(ClientElement parent,EventHandler handler,Message message) {
	  String identity = message.args[1].strValue();
	  int x = message.args[2].intValue;
	  int y = message.args[3].intValue;
	  int width = message.args[4].intValue;
	  int height = message.args[5].intValue;
	  boolean outline = message.args[6].boolValue;
	  int lineRed = message.args[7].intValue;
	  int lineGreen = message.args[8].intValue;
      int lineBlue = message.args[9].intValue;
	  int fillRed = message.args[10].intValue;
	  int fillGreen = message.args[11].intValue;
      int fillBlue = message.args[12].intValue;
      RGB lineColor = getColor(lineRed,lineGreen,lineBlue);
      RGB fillColor = getColor(fillRed,fillGreen,fillBlue);
	  XOS.Value[] rawPoints = message.args[13].values;
	  Vector points = new Vector();
      for(int i=0;i<rawPoints.length;i=i+2) {
	  	int xPos = rawPoints[i].intValue;
	  	int yPos = rawPoints[i+1].intValue;
	  	points.addElement(new Point(xPos,yPos));
	  }
	  return new Shape(parent,handler,identity,x,y,width,height,outline,points,lineColor,fillColor);
	}
}