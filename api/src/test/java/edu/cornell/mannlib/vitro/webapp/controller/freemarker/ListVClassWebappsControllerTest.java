/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import java.text.Collator;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

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
 *
 * Ontology is not specified
 * Ontology is specified and matches
 * Ontology is specified and does not match
 *
 * classes = empty
 * classes = null -- UNREALISTIC
 *
 * no pickListName
 * pickListName
 *
 * shortDef
 * no shortDef
 *
 * no group uri
 * no group for uri
 * no name for group
 * group with name
 *
 * ontology not found
 * ontology no name
 * ontology with name
 *
 * VC1 - Ont1, no pickListName, no shortDef, no GroupURI, no matching Ontology
 * VC2 - Ont2, pickListName, shortDef, no group for GroupURI, ontology has no name
 * VC3 - Ont2, pickListName, shortDef, group has no name, ontology with name
 * VC4 - Ont1, pickListName, shortDef, group with name, no matching Ontology
 *
 * Try once with no data
 * Try with all data and no ontology specified
 * Try with all data and Ont1, Ont2, Ont3
 * Sorted by picklist
 * </pre>
 */

public class ListVClassWebappsControllerTest extends ListControllerTestBase {
	private static final String PATH = "./vclassEdit";
	private static final String ONT1 = "http://ont1/";
	private static final String ONT2 = "http://ont2/";
	private static final String ONT3 = "http://ont3/";
	private static final String ONT2_NAME = "Fabulous Ontology";
	private static final String VC1 = ONT1 + "vc1";
	private static final String VC2 = ONT2 + "vc2";
	private static final String VC3 = ONT2 + "vc3";
	private static final String VC4 = ONT1 + "vc4";
	private static final String VC2_NAME = "Carol";
	private static final String VC3_NAME = "Ted";
	private static final String VC4_NAME = "ALice";
	private static final String VC2_SHORT_DEF = "Short Carol";
	private static final String VC3_SHORT_DEF = "Tiny Ted";
	private static final String VC4_SHORT_DEF = "Wee ALice";
	private static final String GROUP_NONE = "http://domain/noSuchGroup";
	private static final String GROUP_NO_NAME = "http://domain/groupWithNoName";
	private static final String GROUP_W_NAME = "http://domain/namedGroup";
	private static final String NAME_GROUP = "The Groupsters";

	private static final JsonNode JSON_EMPTY_RESPONSE = arrayOf();

	private static final JsonNode RESPONSE_UNFILTERED = arrayOf(
			vclassListNode(PATH, VC4, VC4_NAME, VC4_SHORT_DEF, NAME_GROUP,
					ONT1),
			vclassListNode(PATH, VC2, VC2_NAME, VC2_SHORT_DEF, "", ONT2_NAME),
			vclassListNode(PATH, VC3, VC3_NAME, VC3_SHORT_DEF, "", ONT2_NAME),
			degenerateVclassListNode("", "", "", ONT1) // VC1
	);

	private static final JsonNode RESPONSE_FILTERED_BY_ONT1 = arrayOf(
			vclassListNode(PATH, VC4, VC4_NAME, VC4_SHORT_DEF, NAME_GROUP,
					ONT1),
			degenerateVclassListNode("", "", "", ONT1) // VC1
	);

	private static final JsonNode RESPONSE_FILTERED_BY_ONT2 = arrayOf(
			vclassListNode(PATH, VC2, VC2_NAME, VC2_SHORT_DEF, "", ONT2_NAME),
			vclassListNode(PATH, VC3, VC3_NAME, VC3_SHORT_DEF, "", ONT2_NAME));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT3 = arrayOf();

	private ListVClassWebappsController controller;
	private HttpServletRequestStub req;
	private ModelAccessFactoryStub modelsFactory;
	private WebappDaoFactoryStub wadf;
	private OntologyDaoStub odao;
	private VClassDaoStub vcdao;
	private VClassGroupDaoStub vcgdao;

	@Before
	public void setup() {
		controller = new ListVClassWebappsController();

		req = new HttpServletRequestStub();
		new VitroRequest(req).setCollator(Collator.getInstance());

		odao = new OntologyDaoStub();
		vcdao = new VClassDaoStub();
		vcgdao = new VClassGroupDaoStub();

		wadf = new WebappDaoFactoryStub();
		wadf.setOntologyDao(odao);
		wadf.setVClassDao(vcdao);
		wadf.setVClassGroupDao(vcgdao);

		modelsFactory = new ModelAccessFactoryStub();
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL);
		// modelsFactory.get(req).setWebappDaoFactory(wadf, LANGUAGE_NEUTRAL,
		// POLICY_NEUTRAL);
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

		// No name produces invalid JSON so we kluge it for easy comparison
		String rawResponse = getJsonFromController(controller, req);
		String kluged = rawResponse.replace("\"\"\"", "\"\",\"");
		assertKlugedJson(RESPONSE_UNFILTERED, kluged);
	}

	@Test
	public void filteredByOnt1() throws Exception {
		populate();
		req.addParameter("ontologyUri", ONT1);
		// No name produces invalid JSON so we kluge it for easy comparison
		// Filtered out classes leave their commas behind, so remove them
		String rawResponse = getJsonFromController(controller, req);
		String kluged = rawResponse.replace("\"\"\"", "\"\",\"")
				.replace(", , ,", ",");
		assertKlugedJson(RESPONSE_FILTERED_BY_ONT1, kluged);
	}

	@Test
	public void filteredByOnt2() throws Exception {
		populate();
		req.addParameter("ontologyUri", ONT2);
		// Filtered out classes leave their commas behind, so remove them
		String rawResponse = getJsonFromController(controller, req);
		String kluged = rawResponse.replaceAll(", $", " ");
		assertKlugedJson(RESPONSE_FILTERED_BY_ONT2, kluged);
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
		odao.insertNewOntology(ontology(ONT2, ONT2_NAME));
		odao.insertNewOntology(ontology(ONT3, null));
		vcgdao.setGroups(vclassGroup(GROUP_NO_NAME, null));
		vcgdao.setGroups(vclassGroup(GROUP_W_NAME, NAME_GROUP));
		vcdao.setVClass(vclass(VC1, null, null, null));
		vcdao.setVClass(vclass(VC2, VC2_NAME, VC2_SHORT_DEF, GROUP_NONE));
		vcdao.setVClass(vclass(VC3, VC3_NAME, VC3_SHORT_DEF, GROUP_NO_NAME));
		vcdao.setVClass(vclass(VC4, VC4_NAME, VC4_SHORT_DEF, GROUP_W_NAME));
	}

	private VClass vclass(String uri, String name, String shortDef,
			String groupURI) {
		VClass vc = new VClass();
		vc.setURI(uri);
		if (name != null) {
			vc.setName(name);
			vc.setPickListName(name);
		}
		if (shortDef != null) {
			vc.setShortDef(shortDef);
		}
		if (groupURI != null) {
			vc.setGroupURI(groupURI);
		}
		return vc;
	}

	private VClassGroup vclassGroup(String uri, String name) {
		VClassGroup vcg = new VClassGroup();
		vcg.setURI(uri);
		vcg.setPublicName(name);
		return vcg;
	}

	private Ontology ontology(String uri, String name) {
		Ontology o = new Ontology();
		o.setURI(uri);
		o.setName(name);
		return o;
	}
}
