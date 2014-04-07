/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.i18n.I18nStub;
import stubs.javax.servlet.ServletConfigStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.UserBasedIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.BasicPolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.AuthenticatorStub;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.AuthenticatorStub.Factory;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

/**
 * TODO
 */
public class SparqlQueryServletTest extends AbstractTestClass {
	private static final String MODEL_CONTENTS_N3 = "<http://here.edu/subject> \n"
			+ "    <http://here.edu/predicate> <http://here.edu/object> .";
	private static final String BASE_URI = "http://here.edu";

	private static final String SELECT_ALL_QUERY = "SELECT ?s ?p ?o WHERE {?s ?p ?o}";
	private static final String SELECT_RESULT_TEXT = ""
			+ "--------------------------------------------------------------------------------------\n"
			+ "| s                         | p                           | o                        |\n"
			+ "======================================================================================\n"
			+ "| <http://here.edu/subject> | <http://here.edu/predicate> | <http://here.edu/object> |\n"
			+ "--------------------------------------------------------------------------------------\n";
	private static final String SELECT_RESULT_CSV = "s,p,o\r\n"
			+ "http://here.edu/subject,http://here.edu/predicate,http://here.edu/object\r\n";
	private static final String SELECT_RESULT_TSV = "s\tp\to\r\n"
			+ "http://here.edu/subject\thttp://here.edu/predicate\thttp://here.edu/object\r\n";
	private static final String SELECT_RESULT_XML = "<?xml version=\"1.0\"?>"
			+ "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">"
			+ "<head>" + "<variable name=\"x\"/>"
			+ "<variable name=\"hpage\"/>" + "</head>" + "<results>"
			+ "<result>" + "<binding name=\"x\"> ... </binding>"
			+ "<binding name=\"hpage\"> ... </binding>" + "</result>"
			+ "</results>" + "</sparql>";
	private static final String SELECT_RESULT_JSON = "{\"head\": { \"vars\": [ \"s\" , \"o\", \"p\" ]  } ,"
			+ "\"results\": {"
			+ "\"bindings\": ["
			+ " {"
			+ "\"s\": { \"type\": \"uri\" , \"value\": \"http://here.edu/subject\" } ,"
			+ "\"p\": { \"type\": \"uri\" , \"value\": \"http://here.edu/predicate\" } ,"
			+ "\"o\": { \"type\": \"uri\" , \"value\": \"http://here.edu/object\" } ,"
			+ "}" + "]" + "}" + "}";
	private static final String ACCEPTABLE_FOR_SELECT = "For SELECT queries, Accept header must be one of "
			+ "'text/plain', 'text/csv', 'text/tsv', 'application/sparql-results+xml', "
			+ "or 'application/sparql-results+json'";

	private static final String ASK_ALL_QUERY = "ASK WHERE {?s ?p ?o}";
	private static final String ASK_RESULT_TEXT = "true";
	private static final String ASK_RESULT_CSV = "true";
	private static final String ASK_RESULT_TSV = "true";
	private static final String ASK_RESULT_XML = "<?xml version=\"1.0\"?>"
			+ "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">"
			+ "<head></head><boolean>true</boolean></sparql>";
	private static final String ASK_RESULT_JSON = "{\"head\" : { } , \"boolean\" : true}";
	private static final String ACCEPTABLE_FOR_ASK = "For ASK queries, Accept header must be one of "
			+ "'text/plain', 'text/csv', 'text/tsv', 'application/sparql-results+xml', "
			+ "or 'application/sparql-results+json'";

	private static final String CONSTRUCT_ALL_QUERY = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
	private static final String CONSTRUCT_RESULT_TEXT = "<http://here.edu/subject> "
			+ "<http://here.edu/predicate> <http://here.edu/object> .\n";
	private static final String CONSTRUCT_RESULT_TURTLE = "<rdf:RDF\n"
			+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
			+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
			+ "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
			+ "    xmlns:j.0=\"http://here.edu/\"\n"
			+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
			+ "  <rdf:Description rdf:about=\"http://here.edu/subject\">\n"
			+ "    <j.0:predicate rdf:resource=\"http://here.edu/object\"/>\n"
			+ "  </rdf:Description>\n" + "</rdf:RDF>\n";
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
			+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
			+ "  <rdf:Description rdf:about=\"http://here.edu/subject\">\n"
			+ "    <j.0:predicate rdf:resource=\"http://here.edu/object\"/>\n"
			+ "  </rdf:Description>\n" + "</rdf:RDF>\n";
	private static final String CONSTRUCT_RESULT_JSONLD = "["
			+ "{\"@id\":\"http://here.edu/object\"},"
			+ "{\"@id\":\"http://here.edu/subject\",\"http://here.edu/predicate\":[{\"@id\":\"http://here.edu/object\"}]}"
			+ "]";
	private static final String ACCEPTABLE_FOR_CONSTRUCT = "For CONSTRUCT queries, Accept header must be one of "
			+ "'text/plain', 'application/rdf+xml', 'text/n3', 'text/turtle', or 'application/json'";

