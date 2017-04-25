/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * TODO
 */
public class ListControllerTestBase extends AbstractTestClass {
	/**
	 * Based on the fact that each of these controllers returns a
	 * TemplateResponseValues object with a "jsonTree" in the body map.
	 *
	 * That jsonTree would be an standard JSON array, except that it is missing
	 * the enclosing brackets, so our expected result is also missing the
	 * brackets.
	 * 
	 * Add the brackets, read the strings, and compare.
	 */
	protected static void assertMatchingJson(FreemarkerHttpServlet controller,
			HttpServletRequest req, String expectedString) throws Exception {
		String jsonString = getJsonFromController(controller, req);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode expected = mapper.readTree("[" + expectedString + "]");
		JsonNode actual = mapper.readTree("[" + jsonString + "]");
		assertEquals(expected, actual);
	}

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
	 * Some of the edge cases do not return valid JSON, so we need to force a
	 * match.
	 */
	protected static void assertKlugedJson(String expectedString,
			String klugedActualString) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode expected = mapper.readTree("[" + expectedString + "]");
		JsonNode actual = mapper.readTree("[" + klugedActualString + "]");
		assertEquals(expected, actual);
	}

}
