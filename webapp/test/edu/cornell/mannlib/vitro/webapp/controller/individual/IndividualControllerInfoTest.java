/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.individual;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.FS_ALIAS_URL;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.FS_DOWNLOAD_LOCATION;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.FS_FILENAME;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.FS_MIME_TYPE;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.OWL_THING;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.RDF_TYPE;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.VITRO_PUBLIC;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.beans.IndividualStub;
import stubs.edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfigurationStub;
import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.ApplicationDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.IndividualDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.MenuDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.OntologyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapperStub;
import stubs.javax.servlet.ServletConfigStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerSetup;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualController;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * TODO
 */
public class IndividualControllerInfoTest extends AbstractTestClass {
	/** The root of the application */
	private static final String URL_HOME_PAGE = "http://vivo.mydomain.edu";

	/** The page we're testing. */
	private static final String URL_INDIVIDUAL_PAGE = URL_HOME_PAGE
			+ "/individual";

	/** The default namespace is based on this page URL! */
	private static final String DEFAULT_NAMESPACE = URL_INDIVIDUAL_PAGE + "/";

	/** The ID of the Individual we're trying to find. */
	private static final String ID_INDIVIDUAL_TEST = "testId";

	/** The ID of an Individual that represents a FileBytestream object. */
	private static final String ID_FILE_BYTESTREAM = "bytesId";

	/** The ID of an Individual that is the surrogate for ID_FILE_BYTESTREAM. */
	private static final String ID_FILE_SURROGATE = "fileId";

	/** The URI of the Individual we're trying to find. */
	private static final String URI_INDIVIDUAL_TEST = DEFAULT_NAMESPACE
			+ ID_INDIVIDUAL_TEST;

	/** The URI of the Individual that represents a FileBytestream object. */
	private static final String URI_FILE_BYTESTREAM = DEFAULT_NAMESPACE
			+ ID_FILE_BYTESTREAM;

	/** The URI of the Individual that represents a FileSurrogate object. */
	private static final String URI_FILE_SURROGATE = DEFAULT_NAMESPACE
			+ ID_FILE_SURROGATE;

	/** The name of the file bytestream. */
	private static final String BYTESTREAM_FILENAME = "imageFilename.jpg";
	/** The direct location of the file bytestream. */
	private static final String URL_BYTESTREAM_ALIAS = URL_HOME_PAGE + "/file/"
			+ ID_FILE_BYTESTREAM + "/" + BYTESTREAM_FILENAME;

	/** The Net ID of the User whose profile we're trying to find. */
	private static final String NETID_USER_TEST = "joeUser";

	private IndividualStub testIndividual;
	private IndividualStub bytestreamIndividual;
	private IndividualStub surrogateIndividual;
	private edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualController controller;
	private ServletContextStub ctx;
	private ServletConfigStub config;
	private HttpSessionStub session;
	private HttpServletRequestStub req;
	private HttpServletResponseStub resp;

	private WebappDaoFactoryStub waDao;
	private ApplicationDaoStub aDao;
	private MenuDaoStub menuDao;
	private IndividualDaoStub iDao;
	private OntologyDaoStub ontDao;
	private ObjectPropertyStatementDaoStub opsDao;
	private ConfigurationPropertiesStub props;
	private NamespaceMapperStub namespaceMapper;

	private File templatesDir;

