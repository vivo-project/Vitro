/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ASSERTIONS_ONLY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.POLICY_NEUTRAL;

import java.text.Collator;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.DatatypeDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.VClassDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.http.HttpServletRequestStub;

/**
 * Not a well-formed set of unit tests. Rather, this simply captures one output
 * from the Controller, so we can be sure that it's the same when we change JSON
 * engines.
 * 
 * On the other hand, the data is chosen in such a way as to exercise many of
 * the branches in the JSON generation code, so there's that.
 */
public class ShowDataPropertyHierarchyControllerTest
		extends ListControllerTestBase {
	private static final String NO_DATA_JSON = "" //
			+ "{ \n " //
			+ "  \"name\" : \"<a href='datapropEdit?uri=nullfake'>ullfake</a>\", \n " //
			+ "  \"data\" : { \n " //
			+ "    \"internalName\" : \"ullfake\", \n " //
			+ "    \"domainVClass\" : \"\", \n " //
			+ "    \"rangeVClass\" : \"\", \n " //
			+ "    \"group\" : \"unspecified\" \n " //
			+ "  }, \n " //
			+ "  \"children\" : [] \n " //
			+ "} \n " //
	;

	private static final String BASIC_JSON_EXPECTED = "" //
			+ "{ \n " //
			+ "  \"name\" : \"<a href='datapropEdit?uri=http%3A%2F%2Ftest%2FdataProperty1'>dataProperty1</a>\", \n " //
			+ "  \"data\" : { \n " //
			+ "    \"internalName\" : \"dataProperty1\", \n " //
			+ "    \"domainVClass\" : \"\", \n " //
			+ "    \"rangeVClass\" : \"\", \n " //
			+ "    \"group\" : \"unspecified\" \n " //
			+ "  }, \n " //
			+ "  \"children\" : [ \n " //
			+ "    { \n " //
			+ "      \"name\" : \"<a href='datapropEdit?uri=http%3A%2F%2Ftest%2FdataProperty2'>dataProperty2</a>\", \n " //
			+ "      \"data\" : { \n " //
			+ "        \"internalName\" : \"dataProperty2\", \n " //
			+ "        \"domainVClass\" : \"\", \n " //
			+ "        \"rangeVClass\" : \"\", \n " //
			+ "        \"group\" : \"unspecified\" \n " //
			+ "      }, \n " //
			+ "      \"children\" : [] \n " //
			+ "    }, \n " //
			+ "    { \n " //
			+ "      \"name\" : \"<a href='datapropEdit?uri=http%3A%2F%2Ftest%2FdataProperty3'>Property Three</a>\", \n " //
			+ "      \"data\" : { \n " //
			+ "        \"internalName\" : \"Property Three\", \n " //
			+ "        \"domainVClass\" : \"domain1\", \n " //
			+ "        \"rangeVClass\" : \"A_range\", \n " //
			+ "        \"group\" : \"PropGroup\" \n " //
			+ "      }, \n " //
			+ "      \"children\" : [] \n " //
			+ "    } \n " //
			+ "  ] \n " //
			+ "} \n " //
	;

	private static final String DP_URI_1 = "http://test/dataProperty1";
	private static final String DP_URI_2 = "http://test/dataProperty2";
	private static final String DP_URI_3 = "http://test/dataProperty3";
	private static final String DOMAIN_CLASS_URI = "http://test/domain1";
	private static final String RANGE_DATATYPE_URI = "http://test/range1";
	private static final String GROUP_URI = "http://test/classGroup1";

	private ShowDataPropertyHierarchyController controller;
	private HttpServletRequestStub req;
	private ModelAccessFactoryStub modelsFactory;
	private WebappDaoFactoryStub wadf;
	private DatatypeDaoStub ddao;
	private DataPropertyDaoStub dpdao;
	private PropertyGroupDaoStub pgdao;
	private VClassDaoStub vcdao;

	@Before
	public void setup() {
		controller = new ShowDataPropertyHierarchyController();

		req = new HttpServletRequestStub();
		new VitroRequest(req).setCollator(Collator.getInstance());

		ddao = new DatatypeDaoStub();

		dpdao = new DataPropertyDaoStub();

		pgdao = new PropertyGroupDaoStub();

		vcdao = new VClassDaoStub();

		wadf = new WebappDaoFactoryStub();
		wadf.setDatatypeDao(ddao);
		wadf.setDataPropertyDao(dpdao);
		wadf.setPropertyGroupDao(pgdao);
		wadf.setVClassDao(vcdao);

		modelsFactory = new ModelAccessFactoryStub();
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL);
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL,
				ASSERTIONS_ONLY);
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL,
				LANGUAGE_NEUTRAL);
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void noDataTest() throws Exception {
		// The NO DATA response is not valid JSON unless we kluge it.
		String rawResponse = getJsonFromController(controller, req);
		String kluged = rawResponse + "]}";
		assertKlugedJson(NO_DATA_JSON, kluged);

		// assertMatchingJson(controller, req, NO_DATA_JSON);
	}

	@Test
	public void basicJsonTest() throws Exception {
		dpdao.addDataProperty(dataProperty(DP_URI_1));
		dpdao.addDataProperty(dataProperty(DP_URI_2), DP_URI_1);
		dpdao.addDataProperty(dataProperty(DP_URI_3, "Property Three",
				DOMAIN_CLASS_URI, RANGE_DATATYPE_URI, GROUP_URI), DP_URI_1);
		ddao.addDatatype(datatype(RANGE_DATATYPE_URI, "A_range"));
		pgdao.addPropertyGroup(propertyGroup(GROUP_URI, "PropGroup"));
		vcdao.setVClass(vclass(DOMAIN_CLASS_URI, "The_domain"));
		assertMatchingJson(controller, req, BASIC_JSON_EXPECTED);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private DataProperty dataProperty(String uri) {
		DataProperty dp = new DataProperty();
		dp.setURI(uri);
		return dp;
	}

	private DataProperty dataProperty(String uri, String name,
			String domainClassUri, String rangeDatatypeUri, String groupUri) {
		DataProperty dp = new DataProperty();
		dp.setURI(uri);
		dp.setPickListName(name);
		dp.setDomainClassURI(domainClassUri);
		dp.setRangeDatatypeURI(rangeDatatypeUri);
		dp.setGroupURI(groupUri);
		return dp;
	}

	private Datatype datatype(String uri, String name) {
		Datatype d = new Datatype();
		d.setUri(uri);
		d.setName(name);
		return d;
	}

	private PropertyGroup propertyGroup(String uri, String name) {
		PropertyGroup pg = new PropertyGroup();
		pg.setURI(uri);
		pg.setName(name);
		return pg;
	}

	private VClass vclass(String uri) {
		VClass vc = new VClass();
		vc.setURI(uri);
		return vc;
	}

	private VClass vclass(String uri, String name) {
		VClass vc = vclass(uri);
		vc.setName(name);
		return vc;
	}

}
