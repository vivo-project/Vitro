/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDaoStub;
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
public class ListPropertyGroupsControllerTest extends ListControllerTestBase {
	private static final ArrayNode BASIC_JSON_RESPONSE = arrayOf(
			groupListNode(
					"<a href='./editForm?uri=http%3A%2F%2Fproperty.group%2Fgroup1&amp;controller=PropertyGroup'>(unnamed group)</a>",
					""),
			groupListNode(
					"<a href='./editForm?uri=http%3A%2F%2Fproperty.group%2Fgroup2&amp;controller=PropertyGroup'>Group2</a>",
					"2",
					groupMemberNode(
							"<a href='propertyEdit?uri=http%3A%2F%2Fproperty%2Fprop'>PlainProp</a>",
							""),
					groupMemberNode(
							"<a href='propertyEdit?uri=http%3A%2F%2Fproperty%2FobjectProp'>ObjectProp</a>",
							""),
					groupMemberNode(
							"<a href='datapropEdit?uri=http%3A%2F%2Fproperty%2FdataProp'>DataProp</a>",
							"")));

	private ListPropertyGroupsController controller;
	private HttpServletRequestStub req;
	private ModelAccessFactoryStub modelsFactory;
	private WebappDaoFactoryStub wadf;
	private PropertyGroupDaoStub pgdao;

	@Before
	public void setup() {
		controller = new ListPropertyGroupsController();

		req = new HttpServletRequestStub();

		pgdao = new PropertyGroupDaoStub();
		pgdao.addPropertyGroup(
				propertyGroup("http://property.group/group1", "", -1));
		pgdao.addPropertyGroup(propertyGroup("http://property.group/group2",
				"Group2", 2, property("http://property/prop", "PlainProp"),
				objectProperty("http://property/objectProp", "ObjectProp"),
				dataProperty("http://property/dataProp", "DataProp")));

		wadf = new WebappDaoFactoryStub();
		wadf.setPropertyGroupDao(pgdao);

		modelsFactory = new ModelAccessFactoryStub();
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL);
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void basicJsonTest() throws Exception {
		assertMatchingJson(controller, req, BASIC_JSON_RESPONSE);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private PropertyGroup propertyGroup(String uri, String name,
			int displayRank, Property... properties) {
		PropertyGroup pg = new PropertyGroup();
		pg.setURI(uri);
		pg.setName(name);
		pg.setDisplayRank(displayRank);
		pg.setPropertyList(new ArrayList<>(Arrays.asList(properties)));
		return pg;
	}

	private Property property(String uri, String name) {
		Property p = new Property();
		p.setURI(uri);
		p.setLabel(name);
		return p;
	}

	private ObjectProperty objectProperty(String uri, String name) {
		ObjectProperty op = new ObjectProperty();
		op.setURI(uri);
		op.setDomainPublic(name);
		return op;
	}

	private DataProperty dataProperty(String uri, String name) {
		DataProperty dp = new DataProperty();
		dp.setURI(uri);
		dp.setName(name);
		return dp;
	}

}
