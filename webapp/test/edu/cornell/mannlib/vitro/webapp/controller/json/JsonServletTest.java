/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.json;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.dao.VClassDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modules.ApplicationStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineStub;
import stubs.javax.servlet.ServletConfigStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

/**
 * TODO
 */
public class JsonServletTest extends AbstractTestClass {
	private static final String GET_SEARCH_INDIVIDUALS_BY_VCLASS = "getSearchIndividualsByVClass";

	private static final String GET_VCLASSES_FOR_VCLASS_GROUP = "getVClassesForVClassGroup";

	private static final String VCLASS_ID = "vclassId";

	/**
	 * Test plan
	 * 
	 * <pre>
	 * 
	 * GetEntitiesByVClass, GetEntitiesByVClassContinuation
	 * 	from ents_edit.js
	 * 		ents_edit_head.jsp
	 *  (there is an ents_edit.jsp, invoked from EntityEditController, which does not seem to invoke ents_edit.js)
	 * 
	 * GetSearchIndividualsByVClass
	 * 	Mock out search engine and IndividualDao
	 *  invoked by BrowseDataGetter.java
	 *  	home page
	 *  invoked by ClassGroupPageData.java
	 *  	>>>> Bring up "People" tab.
	 *  invoked by BrowseWidget.java
	 * 
	 * GetSearchIndividualsByVClasses
	 * 	Mock out search engine and IndividualDao
	 *  invoked by IndividualsForClassesDataGetter.java
	 *  	ProcessIndividualsForClasses
	 *  		extended in vivo by ProcessInternalClasses
	 *  		SelectDataGetterUtils.java
	 *  	SelectDataGetterUtils
	 *  		MenuManagementEdit.java
	 *  		MenuManagementController.java
	 *  	extended in vivo by InternalClassesDataGetter, also invoked by SelectDataGetterUtils
	 * 
	 * GetDataForPage
	 * 	Mock out PageDao
	 * </pre>
	 */

	private JsonServlet servlet;
	private ServletConfigStub config;
	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;
	private HttpServletResponseStub resp;

	private WebappDaoFactoryStub wadf;
	private VClassDaoStub vcDao;

	private SearchEngineStub search;

	@Before
	public void setup() throws ServletException {
		ctx = new ServletContextStub();

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		config = new ServletConfigStub();
		config.setServletContext(ctx);

		servlet = new JsonServlet();
		servlet.init(config);

		req = new HttpServletRequestStub();
		req.setMethod("GET");
		req.setSession(session);

		resp = new HttpServletResponseStub();

		wadf = new WebappDaoFactoryStub();
		new ModelAccessFactoryStub().get(req).setWebappDaoFactory(wadf);

		vcDao = new VClassDaoStub();
		wadf.setVClassDao(vcDao);

		search = new SearchEngineStub();
		ApplicationStub.setup(new ServletContextStub(), search);
	}

	@Test
	public void noRecognizedRequestParameters() throws ServletException,
			IOException {
		servlet.service(req, resp);
		assertEquals("empty response", "", resp.getOutput());
		assertEquals("status=ok", SC_OK, resp.getStatus());
	}

	@Test
	public void vclassesNoClassgroup() throws ServletException, IOException {
		setLoggerLevel(JsonServlet.class, Level.FATAL);
		setLoggerLevel(JsonObjectProducer.class, Level.FATAL);
		req.addParameter(GET_VCLASSES_FOR_VCLASS_GROUP, "true");
		servlet.service(req, resp);
		assertFailureWithErrorMessage("java.lang.Exception: no URI passed for classgroupUri");
		assertEquals("status=failure", SC_INTERNAL_SERVER_ERROR,
				resp.getStatus());
	}