	private static final String DESCRIBE_ALL_QUERY = "DESCRIBE <http://here.edu/subject>";
	private static final String DESCRIBE_RESULT_TEXT = "<http://here.edu/subject> "
			+ "<http://here.edu/predicate> <http://here.edu/object> .\n";
	private static final String DESCRIBE_RESULT_RDFXML = "<rdf:RDF\n"
			+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
			+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
			+ "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
			+ "    xmlns:j.0=\"http://here.edu/\"\n"
			+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
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
	private static final String DESCRIBE_RESULT_TURTLE = "<rdf:RDF\n"
			+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
			+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
			+ "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
			+ "    xmlns:j.0=\"http://here.edu/\"\n"
			+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
			+ "  <rdf:Description rdf:about=\"http://here.edu/subject\">\n"
			+ "    <j.0:predicate rdf:resource=\"http://here.edu/object\"/>\n"
			+ "  </rdf:Description>\n" + "</rdf:RDF>\n";
	private static final String DESCRIBE_RESULT_JSONLD = "["
			+ "{\"@id\":\"http://here.edu/object\"},"
			+ "{\"@id\":\"http://here.edu/subject\",\"http://here.edu/predicate\":[{\"@id\":\"http://here.edu/object\"}]}"
			+ "]";
	private static final String ACCEPTABLE_FOR_DESCRIBE = "For DESCRIBE queries, Accept header must be one of "
			+ "'text/plain', 'application/rdf+xml', 'text/n3', 'text/turtle', or 'application/json'";

	private static final String SERVLET_PATH_INFO = "/admin/sparqlquery";
	private static final String REDIRECT_TO_LOGIN_URL = "/authenticate?";
	private static final String REDIRECT_TO_HOME_URL = "";

	public static final String KILROY_URI = "http://here.edu/kilroyUser";
	public static final String KILROY_EMAIL = "kilroy_email";
	public static final String KILROY_PASSWORD = "kilroy_password";
	public static final UserAccount KILROY = createUserAccount(KILROY_URI,
			KILROY_EMAIL, KILROY_PASSWORD);

	public static final String BONZO_URI = "http://here.edu/bonzoUser";
	public static final String BONZO_EMAIL = "bonzo_email";
	public static final String BONZO_PASSWORD = "bonzo_password";
	public static final UserAccount BONZO = createUserAccount(BONZO_URI,
			BONZO_EMAIL, BONZO_PASSWORD);

	private static UserAccount createUserAccount(String uri, String name,
			String password) {
		UserAccount user = new UserAccount();
		user.setEmailAddress(name);
		user.setUri(uri);
		user.setMd5Password(Authenticator.applyMd5Encoding(password));
		user.setLoginCount(10);
		user.setPasswordChangeRequired(false);
		return user;
	}

	private ServletContextStub ctx;
	private ServletConfigStub servletConfig;
	private HttpSessionStub session;
	private SparqlQueryServlet servlet;
	private HttpServletResponseStub resp;
	private HttpServletRequestStub req;

	private OntModel model;

	@Before
	public void setup() throws ServletException, MalformedURLException {
		ctx = new ServletContextStub();

		servletConfig = new ServletConfigStub();
		servletConfig.setServletContext(ctx);

		servlet = new SparqlQueryServlet();
		servlet.init(servletConfig);

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		resp = new HttpServletResponseStub();

		req = new HttpServletRequestStub();
		req.setSession(session);
		req.setRequestUrl(new URL(BASE_URI + SERVLET_PATH_INFO));

		I18nStub.setup();

		initializePolicyList(new AuthorizeEveryone());

		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.read(new StringReader(MODEL_CONTENTS_N3), BASE_URI, "N3");
		ModelAccess.on(ctx).setJenaOntModel(model);
		RDFServiceUtils.setRDFServiceFactory(ctx, new RDFServiceFactorySingle(
				new RDFServiceModel(model)));
	}

