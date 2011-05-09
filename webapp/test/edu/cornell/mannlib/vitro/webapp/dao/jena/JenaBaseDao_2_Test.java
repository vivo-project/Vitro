/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * Another set of tests for JenaBaseDao.
 */
public class JenaBaseDao_2_Test extends AbstractTestClass {
	private static final String NS_MINE = "http://my.namespace.edu/";

	private static final String EMPTY_RESOURCE_URI = NS_MINE + "emptyResource";
	private static final String FULL_RESOURCE_URI = NS_MINE + "fullResource";

	private static final String OLD_URI_1 = NS_MINE + "oldUri1";
	private static final String OLD_URI_2 = NS_MINE + "oldUri2";
	private static final String NEW_URI_1 = NS_MINE + "newUri1";
	private static final String NEW_URI_2 = NS_MINE + "newUri2";
	private static final String BOGUS_URI = "bogusUri";

	private OntModel ontModel;

	private Property prop1;

	private Resource emptyResource;
	private Resource fullResource;

	private JenaBaseDao dao;

	@Before
	public void initializeThings() {
		ontModel = ModelFactory.createOntologyModel();

		prop1 = ontModel.createProperty("property1");

		emptyResource = ontModel.createResource(EMPTY_RESOURCE_URI);

		fullResource = ontModel.createResource(FULL_RESOURCE_URI);
		ontModel.createStatement(fullResource, prop1,
				ontModel.createResource(OLD_URI_1));
		ontModel.createStatement(fullResource, prop1,
				ontModel.createResource(OLD_URI_2));

		WebappDaoFactoryJena wdfj = new WebappDaoFactoryJena(ontModel);
		dao = new JenaBaseDao(wdfj);
	}

	// ----------------------------------------------------------------------
	// tests of updatePropertyResourceURIValues()
	// ----------------------------------------------------------------------

	@Test
	public void updatePropertyResourceURIValuesFromNothing() {
		updateAndConfirm(emptyResource, prop1,
				buildSet(NEW_URI_1, NEW_URI_2));
	}

	@Test
	public void updatePropertyResourceURIValuesToNothing() {
		updateAndConfirm(fullResource, prop1, Collections.<String>emptySet());
	}

	@Test
	public void updatePropertyResourceURIValuesNoChange() {
		updateAndConfirm(fullResource, prop1,
				buildSet(OLD_URI_1, OLD_URI_2));
	}

	@Test
	public void updatePropertyResourceURIValuesReplaceSome() {
		updateAndConfirm(fullResource, prop1,
				buildSet(OLD_URI_1, NEW_URI_2));
	}

	@Test
	public void updatePropertyResourceURIValuesReplaceAll() {
		updateAndConfirm(fullResource, prop1, buildSet(NEW_URI_1));
	}

	@Test
	public void updatePropertyResourceURIValuesTryToAddEmptyURI() {
		Set<String> uris = buildSet("");
		dao.updatePropertyResourceURIValues(emptyResource, prop1, uris,
				ontModel);
		assertExpectedUriValues("update URIs", emptyResource, prop1,
				Collections.<String> emptySet());
	}

	@Test
	public void updatePropertyResourceURIValuesTryToAddInvalidURI() {
		setLoggerLevel(JenaBaseDao.class, Level.ERROR);
		Set<String> uris = buildSet(BOGUS_URI);
		dao.updatePropertyResourceURIValues(emptyResource, prop1, uris,
				ontModel);
		assertExpectedUriValues("update URIs", emptyResource, prop1,
				Collections.<String> emptySet());
	}

	// ----------------------------------------------------------------------
	// helper methods
	// ----------------------------------------------------------------------

	private void updateAndConfirm(Resource res, Property prop, Set<String> uris) {
		dao.updatePropertyResourceURIValues(res, prop, uris, ontModel);
		assertExpectedUriValues("update URIs", res, prop, uris);
	}

	private void assertExpectedUriValues(String message, Resource res,
			Property prop, Set<String> expectedUris) {
		Set<String> actualUris = new HashSet<String>();
		StmtIterator stmts = ontModel.listStatements(res, prop, (RDFNode) null);
		while (stmts.hasNext()) {
			Statement stmt = stmts.next();
			actualUris.add(stmt.getObject().asResource().getURI());
		}

		assertEquals(message, expectedUris, actualUris);
	}
}
