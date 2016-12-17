/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;

/**
 * Test that the SparqlQueryApiExecutor can handle all query types and all
 * formats.
 */
public class SparqlQueryApiExecutorTest extends AbstractTestClass {

	/* SPARQL response types */
	private static final String ACCEPT_TEXT = "text/plain";
	private static final String ACCEPT_CSV = "text/csv";
	private static final String ACCEPT_TSV = "text/tab-separated-values";
	private static final String ACCEPT_SPARQL_XML = "application/sparql-results+xml";
	private static final String ACCEPT_SPARQL_JSON = "application/sparql-results+json";

	/* RDF result types */
	private static final String ACCEPT_RDFXML = "application/rdf+xml";
	private static final String ACCEPT_N3 = "text/n3";
	private static final String ACCEPT_TURTLE = "text/turtle";
	private static final String ACCEPT_JSON = "application/json";

	private static final String MODEL_CONTENTS_N3 = "" //
			+ "<http://here.edu/subject> \n"
			+ "    <http://here.edu/predicate> <http://here.edu/object> ."
			+ "<http://here.edu/s2> \n"
			+ "    <http://here.edu/p2> <http://here.edu/o2> .";
	private static final String BASE_URI = "http://here.edu";

	private static final String SELECT_ALL_QUERY = "SELECT ?s ?p ?o WHERE {?s ?p ?o} ORDER BY DESC(?s)";
	private static final String SELECT_RESULT_TEXT = ""
			+ "--------------------------------------------------------------------------------------\n"
			+ "| s                         | p                           | o                        |\n"
			+ "======================================================================================\n"
			+ "| <http://here.edu/subject> | <http://here.edu/predicate> | <http://here.edu/object> |\n"
			+ "| <http://here.edu/s2>      | <http://here.edu/p2>        | <http://here.edu/o2>     |\n"
			+ "--------------------------------------------------------------------------------------\n";
	private static final String SELECT_RESULT_CSV = "s,p,o\n"
			+ "http://here.edu/subject,http://here.edu/predicate,http://here.edu/object\n"
			+ "http://here.edu/s2,http://here.edu/p2,http://here.edu/o2\n";
	private static final String SELECT_RESULT_TSV = "s\tp\to\n"
			+ "http://here.edu/subject\thttp://here.edu/predicate\thttp://here.edu/object\n"
			+ "http://here.edu/s2\thttp://here.edu/p2\thttp://here.edu/o2\n";
	private static final String SELECT_RESULT_XML = "" //
			+ "<?xml version=\"1.0\"?>\n" //
			+ "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n" //
			+ "  <head>\n" //
			+ "    <variable name=\"s\"/>\n" //
			+ "    <variable name=\"p\"/>\n" //
			+ "    <variable name=\"o\"/>\n" //
			+ "  </head>\n" //
			+ "  <results>\n" //
			+ "    <result>\n" //
			+ "      <binding name=\"s\">\n" //
			+ "        <uri>http://here.edu/subject</uri>\n" //
			+ "      </binding>\n" //
			+ "      <binding name=\"p\">\n" //
			+ "        <uri>http://here.edu/predicate</uri>\n" //
			+ "      </binding>\n" //
			+ "      <binding name=\"o\">\n" //
			+ "        <uri>http://here.edu/object</uri>\n" //
			+ "      </binding>\n" //
			+ "    </result>\n" //
			+ "    <result>\n" //
			+ "      <binding name=\"s\">\n" //
			+ "        <uri>http://here.edu/s2</uri>\n" //
			+ "      </binding>\n" //
			+ "      <binding name=\"p\">\n" //
			+ "        <uri>http://here.edu/p2</uri>\n" //
			+ "      </binding>\n" //
			+ "      <binding name=\"o\">\n" //
			+ "        <uri>http://here.edu/o2</uri>\n" //
			+ "      </binding>\n" //
			+ "    </result>\n" //
			+ "  </results>\n" //
			+ "</sparql>\n";
	private static final String SELECT_RESULT_JSON = "" //
			+ "{\n" //
			+ "  \"head\": {\n" //
			+ "    \"vars\": [ \"s\" , \"p\" , \"o\" ]\n" //
			+ "  } ,\n" //
			+ "  \"results\": {\n" //
			+ "    \"bindings\": [\n" //
			+ "      {\n" //
			+ "        \"s\": { \"type\": \"uri\" , \"value\": \"http://here.edu/subject\" } ,\n"
			+ "        \"p\": { \"type\": \"uri\" , \"value\": \"http://here.edu/predicate\" } ,\n"
			+ "        \"o\": { \"type\": \"uri\" , \"value\": \"http://here.edu/object\" }\n"
			+ "      } ,\n" //
			+ "      {\n" //
			+ "        \"s\": { \"type\": \"uri\" , \"value\": \"http://here.edu/s2\" } ,\n"
			+ "        \"p\": { \"type\": \"uri\" , \"value\": \"http://here.edu/p2\" } ,\n"
			+ "        \"o\": { \"type\": \"uri\" , \"value\": \"http://here.edu/o2\" }\n"
			+ "      }\n" //
			+ "    ]\n" //
			+ "  }\n" //
			+ "}\n";

