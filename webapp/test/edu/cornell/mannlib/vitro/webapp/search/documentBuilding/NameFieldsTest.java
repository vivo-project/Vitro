/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.documentBuilding;

import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_RAW;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import stubs.edu.cornell.mannlib.vitro.webapp.beans.IndividualStub;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
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
		RDFServiceFactory rdfServiceFactory = new RDFServiceFactorySingle(
				rdfService);
		nameFields = new NameFields(rdfServiceFactory);
	}

	@Test
	public void nullIndividual() throws SkipIndividualException {
		SearchInputDocument expected = new BaseSearchInputDocument(doc);

		assertResultingSearchDocument(null, expected);
	}

	@Test
	public void nullUri() throws SkipIndividualException {
		SearchInputDocument expected = new BaseSearchInputDocument(doc);

		assertResultingSearchDocument(new IndividualStub(null), expected);
	}

	@Test
	public void foundNoLabels() throws SkipIndividualException {
		SearchInputDocument expected = new BaseSearchInputDocument(doc);
		expected.addField(NAME_RAW, "");

		assertResultingSearchDocument(new IndividualStub(INDIVIDUAL_URI),
				expected);
	}

	@Test
	public void foundOneLabel() throws SkipIndividualException {
		baseModel.add(stmt(INDIVIDUAL_URI, LABEL_PROPERTY_URI, "label1"));

		SearchInputDocument expected = new BaseSearchInputDocument(doc);
		expected.addField(NAME_RAW, "label1 ");

		assertResultingSearchDocument(new IndividualStub(INDIVIDUAL_URI),
				expected);
	}

	@Test
	public void foundTwoLabels() throws SkipIndividualException {
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
			SearchInputDocument expected) throws SkipIndividualException {
		nameFields.modifyDocument(ind, doc);
		assertEquals(expected, doc);
	}

	/**
	 * TODO Test plan
	 * 
	 * <pre>
	 * // also run SPARQL query to get rdfs:label values
	 * String query = &quot;SELECT ?label WHERE {  &quot; + &quot;&lt;&quot; + ind.getURI() + &quot;&gt; &quot;
	 * 		+ &quot;&lt;http://www.w3.org/2000/01/rdf-schema#label&gt; ?label  }&quot;;
	 * 
	 * try {
	 * 	RDFService rdfService = rsf.getRDFService();
	 * 	BufferedReader stream = new BufferedReader(new InputStreamReader(
	 * 			rdfService.sparqlSelectQuery(query, ResultFormat.CSV)));
	 * 
	 * 	StringBuffer buffer = new StringBuffer();
	 * 	String line;
	 * 
	 * 	// throw out first line since it is just a header
	 * 	stream.readLine();
	 * 
	 * 	while ((line = stream.readLine()) != null) {
	 * 		buffer.append(line).append(' ');
	 * 	}
	 * 
	 * 	log.debug(&quot;Adding labels for &quot; + ind.getURI() + &quot; \&quot;&quot; + buffer.toString()
	 * 			+ &quot;\&quot;&quot;);
	 * 	doc.addField(term.NAME_RAW, buffer.toString());
	 * 
	 * } catch (RDFServiceException e) {
	 * 	log.error(&quot;could not get the rdfs:label for &quot; + ind.getURI(), e);
	 * } catch (IOException e) {
	 * 	log.error(&quot;could not get the rdfs:label for &quot; + ind.getURI(), e);
	 * }
	 * 
	 * </pre>
	 */

}
