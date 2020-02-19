/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ASSERTIONS_ONLY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.POLICY_NEUTRAL;

import java.text.Collator;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.OntologyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.VClassDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDaoStub;
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
 * VClass does not match Ontology, but has child properties that do.
 *
 * pickListName
 * no pickListName
 *
 * shortDef
 * no shortDef
 *
 * Group no group URI
 * Group no group for URI
 * Group no name
 * Group has a name
 *
 * No ontology for namespace
 * Ontology but no name
 * Ontology with name
 *
 * DP_1A Ont1, no pickListName, no shortDef, no GroupURI, no matching Ontology
 * DP_1B Ont2, pickListName, shortDef, no group for GroupURI, ontology has no name
 * DP_1B2A Ont3, pickListName, shortDef, group has no name, ontology with name
 * DP_1B2B Ont1, pickListName, shortDef, group with name, no matching Ontology
 *
 * Try once with no data
 * Try with all data and no ontology specified
 * Try with all data and Ont1, Ont2, Ont3
 * </pre>
 */
public class ShowClassHierarchyControllerTest extends ListControllerTestBase {
	private static final String PATH = "vclassEdit";
	private static final String ONT1 = "http://ont1/";
	private static final String ONT2 = "http://ont2/";
	private static final String ONT3 = "http://ont3/";
	private static final String ONT3_NAME = "Fabulous Ontology";
	private static final String URI_GREATAUNT = ONT1 + "greatAunt";
	private static final String URI_GRANDMOTHER = ONT2 + "grandmother";
	private static final String URI_AUNT = ONT3 + "aunt";
	private static final String URI_MOTHER = ONT1 + "mother";
	private static final String NAME_GRANDMOTHER = "GrandMother";
	private static final String NAME_AUNT = "Aunt";
	private static final String NAME_MOTHER = "Mother";
	private static final String SHORT_DEF_GRANDMOTHER = "My GrandMother";
	private static final String SHORT_DEF_AUNT = "My Aunt";
	private static final String SHORT_DEF_MOTHER = "My Mother";
	private static final String GROUP_NONE = "http://domain/noSuchGroup";
	private static final String GROUP_NO_NAME = "http://domain/groupWithNoName";
	private static final String GROUP_W_NAME = "http://domain/namedGroup";
	private static final String NAME_GROUP = "The Groupsters";

	private static final ArrayNode JSON_EMPTY_RESPONSE = arrayOf(
			vclassHierarchyNode(PATH,
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#Resource",
					"Resource", "", "",
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#"));

	private static final JsonNode RESPONSE_UNFILTERED = arrayOf(
			vclassHierarchyNode(PATH,URI_GRANDMOTHER, NAME_GRANDMOTHER,
					SHORT_DEF_GRANDMOTHER, "", ONT2,
					vclassHierarchyNode(PATH,URI_AUNT, NAME_AUNT, SHORT_DEF_AUNT, "",
							ONT3_NAME),
					vclassHierarchyNode(PATH,URI_MOTHER, NAME_MOTHER,
							SHORT_DEF_MOTHER, NAME_GROUP, ONT1)),
			vclassHierarchyNode(PATH,URI_GREATAUNT, "greatAunt", "", "", ONT1));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT1 = arrayOf(
			vclassHierarchyNode(PATH,URI_GREATAUNT, "greatAunt", "", "", ONT1));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT2 = arrayOf(
			vclassHierarchyNode(PATH,URI_GRANDMOTHER, NAME_GRANDMOTHER,
					SHORT_DEF_GRANDMOTHER, "", ONT2));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT3 = arrayOf();

	private ShowClassHierarchyController controller;

	private HttpServletRequestStub req;
	private ModelAccessFactoryStub modelsFactory;

	private OntologyDaoStub ontdao;
	private VClassDaoStub vcdao;
	private VClassGroupDaoStub vcgdao;
	private WebappDaoFactoryStub wadf;

	@Before
	public void setup() {
		controller = new ShowClassHierarchyController();

		req = new HttpServletRequestStub();
		new VitroRequest(req).setCollator(Collator.getInstance());

		ontdao = new OntologyDaoStub();

		vcdao = new VClassDaoStub();

		vcgdao = new VClassGroupDaoStub();

		wadf = new WebappDaoFactoryStub();
		wadf.setOntologyDao(ontdao);
		wadf.setVClassDao(vcdao);
		wadf.setVClassGroupDao(vcgdao);

		modelsFactory = new ModelAccessFactoryStub();
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL);
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL,
				ASSERTIONS_ONLY);
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void noData() throws Exception {
		assertMatchingJson(controller, req, JSON_EMPTY_RESPONSE);
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
		ontdao.insertNewOntology(ontology(ONT2, null));
		ontdao.insertNewOntology(ontology(ONT3, ONT3_NAME));
		vcgdao.setGroups(vclassGroup(GROUP_NO_NAME, null));
		vcgdao.setGroups(vclassGroup(GROUP_W_NAME, NAME_GROUP));
		vcdao.setVClass(vclass(URI_GREATAUNT, null, null, null));
		vcdao.setVClass(vclass(URI_GRANDMOTHER, NAME_GRANDMOTHER, GROUP_NONE,
				SHORT_DEF_GRANDMOTHER));
		vcdao.setVClass(
				vclass(URI_AUNT, NAME_AUNT, GROUP_NO_NAME, SHORT_DEF_AUNT),
				URI_GRANDMOTHER);
		vcdao.setVClass(
				vclass(URI_MOTHER, NAME_MOTHER, GROUP_W_NAME, SHORT_DEF_MOTHER),
				URI_GRANDMOTHER);
	}

	private VClass vclass(String uri, String name, String groupUri,
			String shortDef) {
		VClass vc = new VClass();
		vc.setURI(uri);
		vc.setPickListName(name);
		vc.setShortDef(shortDef);
		vc.setGroupURI(groupUri);
		VClassGroup group = vcgdao.getGroupByURI(groupUri);
		if (group != null) {
			group.add(vc);
		}
		return vc;
	}

	private Ontology ontology(String uri, String name) {
		Ontology o = new Ontology();
		o.setURI(uri);
		o.setName(name);
		return o;
	}

	private VClassGroup vclassGroup(String uri, String name) {
		VClassGroup vcg = new VClassGroup();
		vcg.setURI(uri);
		vcg.setPublicName(name);
		return vcg;
	}
}
