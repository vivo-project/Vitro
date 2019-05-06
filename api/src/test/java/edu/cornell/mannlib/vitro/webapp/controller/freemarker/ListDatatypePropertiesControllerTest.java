/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import java.text.Collator;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

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
 * Not a well-formed set of unit tests. But it's a pretty good exercise of the
 * different possibilities in the output stage.
 *
 * Test plan:
 *
 * <pre>
 * No data - roots is null -- NONSENSICAL
 * No data - roots is empty
 *
 * Ontology is not specified
 * Ontology is specified and matches
 * Ontology is specified and does not match
 * DataProperty does not match Ontology, but has child properties that do.
 *
 * Name from picklistName
 * Name from getName
 * Name from getUri
 * Name no URL? -- NONSENSICAL
 *
 * InternalName from picklistName
 * InternalName missing.
 *
 * Domain class no class URI
 * Domain class no class for URI
 * Domain class get picklistName from Language Aware DAO
 * Domain class use picklistName from Language Neutral DAO
 *
 * Range no range URI
 * Range no datatype for URI
 * Range datatype has no name
 * Range has a name
 *
 * Group no group URI
 * Group no group for URI
 * Group no name
 * Group has a name
 *
 * DP1 Ont1, no name, no picklistName, no domainClass, no RangeClass, no GroupURI
 * DP2 Ont2, name, no picklistName, no domain class for URI, no range datatype for URI, no group for GroupURI
 * DP3 Ont1, picklistname, domainclass no picklistname, range datatype with no name, group has no name
 * DP4 Ont1, picklistname, domainclass w/picklistname, range datatype with name, group with name
 *
 * Try once with no data
 * Try with all data and no ontology specified
 * Try with all data and Ont1, Ont2, Ont3
 * </pre>
 */
