/*
 * Created on Nov 5, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package XML;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import java.io.*;
import Engine.*;

/**
 * @author tony
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class XMLReader extends DefaultHandler {

	// Reads a file of XML data transforms it into an XMF representation.
	// Use a SAX parser to construct the XML nodes. Each time an element is
	// started push it on a stack of incomplete nodes and mark the stack
	// of complete nodes. Each time a text node is encountered push it on the
	// stack of complete nodes. Each time an element is terminated pop off
	// the complete nodes up to the mark and set them as the children.
	
	private static int SIZE = 10240;

	private static ValueStack openElements = new ValueStack(SIZE); // Elements that have started but not finished.
	private static ValueStack completedNodes = new ValueStack(SIZE); // Nodes that have been created.

	// A machine is required to allocate the XML nodes. Set up various
	// data structures at the start of the parse.

	private Machine machine; // Used to create the instances.

	// The following classes are required to create instances during the
	// parse. They are queried via the root name space.

	private int theClassElement; // Set at the start of the parse.
	private int theClassAttribute; // Set at the start of the parse.
	private int theClassDocument; // Set at the start of the parse.
	private int theClassText; // Set at the start of the parse.
	
	// The following symbols are required to set the slots of XML nodes
	// during the parse. Don't cache them over parses since GC will cause
	// the pointers to become stale.

	private int theSymbolAttributes; // Used to initialise an element.
	private int theSymbolChildren; // Used to set the slot of an element.
	private int theSymbolName; // Used to set an attribute.
	private int theSymbolRoot; // Used to set a document.
	private int theSymbolValue; // Used to set an attribute.
	private int theSymbolTag; // Used to set the tag of an element.
	private int theSymbolText; // Used to set the text slot of a text object.
	
	public XMLReader(Machine machine) {
		
		this.machine = machine;
		this.theClassElement = Element();
		this.theClassAttribute = Attribute();
		this.theClassDocument = Document();
		this.theClassText = Text();
		
		this.theSymbolAttributes = machine.mkSymbol("attributes");
		this.theSymbolChildren = machine.mkSymbol("children");
		this.theSymbolName = machine.mkSymbol("name");
		this.theSymbolRoot = machine.mkSymbol("root");
		this.theSymbolValue = machine.mkSymbol("value");
		this.theSymbolTag = machine.mkSymbol("tag");
		this.theSymbolText = machine.mkSymbol("text");
		
		openElements.reset();
		completedNodes.reset();
	}

	public int Attribute() { this.
		machine.dynamicReference(machine.mkSymbol("Root"));
		machine.refNameSpace(machine.popStack(), machine.mkSymbol("XML"));
		machine.refNameSpace(machine.popStack(), machine.mkSymbol("Attribute"));
		return machine.popStack();
	}

	public int Document() {
		machine.dynamicReference(machine.mkSymbol("Root"));
		machine.refNameSpace(machine.popStack(), machine.mkSymbol("XML"));
		machine.refNameSpace(machine.popStack(), machine.mkSymbol("Document"));
		return machine.popStack();
	}

	public void endElement(String uri, String localName, String qname) {
		int element = openElements.pop();
		setChildren(element);
		completedNodes.push(element);
	}

	public void characters(char[] chars, int start, int length) {
		String s = "";
		for(int i = 0; i < length; i++)
			s = s + (char)chars[i + start];
		int text = machine.instantiate(theClassText);
		machine.objSetAttValue(text,theSymbolText,machine.mkString(s));
		completedNodes.push(text);
	}

	public int document() {
		int document = machine.instantiate(theClassDocument);
		machine.objSetAttValue(document, theSymbolRoot, completedNodes.pop());
		return document;
	}

	public int Element() {
		machine.dynamicReference(machine.mkSymbol("Root"));
		machine.refNameSpace(machine.popStack(), machine.mkSymbol("XML"));
		machine.refNameSpace(machine.popStack(), machine.mkSymbol("Element"));
		return machine.popStack();
	}

	public int newAttribute(String name, String value) {
		int attribute = machine.instantiate(theClassAttribute);
		machine.objSetAttValue(attribute, theSymbolName, machine.mkString(name));
		machine.objSetAttValue(attribute, theSymbolValue, machine.mkString(value));
		return attribute;
	}

	public int newAttributes(Attributes attributes) {
		int A = Machine.nilValue;
		for (int i = 0; i < attributes.getLength(); i++) {
			String name = attributes.getQName(i);
			String value = attributes.getValue(i);
			A = machine.mkCons(newAttribute(name, value), A);
		}
		return A;
	}

	public int newElement(String tag, int attributes) {
		int element = machine.instantiate(theClassElement);
		machine.objSetAttValue(element, theSymbolTag, machine.mkString(tag));
		machine.objSetAttValue(element, theSymbolAttributes, attributes);
		return element;
	}

	public static int parse(String path, Machine machine) {
		try {
			File file = new File(path);
			if (file.exists()) {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setValidating(false);
				SAXParser parser = factory.newSAXParser();
				XMLReader reader = new XMLReader(machine);
				parser.getXMLReader().setErrorHandler(reader);
				parser.parse(file, reader);
				return reader.document();
			} else
				throw new Error("Cannot find file: " + path);
		} catch (Exception x) {
			x.printStackTrace();
			throw new Error(x.toString());
		}
	}

	public static int parse(InputStream in, Machine machine) {
		
		// The following gets around the problem that I don't know how to set up the
		// relative URI for a SAX parser. Reading directly from a stream leaves the
		// SAX parser unaware of where the XML file lives in the file space. Using a 
		// string does not. There must be a way round this to directly set the URI in
		// the SAX parser via its properties or features.
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = new XMLReader(machine);
			parser.parse(in, reader);
			return reader.document();
		} catch (Exception x) {
			x.printStackTrace();
			throw new Error(x.getMessage());
		}
	}

	public void setChildren(int element) {
		int node = completedNodes.pop();
		int children = Machine.nilValue;
		while (node != element) {
			children = machine.mkCons(node, children);
			node = completedNodes.pop();
		}
		machine.objSetAttValue(element, theSymbolChildren, children);
	}

	public void startElement(String uri, String localName, String qname, Attributes attributes) {
		int element = newElement(qname, newAttributes(attributes));
		openElements.push(element);
		completedNodes.push(element);
	}

	public int Text() {
		machine.dynamicReference(machine.mkSymbol("Root"));
		machine.refNameSpace(machine.popStack(), machine.mkSymbol("XML"));
		machine.refNameSpace(machine.popStack(), machine.mkSymbol("Text"));
		return machine.popStack();
	}
	
	public void error(SAXParseException e) {
		System.out.println(e.toString());
	}
	
	public void fatalError(SAXParseException e) {
		System.out.println(e.toString());
	}
	
	public void warning(SAXParseException e) {
		System.out.println(e.toString());
	}
	
	public void skippedEntity(String s) {
		System.out.println("Skipped " + s);
	}

}