	// ----------------------------------------------------------------------
	// invalid requests
	// ----------------------------------------------------------------------

	// Currently throws a NullPointerException
	@Ignore
	@Test
	public void noQueryParameter() {
		req.setHeader("Accept", "text/plain");
		runTheServlet();
		assertEquals("no query parameter", 400, resp.getStatus());
	}

	// ----------------------------------------------------------------------
	// query-response tests by Accept header
	// ----------------------------------------------------------------------

	@Test
	public void selectToTextByHeader() {
		executeWithAcceptHeader("select to text by header", SELECT_ALL_QUERY,
				"text/plain", SELECT_RESULT_TEXT);
	}

	@Test
	public void selectToCsvByHeader() {
		executeWithAcceptHeader("select to csv by header", SELECT_ALL_QUERY,
				"text/csv", SELECT_RESULT_CSV);
	}

	// Not yet supported
	@Ignore
	@Test
	public void selectToTsvByHeader() {
		executeWithAcceptHeader("select to tsv by header", SELECT_ALL_QUERY,
				"text/tab-separated-values", SELECT_RESULT_TSV);
	}

	// Not yet supported
	@Ignore
	@Test
	public void selectToXmlByHeader() {
		executeWithAcceptHeader("select to xml by header", SELECT_ALL_QUERY,
				"application/sparql-results+xml", SELECT_RESULT_XML);
	}

	// Not yet supported
	@Ignore
	@Test
	public void selectToJsonByHeader() {
		executeWithAcceptHeader("select to json by header", SELECT_ALL_QUERY,
				"application/sparql-results+json", SELECT_RESULT_JSON);
	}

	// Currently throws a null pointer exception
	@Ignore
	@Test
	public void selectWithInvalidAcceptHeader() {
		executeWithInvalidAcceptHeader("select with application/rdf+xml",
				SELECT_ALL_QUERY, "application/rdf+xml", ACCEPTABLE_FOR_SELECT);
		executeWithInvalidAcceptHeader("select with text/n3", SELECT_ALL_QUERY,
				"text/n3", ACCEPTABLE_FOR_SELECT);
		executeWithInvalidAcceptHeader("select with text/turtle",
				SELECT_ALL_QUERY, "text/turtle", ACCEPTABLE_FOR_SELECT);
		executeWithInvalidAcceptHeader("select with application/json",
				SELECT_ALL_QUERY, "application/json", ACCEPTABLE_FOR_SELECT);
	}

	// ----------------------------------------------------------------------

	@Test
	public void askToTextByHeader() {
		executeWithAcceptHeader("ask to text by header", ASK_ALL_QUERY,
				"text/plain", ASK_RESULT_TEXT);
	}

	@Test
	public void askToCsvByHeader() {
		executeWithAcceptHeader("ask to csv by header", ASK_ALL_QUERY,
				"text/csv", ASK_RESULT_CSV);
	}

	@Test
	public void askToTsvByHeader() {
		executeWithAcceptHeader("ask to tsv by header", ASK_ALL_QUERY,
				"text/tab-separated-values", ASK_RESULT_TSV);
	}

	// Not yet supported
	@Ignore
	@Test
	public void askToXmlByHeader() {
		executeWithAcceptHeader("ask to xml by header", ASK_ALL_QUERY,
				"application/sparql-results+xml", ASK_RESULT_XML);
	}

	// Not yet supported
	@Ignore
	@Test
	public void askToJsonByHeader() {
		executeWithAcceptHeader("ask to json by header", ASK_ALL_QUERY,
				"application/sparql-results+json", ASK_RESULT_JSON);
	}

	// Not yet supported
	@Ignore
	@Test
	public void askWithInvalidAcceptHeader() {
		executeWithInvalidAcceptHeader("ask with application/rdf+xml",
				ASK_ALL_QUERY, "application/rdf+xml", ACCEPTABLE_FOR_ASK);
		executeWithInvalidAcceptHeader("ask with text/n3", ASK_ALL_QUERY,
				"text/n3", ACCEPTABLE_FOR_ASK);
		executeWithInvalidAcceptHeader("ask with text/turtle", ASK_ALL_QUERY,
				"text/turtle", ACCEPTABLE_FOR_ASK);
		executeWithInvalidAcceptHeader("ask with application/json",
				ASK_ALL_QUERY, "application/json", ACCEPTABLE_FOR_ASK);
	}