	private static final String ASK_ALL_QUERY = "ASK WHERE {?s ?p ?o}";
	private static final String ASK_RESULT_TEXT = "true";
	private static final String ASK_RESULT_CSV = "true";
	private static final String ASK_RESULT_TSV = "true";
	private static final String ASK_RESULT_XML = "" //
			+ "<?xml version=\"1.0\"?>\n" //
			+ "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n" //
			+ "  <head></head>\n" //
			+ "  <boolean>true</boolean>\n" //
			+ "</sparql>";
	private static final String ASK_RESULT_JSON = "" //
			+ "{\n" //
			+ "  \"head\" : { } ,\n" //
			+ "  \"boolean\" : true\n" //
			+ "}\n";

	private static final String CONSTRUCT_ALL_QUERY = "CONSTRUCT {?s ?p ?o} WHERE { LET (?s := <http://here.edu/subject>) <http://here.edu/subject> ?p ?o}";
	private static final String CONSTRUCT_RESULT_TEXT = "" //
			+ "<http://here.edu/subject> <http://here.edu/predicate> <http://here.edu/object> .\n";
	private static final String CONSTRUCT_RESULT_TURTLE = "" //
			+ "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .\n" //
			+ "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .\n" //
			+ "@prefix owl:     <http://www.w3.org/2002/07/owl#> .\n" //
			+ "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" //
			+ "\n" //
			+ "<http://here.edu/subject>\n" //
			+ "      <http://here.edu/predicate>\n" //
			+ "              <http://here.edu/object> .\n";
	private static final String CONSTRUCT_RESULT_N3 = "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .\n"
			+ "@prefix owl:     <http://www.w3.org/2002/07/owl#> .\n"
			+ "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "\n"
			+ "<http://here.edu/subject>\n"
			+ "      <http://here.edu/predicate>\n"
			+ "              <http://here.edu/object> .\n";
	private static final String CONSTRUCT_RESULT_RDFXML = "<rdf:RDF\n"
			+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
			+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
			+ "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
			+ "    xmlns:j.0=\"http://here.edu/\"\n"
			+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" > \n"
			+ "  <rdf:Description rdf:about=\"http://here.edu/subject\">\n"
			+ "    <j.0:predicate rdf:resource=\"http://here.edu/object\"/>\n"
			+ "  </rdf:Description>\n" //
			+ "</rdf:RDF>\n";
	private static final String CONSTRUCT_RESULT_JSONLD = "{\n" +
			"  \"@id\" : \"http://here.edu/subject\",\n" +
			"  \"predicate\" : \"http://here.edu/object\",\n" +
			"  \"@context\" : {\n" +
			"    \"predicate\" : {\n" +
			"      \"@id\" : \"http://here.edu/predicate\",\n" +
			"      \"@type\" : \"@id\"\n" +
			"    },\n" +
			"    \"rdf\" : \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\n" +
			"    \"owl\" : \"http://www.w3.org/2002/07/owl#\",\n" +
			"    \"xsd\" : \"http://www.w3.org/2001/XMLSchema#\",\n" +
			"    \"rdfs\" : \"http://www.w3.org/2000/01/rdf-schema#\"\n" +
			"  }\n" +
			"}\n";

