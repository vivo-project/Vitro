/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createConstructQueryContext;
import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;
import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.queryHolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

/**
 * Top-level smoke tests for the SparqlQueryRunnerTest. Exercise all of the
 * methods that create contexts, and show that we can do the simplest of
 * operations against them.
 */
public class SparqlQueryRunnerTest extends AbstractTestClass {
	private static final String SUBJECT_URI = "http://namespace/subject_uri";
	private static final String PREDICATE_URI = "http://namespace/predicate_uri";
	private static final String OBJECT_VALUE = "object_value";

	private static final String SELECT_QUERY = "SELECT ?s ?p ?o WHERE { ?s ?p ?o . }";
	private static final JsonObject EXPECTED_SELECT_RESULTS = JSON.parse(""
			+ "{  " //
			+ "  'head' : {  " //
			+ "    'vars' : [ 's', 'p', 'o' ]  " //
			+ "  } , " //
			+ "  'results' : {  " //
			+ "    'bindings' : [  " //
			+ "      {  " //
			+ "        'p' : {  " //
			+ "          'type' : 'uri' ,  " //
			+ "          'value' : 'http://namespace/predicate_uri'  " //
			+ "        } , " //
			+ "        'o' : {  " //
			+ "          'type' : 'literal' ,  " //
			+ "          'value' : 'object_value'  " //
			+ "        } , " //
			+ "        's' : {  " //
			+ "          'type' : 'uri' ,  " //
			+ "          'value' : 'http://namespace/subject_uri'  " //
			+ "        } " //
			+ "      }  " //
			+ "    ]  " //
			+ "  } " //
			+ "} ");

	private static final String CONSTRUCT_QUERY = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

	private Model model;
	private RDFService rdfService;
	private ByteArrayOutputStream buffer;
	private Model constructed;

	@Before
	public void setup() {
		model = model(dataProperty(SUBJECT_URI, PREDICATE_URI, OBJECT_VALUE));
		rdfService = new RDFServiceModel(model);
		buffer = new ByteArrayOutputStream();
	}

	// ----------------------------------------------------------------------
	// SELECT tests
	// ----------------------------------------------------------------------

	@Test
	public void selectQueryAgainstModel() {
		createSelectQueryContext(model, SELECT_QUERY).execute().writeToOutput(
				buffer);
		assertExpectedSelectResults();
	}

	@Test
	public void selectQueryHolderAgainstModel() {
		createSelectQueryContext(model, queryHolder(SELECT_QUERY)).execute()
				.writeToOutput(buffer);
		assertExpectedSelectResults();
	}

	@Test
	public void selectQueryAgainstRDFService() {
		createSelectQueryContext(rdfService, SELECT_QUERY).execute()
				.writeToOutput(buffer);
		assertExpectedSelectResults();
	}

	@Test
	public void selectQueryHolderAgainstRDFService() {
		createSelectQueryContext(rdfService, queryHolder(SELECT_QUERY))
				.execute().writeToOutput(buffer);
		assertExpectedSelectResults();
	}

	/**
	 * We've shown that all select contexts work. It should suffice that one of
	 * them can convert to string fields.
	 */
	@Test
	public void selectToStringFields() {
		List<String> objectValues = createSelectQueryContext(model,
				SELECT_QUERY).execute().toStringFields("o").flatten();
		assertEquals(Arrays.asList(OBJECT_VALUE), objectValues);
	}

	// ----------------------------------------------------------------------
	// CONSTRUCT tests
	// ----------------------------------------------------------------------

	@Test
	public void constructQueryAgainstModel() {
		constructed = createConstructQueryContext(model, CONSTRUCT_QUERY)
				.execute().toModel();
		assertExpectedConstructResults();
	}

	@Test
	public void constructQueryHolderAgainstModel() {
		constructed = createConstructQueryContext(model,
				queryHolder(CONSTRUCT_QUERY)).execute().toModel();
		assertExpectedConstructResults();
	}

	@Test
	public void constructQueryAgainstRDFService() {
		constructed = createConstructQueryContext(rdfService, CONSTRUCT_QUERY)
				.execute().toModel();
		assertExpectedConstructResults();
	}

	@Test
	public void constructQueryHolderAgainstRDFService() {
		constructed = createConstructQueryContext(rdfService,
				queryHolder(CONSTRUCT_QUERY)).execute().toModel();
		assertExpectedConstructResults();
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void assertExpectedSelectResults() {
		try {
			JsonObject actual = JSON.parse(buffer.toString("UTF-8"));
			assertEquals(EXPECTED_SELECT_RESULTS, actual);
		} catch (UnsupportedEncodingException e) {
			fail(e.toString());
		}
	}

	private void assertExpectedConstructResults() {
		assertEquals(model.listStatements().toSet(), constructed
				.listStatements().toSet());
	}
}