	// ----------------------------------------------------------------------

	@Test
	public void constructToTextByHeader() {
		executeWithAcceptHeader("construct to text by header",
				CONSTRUCT_ALL_QUERY, "text/plain", CONSTRUCT_RESULT_TEXT);
	}

	@Test
	public void constructToRdfXmlByHeader() {
		executeWithAcceptHeader("construct to rdf/xml by header",
				CONSTRUCT_ALL_QUERY, "application/rdf+xml",
				CONSTRUCT_RESULT_RDFXML);
	}

	@Test
	public void constructToN3ByHeader() {
		executeWithAcceptHeader("construct to n3 by header",
				CONSTRUCT_ALL_QUERY, "text/n3", CONSTRUCT_RESULT_N3);
	}

	@Test
	public void constructToTurtleByHeader() {
		executeWithAcceptHeader("construct to turtle by header",
				CONSTRUCT_ALL_QUERY, "text/turtle", CONSTRUCT_RESULT_TURTLE);
	}

	// The servlet only recognizes "application/javascript", which is incorrect.
	@Ignore
	@Test
	public void constructToJsonldByHeader() {
		executeWithAcceptHeader("construct to JSON-LD by header",
				CONSTRUCT_ALL_QUERY, "application/json",
				CONSTRUCT_RESULT_JSONLD);
	}

	// Currently throws a null pointer exception
	@Ignore
	@Test
	public void constructWithInvalidAcceptHeader() {
		executeWithInvalidAcceptHeader("construct with text/csv",
				CONSTRUCT_ALL_QUERY, "text/csv", ACCEPTABLE_FOR_CONSTRUCT);
		executeWithInvalidAcceptHeader("construct with text/tsv",
				CONSTRUCT_ALL_QUERY, "text/tsv", ACCEPTABLE_FOR_CONSTRUCT);
		executeWithInvalidAcceptHeader(
				"construct with application/sparql-results+xml",
				CONSTRUCT_ALL_QUERY, "application/sparql-results+xml",
				ACCEPTABLE_FOR_CONSTRUCT);
		executeWithInvalidAcceptHeader(
				"construct with application/sparql-results+json",
				CONSTRUCT_ALL_QUERY, "application/sparql-results+json",
				ACCEPTABLE_FOR_CONSTRUCT);
	}

	// ----------------------------------------------------------------------

	@Test
	public void describeToTextByHeader() {
		executeWithAcceptHeader("describe to text by header",
				DESCRIBE_ALL_QUERY, "text/plain", DESCRIBE_RESULT_TEXT);
	}

	@Test
	public void describeToRdfXmlByHeader() {
		executeWithAcceptHeader("describe to rdf/xml by header",
				DESCRIBE_ALL_QUERY, "application/rdf+xml",
				DESCRIBE_RESULT_RDFXML);
	}

	@Test
	public void describeToN3ByHeader() {
		executeWithAcceptHeader("describe to n3 by header", DESCRIBE_ALL_QUERY,
				"text/n3", DESCRIBE_RESULT_N3);
	}

	@Test
	public void describeToTurtleByHeader() {
		executeWithAcceptHeader("describe to turtle by header",
				DESCRIBE_ALL_QUERY, "text/turtle", DESCRIBE_RESULT_TURTLE);
	}

	// The servlet only recognizes "application/javascript", which is incorrect.
	@Ignore
	@Test
	public void describeToJsonldByHeader() {
		executeWithAcceptHeader("describe to JSON-LD by header",
				DESCRIBE_ALL_QUERY, "application/json", DESCRIBE_RESULT_JSONLD);
	}

	// Currently throws a null pointer exception
	@Ignore
	@Test
	public void describeWithInvalidAcceptHeader() {
		executeWithInvalidAcceptHeader("describe with text/csv",
				DESCRIBE_ALL_QUERY, "text/csv", ACCEPTABLE_FOR_DESCRIBE);
		executeWithInvalidAcceptHeader("describe with text/tsv",
				DESCRIBE_ALL_QUERY, "text/tsv", ACCEPTABLE_FOR_DESCRIBE);
		executeWithInvalidAcceptHeader(
				"describe with application/sparql-results+xml",
				DESCRIBE_ALL_QUERY, "application/sparql-results+xml",
				ACCEPTABLE_FOR_DESCRIBE);
		executeWithInvalidAcceptHeader(
				"describe with application/sparql-results+json",
				DESCRIBE_ALL_QUERY, "application/sparql-results+json",
				ACCEPTABLE_FOR_DESCRIBE);
	}

