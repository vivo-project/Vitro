/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * TODO
 */
public class QueryUtilsTest extends AbstractTestClass {
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
	// Helper methods
	// ----------------------------------------------------------------------

	private void assertBoundQueryEquals(String template, String expected) {
		String actual = QueryUtils.bindVariables(template, bindings);
		assertEquals("bounding results", expected, actual);
	}
}
