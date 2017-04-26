/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ASSERTIONS_ONLY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.POLICY_NEUTRAL;

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
public class ShowClassHierarchyControllerTest extends ListControllerTestBase {
	private static final ArrayNode JSON_FULL_RESPONSE = arrayOf(
			vclassHierarchyNode(
					"<a href='vclassEdit?uri=http%3A%2F%2Ftest1%2Fvclass1'>TestOne</a>",
					"My favorite class", "TheGroup", "OntoMania"),
			vclassHierarchyNode(
					"<a href='vclassEdit?uri=http%3A%2F%2Ftest2%2FvclassParent'>vclassParent</a>",
					"", "", "http://test2/",
					vclassHierarchyNode(
							"<a href='vclassEdit?uri=http%3A%2F%2Ftest3%2FvclassChild1'>Child One</a>",
							"", "", "http://test3/",
							vclassHierarchyNode(
									"<a href='vclassEdit?uri=http%3A%2F%2Ftest3%2FvclassGrandchild'>vclassGrandchild</a>",
									"", "", "http://test3/")),
					vclassHierarchyNode(
							"<a href='vclassEdit?uri=http%3A%2F%2Ftest2%2FvclassChild2'>Child Two</a>",
							"", "", "http://test2/")));

	private static final ArrayNode JSON_RESTRICTED_TO_TEST2_RESPONSE = arrayOf(
			vclassHierarchyNode(
					"<a href='vclassEdit?uri=http%3A%2F%2Ftest2%2FvclassParent'>vclassParent</a>",
					"", "", "http://test2/",
					vclassHierarchyNode(
							"<a href='vclassEdit?uri=http%3A%2F%2Ftest2%2FvclassChild2'>Child Two</a>",
							"", "", "http://test2/")));

	private static final ArrayNode JSON_EMPTY_RESPONSE = arrayOf();

	private static final String VCLASS_URI_1 = "http://test1/vclass1";
	private static final String VCLASS_URI_PARENT = "http://test2/vclassParent";
	private static final String VCLASS_URI_CHILD_1 = "http://test3/vclassChild1";
	private static final String VCLASS_URI_CHILD_2 = "http://test2/vclassChild2";
	private static final String VCLASS_URI_GRANDCHIILD = "http://test3/vclassGrandchild";

	private static final String VCLASS_GROUP_URI_1 = "http://test1/vclassGroup";

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

		VClassGroup vclassGroup = vclassGroup(VCLASS_GROUP_URI_1, "TheGroup");
		vcgdao.setGroups(vclassGroup);

		ontdao.insertNewOntology(ontology("http://test1/", "OntoMania"));

		vcdao.setVClass(vclass(VCLASS_URI_1, "TestOne", vclassGroup,
				"My favorite class"));

		vcdao.setVClass(vclass(VCLASS_URI_PARENT));
		vcdao.setVClass(vclass(VCLASS_URI_CHILD_1, "Child One"),
				VCLASS_URI_PARENT);
		vcdao.setVClass(vclass(VCLASS_URI_CHILD_2, "Child Two"),
				VCLASS_URI_PARENT);
		vcdao.setVClass(vclass(VCLASS_URI_GRANDCHIILD), VCLASS_URI_CHILD_1);
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void basicJsonTest() throws Exception {
		assertMatchingJson(controller, req, JSON_FULL_RESPONSE);
	}

	@Test
	public void restrictByOntology() throws Exception {
		req.addParameter("ontologyUri", "http://test2/");
		assertMatchingJson(controller, req, JSON_RESTRICTED_TO_TEST2_RESPONSE);
	}

	@Test
	public void restrictToNothing() throws Exception {
		req.addParameter("ontologyUri", "http://BOGUS/");
		assertMatchingJson(controller, req, JSON_EMPTY_RESPONSE);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------
	private VClass vclass(String uri) {
		VClass vc = new VClass();
		vc.setURI(uri);
		return vc;
	}

	private VClass vclass(String uri, String name) {
		VClass vc = vclass(uri);
		vc.setPickListName(name);
		return vc;
	}

	private VClass vclass(String uri, String name, VClassGroup vcg,
			String shortDef) {
		VClass vc = vclass(uri);
		vc.setPickListName(name);
		vc.setGroupURI(vcg.getURI());
		vcg.add(vc);
		vc.setShortDef(shortDef);
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