	// ----------------------------------------------------------------------
	// query-response tests by format parameter
	// ----------------------------------------------------------------------

	@Test
	public void selectToTextByResultFormat() {
		executeWithResultFormat("select to text by result format",
				SELECT_ALL_QUERY, "RS_TEXT", SELECT_RESULT_TEXT);
	}

	@Test
	public void selectToCsvByResultFormat() {
		executeWithResultFormat("select to csv by result format",
				SELECT_ALL_QUERY, "vitro:csv", SELECT_RESULT_CSV);
	}

	// Not yet supported
	@Ignore
	@Test
	public void selectToTsvByResultFormat() {
		executeWithResultFormat("select to tsv by result format",
				SELECT_ALL_QUERY, "text/tab-separated-values",
				SELECT_RESULT_TSV);
	}

	// Not yet supported
	@Ignore
	@Test
	public void selectToXmlByResultFormat() {
		executeWithResultFormat("select to xml by result format",
				SELECT_ALL_QUERY, "application/sparql-results+xml",
				SELECT_RESULT_XML);
	}

	// Not yet supported
	@Ignore
	@Test
	public void selectToJsonByResultFormat() {
		executeWithResultFormat("select to json by result format",
				SELECT_ALL_QUERY, "application/sparql-results+json",
				SELECT_RESULT_JSON);
	}

	// Currently throws a null pointer exception
	@Ignore
	@Test
	public void selectWithInvalidResultFormat() {
		executeWithInvalidResultFormat("select with N-TRIPLE",
				SELECT_ALL_QUERY, "N-TRIPLE", ACCEPTABLE_FOR_SELECT);
		executeWithInvalidResultFormat("select with RDF/XML", SELECT_ALL_QUERY,
				"RDF/XML", ACCEPTABLE_FOR_SELECT);
		executeWithInvalidResultFormat("select with N3", SELECT_ALL_QUERY,
				"N3", ACCEPTABLE_FOR_SELECT);
		executeWithInvalidResultFormat("select with TTL", SELECT_ALL_QUERY,
				"TTL", ACCEPTABLE_FOR_SELECT);
		executeWithInvalidResultFormat("select with JSON-LD", SELECT_ALL_QUERY,
				"JSON-LD", ACCEPTABLE_FOR_SELECT);
	}

	// ----------------------------------------------------------------------

	@Test
	public void askToTextByResultFormat() {
		executeWithResultFormat("ask to text by result format", ASK_ALL_QUERY,
				"RS_TEXT", ASK_RESULT_TEXT);
	}

	@Test
	public void askToCsvByResultFormat() {
		executeWithResultFormat("ask to csv by result format", ASK_ALL_QUERY,
				"vitro:csv", ASK_RESULT_CSV);
	}

	@Test
	public void askToTsvByResultFormat() {
		executeWithResultFormat("ask to tsv by result format", ASK_ALL_QUERY,
				"text/tab-separated-values", ASK_RESULT_TSV);
	}

	// Not yet supported
	@Ignore
	@Test
	public void askToXmlByResultFormat() {
		executeWithResultFormat("ask to xml by result format", ASK_ALL_QUERY,
				"application/sparql-results+xml", ASK_RESULT_XML);
	}

	// Not yet supported
	@Ignore
	@Test
	public void askToJsonByResultFormat() {
		executeWithResultFormat("ask to json by result format", ASK_ALL_QUERY,
				"application/sparql-results+json", ASK_RESULT_JSON);
	}

	// Not yet supported
	@Ignore
	@Test
	public void askWithInvalidResultFormat() {
		executeWithInvalidResultFormat("ask with N-TRIPLE", ASK_ALL_QUERY,
				"N-TRIPLE", ACCEPTABLE_FOR_ASK);
		executeWithInvalidResultFormat("ask with RDF/XML", ASK_ALL_QUERY,
				"RDF/XML", ACCEPTABLE_FOR_ASK);
		executeWithInvalidResultFormat("ask with N3", ASK_ALL_QUERY, "N3",
				ACCEPTABLE_FOR_ASK);
		executeWithInvalidResultFormat("ask with TTL", ASK_ALL_QUERY, "TTL",
				ACCEPTABLE_FOR_ASK);
		executeWithInvalidResultFormat("ask with JSON-LD", ASK_ALL_QUERY,
				"JSON-LD", ACCEPTABLE_FOR_ASK);
	}

