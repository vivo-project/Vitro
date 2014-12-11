/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import static org.junit.Assert.*;
import static edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner.*;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * For now, just test the methods that manipulate the query string.
 */
public class SparqlQueryRunnerTest extends AbstractTestClass {
	@Test
	public void bindValuesNameNotFound() {
		String raw = "No such name here";
		String expected = raw;
		assertEquals(expected, bindValues(raw, uriValue("bogus", "BOGUS")));
	}

	@Test
	public void bindOneUri() {
		String raw = "Replace both ?this and ?this also.";
		String expected = "Replace both <URI> and <URI> also.";
		assertEquals(expected, bindValues(raw, uriValue("this", "URI")));
	}

	@Test
	public void bindTwoUris() {
		String raw = "Replace both ?this and ?that also.";
		String expected = "Replace both <URI> and <ANOTHER> also.";
		assertEquals(
				expected,
				bindValues(raw, uriValue("this", "URI"),
						uriValue("that", "ANOTHER")));
	}

	@Test
	public void honorWordBoundary() {
		String raw = "Replace ?this but not ?thistle.";
		String expected = "Replace <URI> but not ?thistle.";
		assertEquals(expected, bindValues(raw, uriValue("this", "URI")));
	}

	@Test
	public void honorStringLimit() {
		String raw = "?this";
		String expected = "<URI>";
		assertEquals(expected, bindValues(raw, uriValue("this", "URI")));
	}

	private static final String REAL_WORLD_RAW = "" //
			+ "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" //
			+ "\n" //
			+ "SELECT DISTINCT ?context ?config \n" //
			+ "WHERE { \n" //
			+ "    ?context a :ConfigContext ; \n" //
			+ "        :configContextFor ?baseUri ; \n" //
			+ "        :qualifiedByDomain ?domainUri ; \n" //
			+ "        :qualifiedBy ?rangeUri ; \n" //
			+ "        :hasConfiguration ?config . \n" //
			+ "} \n"; //

	private static final String REAL_WORLD_EXPECTED = "" //
			+ "PREFIX : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" //
			+ "\n" //
			+ "SELECT DISTINCT ?context ?config \n" //
			+ "WHERE { \n" //
			+ "    ?context a :ConfigContext ; \n" //
			+ "        :configContextFor <http://vivoweb.org/ontology/core#relates> ; \n" //
			+ "        :qualifiedByDomain <http://vivoweb.org/ontology/core#Contract> ; \n" //
			+ "        :qualifiedBy <http://vivoweb.org/ontology/core#ResearcherRole> ; \n" //
			+ "        :hasConfiguration ?config . \n" //
			+ "} \n"; //

	@Test
	public void realWorldExample() {
		assertEquals(
				REAL_WORLD_EXPECTED,
				bindValues(
						REAL_WORLD_RAW,
						uriValue("baseUri",
								"http://vivoweb.org/ontology/core#relates"),
						uriValue("domainUri",
								"http://vivoweb.org/ontology/core#Contract"),
						uriValue("rangeUri",
								"http://vivoweb.org/ontology/core#ResearcherRole")));
	}

}