	@Before
	public void setup() throws ServletException, IOException {
		testIndividual = new IndividualStub(URI_INDIVIDUAL_TEST);

		bytestreamIndividual = new IndividualStub(URI_FILE_BYTESTREAM);
		bytestreamIndividual.addVclass(VITRO_PUBLIC, "FileByteStream",
				"Bytestream");
		bytestreamIndividual.addDataPropertyStatement(FS_ALIAS_URL,
				URL_BYTESTREAM_ALIAS);

		surrogateIndividual = new IndividualStub(URI_FILE_SURROGATE);
		surrogateIndividual.addVclass(VITRO_PUBLIC, "File", "Surrogate");
		surrogateIndividual.addDataPropertyStatement(FS_FILENAME,
				BYTESTREAM_FILENAME);
		surrogateIndividual
				.addDataPropertyStatement(FS_MIME_TYPE, "image/jpeg");
		surrogateIndividual
				.addPopulatedObjectPropertyStatement(FS_DOWNLOAD_LOCATION,
						URI_FILE_BYTESTREAM, bytestreamIndividual);

		ctx = new ServletContextStub();

		setLoggerLevel(ConfigurationProperties.class, Level.WARN);
		props = new ConfigurationPropertiesStub();
		props.setBean(ctx);

		config = new ServletConfigStub();
		config.setServletContext(ctx);

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		namespaceMapper = new NamespaceMapperStub();
		namespaceMapper.setPrefixForNamespace("mydomain",
				"http://vivo.mydomain.edu/individual/");
		ctx.setAttribute("NamespaceMapper", namespaceMapper);

		SelfEditingConfigurationStub sec = new SelfEditingConfigurationStub();
		session.setAttribute(SelfEditingConfiguration.class.getName(), sec);
		sec.addAssociatedIndividual(NETID_USER_TEST, testIndividual);

		controller = new edu.cornell.mannlib.vitro.webapp.controller.freemarker.IndividualController();
		controller.init(config);

		req = new HttpServletRequestStub();
		req.setSession(session);

		resp = new HttpServletResponseStub();

		waDao = new WebappDaoFactoryStub();
		req.setAttribute("webappDaoFactory", waDao);
		waDao.setDefaultNamespace(DEFAULT_NAMESPACE);

		ApplicationBean appBean = new ApplicationBean();

		aDao = new ApplicationDaoStub();
		aDao.setApplicationBean(appBean);
		waDao.setApplicationDao(aDao);

		menuDao = new MenuDaoStub();
		waDao.setMenuDao(menuDao);

		iDao = new IndividualDaoStub();
		waDao.setIndividualDao(iDao);
		iDao.addIndividual(testIndividual);
		iDao.addIndividual(bytestreamIndividual);
		iDao.addIndividual(surrogateIndividual);

		ontDao = new OntologyDaoStub();
		waDao.setOntologyDao(ontDao);

		opsDao = new ObjectPropertyStatementDaoStub();
		waDao.setObjectPropertyStatementDao(opsDao);
		opsDao.addObjectPropertyStatement(URI_FILE_SURROGATE,
				FS_DOWNLOAD_LOCATION, URI_FILE_BYTESTREAM);

		OntModel ontModel = ModelFactory.createOntologyModel();
		addObjectProperty(ontModel, URI_INDIVIDUAL_TEST, RDF_TYPE, OWL_THING);
		addObjectProperty(ontModel, URI_FILE_BYTESTREAM, RDF_TYPE,
				VitroVocabulary.FS_BYTESTREAM_CLASS);
		req.setAttribute("jenaOntModel", ontModel);

		templatesDir = createTempDirectory("templates");
		ctx.setRealPath("/templates/freemarker", templatesDir.getAbsolutePath());
		createFile(templatesDir, "error-titled.ftl", "Bogus error-titled.ftl");
		createFile(templatesDir, "individual-help.ftl",
				"Bogus individual-help.ftl");
		createFile(templatesDir, "page.ftl", "Bogus page.ftl: ${body}");
		createFile(templatesDir, "pageSetup.ftl", "Bogus page setup.ftl");
		createFile(templatesDir, "individual.ftl",
				"Individual page: ${individual.uri}");

		new FreemarkerSetup().contextInitialized(new ServletContextEvent(ctx));
	}

	@Before
	public void setLoggerLevels() {
		setLoggerLevel(RevisionInfoBean.class, Level.ERROR);
		setLoggerLevel(FreemarkerSetup.class, Level.WARN);
//		setLoggerLevel(IndividualController.class, Level.DEBUG);
	}

	@After
	public void teardown() {
		purgeDirectory(templatesDir);
	}

	// ----------------------------------------------------------------------
	// Tests - locate by parameter
	// ----------------------------------------------------------------------