	// ----------------------------------------------------------------------

	@Test
	public void constructToTextByRdfResultFormat() {
		executeWithRdfResultFormat("construct to text by rdf result format",
				CONSTRUCT_ALL_QUERY, "N-TRIPLE", CONSTRUCT_RESULT_TEXT);
	}

	// Differs by white space?
	@Ignore
	@Test
	public void constructToRdfXmlByRdfResultFormat() {
		executeWithRdfResultFormat("construct to rdf/xml by rdf result format",
				CONSTRUCT_ALL_QUERY, "RDF/XML", CONSTRUCT_RESULT_RDFXML);
	}

	@Test
	public void constructToN3ByRdfResultFormat() {
		executeWithRdfResultFormat("construct to n3 by rdf result format",
				CONSTRUCT_ALL_QUERY, "N3", CONSTRUCT_RESULT_N3);
	}

	// Either this or constructToTurtleByHeader is wrong.
	@Ignore
	@Test
	public void constructToTurtleByRdfResultFormat() {
		executeWithRdfResultFormat("construct to turtle by rdf result format",
				CONSTRUCT_ALL_QUERY, "TTL", CONSTRUCT_RESULT_TURTLE);
	}

	@Test
	public void constructToJsonldByRdfResultFormat() {
		executeWithRdfResultFormat("construct to JSON-LD by rdf result format",
				CONSTRUCT_ALL_QUERY, "JSON-LD", CONSTRUCT_RESULT_JSONLD);
	}

	// Currently throws a null pointer exception
	@Ignore
	@Test
	public void constructWithInvalidAcceptRdfResultFormat() {
		executeWithInvalidRdfResultFormat("construct with RS_TEXT",
				CONSTRUCT_ALL_QUERY, "RS_TEXT", ACCEPTABLE_FOR_CONSTRUCT);
		executeWithInvalidRdfResultFormat("construct with vitro:csv",
				CONSTRUCT_ALL_QUERY, "vitro:csv", ACCEPTABLE_FOR_CONSTRUCT);
		executeWithInvalidRdfResultFormat("construct with text/tsv",
				CONSTRUCT_ALL_QUERY, "text/tsv", ACCEPTABLE_FOR_CONSTRUCT);
		executeWithInvalidRdfResultFormat(
				"construct with application/sparql-results+xml",
				CONSTRUCT_ALL_QUERY, "application/sparql-results+xml",
				ACCEPTABLE_FOR_CONSTRUCT);
		executeWithInvalidRdfResultFormat(
				"construct with application/sparql-results+json",
				CONSTRUCT_ALL_QUERY, "application/sparql-results+json",
				ACCEPTABLE_FOR_CONSTRUCT);
	}

	// ----------------------------------------------------------------------

	@Test
	public void describeToTextByRdfResultFormat() {
		executeWithRdfResultFormat("describe to text by rdf result format",
				DESCRIBE_ALL_QUERY, "N-TRIPLE", DESCRIBE_RESULT_TEXT);
	}

	// Differs by white space?
	@Ignore
	@Test
	public void describeToRdfXmlByRdfResultFormat() {
		executeWithRdfResultFormat("describe to rdf/xml by rdf result format",
				DESCRIBE_ALL_QUERY, "RDF/XML", DESCRIBE_RESULT_RDFXML);
	}

	@Test
	public void describeToN3ByRdfResultFormat() {
		executeWithRdfResultFormat("describe to n3 by rdf result format",
				DESCRIBE_ALL_QUERY, "N3", DESCRIBE_RESULT_N3);
	}

	// Either this or describeToTurtleByHeader is wrong.
	@Ignore
	@Test
	public void describeToTurtleByRdfResultFormat() {
		executeWithRdfResultFormat("describe to turtle by rdf result format",
				DESCRIBE_ALL_QUERY, "TTL", DESCRIBE_RESULT_TURTLE);
	}

	@Test
	public void describeToJsonldByRdfResultFormat() {
		executeWithRdfResultFormat("describe to JSON-LD by rdf result format",
				DESCRIBE_ALL_QUERY, "JSON-LD", DESCRIBE_RESULT_JSONLD);
	}

