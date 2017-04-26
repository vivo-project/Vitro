/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;

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
 * Not a well-formed set of unit tests. Rather, this simply captures one output
 * from the Controller, so we can be sure that it's the same when we change JSON
 * engines.
 * 
 * On the other hand, the data is chosen in such a way as to exercise many of
 * the branches in the JSON generation code, so there's that.
 */
public class ListPropertyWebappsControllerTest extends ListControllerTestBase {
	private static final ArrayNode NO_PROPERTIES_RESPONSE = arrayOf(mapper
			.createObjectNode().put("name", "No object properties found"));

	private static final ArrayNode BASIC_JSON_RESPONSE = arrayOf(
			propertyListNode(
					"<a href='./propertyEdit?uri=http%3A%2F%2Fobject.property%2Fop1'>ObjectProp1</a>",
					"op1", "", "", "unspecified"),
			propertyListNode(
					"<a href='./propertyEdit?uri=http%3A%2F%2Fobject.property%2Fop2'>ObjectProp2</a>",
					"op2", "DomainClass", "RangeClass", "PropGroup"));

	private static final String PROPERTY_GROUP_URI = "http://group/group1";
	private static final String DOMAIN_CLASS_URI = "http://domain/d1";
	private static final String RANGE_CLASS_URI = "http://range/r1";

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
	public void nothingFound() throws Exception {
		assertMatchingJson(controller, req, NO_PROPERTIES_RESPONSE);
	}

	@Test
	public void basicJsonTest() throws Exception {
		opdao.addObjectProperty(
				objectProperty("http://object.property/op1", "ObjectProp1"));
		opdao.addObjectProperty(
				objectProperty("http://object.property/op2", "ObjectProp2",
						RANGE_CLASS_URI, DOMAIN_CLASS_URI, PROPERTY_GROUP_URI));

		vcdao.setVClass(vclass(DOMAIN_CLASS_URI, "DomainClass"));
		vcdao.setVClass(vclass(RANGE_CLASS_URI, "RangeClass"));

		pgdao.addPropertyGroup(
				propertyGroup(PROPERTY_GROUP_URI, "PropGroup", 1));

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

	private ObjectProperty objectProperty(String uri, String name) {
		ObjectProperty op = new ObjectProperty();
		op.setURI(uri);
		op.setPickListName(name);
		return op;
	}

	private ObjectProperty objectProperty(String uri, String name,
			String rangeVClass, String domainVClass, String propertyGroup) {
		ObjectProperty op = objectProperty(uri, name);
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
