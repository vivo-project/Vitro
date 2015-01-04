/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_RAW;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.beans.IndividualStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccessStub;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;

/**
 * TODO NameFields should add the values as separate objects.
 */
public class NameFieldsTest {
	private static final String INDIVIDUAL_URI = "http://mydomain.edu/individual/n3012";
	private static final String LABEL_PROPERTY_URI = "http://www.w3.org/2000/01/rdf-schema#label";
	private Model baseModel;
	private NameFields nameFields;
	private BaseSearchInputDocument doc;

	@Before
	public void setup() {
		baseModel = ModelFactory.createDefaultModel();

		doc = new BaseSearchInputDocument();

		RDFServiceModel rdfService = new RDFServiceModel(baseModel);
		ContextModelAccessStub models = new ContextModelAccessStub();
		models.setRDFService(CONTENT, rdfService);

		nameFields = new NameFields();
		nameFields.setContextModels(models);
	}

	@Test
	public void nullIndividual() {
		SearchInputDocument expected = new BaseSearchInputDocument(doc);

		assertResultingSearchDocument(null, expected);
	}

	@Test
	public void nullUri() {
		SearchInputDocument expected = new BaseSearchInputDocument(doc);

		assertResultingSearchDocument(new IndividualStub(null), expected);
	}

	@Test
	public void foundNoLabels() {
		SearchInputDocument expected = new BaseSearchInputDocument(doc);
		expected.addField(NAME_RAW, "");

		assertResultingSearchDocument(new IndividualStub(INDIVIDUAL_URI),
				expected);
	}

	@Test
	public void foundOneLabel() {
		baseModel.add(stmt(INDIVIDUAL_URI, LABEL_PROPERTY_URI, "label1"));

		SearchInputDocument expected = new BaseSearchInputDocument(doc);
		expected.addField(NAME_RAW, "label1 ");

		assertResultingSearchDocument(new IndividualStub(INDIVIDUAL_URI),
				expected);
	}

	@Test
	public void foundTwoLabels() {
		baseModel.add(stmt(INDIVIDUAL_URI, LABEL_PROPERTY_URI, "label1"));
		baseModel.add(stmt(INDIVIDUAL_URI, LABEL_PROPERTY_URI, "label2"));

		SearchInputDocument expected = new BaseSearchInputDocument(doc);
		expected.addField(NAME_RAW, "label2 label1 ");

		assertResultingSearchDocument(new IndividualStub(INDIVIDUAL_URI),
				expected);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private Statement stmt(String subjectUri, String propertyUri, String literal) {
		return baseModel.createStatement(baseModel.createResource(subjectUri),
				baseModel.createProperty(propertyUri),
				baseModel.createLiteral(literal));
	}

	private void assertResultingSearchDocument(Individual ind,
			SearchInputDocument expected) {
		nameFields.modifyDocument(ind, doc);
		assertEquals(expected, doc);
	}
}
