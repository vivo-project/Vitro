/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccessStub;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

/**
 * TODO
 * 
 * If the statement qualifies, execute the queries and return the accumulated
 * results.
 * 
 * A statement qualifies if the predicate matches any of the restrictions, or if
 * there are no restrictions.
 * 
 * If a query contains a ?subject or ?object variable, it will be bound to the
 * URI of the subject or object of the statement, respectively. If the subject
 * or object has no URI for the query, then the query will be ignored.
 * 
 * All of the result fields of all result rows of all of the queries will be
 * returned.
 * 
 * A label may be supplied to the instance, for use in logging. If no label is
 * supplied, one will be generated.
 */
public class SelectQueryUriFinderTest extends AbstractTestClass {
	private static final Log log = LogFactory
			.getLog(SelectQueryUriFinderTest.class);

	private static final String BOB_URI = "http://ns#Bob";
	private static final String BETTY_URI = "http://ns#Betty";
	private static final String DICK_URI = "http://ns#Dick";
	private static final String JANE_URI = "http://ns#Jane";
	private static final String FRIEND_URI = "http://ns#Friend";
	private static final String SEES_URI = "http://ns#Sees";
	private static final String OTHER_URI = "http://ns#Other";

	private static final Resource BOB = createResource(BOB_URI);
	private static final Resource BETTY = createResource(BETTY_URI);
	private static final Resource DICK = createResource(DICK_URI);
	private static final Resource JANE = createResource(JANE_URI);
	private static final Property FRIEND = createProperty(FRIEND_URI);
	private static final Property SEES = createProperty(SEES_URI);

	private static final String QUERY1 = "SELECT ?friend WHERE {?subject <"
			+ FRIEND_URI + "> ?friend}";
	private static final String QUERY2 = "SELECT ?partner WHERE {?object <"
			+ FRIEND_URI + "> ?partner}";

	private Model m;
	private RDFService rdfService;
	private SelectQueryUriFinder finder;
	private List<String> foundUris;

	@Before
	public void populateModel() {
		m = ModelFactory.createDefaultModel();
		m.add(createStatement(BOB, FRIEND, BETTY));
		m.add(createStatement(DICK, FRIEND, JANE));

		rdfService = new RDFServiceModel(m);

		ContextModelAccessStub models = new ContextModelAccessStub();
		models.setRDFService(CONTENT, rdfService);

		finder = new SelectQueryUriFinder();
		finder.setContextModels(models);
		finder.addQuery(QUERY1);
		finder.addQuery(QUERY2);
	}

	@Test
	public void fullSuccess_bothResults() {
		setPredicateRestrictions();
		exerciseUriFinder(BOB, SEES, DICK);
		assertExpectedUris(BETTY_URI, JANE_URI);
	}

	@Test
	public void acceptableRestriction_bothResults() {
		setPredicateRestrictions(SEES_URI);
		exerciseUriFinder(BOB, SEES, DICK);
		assertExpectedUris(BETTY_URI, JANE_URI);
	}

	@Test
	public void excludingRestriction_noResults() {
		setPredicateRestrictions(OTHER_URI);
		exerciseUriFinder(BOB, SEES, DICK);
		assertExpectedUris();
	}

	@Test
	public void blankSubject_justObjectResult() {
		setPredicateRestrictions();
		exerciseUriFinder(createResource(), SEES, DICK);
		assertExpectedUris(JANE_URI);
	}

	@Test
	public void literalObject_justSubjectResult() {
		setPredicateRestrictions();
		exerciseUriFinder(BOB, SEES, createPlainLiteral("Bogus"));
		assertExpectedUris(BETTY_URI);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void setPredicateRestrictions(String... uris) {
		for (String uri : uris) {
			finder.addPredicateRestriction(uri);
		}
	}

	private void exerciseUriFinder(Resource subject, Property predicate,
			RDFNode object) {
		foundUris = finder.findAdditionalURIsToIndex(createStatement(subject,
				predicate, object));
	}

	private void assertExpectedUris(String... expectedArray) {
		Set<String> expected = new HashSet<>(Arrays.asList(expectedArray));
		Set<String> actual = new HashSet<>(foundUris);
		assertEquals("found URIs", expected, actual);
	}

}
