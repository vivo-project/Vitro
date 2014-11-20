/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import static edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet.N3_MIMETYPE;
import static edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet.RDFXML_MIMETYPE;
import static edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet.TTL_MIMETYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.beans.IndividualStub;
import stubs.edu.cornell.mannlib.vitro.webapp.controller.individual.IndividualRequestAnalysisContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;

/**
 * Are we able to figure out what sort of Individual request this is?
 */
public class IndividualRequestAnalyzerTest extends AbstractTestClass {

	/**
	 * Info about the application.
	 */
	private static final String URL_HOME_PAGE = "http://vivo.mydomain.edu";
	private static final String URL_INDIVIDUAL_PAGE = URL_HOME_PAGE
			+ "/individual";
	private static final String DEFAULT_NAMESPACE = URL_INDIVIDUAL_PAGE + "/";

	/**
	 * Info about the individual that we're testing (mostly).
	 */
	private static final String ID_INDIVIDUAL_TEST = "testId";
	private static final String URI_INDIVIDUAL_TEST = DEFAULT_NAMESPACE
			+ ID_INDIVIDUAL_TEST;
	private static final String NETID_USER_TEST = "joeUser";

	/**
	 * Info about the file bytestream that appears in one test.
	 */
	/** The ID of an Individual that represents a FileBytestream object. */
	private static final String ID_FILE_BYTESTREAM = "bytesId";
	private static final String URI_FILE_BYTESTREAM = DEFAULT_NAMESPACE
			+ ID_FILE_BYTESTREAM;
	private static final String BYTESTREAM_FILENAME = "imageFilename.jpg";
	private static final String URL_BYTESTREAM_ALIAS = URL_HOME_PAGE + "/file/"
			+ ID_FILE_BYTESTREAM + "/" + BYTESTREAM_FILENAME;

	private IndividualRequestAnalyzer analyzer;
	private IndividualRequestAnalysisContextStub analysisContext;
	private HttpServletRequestStub req;
	private VitroRequest vreq;
	private IndividualRequestInfo requestInfo;

	private IndividualStub testIndividual;
	private IndividualStub bytestreamIndividual;

	@Before
	public void setup() {
		req = new HttpServletRequestStub();
		analysisContext = new IndividualRequestAnalysisContextStub(DEFAULT_NAMESPACE);

		testIndividual = new IndividualStub(URI_INDIVIDUAL_TEST);
		analysisContext.addIndividual(testIndividual);
		analysisContext.addProfilePage(NETID_USER_TEST, testIndividual);

		bytestreamIndividual = new IndividualStub(URI_FILE_BYTESTREAM);
		analysisContext.addIndividual(bytestreamIndividual);
		analysisContext.setAliasUrl(URI_FILE_BYTESTREAM, URL_BYTESTREAM_ALIAS);
	}

	// ----------------------------------------------------------------------
	// Tests - locate by parameter
	// ----------------------------------------------------------------------

