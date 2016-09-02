/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * TODO
 */
public class QueryUtilsTest extends AbstractTestClass {

	// ----------------------------------------------------------------------
	// Test bindVariables
	// ----------------------------------------------------------------------

	private QuerySolutionMap bindings = new QuerySolutionMap();

	@Test
	public void bindResource() {
		bindings.add("uri", ResourceFactory.createResource("http://my.uri"));
		assertBoundQueryEquals("a resource ?uri", "a resource <http://my.uri>");
	}

	@Test
	public void bindPlainLiteral() {
		bindings.add("plain", ResourceFactory.createPlainLiteral("too easy"));
		assertBoundQueryEquals("This is ?plain ?plain",
				"This is \"too easy\" \"too easy\"");
	}

	@Test
	public void bindTypedLiteral() {
		bindings.add("typed", ResourceFactory.createTypedLiteral(100L));
		assertBoundQueryEquals("take this ?typed number",
				"take this \"100\"^^<http://www.w3.org/2001/XMLSchema#long> number");
	}

	@Test
	public void bindLanguageLiteral() {
		Literal l = ModelFactory.createDefaultModel().createLiteral("Spanish",
				"es-ES");
		bindings.add("lang", l);
		assertBoundQueryEquals("speak my ?lang?", "speak my \"Spanish\"@es-ES?");
	}

	@Ignore
	@Test
	public void bindAnon() {
		fail("bindAnon not implemented");
	}

	// ----------------------------------------------------------------------

	private void assertBoundQueryEquals(String template, String expected) {
		String actual = QueryUtils.bindVariables(template, bindings);
		assertEquals("bounding results", expected, actual);
	}

	// ----------------------------------------------------------------------
	// Test removeDuplicatesMapsFromList
	// ----------------------------------------------------------------------

	private List<Map<String, String>> theList = list(
			map(pair("id", "1"), pair("color", "blue"), pair("size", "large")),
			map(pair("id", "2"), pair("color", "red"), pair("size", "large"),
					pair("parity", "odd")));
	private List<Map<String, String>> filteredList;

	@Test
	public void noKeys() {
		assertExpectedIDs(ids("1", "2"), keys());
	}

	@Test
	public void emptyList() {
		theList = new ArrayList<>();
		assertExpectedIDs(ids(), keys("color"));
	}

	@Test
	public void unrecognizedKey() {
		assertExpectedIDs(ids("1", "2"), keys("bogus"));
	}

	@Test
	public void unmatchedKey() {
		assertExpectedIDs(ids("1", "2"), keys("parity"));
	}

	@Test
	public void foundDuplicate() {
		assertExpectedIDs(ids("1"), keys("size"));
	}

	@Test
	public void noDuplicates() {
		assertExpectedIDs(ids("1", "2"), keys("color"));
	}

	@Test
	public void matchOneKeyOfMany() {
		assertExpectedIDs(ids("1"), keys("color", "size"));
	}

	@Test
	public void multipleDuplicatesOfASingleRecord() {
		theList.add(map(pair("id", "3"), pair("size", "large")));
		assertExpectedIDs(ids("1"), keys("color", "size"));
	}

	// ----------------------------------------------------------------------

	private void assertExpectedIDs(String[] ids, String[] keys) {
		filteredList = QueryUtils.removeDuplicatesMapsFromList(theList, keys);
		assertEquals("ids", Arrays.asList(ids), idsInFilteredList());
	}

	private List<String> idsInFilteredList() {
		List<String> ids = new ArrayList<>();
		for (Map<String, String> map : filteredList) {
			String id = map.get("id");
			if (id == null) {
				fail("ID was null");
			} else {
				ids.add(id);
			}
		}
		return ids;
	}

	@SafeVarargs
	private final List<Map<String, String>> list(Map<String, String>... maps) {
		return new ArrayList<>(Arrays.asList(maps));
	}

	private Map<String, String> map(String[]... pairs) {
		Map<String, String> map = new HashMap<>();
		for (String[] pair : pairs) {
			map.put(pair[0], pair[1]);
		}
		return map;
	}

	private String[] pair(String... s) {
		return s;
	}

	private String[] keys(String... keys) {
		return keys;
	}

	private String[] ids(String... ids) {
		return ids;
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

}
