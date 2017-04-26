/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.javax.servlet.http.HttpServletRequestStub;

/**
 * A simple test of the JSON produced by the ListClassController. One annoying
 * aspect is that it is not valid JSON.
 * 
 * Instead, the output is valid JSON objects separated by commas, so we wrap the
 * string in brackets [] and it becomes a valid JSON array.
 * 
 * This is not a well-formed set of unit tests. Rather, this simply captures one
 * output from the Controller, so we can be sure that it's the same when we
 * change JSON engines.
 * 
 * On the other hand, the data is chosen in such a way as to exercise many of
 * the branches in the JSON generation code, so there's that.
 */
public class ListClassGroupsControllerTest extends ListControllerTestBase {
	private static ArrayNode BASIC_JSON_RESPONSE = arrayOf(
			groupListNode(
					"<a href='./editForm?uri=http%3A%2F%2Fthe.class.groups%2Fgroup1&amp;controller=Classgroup'>Group1</a>",
					"99",
					groupMemberNode(
							"<a href='vclassEdit?uri=http%3A%2F%2Fthe.classes%2Fclass1a'>Class1A</a>",
							"")),
			groupListNode(
					"<a href='./editForm?uri=http%3A%2F%2Fthe.class.groups%2Fgroup2&amp;controller=Classgroup'>(unnamed group)</a>",
					"",
					groupMemberNode(
							"<a href='vclassEdit?uri=http%3A%2F%2Fthe.classes%2Fclass2a'>Class2A</a>",
							""),
					groupMemberNode(
							"<a href='vclassEdit?uri=http%3A%2F%2Fthe.classes%2Fclass2b'>Class2B</a>",
							"")),
			groupListNode(
					"<a href='./editForm?uri=http%3A%2F%2Fthe.class.groups%2Fgroup3&amp;controller=Classgroup'>Group3</a>",
					"15"));

	private ListClassGroupsController controller;
	private HttpServletRequestStub req;
	private ModelAccessFactoryStub modelsFactory;
	private WebappDaoFactoryStub wadf;
	private VClassGroupDaoStub vcgDao;

	@Before
	public void setup() {
		controller = new ListClassGroupsController();

		req = new HttpServletRequestStub();

		vcgDao = new VClassGroupDaoStub();

		wadf = new WebappDaoFactoryStub();
		wadf.setVClassGroupDao(vcgDao);

		modelsFactory = new ModelAccessFactoryStub();
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL);
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void basicJsonTest() throws Exception {
		/**
		 * Test the conditionals: rank is -1, group name is empty, group has no
		 * children.
		 */
		vcgDao.setGroups(
				vClassGroup("http://the.class.groups/group1", "Group1", 99,
						new VClass("http://the.classes/", "class1a",
								"Class1A")),
				vClassGroup("http://the.class.groups/group2", "", -1,
						new VClass("http://the.classes/", "class2a", "Class2A"),
						new VClass("http://the.classes/", "class2b",
								"Class2B")),
				vClassGroup("http://the.class.groups/group3", "Group3", 15));

		assertMatchingJson(controller, req, BASIC_JSON_RESPONSE);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private VClassGroup vClassGroup(String uri, String name, int rank,
			VClass... vClasses) {
		VClassGroup group = new VClassGroup(uri, name, rank);
		for (VClass vClass : vClasses) {
			group.add(vClass);
		}
		return group;
	}

}