	/** /individual?uri=urlencodedURI */
	@Test
	public void findIndividualByUriParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		analyzeIt();
		assertDefaultRequestInfo("find by URI parameter", URI_INDIVIDUAL_TEST);
	}

	/** /individual?netId=bdc34 */
	@Test
	public void findIndividualByNetIdParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("netId", NETID_USER_TEST);
		analyzeIt();
		assertDefaultRequestInfo("find by netId parameter", URI_INDIVIDUAL_TEST);
	}

	/** /individual?netid=bdc34 */
	@Test
	public void findIndividualByNetidParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("netid", NETID_USER_TEST);
		analyzeIt();
		assertDefaultRequestInfo("find by netid parameter", URI_INDIVIDUAL_TEST);
	}

	// ----------------------------------------------------------------------
	// Tests - miscellaneous
	// ----------------------------------------------------------------------

	/** /display/localname */
	@Test
	public void findIndividualByDisplayPath() {
		req.setRequestUrl(url(URL_HOME_PAGE + "/display/" + ID_INDIVIDUAL_TEST));
		analyzeIt();
		assertDefaultRequestInfo("find by display path", URI_INDIVIDUAL_TEST);
	}

	/** /individual/a/b/c fails. */
	@Test
	public void unrecognizedPath() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + "this/that/theOther"));
		analyzeIt();
		assertNoIndividualRequestInfo("unrecognized path");
	}

	/** /display/localname but no such individual */
	@Test
	public void findNoSuchIndividualByDisplayPath() {
		req.setRequestUrl(url(URL_HOME_PAGE + "/display/" + "bogusID"));
		analyzeIt();
		assertNoIndividualRequestInfo("unrecognized ID");
	}

	// ----------------------------------------------------------------------
	// Tests - redirect a FileBytestream
	// ----------------------------------------------------------------------

	@Test
	public void redirectAFileBytestreamIndividual() {
		req.setRequestUrl(url(URL_HOME_PAGE + "/display/" + ID_FILE_BYTESTREAM));
		analyzeIt();
		assertBytestreamRedirectInfo("bytestream redirect",
				URL_BYTESTREAM_ALIAS);
	}

	// ----------------------------------------------------------------------
	// Tests - redirect from a Linked Data path
	// ----------------------------------------------------------------------

	/** /individual/localname, accept=RDF redirects to /individual/id/id.rdf */
	@Test
	public void redirectFromLinkedDataPathAcceptRdf() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", RDFXML_MIMETYPE);
		analyzeIt();
		assertRdfRedirectRequestInfo("by linked data path, accept RDF",
				redirectUrlForRdfStream(ID_INDIVIDUAL_TEST, ".rdf"));
	}

	/** /individual/localname, accept=N3 redirects to /individual/id/id.n3 */
	@Test
	public void redirectFromLinkedDataPathAcceptN3() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", N3_MIMETYPE);
		analyzeIt();
		assertRdfRedirectRequestInfo("by linked data path, accept N3",
				redirectUrlForRdfStream(ID_INDIVIDUAL_TEST, ".n3"));
	}

	/** /individual/localname, accept=TTL redirects to /individual/id/id.ttl */
	@Test
	public void redirectFromLinkedDataPathAcceptTurtle() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", TTL_MIMETYPE);
		analyzeIt();
		assertRdfRedirectRequestInfo("by linked data path, accept TTL",
				redirectUrlForRdfStream(ID_INDIVIDUAL_TEST, ".ttl"));
	}

	/** /individual/localname, no accept, redirects to /display/id */
	@Test
	public void redirectFromLinkedDataPathNoAccept() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		analyzeIt();
		assertRdfRedirectRequestInfo("by linked data path with no accept",
				"/display/" + ID_INDIVIDUAL_TEST);
	}

	/**
	 * If the accept header is set to a recognized value, but not one of the
	 * onese that we like, treat the same as no accept.
	 */
	@Test
	public void redirectFromLinkedDataPathAcceptStrange() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", "image/jpg");
		analyzeIt();
		assertRdfRedirectRequestInfo(
				"by linked data path, accept a strange content type",
				"/display/" + ID_INDIVIDUAL_TEST);
	}

	/**
	 * If the accept header is set to an unrecognized value, treat the same as
	 * no accept.
	 */
	@Test
	public void redirectFromLinkedDataPathAcceptGarbage() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", "a/b/c");
		analyzeIt();
		assertRdfRedirectRequestInfo(
				"by linked data path, accept an unrecognized content type",
				"/display/" + ID_INDIVIDUAL_TEST);
	}

	// ----------------------------------------------------------------------
	// Tests - satisfy requests for RDF formats.
	// ----------------------------------------------------------------------

	@Test
	public void getRdfByUriAndFormatParameters() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		req.addParameter("format", "rdfxml");
		analyzeIt();
		assertLinkedDataRequestInfo("RDF by uri and format parameters",
				URI_INDIVIDUAL_TEST, ContentType.RDFXML);
	}

	@Test
	public void getN3ByUriAndFormatParameters() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		req.addParameter("format", "n3");
		analyzeIt();
		assertLinkedDataRequestInfo("N3 by uri and format parameters",
				URI_INDIVIDUAL_TEST, ContentType.N3);
	}

	@Test
	public void getTurtleByUriAndFormatParameters() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		req.addParameter("format", "ttl");
		analyzeIt();
		assertLinkedDataRequestInfo("Turtle by uri and format parameters",
				URI_INDIVIDUAL_TEST, ContentType.TURTLE);
	}

	@Test
	public void unrecognizedFormatParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		req.addParameter("format", "bogus");
		analyzeIt();
		assertDefaultRequestInfo("unrecognized format means HTML response",
				URI_INDIVIDUAL_TEST);
	}

	/** http://vivo.cornell.edu/individual/n23/n23.rdf */
	@Test
	public void getRdfByStreamRequest() {
		req.setRequestUrl(absoluteUrlForRdfStream(ID_INDIVIDUAL_TEST, ".rdf"));
		analyzeIt();
		assertLinkedDataRequestInfo("RDF by stream request",
				URI_INDIVIDUAL_TEST, ContentType.RDFXML);
	}

	/** http://vivo.cornell.edu/individual/n23/n23.n3 */
	@Test
	public void getN3ByStreamRequest() {
		req.setRequestUrl(absoluteUrlForRdfStream(ID_INDIVIDUAL_TEST, ".n3"));
		analyzeIt();
		assertLinkedDataRequestInfo("N3 by stream request",
				URI_INDIVIDUAL_TEST, ContentType.N3);
	}

	/** http://vivo.cornell.edu/individual/n23/n23.rdf */
	@Test
	public void getTurtleByStreamRequest() {
		req.setRequestUrl(absoluteUrlForRdfStream(ID_INDIVIDUAL_TEST, ".ttl"));
		analyzeIt();
		assertLinkedDataRequestInfo("Turtle by stream request",
				URI_INDIVIDUAL_TEST, ContentType.TURTLE);
	}

	/** http://vivo.cornell.edu/individual/n23/n23.bogus is an error */
	@Test
	public void unrecognizedFormatForRdfStreamRequest() {
		req.setRequestUrl(absoluteUrlForRdfStream(ID_INDIVIDUAL_TEST, ".bogus"));
		analyzeIt();
		assertNoIndividualRequestInfo("Unrecognized RDF stream request");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/** /individual/n23/n23.rdf, or the like */
	private String redirectUrlForRdfStream(String id, String extension) {
		return "/individual/" + id + "/" + id + extension;
	}

	/** http://vivo.mydomain.edu/individual/n23/n23.rdf, or the like */
	private URL absoluteUrlForRdfStream(String id, String extension) {
		return url(DEFAULT_NAMESPACE + id + "/" + id + extension);
	}

	private void analyzeIt() {
		vreq = new VitroRequest(req);
		analyzer = new IndividualRequestAnalyzer(vreq, analysisContext);
		requestInfo = analyzer.analyze();
	}

	/** We should have a DEFAULT request with the expected Individual. */
	private void assertDefaultRequestInfo(String message, String individualUri) {
		assertEquals(message + ": expecting DEFAULT request type",
				IndividualRequestInfo.Type.DEFAULT, requestInfo.getType());
		assertNotNull(message + ": expected an individual",
				requestInfo.getIndividual());
		assertEquals(message + ": expected individual", individualUri,
				requestInfo.getIndividual().getURI());
	}

	/** We should have a RDF_REDIRECT request with the expected URL. */
	private void assertRdfRedirectRequestInfo(String message, String redirectUrl) {
		assertEquals(message + ": expecting RDF_REDIRECT request type",
				IndividualRequestInfo.Type.RDF_REDIRECT, requestInfo.getType());
		assertEquals(message + ": expected redirect URL", redirectUrl,
				requestInfo.getRedirectUrl());
	}

	/**
	 * We should have a BYTESTREAM_REDIRECT request with the expected Individual
	 * and alias URL.
	 */
	private void assertBytestreamRedirectInfo(String message, String aliasUrl) {
		assertEquals(message + ": expecting BYTESTREAM_REDIRECT request type",
				IndividualRequestInfo.Type.BYTESTREAM_REDIRECT,
				requestInfo.getType());
		assertEquals(message + ": expected alias URL", aliasUrl,
				requestInfo.getRedirectUrl());
	}

	/**
	 * We should have a NO_INDIVIDUAL request.
	 */
	private void assertNoIndividualRequestInfo(String message) {
		assertEquals(message + ": expecting NO_INDIVIDUAL request type",
				IndividualRequestInfo.Type.NO_INDIVIDUAL, requestInfo.getType());
	}

	/**
	 * We should have a LINKED_DATA request, with the expected Individual and
	 * content type.
	 */
	private void assertLinkedDataRequestInfo(String message,
			String individualUri, ContentType contentType) {
		assertEquals(message + ": expecting LINKED_DATA request type",
				IndividualRequestInfo.Type.LINKED_DATA, requestInfo.getType());
		assertNotNull(message + ": expected an individual",
				requestInfo.getIndividual());
		assertEquals(message + ": expected individual", individualUri,
				requestInfo.getIndividual().getURI());
		assertNotNull(message + ": expected a content type",
				requestInfo.getRdfFormat());
		assertEquals(message + ": expected contentType", contentType,
				requestInfo.getRdfFormat());
	}

}