	/** /individual?id=individualLocalName */
	@Ignore
	// Doesn't work; won't fix.
	@Test
	public void findIndividualByIdParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("id", ID_INDIVIDUAL_TEST);
		runTheRequest();
		assertResponseContains("by id parameter", "Individual page: "
				+ URI_INDIVIDUAL_TEST);
	}

	/** /individual?entityId=individualLocalName */
	@Ignore
	// Doesn't work; won't fix.
	@Test
	public void findIndividualByEntityIdParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("entityId", ID_INDIVIDUAL_TEST);
		runTheRequest();
		assertResponseContains("by id parameter", "Individual page: "
				+ URI_INDIVIDUAL_TEST);
	}

	/** /individual?uri=urlencodedURI */
	@Test
	public void findIndividualByUriParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		runTheRequest();
		assertResponseContains("by uri parameter", "Individual page: "
				+ URI_INDIVIDUAL_TEST);
	}

	/** /individual?netId=bdc34 */
	@Test
	public void findIndividualByNetIdParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("netId", NETID_USER_TEST);
		runTheRequest();
		assertResponseContains("by uri parameter", "Individual page: "
				+ URI_INDIVIDUAL_TEST);
	}

	/** /individual?netid=bdc34 */
	@Test
	public void findIndividualByNetidParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("netid", NETID_USER_TEST);
		runTheRequest();
		assertResponseContains("by uri parameter", "Individual page: "
				+ URI_INDIVIDUAL_TEST);
	}

	// ----------------------------------------------------------------------
	// Tests - miscellaneous
	// ----------------------------------------------------------------------

	/** /display/localname */
	@Test
	public void findIndividualByDisplayPath() {
		req.setRequestUrl(url(URL_HOME_PAGE + "/display/" + ID_INDIVIDUAL_TEST));
		runTheRequest();
		assertResponseContains("by uri parameter", "Individual page: "
				+ URI_INDIVIDUAL_TEST);
	}

	/** /individual/nsPrefix/localname */
	@Test
	public void findByPrefixAndLocalname() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + "mydomain/"
				+ ID_INDIVIDUAL_TEST));
		runTheRequest();
		assertResponseContains("by prefix and localname", "Individual page: "
				+ URI_INDIVIDUAL_TEST);
	}

	/** /individual/a/b/c fails. */
	@Test
	public void unrecognizedPath() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + "this/that/theOther"));
		runTheRequest();
		assertResponseContains("unrecognized path", "error-titled.ftl");
	}

	/** /display/localname but no such individual */
	@Test
	public void findNoSuchIndividualByDisplayPath() {
		req.setRequestUrl(url(URL_HOME_PAGE + "/display/" + "bogusID"));
		req.addParameter("netid", NETID_USER_TEST);
		runTheRequest();
		assertResponseContains("by uri parameter", "error-titled.ftl");
	}

	// ----------------------------------------------------------------------
	// Tests - redirect a FileBytestream
	// ----------------------------------------------------------------------

	@Test
	public void redirectAFileBytestreamIndividual() {
		req.setRequestUrl(url(URL_HOME_PAGE + "/display/" + ID_FILE_BYTESTREAM));
		runTheRequest();
		assertRedirect("redirect file bytestream", URL_BYTESTREAM_ALIAS);
	}

	// ----------------------------------------------------------------------
	// Tests - redirect from a Linked Data path
	// ----------------------------------------------------------------------

	/** /individual/localname, accept=RDF redirects to /individual/id/id.rdf */
	@Test
	public void redirectFromLinkedDataPathAcceptRdf() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", VitroHttpServlet.RDFXML_MIMETYPE);
		runTheRequest();
		assertRedirect("by linked data path, accept RDF",
				redirectUrlForRdfStream(ID_INDIVIDUAL_TEST, ".rdf"));
	}

	/** /individual/localname, accept=RDF redirects to /individual/id/id.rdf */
	@Test
	public void redirectFromLinkedDataPathAcceptN3() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", VitroHttpServlet.N3_MIMETYPE);
		runTheRequest();
		assertRedirect("by linked data path, accept RDF",
				redirectUrlForRdfStream(ID_INDIVIDUAL_TEST, ".n3"));
	}

	/** /individual/localname, accept=RDF redirects to /individual/id/id.rdf */
	@Test
	public void redirectFromLinkedDataPathAcceptTurtle() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", VitroHttpServlet.TTL_MIMETYPE);
		runTheRequest();
		assertRedirect("by linked data path, accept RDF",
				redirectUrlForRdfStream(ID_INDIVIDUAL_TEST, ".ttl"));
	}

	/** /individual/localname, no accept, redirects to /display/id */
	@Test
	public void redirectFromLinkedDataPathNoAccept() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		runTheRequest();
		assertRedirect("by linked data path, no accept", "/display/"
				+ ID_INDIVIDUAL_TEST);
	}

	/**
	 * If the accept header is set to a recognized value, but not one of the
	 * onese that we like, treat the same as no accept.
	 */
	@Test
	public void redirectFromLinkedDataPathAcceptStrange() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", "application/json");
		runTheRequest();
		assertRedirect("by linked data path, no accept", "/display/"
				+ ID_INDIVIDUAL_TEST);
	}

	/**
	 * If the accept header is set to an unrecognized value, treat the same as
	 * no accept.
	 */
	@Test
	public void redirectFromLinkedDataPathAcceptGarbage() {
		req.setRequestUrl(url(DEFAULT_NAMESPACE + ID_INDIVIDUAL_TEST));
		req.setHeader("accept", "a/b/c");
		runTheRequest();
		assertRedirect("by linked data path, no accept", "/display/"
				+ ID_INDIVIDUAL_TEST);
	}

	// ----------------------------------------------------------------------
	// Tests - satisfy requests for RDF formats.
	// ----------------------------------------------------------------------

	@Test
	public void getRdfByUriAndFormatParameters() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		req.addParameter("format", "rdfxml");
		runTheRequest();
		assertResponseContains("RDF by uri and format parameters", "<rdf:RDF");
	}

	@Test
	public void getN3ByUriAndFormatParameters() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		req.addParameter("format", "n3");
		runTheRequest();
		assertResponseContains("N3 by uri and format parameters",
				"@prefix vitro:");
	}

	@Test
	public void getTurtleByUriAndFormatParameters() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		req.addParameter("format", "ttl");
		runTheRequest();
		assertResponseContains("Turtle by uri and format parameters",
				"@prefix vitro:");
	}

	@Test
	public void unrecognizedFormatParameter() {
		req.setRequestUrl(url(URL_INDIVIDUAL_PAGE));
		req.addParameter("uri", URI_INDIVIDUAL_TEST);
		req.addParameter("format", "bogus");
		runTheRequest();
		assertResponseContains("unrecognized format means HTML response",
				"Individual page: " + URI_INDIVIDUAL_TEST);
	}

	/** http://vivo.cornell.edu/individual/n23/n23.rdf */
	@Test
	public void getRdfByStreamRequest() {
		req.setRequestUrl(absoluteUrlForRdfStream(ID_INDIVIDUAL_TEST, ".rdf"));
		runTheRequest();
		assertResponseContains("RDF by RDF stream request", "<rdf:RDF");
	}

	/** http://vivo.cornell.edu/individual/n23/n23.n3 */
	@Test
	public void getN3ByStreamRequest() {
		req.setRequestUrl(absoluteUrlForRdfStream(ID_INDIVIDUAL_TEST, ".n3"));
		runTheRequest();
		assertResponseContains("N3 by RDF stream request", "@prefix vitro:");
	}

	/** http://vivo.cornell.edu/individual/n23/n23.rdf */
	@Test
	public void getTurtleByStreamRequest() {
		req.setRequestUrl(absoluteUrlForRdfStream(ID_INDIVIDUAL_TEST, ".ttl"));
		runTheRequest();
		assertResponseContains("Turtle by RDF stream request", "@prefix vitro:");
	}

	/** http://vivo.cornell.edu/individual/n23/n23.bogus is an error */
	@Test
	public void unrecognizedFormatForRdfStreamRequest() {
		req.setRequestUrl(absoluteUrlForRdfStream(ID_INDIVIDUAL_TEST, ".bogus"));
		runTheRequest();
		assertResponseContains("Unrecognized RDF stream request",
				"error-titled.ftl");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void addObjectProperty(OntModel ontModel, String subjectUri,
			String propertyUri, String objectUri) {
		Resource s = ontModel.getResource(subjectUri);
		Property p = ontModel.getProperty(propertyUri);
		Resource o = ontModel.getResource(objectUri);
		ontModel.add(s, p, o);
	}

	/** /individual/n23/n23.rdf, or the like */
	private String redirectUrlForRdfStream(String id, String extension) {
		return "/individual/" + id + "/" + id + extension;
	}

	/** http://vivo.mydomain.edu/individual/n23/n23.rdf, or the like */
	private URL absoluteUrlForRdfStream(String id, String extension) {
		return url(DEFAULT_NAMESPACE + id + "/" + id + extension);
	}

	private void runTheRequest() {
		try {
			controller.doGet(req, resp);
		} catch (IOException e) {
			fail("Error while running the controller: " + e.toString());
		} catch (ServletException e) {
			fail("Error while running the controller: " + e.toString());
		}
	}

	private void assertResponseContains(String message, String outputFragment) {
		String output = resp.getOutput();
		if (!output.contains(outputFragment)) {
			fail(message + ": output did not contain '" + outputFragment
					+ "': output was '" + output + "'");
		}
	}

	private void assertRedirect(String message, String redirectUrl) {
		if (!resp.containsHeader("Location")) {
			fail(message + ":, Not redirected - no Location header");
		}
		String actual = resp.getHeader("Location");
		assertEquals(message, redirectUrl, actual);
	}
}