	/**
	 * TODO Modify VClassGroupCache so it can be stubbed out. JsonServlet asks
	 * VClassGroupCache for the current instance, and VClassGroupCache is a
	 * concrete class instead of an interface, so we can't replace the instance
	 * with one we like better. Furthermore, VClassGroupCache has a private
	 * constructor, so we can't change its behavior at all.
	 * 
	 * Also test: success but no VClasses found, success with one VClass,
	 * success with multiple VClasses. In each case, confirm proper status,
	 * character encoding, and content type on the response.
	 */
	@Ignore
	@Test
	public void vclassesClassgroupNotRecognized() throws ServletException,
			IOException {
		req.addParameter(GET_VCLASSES_FOR_VCLASS_GROUP, "true");
		req.addParameter("classgroupUri", "http://bogusUri");
		servlet.service(req, resp);
		assertEquals("empty response", "", resp.getOutput());
		assertEquals("status=failure", SC_INTERNAL_SERVER_ERROR,
				resp.getStatus());
	}

	@Test
	public void individualsByClassNoVClass() throws ServletException,
			IOException {
		setLoggerLevel(JsonServlet.class, Level.FATAL);
		setLoggerLevel(JsonObjectProducer.class, Level.FATAL);
		req.addParameter(GET_SEARCH_INDIVIDUALS_BY_VCLASS, "true");
		servlet.service(req, resp);
		assertFailureWithErrorMessage("java.lang.Exception: "
				+ "parameter vclassId URI parameter expected ");
	}

	@Test
	public void individualsByClassUnrecognizedVClass() throws ServletException,
			IOException {
		setLoggerLevel(JsonServlet.class, Level.FATAL);
		setLoggerLevel(JsonObjectProducer.class, Level.FATAL);
		String vclassId = "http://bogusVclass";
		req.addParameter(GET_SEARCH_INDIVIDUALS_BY_VCLASS, "true");
		req.addParameter(VCLASS_ID, vclassId);

		servlet.service(req, resp);
		assertFailureWithErrorMessage("java.lang.Exception: " + "Class "
				+ vclassId + " not found");
	}

	/**
	 * TODO test successful responses. This will require figuring out how to
	 * stub SearchEngine. Since we are no longer dealing with an abstract class
	 * (like SolrServer), so we just need to figure out what sort of NamedList
	 * is required as a response to a request.
	 */
	@Test
	public void individualsByClassNoIndividuals() throws ServletException,
			IOException {
		setLoggerLevel(JsonServlet.class, Level.FATAL);
		setLoggerLevel(ModelAccess.class, Level.ERROR);
		String vclassId = "http://myVclass";
		vcDao.setVClass(vclassId, new VClass(vclassId));
		req.addParameter(GET_SEARCH_INDIVIDUALS_BY_VCLASS, "true");
		req.addParameter(VCLASS_ID, vclassId);

		servlet.service(req, resp);
		assertSuccessWithIndividuals(vclassId, 0);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/**
	 * The response should be a JSONObject that contained this error-message,
	 * and the status should be set to INTERNAL_SERVER_ERROR.
	 */
	private void assertFailureWithErrorMessage(String expected) {
		try {
			JSONObject result = new JSONObject(resp.getOutput());
			assertEquals("errorMessage", expected,
					getFieldValue(result, "errorMessage"));
			assertEquals("status", SC_INTERNAL_SERVER_ERROR, resp.getStatus());
		} catch (JSONException e) {
			fail(e.toString());
		}
	}

	private void assertSuccessWithIndividuals(String vclassId, int count) {
		try {
			JSONObject actual = new JSONObject(resp.getOutput());
			assertEquals("errorMessage", "",
					getFieldValue(actual, "errorMessage"));
			assertEquals("count", count, getFieldValue(actual, "totalCount"));

			JSONObject vclassObj = (JSONObject) getFieldValue(actual, "vclass");
			assertEquals("vclass name", vclassId.split("://")[1],
					getFieldValue(vclassObj, "name"));
			assertEquals("vclass uri", vclassId,
					getFieldValue(vclassObj, "URI"));

			assertEquals("status", SC_OK, resp.getStatus());
		} catch (JSONException e) {
			fail(e.toString());
		}
	}

	private Object getFieldValue(JSONObject json, String fieldName) {
		try {
			assertEquals("find " + fieldName, true, json.has(fieldName));
			return json.get(fieldName);
		} catch (JSONException e) {
			fail(e.toString());
			return -1;
		}
	}

}