	// Currently throws a null pointer exception
	@Ignore
	@Test
	public void describeWithInvalidAcceptRdfResultFormat() {
		executeWithInvalidRdfResultFormat("describe with RS_TEXT",
				DESCRIBE_ALL_QUERY, "RS_TEXT", ACCEPTABLE_FOR_DESCRIBE);
		executeWithInvalidRdfResultFormat("describe with vitro:csv",
				DESCRIBE_ALL_QUERY, "vitro:csv", ACCEPTABLE_FOR_DESCRIBE);
		executeWithInvalidRdfResultFormat("describe with text/tsv",
				DESCRIBE_ALL_QUERY, "text/tsv", ACCEPTABLE_FOR_DESCRIBE);
		executeWithInvalidRdfResultFormat(
				"describe with application/sparql-results+xml",
				DESCRIBE_ALL_QUERY, "application/sparql-results+xml",
				ACCEPTABLE_FOR_DESCRIBE);
		executeWithInvalidRdfResultFormat(
				"describe with application/sparql-results+json",
				DESCRIBE_ALL_QUERY, "application/sparql-results+json",
				ACCEPTABLE_FOR_DESCRIBE);
	}

	// ----------------------------------------------------------------------
	// Authentication tests
	// ----------------------------------------------------------------------

	@Test
	public void authNoParmsNotLoggedIn() {
		initializeForAuthTest();
		executeToRedirect("invalid parms", SELECT_ALL_QUERY,
				REDIRECT_TO_LOGIN_URL);
	}

	@Test
	public void authNoParmsNotLoggedInEnough() {
		initializeForAuthTest();
		setLoggedInUser(BONZO_URI);
		executeToRedirect("redirect to home", SELECT_ALL_QUERY,
				REDIRECT_TO_HOME_URL);
	}

	@Test
	public void authNoParmsLoggedIn() {
		initializeForAuthTest();
		setLoggedInUser(KILROY_URI);
		executeWithAcceptHeader("logged in properly", SELECT_ALL_QUERY,
				"text/plain", SELECT_RESULT_TEXT);
	}

	@Test
	public void authInvalidParms() {
		initializeForAuthTest();
		setParms(KILROY_EMAIL, "bogus_password");
		executeToRedirect("invalid parms", SELECT_ALL_QUERY,
				REDIRECT_TO_LOGIN_URL);
	}

	@Test
	public void authWrongValidParms() {
		initializeForAuthTest();
		setParms(BONZO_EMAIL, BONZO_PASSWORD);
		executeToRedirect("logged in to wrong user", SELECT_ALL_QUERY,
				REDIRECT_TO_LOGIN_URL);
	}