	private static final String DESCRIBE_ALL_QUERY = "DESCRIBE <http://here.edu/subject>";
	private static final String DESCRIBE_RESULT_TEXT = "<http://here.edu/subject> "
			+ "<http://here.edu/predicate> <http://here.edu/object> .\n";
	private static final String DESCRIBE_RESULT_RDFXML = "<rdf:RDF\n"
			+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
			+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
			+ "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
			+ "    xmlns:j.0=\"http://here.edu/\"\n"
			+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" > \n"
			+ "  <rdf:Description rdf:about=\"http://here.edu/subject\">\n"
			+ "    <j.0:predicate rdf:resource=\"http://here.edu/object\"/>\n"
			+ "  </rdf:Description>\n" + "</rdf:RDF>\n";
	private static final String DESCRIBE_RESULT_N3 = "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .\n"
			+ "@prefix owl:     <http://www.w3.org/2002/07/owl#> .\n"
			+ "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "\n"
			+ "<http://here.edu/subject>\n"
			+ "      <http://here.edu/predicate>\n"
			+ "              <http://here.edu/object> .\n";
	private static final String DESCRIBE_RESULT_TURTLE = "" //
			+ "@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .\n" //
			+ "@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .\n" //
			+ "@prefix owl:     <http://www.w3.org/2002/07/owl#> .\n" //
			+ "@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" //
			+ "\n" //
			+ "<http://here.edu/subject>\n" //
			+ "      <http://here.edu/predicate>\n" //
			+ "              <http://here.edu/object> .\n";
	private static final String DESCRIBE_RESULT_JSONLD =  "{\n" +
			"  \"@id\" : \"http://here.edu/subject\",\n" +
			"  \"predicate\" : \"http://here.edu/object\",\n" +
			"  \"@context\" : {\n" +
			"    \"predicate\" : {\n" +
			"      \"@id\" : \"http://here.edu/predicate\",\n" +
			"      \"@type\" : \"@id\"\n" +
			"    },\n" +
			"    \"rdf\" : \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\n" +
			"    \"owl\" : \"http://www.w3.org/2002/07/owl#\",\n" +
			"    \"xsd\" : \"http://www.w3.org/2001/XMLSchema#\",\n" +
			"    \"rdfs\" : \"http://www.w3.org/2000/01/rdf-schema#\"\n" +
			"  }\n" +
			"}\n";

	private OntModel model;
	private RDFService rdfService;

	@Before
	public void setup() {
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.read(new StringReader(MODEL_CONTENTS_N3), BASE_URI, "N3");
		rdfService = new RDFServiceModel(model);
	}

	// ----------------------------------------------------------------------
	// Tests
	// ----------------------------------------------------------------------

	@Test(expected = NullPointerException.class)
	public void nullRdfService() throws Exception {
		SparqlQueryApiExecutor.instance(null, SELECT_ALL_QUERY, ACCEPT_TEXT);
	}

	@Test(expected = NullPointerException.class)
	public void nullQuery() throws Exception {
		SparqlQueryApiExecutor.instance(rdfService, null, ACCEPT_TEXT);
		fail("nullQuery not implemented");
	}

	@Test(expected = QueryParseException.class)
	public void emptyQuery() throws Exception {
		SparqlQueryApiExecutor.instance(rdfService, "", ACCEPT_TEXT);
		fail("emptyQuery not implemented");
	}

	@Test(expected = QueryParseException.class)
	public void cantParseQuery() throws Exception {
		SparqlQueryApiExecutor.instance(rdfService, "BOGUS", ACCEPT_TEXT);
		fail("cantParseQuery not implemented");
	}

	// Can't figure out how to create a Query of a type other than SELECT, ASK,
	// CONSTRUCT and DESCRIBE.

	// Null accept header is treated as "*/*"

	@Test(expected = NotAcceptableException.class)
	public void noAcceptableContentType() throws Exception {
		SparqlQueryApiExecutor.instance(rdfService, SELECT_ALL_QUERY, "bogus");
		fail("noAcceptableContentType not implemented");
	}

	// ----------------------------------------------------------------------

	@Test
	public void selectToText() throws Exception {
		executeQuery("select to text", SELECT_ALL_QUERY, ACCEPT_TEXT,
				SELECT_RESULT_TEXT);
	}

	@Test
	public void selectToCsv() throws Exception {
		executeQuery("select to csv", SELECT_ALL_QUERY, ACCEPT_CSV,
				SELECT_RESULT_CSV);
	}

	@Test
	public void selectToTsv() throws Exception {
		executeQuery("select to tsv", SELECT_ALL_QUERY, ACCEPT_TSV,
				SELECT_RESULT_TSV);
	}

	@Test
	public void selectToXml() throws Exception {
		executeQuery("select to xml", SELECT_ALL_QUERY, ACCEPT_SPARQL_XML,
				SELECT_RESULT_XML);
	}

	@Test
	public void selectToJson() throws Exception {
		executeQuery("select to json", SELECT_ALL_QUERY, ACCEPT_SPARQL_JSON,
				SELECT_RESULT_JSON);
	}

	@Test
	public void selectWithInvalidContentType() throws Exception {
		executeWithInvalidAcceptHeader("select with application/rdf+xml",
				SELECT_ALL_QUERY, ACCEPT_RDFXML);
		executeWithInvalidAcceptHeader("select with text/n3", SELECT_ALL_QUERY,
				ACCEPT_N3);
		executeWithInvalidAcceptHeader("select with text/turtle",
				SELECT_ALL_QUERY, ACCEPT_TURTLE);
		executeWithInvalidAcceptHeader("select with application/json",
				SELECT_ALL_QUERY, ACCEPT_JSON);
	}

