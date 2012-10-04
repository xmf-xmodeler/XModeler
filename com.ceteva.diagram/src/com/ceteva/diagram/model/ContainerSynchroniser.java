package com.ceteva.diagram.model;

import java.util.Vector;

import com.ceteva.client.xml.Element;

public class ContainerSynchroniser {
	
	private static void synchroniseBoxes(Container container, Element element) {
		
		// check that the display container has a box for each of the boxes in the document

		for (int i = 0; i < element.childrenSize(); i++) {
			Element child = (Element) element.getChild(i);
			if (child.hasName(XMLBindings.box)) {
				String id = child.getString("identity");
				if (!synchroniseDisplayWithDocument(container, child, id))
					ModelFactory.newBox(container, child);
			}
		}

		// check that each of the boxes has a box in the document

		for (int i = 0; i < container.displays.size(); i++) {
			Display display = (Display) container.displays.elementAt(i);
			if (display instanceof Box) {
				if (!synchroniseDocumentWithDisplay(display, element, XMLBindings.box))
					display.delete();
			}
		}
	}
	
	private static void synchroniseEllipses(Container container, Element element) {

		// check that the display container has a ellipse for each of the ellipses in the document

		for (int i = 0; i < element.childrenSize(); i++) {
			Element child = (Element) element.getChild(i);
			if (child.hasName(XMLBindings.ellipse)) {
				String id = child.getString("identity");
				if (!synchroniseDisplayWithDocument(container, child, id))
					ModelFactory.newEllipse(container, child);
			}
		}

		// check that each of the ellipses has an ellipse in the document

		for (int i = 0; i < container.displays.size(); i++) {
			Display display = (Display) container.displays.elementAt(i);
			if (display instanceof Ellipse) {
				if (!synchroniseDocumentWithDisplay(display, element, XMLBindings.ellipse))
					display.delete();
			}
		}
	}

	private static boolean synchroniseDisplayWithDocument(Container container,Element element, String identity) {
		Vector displays = container.displays;
		for (int i = 0; i < displays.size(); i++) {
			Display display = (Display) displays.elementAt(i);
			if (display.getIdentity().equals(identity)) {
				display.synchronise(element);
				return true;
			}
		}
		return false;
	}

	private static boolean synchroniseDocumentWithDisplay(Display display,Element element, String name) {
		for (int z = 0; z < element.childrenSize(); z++) {
			Element child = element.getChild(z);
			if (child.hasName(name) && child.getString("identity").equals(display.getIdentity()))
				return true;
		}
		return false;
	}

	public static void synchronise(Container container, Element element) {

		// Synchronisation for image and shape needs to be implemented	

		synchroniseBoxes(container,element);
		synchroniseEllipses(container,element);
		synchroniseGroups(container,element);
		synchroniseLines(container,element);
		synchroniseMultilineTexts(container,element);
		synchroniseTexts(container,element);
	}
	
	private static void synchroniseGroups(Container container, Element element) {

		// check that the display container has a ellipse for each of the ellipses in the document

		for (int i = 0; i < element.childrenSize(); i++) {
			Element child = (Element) element.getChild(i);
			if (child.hasName(XMLBindings.group)) {
				String id = child.getString("identity");
				if (!synchroniseDisplayWithDocument(container, child, id))
					ModelFactory.newGroup(container, child);
			}
		}

		// check that each of the ellipses has an ellipse in the document

		for (int i = 0; i < container.displays.size(); i++) {
			Display display = (Display) container.displays.elementAt(i);
			if (display instanceof Group) {
				if (!synchroniseDocumentWithDisplay(display, element, XMLBindings.group))
					display.delete();
			}
		}
	}
	
	private static void synchroniseLines(Container container, Element element) {

		// check that the display container has a ellipse for each of the ellipses in the document

		for (int i = 0; i < element.childrenSize(); i++) {
			Element child = (Element) element.getChild(i);
			if (child.hasName(XMLBindings.line)) {
				String id = child.getString("identity");
				if (!synchroniseDisplayWithDocument(container, child, id))
					ModelFactory.newLine(container, child);
			}
		}

		// check that each of the ellipses has an ellipse in the document

		for (int i = 0; i < container.displays.size(); i++) {
			Display display = (Display) container.displays.elementAt(i);
			if (display instanceof Line) {
				if (!synchroniseDocumentWithDisplay(display, element, XMLBindings.line))
					display.delete();
			}
		}
	}
	
	private static void synchroniseMultilineTexts(Container container, Element element) {

		// check that the display container has a ellipse for each of the ellipses in the document

		for (int i = 0; i < element.childrenSize(); i++) {
			Element child = (Element) element.getChild(i);
			if (child.hasName(XMLBindings.multilinetext)) {
				String id = child.getString("identity");
				if (!synchroniseDisplayWithDocument(container, child, id))
					ModelFactory.newMultilineText(container, child);
			}
		}

		// check that each of the ellipses has an ellipse in the document

		for (int i = 0; i < container.displays.size(); i++) {
			Display display = (Display) container.displays.elementAt(i);
			if (display instanceof MultilineText) {
				if (!synchroniseDocumentWithDisplay(display, element, XMLBindings.multilinetext))
					display.delete();
			}
		}
	}	
	
	private static void synchroniseTexts(Container container, Element element) {

		// check that the display container has a ellipse for each of the ellipses in the document

		for (int i = 0; i < element.childrenSize(); i++) {
			Element child = (Element) element.getChild(i);
			if (child.hasName(XMLBindings.text)) {
				String id = child.getString("identity");
				if (!synchroniseDisplayWithDocument(container, child, id))
					ModelFactory.newText(container, child);
			}
		}

		// check that each of the ellipses has an ellipse in the document

		for (int i = 0; i < container.displays.size(); i++) {
			Display display = (Display) container.displays.elementAt(i);
			if (display instanceof Text) {
				if (!synchroniseDocumentWithDisplay(display, element, XMLBindings.text))
					display.delete();
			}
		}
	}

}
