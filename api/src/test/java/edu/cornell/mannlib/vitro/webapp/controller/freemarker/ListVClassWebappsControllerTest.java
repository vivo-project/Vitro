/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import java.text.Collator;

import org.junit.Before;
import org.junit.Test;

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
 * Not a well-formed set of unit tests. Rather, this simply captures one output
 * from the Controller, so we can be sure that it's the same when we change JSON
 * engines.
 * 
 * On the other hand, the data is chosen in such a way as to exercise many of
 * the branches in the JSON generation code, so there's that.
 */
public class ListVClassWebappsControllerTest extends ListControllerTestBase {
	private static final ArrayNode BASIC_JSON_RESPONSE = arrayOf(
			vclassListNode(
					"<a href='./vclassEdit?uri=http%3A%2F%2Fontology2%2Fvclass2'>ClassTwo</a>",
					"Short definition.", "Group", "OntOntOnt"),
			vclassListNode("", "", "", "http://ontology1/"));

	private static final String ONTOLOGY_URI_2 = "http://ontology2/";

	private static final String VCLASS_URI_1 = "http://ontology1/vclass1";
	private static final String VCLASS_URI_2 = ONTOLOGY_URI_2 + "vclass2";

	private static final String CLASS_GROUP_URI = "http://class.group/group1";

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
	public void basicJsonTest() throws Exception {
		odao.insertNewOntology(ontology(ONTOLOGY_URI_2, "OntOntOnt"));

		vcdao.setVClass(vclass(VCLASS_URI_1, null, null, null));
		vcdao.setVClass(vclass(VCLASS_URI_2, "ClassTwo", "Short definition.",
				CLASS_GROUP_URI));

		vcgdao.setGroups(vclassGroup(CLASS_GROUP_URI, "Group"));

		// The MISSING NAME part of the response is not valid JSON unless we
		// kluge it.
		String rawResponse = getJsonFromController(controller, req);
		String kluged = rawResponse.replace("\"\"\"", "\"\",\"");
		assertKlugedJson(BASIC_JSON_RESPONSE, kluged);

		// assertMatchingJson(controller, req, BASIC_JSON_RESPONSE);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

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