	// ----------------------------------------------------------------------

	@Test
	public void askToText() throws Exception {
		executeQuery("ask to text", ASK_ALL_QUERY, ACCEPT_TEXT, ASK_RESULT_TEXT);
	}

	@Test
	public void askToCsv() throws Exception {
		executeQuery("ask to csv", ASK_ALL_QUERY, ACCEPT_CSV, ASK_RESULT_CSV);
	}

	@Test
	public void askToTsv() throws Exception {
		executeQuery("ask to tsv", ASK_ALL_QUERY, ACCEPT_TSV, ASK_RESULT_TSV);
	}

	@Test
	public void askToXml() throws Exception {
		executeQuery("ask to xml", ASK_ALL_QUERY, ACCEPT_SPARQL_XML,
				ASK_RESULT_XML);
	}

	@Test
	public void askToJson() throws Exception {
		executeQuery("ask to json", ASK_ALL_QUERY, ACCEPT_SPARQL_JSON,
				ASK_RESULT_JSON);
	}

	@Test
	public void askWithInvalidAcceptHeader() throws Exception {
		executeWithInvalidAcceptHeader("ask with application/rdf+xml",
				ASK_ALL_QUERY, ACCEPT_RDFXML);
		executeWithInvalidAcceptHeader("ask with text/n3", ASK_ALL_QUERY,
				ACCEPT_N3);
		executeWithInvalidAcceptHeader("ask with text/turtle", ASK_ALL_QUERY,
				ACCEPT_TURTLE);
		executeWithInvalidAcceptHeader("ask with application/json",
				ASK_ALL_QUERY, ACCEPT_JSON);
	}

	// ----------------------------------------------------------------------

	@Test
	public void constructToText() throws Exception {
		executeQuery("construct to text", CONSTRUCT_ALL_QUERY, ACCEPT_TEXT,
				CONSTRUCT_RESULT_TEXT);
	}

	@Test
	public void constructToRdfXml() throws Exception {
		executeQuery("construct to rdf/xml", CONSTRUCT_ALL_QUERY,
				ACCEPT_RDFXML, CONSTRUCT_RESULT_RDFXML);
	}

	@Test
	public void constructToN3() throws Exception {
		executeQuery("construct to n3", CONSTRUCT_ALL_QUERY, ACCEPT_N3,
				CONSTRUCT_RESULT_N3);
	}

	@Test
	public void constructToTurtle() throws Exception {
		executeQuery("construct to turtle", CONSTRUCT_ALL_QUERY, ACCEPT_TURTLE,
				CONSTRUCT_RESULT_TURTLE);
	}

	@Test
	public void constructToJsonld() throws Exception {
		executeQuery("construct to JSON-LD", CONSTRUCT_ALL_QUERY, ACCEPT_JSON,
				CONSTRUCT_RESULT_JSONLD);
	}

	@Test
	public void constructWithInvalidAcceptHeader() throws Exception {
		executeWithInvalidAcceptHeader("construct with text/csv",
				CONSTRUCT_ALL_QUERY, ACCEPT_CSV);
		executeWithInvalidAcceptHeader("construct with text/tsv",
				CONSTRUCT_ALL_QUERY, "text/tsv");
		executeWithInvalidAcceptHeader(
				"construct with application/sparql-results+xml",
				CONSTRUCT_ALL_QUERY, ACCEPT_SPARQL_XML);
		executeWithInvalidAcceptHeader(
				"construct with application/sparql-results+json",
				CONSTRUCT_ALL_QUERY, ACCEPT_SPARQL_JSON);
	}

	// ----------------------------------------------------------------------

	@Test
	public void describeToText() throws Exception {
		executeQuery("describe to text", DESCRIBE_ALL_QUERY, ACCEPT_TEXT,
				DESCRIBE_RESULT_TEXT);
	}

	@Test
	public void describeToRdfXml() throws Exception {
		executeQuery("describe to rdf/xml", DESCRIBE_ALL_QUERY, ACCEPT_RDFXML,
				DESCRIBE_RESULT_RDFXML);
	}

	@Test
	public void describeToN3() throws Exception {
		executeQuery("describe to n3", DESCRIBE_ALL_QUERY, ACCEPT_N3,
				DESCRIBE_RESULT_N3);
	}

	@Test
	public void describeToTurtle() throws Exception {
		executeQuery("describe to turtle", DESCRIBE_ALL_QUERY, ACCEPT_TURTLE,
				DESCRIBE_RESULT_TURTLE);
	}

