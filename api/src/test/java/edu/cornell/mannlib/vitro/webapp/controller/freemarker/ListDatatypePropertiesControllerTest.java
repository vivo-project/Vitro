/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import java.text.Collator;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Datatype;
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
public class ListDatatypePropertiesControllerTest
		extends ListControllerTestBase {
	private static final ArrayNode BASIC_JSON_RESPONSE = arrayOf( //
			propertyListNode(
					"<a href='datapropEdit?uri=http%3A%2F%2Fdata.property%2Fone'>One_pick</a>",
					"One_pick", "", "", "unspecified"),
			propertyListNode(
					"<a href='datapropEdit?uri=http%3A%2F%2Fdata.property%2Ftwo'>Two_pick</a>",
					"Two_pick", "domain", "range_name", "unknown group"));

	private static final ArrayNode NO_DATA_RESPONSE = arrayOf(
			mapper.createObjectNode().put("name", "No data properties found"));

	private static final String RANGE_VCLASS_URI = "http://v.class/range";
	private static final String DOMAIN_VCLASS_URI = "http://v.class/domain";
	private static final String PROPERTY_GROUP_URI = "http://prop.group/group1";

	private ListDatatypePropertiesController controller;
	private HttpServletRequestStub req;
	private ModelAccessFactoryStub modelsFactory;
	private WebappDaoFactoryStub wadf;
	private DataPropertyDaoStub dpdao;
	private DatatypeDaoStub dtdao;
	private VClassDaoStub vcdao;
	private PropertyGroupDaoStub pgdao;

	@Before
	public void setup() {
		controller = new ListDatatypePropertiesController();

		req = new HttpServletRequestStub();
		new VitroRequest(req).setCollator(Collator.getInstance());

		dpdao = new DataPropertyDaoStub();
		dtdao = new DatatypeDaoStub();
		vcdao = new VClassDaoStub();
		pgdao = new PropertyGroupDaoStub();

		wadf = new WebappDaoFactoryStub();
		wadf.setDataPropertyDao(dpdao);
		wadf.setDatatypeDao(dtdao);
		wadf.setVClassDao(vcdao);
		wadf.setPropertyGroupDao(pgdao);

		modelsFactory = new ModelAccessFactoryStub();
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL);
		modelsFactory.get(req).setWebappDaoFactory(wadf, LANGUAGE_NEUTRAL,
				POLICY_NEUTRAL);
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void basicJsonTest() throws Exception {
		dpdao.addDataProperty(dataProperty("http://data.property/one", "One"));
		dpdao.addDataProperty(dataProperty("http://data.property/two", "Two",
				RANGE_VCLASS_URI, DOMAIN_VCLASS_URI, PROPERTY_GROUP_URI));

		dtdao.addDatatype(datatype("http://data.type/one", "one_name"));
		dtdao.addDatatype(datatype(RANGE_VCLASS_URI, "range_name"));

		vcdao.setVClass(vclass(DOMAIN_VCLASS_URI));

		assertMatchingJson(controller, req, BASIC_JSON_RESPONSE);
	}

	@Test
	public void noDataProperties() throws Exception {
		assertMatchingJson(controller, req, NO_DATA_RESPONSE);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private DataProperty dataProperty(String uri, String name) {
		DataProperty dp = new DataProperty();
		dp.setURI(uri);
		dp.setName(name);
		dp.setPickListName(name + "_pick");
		return dp;
	}

	private DataProperty dataProperty(String uri, String name,
			String rangeVClass, String domainVClass, String propertyGroup) {
		DataProperty dp = dataProperty(uri, name);
		dp.setRangeDatatypeURI(rangeVClass);
		dp.setDomainClassURI(domainVClass);
		dp.setGroupURI(propertyGroup);
		return dp;
	}

	private Datatype datatype(String uri, String name) {
		Datatype dt = new Datatype();
		dt.setUri(uri);
		dt.setName(name);
		return dt;
	}

	private VClass vclass(String uri) {
		VClass vc = new VClass();
		vc.setURI(uri);
		return vc;
	}

}
