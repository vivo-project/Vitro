/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.containerneutral;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Look at web.xml, and check for conditions that violate the Servlet 2.4 spec,
 * but that might not be noticed because Tomcat doesn't complain.
 * 
 * ------
 * 
 * Values of the <dispatcher/> tag:
 * 
 * The spec permits only these values: "FORWARD", "REQUEST", "INCLUDE", "ERROR",
 * but Tomcat also allows the lower-case equivalents. GlassFish or WebLogic will
 * barf on lower-case.
 * 
 * Check to see that only the upper-case values are used.
 * 
 * ------
 * 
 * Existence of Servlet classes:
 * 
 * The spec allows the container to either load all servlets at startup, or to
 * load them when requested. Since Tomcat only loads servlet when requested, it
 * doesn't notice or complain if web.xml cites a <servlet-class/> that doesn't
 * exist, as long as it is never invoked. On the other hand, WebLogic loads all
 * serlvets at startup, and will barf if the class is not found.
 * 
 * Check each <servlet-class/> to insure that the class can be loaded and
 * instantiated and assigned to HttpServlet.
 * 
 * ------
 * 
 * Embedded URIs in taglibs.
 * 
 * I can't find this definitively in the JSP spec, but some containers complain
 * if web.xml specifies a <taglib-uri/> that conflicts with the <uri/> embedded
 * in the taglib itself. As far as I can see in the spec, the embedded <uri/>
 * tag is not required or referenced unless we are using
 * "Implicit Map Entries From TLDs", which in turn is only relevant for TLDs
 * packaged in JAR files. So, I can't find support for this complaint, but it
 * seems a reasonable one.
 * 
 * Check each <taglib/> specified in web.xml. If the taglib has an embedded
 * <uri/> tag, it should match the <taglib-uri/> from web.xml.
 * 
 * ------
 * 
 * Existence of Listener and Filter classes.
 * 
 * As far as I can tell, there is no ambiguity here, and every container will
 * complain if any of the <listener-class/> or <filter-class/> entries are
 * unsuitable. I check them anyway, since the mechanism was already assembled
 * for checking <servlet-class/> entries.
 * 
 * Check each <listener-class/> to insure that the class can be loaded and
 * instantiated and assigned to ServletContextListener.
 * 
 * Check each <filter-class/> to insure that the class can be loaded and
 * instantiated and assigned to Filter.
 * 
 * ------
 * 
 * A <servlet/> tag for every <servlet-mapping/> tag
 * 
 * I can't find a mention of this in the spec, but Tomcat complains and refuses
 * to load the app if there is a <servlet-mapping/> tag whose <servlet-name/> is
 * not matched by a <servlet-name/> in a <servlet/> tag.
 * 
 * Get sets of all <servlet-name/> tags that are specified in <servlet/> and
 * <servlet-mapping/> tags. There should not be any names in the
 * servlet-mappings that are not in the servlets.
 * 
 * ---------------------------------------------------------------------
 * 
 * Although this class is executed as a JUnit test, it doesn't have the usual
 * structure for a unit test.
 * 
 * In order to produce the most diagnostic information, the test does not abort
 * on the first failure. Rather, failure messages are accumulated until all
 * checks have been performed, and the test list all such messages on failure.
 * 
 * ---------------------------------------------------------------------
 * 
 * Since this is not executed as part of the standard Vitro unit tests, it also
 * cannot use the standard logging mechanism. Log4J has not been initialized.
 * 
 */
public class CheckContainerNeutrality {
	private static final String PROPERTY_WEBAPP_DIR = "CheckContainerNeutrality.webapp.dir";

	private static DocumentBuilder docBuilder;
	private static XPath xpath;