	@Test
	public void describeToJsonld() throws Exception {
		executeQuery("describe to JSON-LD", DESCRIBE_ALL_QUERY, ACCEPT_JSON,
				DESCRIBE_RESULT_JSONLD);
	}

	@Test
	public void describeWithInvalidAcceptHeader() throws Exception {
		executeWithInvalidAcceptHeader("describe with text/csv",
				DESCRIBE_ALL_QUERY, ACCEPT_CSV);
		executeWithInvalidAcceptHeader("describe with text/tsv",
				DESCRIBE_ALL_QUERY, "text/tsv");
		executeWithInvalidAcceptHeader(
				"describe with application/sparql-results+xml",
				DESCRIBE_ALL_QUERY, ACCEPT_SPARQL_XML);
		executeWithInvalidAcceptHeader(
				"describe with application/sparql-results+json",
				DESCRIBE_ALL_QUERY, ACCEPT_SPARQL_JSON);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void executeQuery(String message, String queryString,
			String acceptHeader, String expected) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		SparqlQueryApiExecutor executor = SparqlQueryApiExecutor.instance(
				rdfService, queryString, acceptHeader);
		executor.executeAndFormat(out);

		if (ACCEPT_RDFXML.equals(acceptHeader)) {
			assertEquivalentRdfxml(message, expected, out.toString());
		} else if (ACCEPT_TURTLE.equals(acceptHeader)) {
			assertEquivalentTurtle(message, expected, out.toString());
		} else if (ACCEPT_N3.equals(acceptHeader)) {
			assertEquivalentN3(message, expected, out.toString());
		} else {
			assertEqualsIgnoreWhiteSpace(message, expected, out.toString());
		}
	}

	/**
	 * RDF/XML namespaces may come in any order, so separate them out and test
	 * accordingly.
	 */
	private void assertEquivalentRdfxml(String message, String expected,
			String actual) {
		assertEquals(message, getRdfxmlNamespaces(expected),
				getRdfxmlNamespaces(actual));
		assertEqualsIgnoreWhiteSpace(message, omitRdfxmlNamespaces(expected),
				omitRdfxmlNamespaces(actual));
	}

	private Set<String> getRdfxmlNamespaces(String rdfxml) {
		Set<String> namespaces = new TreeSet<>();
		Pattern p = Pattern.compile("xmlns:\\w+=\\\"[^\\\"]*\\\"");
		Matcher m = p.matcher(rdfxml);
		while (m.find()) {
			namespaces.add(m.group());
		}
		return namespaces;
	}

	private String omitRdfxmlNamespaces(String rdfxml) {
		return rdfxml.replaceAll("xmlns:\\w+=\\\"[^\\\"]*\\\"", "");
	}

	/**
	 * TTL prefix lines may come in any order, so separate them out and test
	 * accordingly.
	 */
	private void assertEquivalentTurtle(String message, String expected,
			String actual) {
		assertEquals(message, getTurtlePrefixes(expected),
				getTurtlePrefixes(actual));
		assertEqualsIgnoreWhiteSpace(message, getTurtleRemainder(expected),
				getTurtleRemainder(actual));
	}

	/**
	 * N3 is like TTL, as far as prefix lines are concerned.
	 */
	private void assertEquivalentN3(String message, String expected,
			String actual) {
		assertEquivalentTurtle(message, expected, actual);
	}

	private Set<String> getTurtlePrefixes(String ttl) {
		Set<String> prefixes = new TreeSet<>();
		for (String line : ttl.split("[\\n\\r]+")) {
			if (line.startsWith("@prefix")) {
				prefixes.add(line.replaceAll("\\s+", " "));
			}
		}
		return prefixes;
	}

	private String getTurtleRemainder(String ttl) {
		List<String> remainder = new ArrayList<>();
		for (String line : ttl.split("[\\n\\r]+")) {
			if (!line.startsWith("@prefix")) {
				remainder.add(line);
			}
		}
		return StringUtils.join(remainder, "\n");
	}

	private void assertEqualsIgnoreWhiteSpace(String message, String expected,
			String actual) {
		assertEquals(message, expected.replaceAll("\\s+", " "),
				actual.replaceAll("\\s+", " "));
	}

	private void executeWithInvalidAcceptHeader(String message,
			String queryString, String acceptHeader) throws Exception {
		try {
			SparqlQueryApiExecutor.instance(rdfService, queryString,
					acceptHeader);
			fail(message + " - Expected a NotAcceptableException");
		} catch (NotAcceptableException e) {
			// expected
		}
	}
}
