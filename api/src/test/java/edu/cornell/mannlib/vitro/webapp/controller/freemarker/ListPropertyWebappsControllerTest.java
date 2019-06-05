/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.OntologyDaoStub;
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
 * Name from getUri
 * Name no URL? -- NONSENSICAL
 *
 * InternalName from localNameWithPrefix
 * InternalName from localName
 * InternalName from URI
 *
 * Domain class no class URI
 * Domain class no class for URI
 * Domain class use picklistName
 *
 * Range class no class URI
 * Range class no class for URI
 * Range class use picklistName
 *
 * Group no group URI
 * Group no group for URI
 * Group no name
 * Group has a name
 *
 * OP1 Ont1, no picklistName, no localNameWithPrefix, no domainClass, no rangeClass, no GroupURI
 * OP2 Ont2, picklistName, no localNameWithPrefix, no domain class for URI, no range class for URI, no group for GroupURI
 * OP3 Ont1, picklistName, localNameWithPrefix, domainclass no picklistname, range class no picklistname, group has no name
 * OP4 Ont1, picklistName, localNameWithPrefix, domainclass w/picklistname, range class w/picklistname, group with name
 *
 * Try once with no data
 * Try with all data and no ontology specified
 * Try with all data and Ont1, Ont2, Ont3
 * </pre>
 */
public class ListPropertyWebappsControllerTest extends ListControllerTestBase {
	private static final String PATH = "./propertyEdit";
	private static final String ONT1 = "http://ont1/";
	private static final String ONT2 = "http://ont2/";
	private static final String ONT3 = "http://ont3/";
	private static final String OP1 = ONT1 + "first";
	private static final String OP2 = ONT2 + "second";
	private static final String OP3 = ONT1 + "third";
	private static final String OP4 = ONT1 + "fourth";
	private static final String OP2_PICK_NAME = "The second one";
	private static final String OP3_PICK_NAME = "The third one";
	private static final String OP4_PICK_NAME = "The fourth one";
	private static final String OP3_LOCALNAME_W_PREFIX = "ontology1:third";
	private static final String OP4_LOCALNAME_W_PREFIX = "ontology1:fourth";
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

	private static final JsonNode NO_DATA_RESPONSE = arrayOf(mapper
			.createObjectNode().put("name", "No object properties found"));

	private static final JsonNode RESPONSE_UNFILTERED = arrayOf(
			propertyListNode(PATH, OP1, "first", "first", "", "",
					"unspecified"),
			propertyListNode(PATH, OP4, OP4_PICK_NAME, OP4_LOCALNAME_W_PREFIX,
					NAME_DOMAIN, NAME_RANGE, NAME_GROUP),
			propertyListNode(PATH, OP2, OP2_PICK_NAME, "second", "", "",
					"unknown group"),
			propertyListNode(PATH, OP3, OP3_PICK_NAME, OP3_LOCALNAME_W_PREFIX,
					"domainWithNoName", "rangeWithNoName", ""));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT1 = arrayOf(
			propertyListNode(PATH, OP1, "first", "first", "", "",
					"unspecified"),
			propertyListNode(PATH, OP4, OP4_PICK_NAME, OP4_LOCALNAME_W_PREFIX,
					NAME_DOMAIN, NAME_RANGE, NAME_GROUP),
			propertyListNode(PATH, OP3, OP3_PICK_NAME, OP3_LOCALNAME_W_PREFIX,
					"domainWithNoName", "rangeWithNoName", ""));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT2 = arrayOf(
			propertyListNode(PATH, OP2, OP2_PICK_NAME, "second", "", "",
					"unknown group"));

	private static final JsonNode RESPONSE_FILTERED_BY_ONT3 = arrayOf(mapper
			.createObjectNode().put("name", "No object properties found"));

	private ListPropertyWebappsController controller;
	private HttpServletRequestStub req;
	private ModelAccessFactoryStub modelsFactory;
	private WebappDaoFactoryStub wadf;
	private ObjectPropertyDaoStub opdao;
	private OntologyDaoStub odao;
	private PropertyGroupDaoStub pgdao;
	private VClassDaoStub vcdao;

	@Before
	public void setup() {
		controller = new ListPropertyWebappsController();

		req = new HttpServletRequestStub();
		new VitroRequest(req).setCollator(Collator.getInstance());

		opdao = new ObjectPropertyDaoStub();
		odao = new OntologyDaoStub();
		pgdao = new PropertyGroupDaoStub();
		vcdao = new VClassDaoStub();

		wadf = new WebappDaoFactoryStub();
		wadf.setObjectPropertyDao(opdao);
		wadf.setOntologyDao(odao);
		wadf.setPropertyGroupDao(pgdao);
		wadf.setVClassDao(vcdao);

		modelsFactory = new ModelAccessFactoryStub();
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL);
		modelsFactory.get(req).setWebappDaoFactory(wadf, POLICY_NEUTRAL,
				LANGUAGE_NEUTRAL);
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
		vcdao.setVClass(vclass(RANGE_NO_NAME, null));
		vcdao.setVClass(vclass(RANGE_W_NAME, NAME_RANGE));
		pgdao.addPropertyGroup(propertyGroup(GROUP_NO_NAME, null, 5));
		pgdao.addPropertyGroup(propertyGroup(GROUP_W_NAME, NAME_GROUP, 3));
		opdao.addObjectProperty(
				objectProperty(OP1, null, null, null, null, null));
		opdao.addObjectProperty(objectProperty(OP2, OP2_PICK_NAME, null,
				DOMAIN_NONE, RANGE_NONE, GROUP_NONE));
		opdao.addObjectProperty(
				objectProperty(OP3, OP3_PICK_NAME, OP3_LOCALNAME_W_PREFIX,
						DOMAIN_NO_NAME, RANGE_NO_NAME, GROUP_NO_NAME));
		opdao.addObjectProperty(
				objectProperty(OP4, OP4_PICK_NAME, OP4_LOCALNAME_W_PREFIX,
						DOMAIN_W_NAME, RANGE_W_NAME, GROUP_W_NAME));
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

	private ObjectProperty objectProperty(String uri, String name,
			String localNameWithPrefix, String domainVClass, String rangeVClass,
			String propertyGroup) {
		ObjectProperty op = new ObjectProperty();
		op.setURI(uri);
		op.setPickListName(name);
		op.setLocalNameWithPrefix(localNameWithPrefix);
		op.setRangeVClassURI(rangeVClass);
		op.setDomainVClassURI(domainVClass);
		op.setGroupURI(propertyGroup);
		return op;
	}

	private VClass vclass(String uri, String name) {
		VClass vc = new VClass();
		vc.setURI(uri);
		vc.setPickListName(name);
		return vc;
	}

}
