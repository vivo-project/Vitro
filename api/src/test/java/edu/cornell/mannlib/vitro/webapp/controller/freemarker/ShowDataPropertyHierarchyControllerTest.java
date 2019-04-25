/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ASSERTIONS_ONLY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.POLICY_NEUTRAL;

import java.text.Collator;

import org.junit.Before;
import org.junit.Test;

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
 * Children no children
 * Children sorted by picklist
 *
 * DP_1A Ont1, no name, no picklistName, no domainClass, no RangeClass, no GroupURI
 * DP_1B Ont2, name, no picklistName, no domain class for URI, no range datatype for URI, no group for GroupURI
 * DP_1B2A Ont1, picklistname, domainclass no picklistname, range datatype with no name, group has no name
 * DP_1B2B Ont1, picklistname(less than 1B2A), domainclass w/picklistname, range datatype with name, group with name
 *
 * Try once with no data
 * Try with all data and no ontology specified
 * Try with all data and Ont1, Ont2, Ont3
 * </pre>
 */
public class ShowDataPropertyHierarchyControllerTest
		extends ListControllerTestBase {
	private static final String PATH = "datapropEdit";
	private static final String ONT1 = "http://ont1/";
	private static final String ONT2 = "http://ont2/";
	private static final String ONT3 = "http://ont3/";
	private static final String URI_GREATAUNT = ONT1 + "greatAunt";
	private static final String URI_GRANDMOTHER = ONT2 + "grandmother";
	private static final String URI_AUNT = ONT1 + "aunt";
	private static final String URI_MOTHER = ONT1 + "mother";
	private static final String URI_DAUGHTER = ONT2 + "daughter";
	private static final String NAME_GRANDMOTHER = "GrandMother";
	private static final String NAME_AUNT = "Aunt";
	private static final String NAME_MOTHER = "Mother";
	private static final String NAME_DAUGHTER = "Me";
	private static final String PICK_NAME_AUNT = "Old Aunt Agnes";
	private static final String PICK_NAME_MOTHER = "My Mother";
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

	private static final ArrayNode NO_DATA_RESPONSE = arrayOf( //
			propertyHierarchyNode(PATH, "nullfake", "ullfake", "ullfake", "",
					"", "unspecified"));

	private static final ArrayNode RESPONSE_UNFILTERED = arrayOf(
			propertyHierarchyNode(PATH, URI_GRANDMOTHER, "grandmother",
					"grandmother", "", "http://domain/noSuchRange",
					"unknown group",
					propertyHierarchyNode(PATH, URI_MOTHER, PICK_NAME_MOTHER,
							"My Mother", "namedDomain", "Home on the range",
							"The Groupsters",
							propertyHierarchyNode(PATH, URI_DAUGHTER,
									"daughter", "daughter", "", "",
									"unspecified")),
					propertyHierarchyNode(PATH, URI_AUNT, PICK_NAME_AUNT,
							"Old Aunt Agnes", "domainWithNoName", "", "")),
			propertyHierarchyNode(PATH, URI_GREATAUNT, "greatAunt", "greatAunt",
					"", "", "unspecified"));

	private static final ArrayNode RESPONSE_FILTERED_BY_ONT1 = arrayOf(
			propertyHierarchyNode(PATH, URI_GREATAUNT, "greatAunt", "greatAunt",
					"", "", "unspecified"));

	private static final ArrayNode RESPONSE_FILTERED_BY_ONT2 = arrayOf(
			propertyHierarchyNode(PATH, URI_GRANDMOTHER, "grandmother",
					"grandmother", "", "http://domain/noSuchRange",
					"unknown group", propertyHierarchyNode(PATH, URI_DAUGHTER,
							"daughter", "daughter", "", "", "unspecified")));

	private static final ArrayNode RESPONSE_FILTERED_BY_ONT3 = arrayOf();

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
		assertKlugedJson(NO_DATA_RESPONSE, kluged);

		// assertMatchingJson(controller, req, NO_DATA_RESPONSE);
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
		ddao.addDatatype(datatype(RANGE_NO_NAME, null));
		ddao.addDatatype(datatype(RANGE_W_NAME, NAME_RANGE));
		pgdao.addPropertyGroup(propertyGroup(GROUP_NO_NAME, null));
		pgdao.addPropertyGroup(propertyGroup(GROUP_W_NAME, NAME_GROUP));
		dpdao.addDataProperty(
				dataProperty(URI_GREATAUNT, null, null, null, null, null));
		dpdao.addDataProperty(dataProperty(URI_GRANDMOTHER, NAME_GRANDMOTHER,
				null, DOMAIN_NONE, RANGE_NONE, GROUP_NONE));
		dpdao.addDataProperty(
				dataProperty(URI_AUNT, NAME_AUNT, PICK_NAME_AUNT,
						DOMAIN_NO_NAME, RANGE_NO_NAME, GROUP_NO_NAME),
				URI_GRANDMOTHER);
		dpdao.addDataProperty(
				dataProperty(URI_MOTHER, NAME_MOTHER, PICK_NAME_MOTHER,
						DOMAIN_W_NAME, RANGE_W_NAME, GROUP_W_NAME),
				URI_GRANDMOTHER);
		dpdao.addDataProperty(dataProperty(URI_DAUGHTER, NAME_DAUGHTER, null,
				null, null, null), URI_MOTHER);
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