public class ListDatatypePropertiesControllerTest
		extends ListControllerTestBase {
	private static final String PATH = "datapropEdit";
	private static final String ONT1 = "http://ont1/";
	private static final String ONT2 = "http://ont2/";
	private static final String ONT3 = "http://ont3/";
	private static final String DP1 = ONT1 + "first";
	private static final String DP2 = ONT2 + "second";
	private static final String DP3 = ONT1 + "third";
	private static final String DP4 = ONT1 + "fourth";
	private static final String DP2_NAME = "TWO";
	private static final String DP3_NAME = "THREE";
	private static final String DP4_NAME = "FOUR";
	private static final String DP3_PICK_NAME = "The third one";
	private static final String DP4_PICK_NAME = "The fourth one";
	private static final String DOMAIN_NONE = "http://domain/noSuchDomain";
	private static final String DOMAIN_NO_NAME = "http://domain/domainWithNoName";
	private static final String DOMAIN_W_NAME = "http://domain/namedDomain";
	private static final String NAME_DOMAIN = "An excellent domain";
	private static final String RANGE_NONE = "http://domain/noSuchRange";
	private static final String RANGE_NO_NAME = "http://domain/rangeWithNoName";
	private static final String RANGE_W_NAME = "http://domain/namedRange";
	private static final String NAME_RANGE = "Home on the range";
	private static final String GROUP_NONE = "http://domain/noSuchGroup";
	private static final String GROUP_NO_NAME = "http://domain/groupWithNoName";
	private static final String GROUP_W_NAME = "http://domain/namedGroup";
	private static final String NAME_GROUP = "The Groupsters";

	private static final ArrayNode NO_DATA_RESPONSE = arrayOf(
			mapper.createObjectNode().put("name", "No data properties found"));

	private static final JsonNode RESPONSE_UNFILTERED = arrayOf(
			propertyListNode(PATH, DP1, "first", "first", "", "",
					"unspecified"),
			propertyListNode(PATH, DP2, "second", "second", "", RANGE_NONE,
					"unknown group"),
			propertyListNode(PATH, DP4, DP4_PICK_NAME, DP4_PICK_NAME,
					NAME_DOMAIN, NAME_RANGE, NAME_GROUP),
			propertyListNode(PATH, DP3, DP3_PICK_NAME, DP3_PICK_NAME,
					"domainWithNoName", "", ""));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT1 = arrayOf(
			propertyListNode(PATH, DP1, "first", "first", "", "",
					"unspecified"),
			propertyListNode(PATH, DP4, DP4_PICK_NAME, DP4_PICK_NAME,
					NAME_DOMAIN, NAME_RANGE, NAME_GROUP),
			propertyListNode(PATH, DP3, DP3_PICK_NAME, DP3_PICK_NAME,
					"domainWithNoName", "", ""));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT2 = arrayOf(
			propertyListNode(PATH, DP2, "second", "second", "", RANGE_NONE,
					"unknown group"));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT3 = arrayOf(
			mapper.createObjectNode().put("name", "No data properties found"));

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
	public void noDataProperties() throws Exception {
		assertMatchingJson(controller, req, NO_DATA_RESPONSE);
	}

	@Test
	public void unfiltered() throws Exception {
		populate();
		assertMatchingJson(controller, req, RESPONSE_UNFILTERED);
	}

	@Test
	public void filteredByOnt1() throws Exception {
		populate();
		req.addParameter("ontologyUri", ONT1);
		assertMatchingJson(controller, req, RESPONSE_FILTERED_BY_ONT1);
	}

	@Test
	public void filteredByOnt2() throws Exception {
		populate();
		req.addParameter("ontologyUri", ONT2);
		assertMatchingJson(controller, req, RESPONSE_FILTERED_BY_ONT2);
	}

	@Test
	public void filteredByOnt3() throws Exception {
		populate();
		req.addParameter("ontologyUri", ONT3);
		assertMatchingJson(controller, req, RESPONSE_FILTERED_BY_ONT3);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void populate() {
		vcdao.setVClass(vclass(DOMAIN_NO_NAME, null));
		vcdao.setVClass(vclass(DOMAIN_W_NAME, NAME_DOMAIN));
		dtdao.addDatatype(datatype(RANGE_NO_NAME, null));
		dtdao.addDatatype(datatype(RANGE_W_NAME, NAME_RANGE));
		pgdao.addPropertyGroup(propertyGroup(GROUP_NO_NAME, null));
		pgdao.addPropertyGroup(propertyGroup(GROUP_W_NAME, NAME_GROUP));
		dpdao.addDataProperty(dataProperty(DP1, null, null, null, null, null));
		dpdao.addDataProperty(dataProperty(DP2, DP2_NAME, null, DOMAIN_NONE,
				RANGE_NONE, GROUP_NONE));
		dpdao.addDataProperty(dataProperty(DP3, DP3_NAME, DP3_PICK_NAME,
				DOMAIN_NO_NAME, RANGE_NO_NAME, GROUP_NO_NAME));
		dpdao.addDataProperty(dataProperty(DP4, DP4_NAME, DP4_PICK_NAME,
				DOMAIN_W_NAME, RANGE_W_NAME, GROUP_W_NAME));
	}

	private DataProperty dataProperty(String uri, String name,
			String pickListName, String domainClassUri, String rangeDatatypeUri,
			String groupUri) {
		DataProperty dp = new DataProperty();
		dp.setURI(uri);
		dp.setName(name);
		dp.setPickListName(pickListName);
		dp.setDomainClassURI(domainClassUri);
		dp.setRangeDatatypeURI(rangeDatatypeUri);
		dp.setGroupURI(groupUri);
		return dp;
	}

	private Datatype datatype(String uri, String name) {
		Datatype dt = new Datatype();
		dt.setUri(uri);
		dt.setName(name);
		return dt;
	}

	private VClass vclass(String uri, String pickListName) {
		VClass vc = new VClass();
		vc.setURI(uri);
		vc.setPickListName(pickListName);
		return vc;
	}

	private PropertyGroup propertyGroup(String uri, String name) {
		PropertyGroup pg = new PropertyGroup();
		pg.setURI(uri);
		pg.setName(name);
		return pg;
	}
}
