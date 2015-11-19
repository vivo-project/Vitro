/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.customlistview;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses a configuration file for a custom list view, and makes the information
 * readily accessible.
 * 
 * If not editing the page, include clauses that will filter out statements with
 * missing linked individual or other critical information missing (e.g., anchor
 * and url on a link). If editing, omit those clauses so the incomplete
 * statements will show, and can be edited or removed.
 * 
 * We might want to refine this based on whether the user can edit the statement
 * in question, but that would require a completely different approach: include
 * the statement in the query results, and then during the postprocessing phase,
 * check the editing policy, and remove the statement if it's not editable. We
 * would not preprocess the query, as here.
 * 
 * If not collating by subgroup, omit clauses that only involve subgroups.
 */
public class CustomListViewConfigFile {
	private static final Log log = LogFactory
			.getLog(CustomListViewConfigFile.class);

	private static final String TAG_CONSTRUCT = "query-construct";
	private static final String TAG_SELECT = "query-select";
	private static final String TAG_TEMPLATE = "template";
	private static final String TAG_POSTPROCESSOR = "postprocessor";
	private static final String TAG_COLLATED = "collated";
	private static final String TAG_CRITICAL = "critical-data-required";

	// Will not be null. This is mutable, but don't modify it. Clone it and
	// modify the clone.
	private final Element selectQueryElement;

	// The set might be empty but will not be null. Each query will not be empty
	// or null.
	private final Set<String> constructQueries;

	// Will not be empty or null.
	private final String templateName;

	// Might be empty but will not be null.
	private final String postprocessorName;

	public CustomListViewConfigFile(Reader reader) throws InvalidConfigurationException {
		Document doc = parseDocument(reader);
		selectQueryElement = parseSelectQuery(doc);
		constructQueries = parseConstructQueries(doc);
		templateName = parseTemplateName(doc);
		postprocessorName = parsePostprocessorName(doc);
	}

	private Document parseDocument(Reader reader)
			throws InvalidConfigurationException {
		if (reader == null) {
			throw new InvalidConfigurationException("Config file reader is null.");
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(reader));
			return doc;
		} catch (ParserConfigurationException e) {
			throw new InvalidConfigurationException("Problem with XML parser.", e);
		} catch (SAXException e) {
			throw new InvalidConfigurationException(
					"Config file is not valid XML: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new InvalidConfigurationException("Unable to read config file.",
					e);
		}
	}

	private Element parseSelectQuery(Document doc)
			throws InvalidConfigurationException {
		Element element = getExactlyOneElement(doc, TAG_SELECT);
		elementMustNotBeEmpty(element);
		return element;
	}

	private Set<String> parseConstructQueries(Document doc) {
		Set<String> queries = new HashSet<String>();
		for (Element element : getElements(doc, TAG_CONSTRUCT)) {
			String content = element.getTextContent();
			if (!content.trim().isEmpty()) {
				queries.add(content);
			}
		}
		return queries;
	}

	private String parseTemplateName(Document doc)
			throws InvalidConfigurationException {
		Element element = getExactlyOneElement(doc, TAG_TEMPLATE);
		elementMustNotBeEmpty(element);
		return element.getTextContent();
	}

	private String parsePostprocessorName(Document doc)
			throws InvalidConfigurationException {
		Element element = getZeroOrOneElement(doc, TAG_POSTPROCESSOR);
		if (element == null) {
			return "";
		} else {
			return element.getTextContent();
		}
	}

	private List<Element> getElements(Document doc, String tagName) {
		List<Element> list = new ArrayList<Element>();
		NodeList nodes = doc.getElementsByTagName(tagName);
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				list.add((Element) nodes.item(i));
			}
		}
		return list;
	}

	private Element getExactlyOneElement(Document doc, String tagName)
			throws InvalidConfigurationException {
		List<Element> elements = getElements(doc, tagName);

		if (elements.size() == 1) {
			return elements.get(0);
		} else if (elements.isEmpty()) {
			throw new InvalidConfigurationException("Config file must contain a "
					+ tagName + " element");
		} else {
			throw new InvalidConfigurationException(
					"Config file may not contain more than one " + tagName
							+ " element");
		}
	}

	private Element getZeroOrOneElement(Document doc, String tagName)
			throws InvalidConfigurationException {
		List<Element> elements = getElements(doc, tagName);
		if (elements.size() == 1) {
			return elements.get(0);
		} else if (elements.isEmpty()) {
			return null;
		} else {
			throw new InvalidConfigurationException(
					"Config file may not contain more than one " + tagName
							+ " element");
		}
	}

	private void elementMustNotBeEmpty(Element element)
			throws InvalidConfigurationException {
		String contents = element.getTextContent();
		if (contents.trim().isEmpty()) {
			throw new InvalidConfigurationException("In a config file, the <"
					+ element.getTagName() + "> element must not be empty.");
		}
	}

	private void removeChildElements(Element element, String tagName) {
		/*
		 * When we remove a child from the element, it disappears from the
		 * NodeList. We process the NodeList in reverse order, so we won't be
		 * disturbed by nodes disappearing off the end. Strange.
		 */
		NodeList doomed = element.getElementsByTagName(tagName);
		for (int i = doomed.getLength() - 1; i >= 0; i--) {
			element.removeChild(doomed.item(i));
		}
	}

	public String getSelectQuery(boolean collated, boolean editing) {
		Element cloned = (Element) selectQueryElement.cloneNode(true);

		if (!collated) {
			removeChildElements(cloned, TAG_COLLATED);
		}
		if (editing) {
			removeChildElements(cloned, TAG_CRITICAL);
		}

		return cloned.getTextContent();
	}

	public Set<String> getConstructQueries() {
		return constructQueries;
	}

	public String getTemplateName() {
		return templateName;
	}

	public String getPostprocessorName() {
		return postprocessorName;
	}

	@Override
	public String toString() {
		return "CustomListViewConfigFile[selectQuery='"
				+ formatXmlNode(selectQueryElement) + "', constructQueries="
				+ constructQueries + ", templateName='" + templateName
				+ "', postprocessorName='" + postprocessorName + "']";
	}

	private String formatXmlNode(Node node) {
		try {
			// Set up the output transformer
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");

			// Print the DOM node
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(node);
			t.transform(source, result);
			return sw.toString();
		} catch (TransformerException e) {
			return "Failed to format XML node: " + node.getTextContent();
		}
	}

}