	@Test
	public void authCorrectParms() {
		initializeForAuthTest();
		setParms(KILROY_EMAIL, KILROY_PASSWORD);
		executeWithAcceptHeader("logged in properly", SELECT_ALL_QUERY,
				"text/plain", SELECT_RESULT_TEXT);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void initializePolicyList(PolicyIface policy) {
		PolicyList policyList = new PolicyList();
		policyList.add(policy);
		ctx.setAttribute(ServletPolicyList.class.getName(), policyList);
	}

	private void executeWithAcceptHeader(String message, String query,
			String acceptHeader, String expectedResult) {
		req.addParameter("query", query);
		req.setHeader("Accept", acceptHeader);
		runAndCheckResult(message, expectedResult);
	}

	private void executeWithResultFormat(String message, String query,
			String resultFormat, String expectedResult) {
		req.addParameter("query", query);
		req.addParameter("resultFormat", resultFormat);
		runAndCheckResult(message, expectedResult);
	}
	
	private void executeWithRdfResultFormat(String message, String query,
			String rdfResultFormat, String expectedResult) {
		req.addParameter("query", query);
		req.addParameter("rdfResultFormat", rdfResultFormat);
		runAndCheckResult(message, expectedResult);
	}

	private void runAndCheckResult(String message, String expectedOutput) {
		runTheServlet();
		assertNormalResponse();
		assertEquals(message, expectedOutput, resp.getOutput());
	}

	private void executeToRedirect(String message, String query,
			String redirectUrl) {
		req.addParameter("query", query);
		runTheServlet();
		assertResponseIsRedirect(message, redirectUrl);
	}

	private void assertNormalResponse() {
		int status = resp.getStatus();
		String redirect = resp.getRedirectLocation();
		if ((status != 200) || (redirect != null)) {
			fail("Not a normal response, status=" + status + ", redirect="
					+ redirect);
		}
	}

	private void assertResponseIsRedirect(String message, String redirectUrl) {
		assertEquals(message + " status", 200, resp.getStatus());

		String redirect = resp.getRedirectLocation();
		if (!redirect.startsWith(redirectUrl)) {
			fail(message + ", expected redirect to start with '" + redirectUrl
					+ "', but redirect was '" + redirect + "'");
		}

		assertEquals(message + " output", "", resp.getOutput());
	}

	private void runTheServlet() {
		try {
			servlet.doGet(req, resp);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void executeWithInvalidAcceptHeader(String message, String query,
			String acceptHeader, String expectedOutput) {
		req.addParameter("query", query);
		req.setHeader("Accept", acceptHeader);
		runTheServlet();
		assertEquals(message + " - status", 406, resp.getStatus());
		assertEquals(message + " - output", expectedOutput, resp.getOutput());
	}

	private void executeWithInvalidResultFormat(String message, String query,
			String resultFormat, String expectedOutput) {
		req.addParameter("query", query);
		req.addParameter("resultFormat", resultFormat);
		runTheServlet();
		assertEquals(message + " - status", 406, resp.getStatus());
		assertEquals(message + " - output", expectedOutput, resp.getOutput());
	}

	private void executeWithInvalidRdfResultFormat(String message,
			String query, String rdfResultFormat, String expectedOutput) {
		req.addParameter("query", query);
		req.addParameter("rdfResultFormat", rdfResultFormat);
		runTheServlet();
		assertEquals(message + " - status", 406, resp.getStatus());
		assertEquals(message + " - output", expectedOutput, resp.getOutput());
	}

	private void setLoggedInUser(String userUri) {
		LoginStatusBean.setBean(session, new LoginStatusBean(userUri,
				AuthenticationSource.INTERNAL));
	}

	private void setParms(String email, String password) {
		req.addParameter("email", email);
		req.addParameter("password", password);
	}

	private void initializeForAuthTest() {
		initializePolicyList(new AuthorizeKilroyOnly());
		initializeAuthenticator();
		initializeIdentifierBundleFactories();
	}

	private void initializeIdentifierBundleFactories() {
		ActiveIdentifierBundleFactories.addFactory(ctx,
				new UserUriIdentifierBundleFactory());
	}

	private void initializeAuthenticator() {
		Factory factory = new AuthenticatorStub.Factory();
		AuthenticatorStub auth = factory.getInstance(req);
		auth.addUser(KILROY);
		auth.addUser(BONZO);
		ctx.setAttribute(AuthenticatorStub.FACTORY_ATTRIBUTE_NAME, factory);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class AuthorizeEveryone implements PolicyIface {
		@Override
		public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
				RequestedAction whatToAuth) {
			return new BasicPolicyDecision(Authorization.AUTHORIZED,
					"Everybody is a winner");
		}
	}

	private static class AuthorizeKilroyOnly implements PolicyIface {
		@Override
		public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
				RequestedAction whatToAuth) {
			for (Identifier id : whoToAuth) {
				if (id instanceof UserUriIdentifier) {
					UserUriIdentifier uuId = (UserUriIdentifier) id;
					if (uuId.userUri.equals(KILROY_URI)) {
						return new BasicPolicyDecision(
								Authorization.AUTHORIZED, "Kilroy is a winner");
					}
				}
			}
			return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
					"Everybody else is a loser");
		}
	}

	private static class UserUriIdentifier implements Identifier {
		public final String userUri;

		public UserUriIdentifier(String userUri) {
			this.userUri = userUri;
		}
	}

	private static class UserUriIdentifierBundleFactory implements
			UserBasedIdentifierBundleFactory {
		@Override
		public IdentifierBundle getIdentifierBundle(HttpServletRequest req) {
			LoginStatusBean lsb = LoginStatusBean.getBean(req);
			return getIdentifierBundle(lsb.getUserURI());
		}

		@Override
		public IdentifierBundle getIdentifierBundleForUser(UserAccount user) {
			return getIdentifierBundle(user.getUri());
		}

		private IdentifierBundle getIdentifierBundle(String userUri) {
			IdentifierBundle bundle = new ArrayIdentifierBundle();
			if (!userUri.isEmpty()) {
				bundle.add(new UserUriIdentifier(userUri));
			}
			return bundle;
		}

	}
}