	@BeforeClass
	public static void createDocBuilder() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true); // never forget this!
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	@BeforeClass
	public static void createXPath() {
		xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new StupidNamespaceContext());
	}

	private File webappDir;
	private File webXmlFile;
	private Document webXmlDoc;
	private List<String> messages;

	@Before
	public void setup() throws SAXException, IOException {
		String webappDirPath = System.getProperty(PROPERTY_WEBAPP_DIR);
		if (webappDirPath == null) {
			fail("System property '" + PROPERTY_WEBAPP_DIR
					+ "' was not provided.");
		}
		webappDir = new File(webappDirPath);
		if (!webappDir.isDirectory()) {
			fail("'" + webappDirPath + "' is not a directory");
		}
		webXmlFile = new File(webappDir, "WEB-INF/web.xml");
		if (!webXmlFile.isFile()) {
			fail("Can't find '" + webXmlFile.getAbsolutePath() + "'");
		}

		webXmlDoc = docBuilder.parse(webXmlFile);

		messages = new ArrayList<String>();
	}

	// ----------------------------------------------------------------------
	// Tests
	// ----------------------------------------------------------------------

	@Test
	public void checkAll() throws IOException {
		checkDispatcherValues();
		checkServletClasses();
		checkListenerClasses();
		checkFilterClasses();
		checkTaglibLocations();
		checkServletNames();

		if (!messages.isEmpty()) {
			fail("Found these problems with '" + webXmlFile.getCanonicalPath()
					+ "'\n   " + StringUtils.join(messages, "\n   "));
		}
	}

	private void checkDispatcherValues() {
		List<String> okValues = Arrays.asList(new String[] { "FORWARD",
				"REQUEST", "INCLUDE", "ERROR" });
		for (Node n : findNodes("//j2ee:dispatcher")) {
			String text = n.getTextContent();
			if (!okValues.contains(text)) {
				messages.add("<dispatcher>" + text
						+ "</dispatcher> is not valid. Acceptable values are "
						+ okValues);
			}
		}
	}

	private void checkServletClasses() {
		for (Node n : findNodes("//j2ee:servlet-class")) {
			String text = n.getTextContent();
			String problem = confirmClassNameIsValid(text, HttpServlet.class);
			if (problem != null) {
				messages.add("<servlet-class>" + text
						+ "</servlet-class> is not valid: " + problem);
			}
		}
	}

	private void checkListenerClasses() {
		for (Node n : findNodes("//j2ee:listener-class")) {
			String text = n.getTextContent();
			String problem = confirmClassNameIsValid(text,
					ServletContextListener.class);
			if (problem != null) {
				messages.add("<listener-class>" + text
						+ "</listener-class> is not valid: " + problem);
			}
		}
	}

	private void checkFilterClasses() {
		for (Node n : findNodes("//j2ee:filter-class")) {
			String text = n.getTextContent();
			String problem = confirmClassNameIsValid(text, Filter.class);
			if (problem != null) {
				messages.add("<filter-class>" + text
						+ "</filter-class> is not valid: " + problem);
			}
		}
	}

	private void checkTaglibLocations() {
		for (Node n : findNodes("//j2ee:jsp-config/j2ee:taglib")) {
			String taglibUri = findNode("j2ee:taglib-uri", n).getTextContent();
			String taglibLocation = findNode("j2ee:taglib-location", n)
					.getTextContent();
			// System.out.println("taglibUri='" + taglibUri
			// + "', taglibLocation='" + taglibLocation + "'");
			String message = checkTaglibUri(taglibUri, taglibLocation);
			if (message != null) {
				messages.add(message);
			}
		}
	}

	private void checkServletNames() {
		Set<String> servletNames = new HashSet<String>();
		for (Node n : findNodes("//j2ee:servlet/j2ee:servlet-name")) {
			servletNames.add(n.getTextContent());
		}

		Set<String> servletMappingNames = new HashSet<String>();
		for (Node n : findNodes("//j2ee:servlet-mapping/j2ee:servlet-name")) {
			servletMappingNames.add(n.getTextContent());
		}

		servletMappingNames.removeAll(servletNames);
		for (String name : servletMappingNames) {
			messages.add("There is a <servlet-mapping> tag for <servlet-name>"
					+ name + "</servlet-name>, but there is "
					+ "no matching <servlet> tag.");
		}
	}

	private String checkTaglibUri(String taglibUri, String taglibLocation) {
		File taglibFile = new File(webappDir, taglibLocation);
		if (!taglibFile.isFile()) {
			return "File '" + taglibLocation + "' can't be found ('"
					+ taglibFile.getAbsolutePath() + "')";
		}

		Document taglibDoc;
		try {
			taglibDoc = docBuilder.parse(taglibFile);
		} catch (SAXException e) {
			return "Failed to parse the taglib file '" + taglibFile + "': " + e;
		} catch (IOException e) {
			return "Failed to parse the taglib file '" + taglibFile + "': " + e;
		}

		List<Node> uriNodes = findNodes("/j2ee:taglib/j2ee:uri",
				taglibDoc.getDocumentElement());
		// System.out.println("uriNodes: " + uriNodes);
		if (uriNodes.isEmpty()) {
			return null;
		}
		if (uriNodes.size() > 1) {
			return "taglib '" + taglibLocation + "' contains "
					+ uriNodes.size()
					+ " <uri> nodes. Expecting no more than 1";
		}

		String embeddedUri = uriNodes.get(0).getTextContent();
		if (taglibUri.equals(embeddedUri)) {
			return null;
		} else {
			return "URI in taglib doesn't match the one in web.xml: taglib='"
					+ taglibLocation + "', internal URI='"
					+ uriNodes.get(0).getTextContent()
					+ "', URI from web.xml='" + taglibUri + "'";
		}
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/**
	 * Search for an Xpath in web.xml, returning a handy list.
	 */
	private List<Node> findNodes(String pattern) {
		return findNodes(pattern, webXmlDoc.getDocumentElement());
	}

	/**
	 * Search for an Xpath within a node of web.xml, returning a handy list.
	 */
	private List<Node> findNodes(String pattern, Node context) {
		try {
			XPathExpression xpe = xpath.compile(pattern);
			NodeList nodes = (NodeList) xpe.evaluate(context,
					XPathConstants.NODESET);
			List<Node> list = new ArrayList<Node>();
			for (int i = 0; i < nodes.getLength(); i++) {
				list.add(nodes.item(i));
			}
			return list;
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Search for an Xpath within a node of web.xml, returning a single node or
	 * throwing an exception.
	 */
	private Node findNode(String pattern, Node context) {
		List<Node> list = findNodes(pattern, context);
		if (list.size() != 1) {
			throw new RuntimeException("Expecting 1 node, but found "
					+ list.size() + " nodes using '" + pattern + "'");
		} else {
			return list.get(0);
		}
	}

	/**
	 * Check that the supplied className can be instantiated with a
	 * zero-argument constructor, and assigned to a variable of the target
	 * class.
	 */
	private String confirmClassNameIsValid(String className,
			Class<?> targetClass) {
		try {
			Class<?> specifiedClass = Class.forName(className);
			Object o = specifiedClass.newInstance();
			if (!targetClass.isInstance(o)) {
				return specifiedClass.getSimpleName()
						+ " is not a subclass of "
						+ targetClass.getSimpleName() + ".";
			}
		} catch (ClassNotFoundException e) {
			return "The class does not exist.";
		} catch (InstantiationException e) {
			return "The class does not have a public constructor "
					+ "that takes zero arguments.";
		} catch (IllegalAccessException e) {
			return "The class does not have a public constructor "
					+ "that takes zero arguments.";
		}
		return null;
	}

	/**
	 * Dump the first 20 nodes of an XML context, excluding comments and blank
	 * text nodes.
	 */
	@SuppressWarnings("unused")
	private int dumpXml(Node xmlNode, int... parms) {
		int remaining = (parms.length == 0) ? 20 : parms[0];
		int level = (parms.length < 2) ? 1 : parms[1];

		Node n = xmlNode;

		if (Node.COMMENT_NODE == n.getNodeType()) {
			return 0;
		}
		if (Node.TEXT_NODE == n.getNodeType()) {
			if (StringUtils.isBlank(n.getTextContent())) {
				return 0;
			}
		}

		int used = 1;

		System.out.println(StringUtils.repeat("-->", level) + n);
		NodeList nl = n.getChildNodes();
		for (int i = 0; (i < nl.getLength() && remaining > used); i++) {
			used += dumpXml(nl.item(i), remaining - used, level + 1);
		}
		return used;
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class StupidNamespaceContext implements NamespaceContext {
		@Override
		public String getNamespaceURI(String prefix) {
			if ("j2ee".equals(prefix)) {
				return "http://java.sun.com/xml/ns/j2ee";
			} else {
				throw new UnsupportedOperationException();
			}
		}

		@Override
		public String getPrefix(String namespaceURI) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<?> getPrefixes(String namespaceURI) {
			throw new UnsupportedOperationException();
		}
	}

}
