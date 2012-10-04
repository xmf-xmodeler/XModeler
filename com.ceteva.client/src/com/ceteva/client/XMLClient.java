package com.ceteva.client;

import java.util.Stack;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.xml.Attribute;
import com.ceteva.client.xml.Document;
import com.ceteva.client.xml.Element;
import com.ceteva.client.xml.XML;

public abstract class XMLClient extends Client {

	Stack xml = new Stack();
	
	public XMLClient(String name) {
		super(name);
	}
	
	public void endDocument(Message message) {
		XML element = (XML)popXML();
		if(element instanceof Document)
		  processXML((Document)element);
	}
	
	public void endElement(Message message) {
		XML element = (XML)popXML();
		XML owner = (XML)popXML();
		owner.addChild(element);
		pushXML(owner);
	}
	
	public boolean parseXML(Message message) {
		if(message.hasName(XMLData.START_DOCUMENT)) {
		  startDocument(message);
		  return true;
		}
		else if(message.hasName(XMLData.END_DOCUMENT)) {
		  endDocument(message);
		  return true;
		}
		else if(message.hasName(XMLData.START_ELEMENT)) {
		  startElement(message);
		  return true;
		}
		else if(message.hasName(XMLData.END_ELEMENT)) {
		  endElement(message);
		  return true;
		}
		return false;
	}
	
	public XML peekXML() {
		return (XML)xml.peek();
	}
	
	public XML popXML() {
		return (XML)xml.pop();
	}
	
	public boolean processMessage(Message message) {
		return parseXML(message);
	}
	
	public abstract void processXML(Document xml);
	
	public void pushXML(XML element) {
		xml.push(element);
	}
	
	public void startDocument(Message message) {
		xml.clear();
		pushXML(new Document());
	}
	
	public void startElement(Message message) {
		String name = message.args[0].strValue();
		Element element = new Element();
		element.setName(name);
		
		// process attributes
		
		Value[] attributes = message.args[1].values;
		for(int i=0;i<attributes.length;i++) {
		  Value[] attribute = attributes[i].values;
		  String aname = attribute[0].strValue();
		  String avalue = attribute[1].strValue();
		  if(attribute[1].type == XOS.XData.INT)
		    avalue = String.valueOf(attribute[1].intValue);
		  if(attribute[1].type == XOS.XData.BOOL)
			avalue = String.valueOf(attribute[1].boolValue);
	      element.addAttribute(new Attribute(aname,avalue));
		}
		
		// push the element

		pushXML(element);
	}
}
