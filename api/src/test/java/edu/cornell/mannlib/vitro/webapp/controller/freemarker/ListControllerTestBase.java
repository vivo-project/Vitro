/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Some useful methods for assembling JSON structures that will match the test
 * results.
 *
 * Also, some methods for running the tests, with and without kluging the
 * results.
 */
public class ListControllerTestBase extends AbstractTestClass {
	protected static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Create a JSON array from nodes.
	 */
	protected static ArrayNode arrayOf(JsonNode... nodes) {
		ArrayNode array = mapper.createArrayNode();
		for (JsonNode node : nodes) {
			array.add(node);
		}
		return array;
	}

	/**
	 * Show a DataProperty or an ObjectProperty in a list.
	 */
	protected static ObjectNode propertyListNode(String path, String uri,
			String name, String internalName, String domainVClass,
			String rangeVClass, String group) {
		String nameString = String.format("<a href='%s?uri=%s'>%s</a>", path,
				urlEncode(uri), name);
		ObjectNode propNode = mapper.createObjectNode().put("name", nameString);
		propNode.putObject("data").put("internalName", internalName)
				.put("domainVClass", domainVClass)
				.put("rangeVClass", rangeVClass).put("group", group);
		return propNode;
	}

	/**
	 * Show a DataProperty or an ObjectProperty in a hierarchy.
	 */
	protected static ObjectNode propertyHierarchyNode(String path, String uri,
			String name, String internalName, String domainVClass,
			String rangeVClass, String group, ObjectNode... children) {
		ObjectNode propNode = propertyListNode(path, uri, name, internalName,
				domainVClass, rangeVClass, group);
		propNode.set("children", arrayOf(children));
		return propNode;
	}

	/**
	 * Show a VClass in a list.
	 */
	protected static ObjectNode degenerateVclassListNode(String name,
			String shortDef, String classGroup, String ontology) {
		ObjectNode vcNode = mapper.createObjectNode().put("name", name);
		vcNode.putObject("data").put("shortDef", shortDef)
				.put("classGroup", classGroup).put("ontology", ontology);
		return vcNode;
	}

	/**
	 * Show a VClass in a list.
	 */
	protected static ObjectNode vclassListNode(String path, String uri,
			String name, String shortDef, String classGroup, String ontology) {
		String nameString = String.format("<a href='%s?uri=%s'>%s</a>", path,
				urlEncode(uri), name);
		ObjectNode vcNode = mapper.createObjectNode().put("name", nameString);
		vcNode.putObject("data").put("shortDef", shortDef)
				.put("classGroup", classGroup).put("ontology", ontology);
		return vcNode;
	}

	/**
	 * Show a VClass in a hierarchy.
	 */
	protected static ObjectNode vclassHierarchyNode(String path, String uri,
			String name, String shortDef, String classGroup, String ontology,
			ObjectNode... children) {
		ObjectNode vcNode = vclassListNode(path, uri, name, shortDef,
				classGroup, ontology);
		vcNode.set("children", arrayOf(children));
		return vcNode;
	}

	/**
	 * Show a ClassGroup or PropertyGroup in a list.
	 */
	protected static ObjectNode groupListNode(String linkFormat, String uri,
			String name, String displayRank, ObjectNode... children) {
		String nameString = String.format(linkFormat, urlEncode(uri), name);
		ObjectNode gNode = mapper.createObjectNode().put("name", nameString);
		gNode.putObject("data").put("displayRank", displayRank);
		gNode.set("children", arrayOf(children));
		return gNode;
	}

	/**
	 * Show a Class or Property as part of a Group.
	 */
	protected static ObjectNode groupMemberNode(String linkFormat, String uri,
			String name, String shortDef, ObjectNode... children) {
		String nameString = String.format(linkFormat, urlEncode(uri), name);
		ObjectNode memberNode = mapper.createObjectNode().put("name",
				nameString);
		memberNode.putObject("data").put("shortDef", shortDef);
		memberNode.set("children", arrayOf(children));
		return memberNode;
	}

	/**
	 * Based on the fact that each of these controllers returns a
	 * TemplateResponseValues object with a "jsonTree" in the body map.
	 *
	 * That jsonTree would be an standard JSON array, except that it is missing
	 * the enclosing brackets, so we need to add them before comparing to the
	 * expected value.
	 *
	 * Add the brackets, read the strings, and compare.
	 */
	protected static void assertMatchingJson(FreemarkerHttpServlet controller,
			HttpServletRequest req, JsonNode expected) throws Exception {
		String jsonString = getJsonFromController(controller, req);
		JsonNode actual = mapper.readTree("[" + jsonString + "]");
		assertEquals(expected, actual);
	}

	/**
	 * Some of the controllers have edge cases that produce invalid JSON, even
	 * when wrapped in enclosing brackets. For those cases, call this method,
	 * massage the result to form valid JSON, and call assertKlugedJson().
	 */
	protected static String getJsonFromController(
			FreemarkerHttpServlet controller, HttpServletRequest req)
			throws Exception {
		ResponseValues rv = controller.processRequest(new VitroRequest(req));
		assertTrue(rv instanceof TemplateResponseValues);
		TemplateResponseValues trv = (TemplateResponseValues) rv;

		Object o = trv.getMap().get("jsonTree");
		assertTrue(o instanceof String);
		String jsonString = (String) o;
		return jsonString;
	}

	/**
	 * If it was necessary to manipulate the response from the controller, this
	 * is how to test it.
	 */
	protected void assertKlugedJson(JsonNode expected,
			String klugedActualString) throws Exception {
		JsonNode actual = mapper.readTree("[" + klugedActualString + "]");
		assertEquals(expected, actual);
	}

	private static String urlEncode(String uri) {
		try {
			return URLEncoder.encode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return uri;
		}
	}
}
