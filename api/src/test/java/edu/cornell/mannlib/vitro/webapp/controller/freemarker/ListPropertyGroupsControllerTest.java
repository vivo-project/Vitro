/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDaoStub;
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
 * name
 * no name - (unnamed group)
 *
 * display rank
 * no display rank
 *
 * no child properties
 * child property is data property
 * child properti is object property
 *
 * child data property has no name
 * child data property has name
 * child object property has no domainPublic
 * child object property has domainPublic
 *
 * G1 no name, displayRank, no classes
 * G2 name, no displayRank, classes
 * G2DP1 no name, no shortDef
 * G2DP2 name, shortdef
 * G2OP1 no domainPublic, no shortDef
 * G2OP2 domainPublic, shortdef
 *
 * Try once with no data
 * Try with all data
 * </pre>
 */

public class ListPropertyGroupsControllerTest extends ListControllerTestBase {
	private static final String LINK_FORMAT_GROUP = "<a href='./editForm?uri=%s&amp;controller=PropertyGroup'>%s</a>";
	private static final String LINK_FORMAT_DATA_PROPERTY = "<a href='datapropEdit?uri=%s'>%s</a>";
	private static final String LINK_FORMAT_OBJECT_PROPERTY = "<a href='propertyEdit?uri=%s'>%s</a>";
	private static final String GROUP1 = "http://ont1/group1";
	private static final String GROUP2 = "http://ont1/group2";
	private static final String DP1 = "http://ont1/dp1";
	private static final String DP2 = "http://ont1/dp2";
	private static final String OP1 = "http://ont1/op1";
	private static final String OP2 = "http://ont1/op2";
	private static final String DP2_NAME = "A second data property";
	private static final String OP2_DOMAIN_PUBLIC = "The second domain";
	private static final int GROUP1_RANK = 5;
	private static final String GROUP2_NAME = "The Second Group";

	private static final JsonNode JSON_EMPTY_RESPONSE = arrayOf();

	private static final JsonNode JSON_FULL_RESPONSE = arrayOf(
			groupListNode(LINK_FORMAT_GROUP, GROUP2, GROUP2_NAME, "",
					groupMemberNode(LINK_FORMAT_DATA_PROPERTY, DP1, null, ""),
					groupMemberNode(LINK_FORMAT_DATA_PROPERTY, DP2, DP2_NAME,
							""),
					groupMemberNode(LINK_FORMAT_OBJECT_PROPERTY, OP1, null, ""),
					groupMemberNode(LINK_FORMAT_OBJECT_PROPERTY, OP2,
							OP2_DOMAIN_PUBLIC, "")),
			groupListNode(LINK_FORMAT_GROUP, GROUP1, "(unnamed group)",
					"" + GROUP1_RANK));

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

		wadf = new WebappDaoFactoryStub();
		wadf.setPropertyGroupDao(pgdao);

		modelsFactory = new ModelAccessFactoryStub();
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL);
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void noData() throws Exception {
		assertMatchingJson(controller, req, JSON_EMPTY_RESPONSE);
	}

	@Test
	public void basicJsonTest() throws Exception {
		populate();

		// /*
		// * The controller attempts to handle the case of a class with no name,
		// * but instead it returns invalid json.
		// */
		// String rawResponse = getJsonFromController(controller, req);
		// String kluged = rawResponse.replace("[\"\"", "[ {\"name\": \"\"");
		// assertKlugedJson(JSON_FULL_RESPONSE, kluged);

		assertMatchingJson(controller, req, JSON_FULL_RESPONSE);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void populate() {
		pgdao.addPropertyGroup(propertyGroup(GROUP1, "", GROUP1_RANK));
		pgdao.addPropertyGroup(
				propertyGroup(GROUP2, GROUP2_NAME, -1, dataProperty(DP1, null),
						dataProperty(DP2, DP2_NAME), objectProperty(OP1, null),
						objectProperty(OP2, OP2_DOMAIN_PUBLIC)));
	}

	private PropertyGroup propertyGroup(String uri, String name,
			int displayRank, Property... properties) {
		PropertyGroup pg = new PropertyGroup();
		pg.setURI(uri);
		pg.setName(name);
		pg.setDisplayRank(displayRank);
		pg.setPropertyList(new ArrayList<>(Arrays.asList(properties)));
		return pg;
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
