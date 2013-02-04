/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.net.httpserver.HttpServer;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * Check to see that web.xml doesn't include any constructs that are permitted
 * by Tomcat but prohibited by the Servlet 2.4 Specification.
 * 
 * These are things that might not be noticed when testing Vitro on Tomcat, but
 * might show up as problems on other containers like GlassFish or WebLogic.
 * <ul>
 * <li>
 * The contents of <dispatcher/> elements must be upper-case. Tomcat permits
 * lower-case, but the specification does not.</li>
 * <li>
 * All <servlet-class/> tags must point to existing classes. Tomcat does not try
 * to load a servlet until it is necessary to service a request, but WebLogic
 * loads them on startup. Either method is permitted by the specification.</li>
 * </ul>
 * 
 * As long as we're here, let's check some things that would cause Vitro to fail
 * in any servlet container.
 * <ul>
 * <li>
 * All <listener-class/> tags must point to existing classes.</li>
 * <li>
 * All <filter-class/> tags must point to existing classes.</li>
 * <li>
 * All <taglib-location/> tags must point to existing files.</li>
 * </ul>
 */
@RunWith(value = Parameterized.class)
public class WebXmlTest extends AbstractTestClass {
	private static final Log log = LogFactory.getLog(WebXmlTest.class);

	@Parameters
	public static Collection<Object[]> findWebXmlFiles() {
		IOFileFilter fileFilter = new NameFileFilter("web.xml");
		IOFileFilter dirFilter = new NotFileFilter(new NameFileFilter(".build"));
		Collection<File> files = FileUtils.listFiles(new File("."), fileFilter,
				dirFilter);
		if (files.isEmpty()) {
			System.out.println("WARNING: could not find web.xml");
		} else {
			if (files.size() > 1) {
				System.out
						.println("WARNING: testing more than one web.xml file: "
								+ files);
			}
		}

		Collection<Object[]> parameters = new ArrayList<Object[]>();
		for (File file : files) {
			parameters.add(new Object[] { file });
		}
		return parameters;
	}

	private static DocumentBuilder docBuilder = createDocBuilder();
	private static XPath xpath = createXPath();

	private static DocumentBuilder createDocBuilder() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true); // never forget this!
			return factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private static XPath createXPath() {
		XPath xp = XPathFactory.newInstance().newXPath();
		xp.setNamespaceContext(new StupidNamespaceContext());
		return xp;
	}

	private File webXmlFile;
	private Document webXmlDoc;
	private List<String> messages = new ArrayList<String>();

	public WebXmlTest(File file) {
		this.webXmlFile = file;
	}

	@Before
	public void parseWebXml() throws SAXException, IOException {
		if (webXmlDoc == null) {
			webXmlDoc = docBuilder.parse(webXmlFile);
		}
	}

	@Test
	public void checkAll() throws IOException {
		checkDispatcherValues();
		checkServletClasses();
		checkListenerClasses();
		checkFilterClasses();
		checkTaglibLocations();

		if (!messages.isEmpty()) {
			for (String message : messages) {
				System.out.println(message);
			}
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
		// TODO Don't know how to do this one. Where do we look for the taglibs?
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/**
	 * Search for an Xpath, returning a handy list.
	 */
	private List<Node> findNodes(String pattern) {
		try {
			XPathExpression xpe = xpath.compile(pattern);
			NodeList nodes = (NodeList) xpe.evaluate(
					webXmlDoc.getDocumentElement(), XPathConstants.NODESET);
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
		} catch (InstantiationException | IllegalAccessException e) {
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
