package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.ParameterUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.SparqlSelectQuery;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonView;

public class SparqlSelectQueryTest {
	private static final String MODEL = "model";
	private static final String STR_VAR = "str";
	private static final String S_VAR = "s";
	private static final String O_VAR = "o";
	private static final String ALL_VAR = "all";
	private static final String LIMIT_VAR = "limit";
    private static final String ORDER_VAR = "order";
	private static final String QUERY_NO_VARS = "SELECT ?str WHERE { BIND(\"test\" as ?str) } ";
	private static final String QUERY_OBJ_VAR = "SELECT ?s WHERE { ?s <test:property> ?str . } ";
	private static final String QUERY_SUBJ_VAR = "SELECT ?o WHERE { ?str <test:property> ?o . } ";
    private static final String QUERY_LIMIT_ORDER = "SELECT ?limit_o WHERE { ?str <test:property> ?limit_o . } ORDER BY ?order(?limit_o) LIMIT ?limit "; 
	private static final String QUERY_LABEL = "SELECT ?o WHERE { ?str <http://www.w3.org/2000/01/rdf-schema#label> ?o . } ";

	private OntModelImpl model;
	private DataStore dataStore;
	private SparqlSelectQuery sparql;

	@Before
	public void init() throws Exception {
		sparql = new SparqlSelectQuery();
		dataStore = new DataStore();
		model = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
	}

	@Test
	public void emptyConfiguration() throws Exception {
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		sparql.setQueryText(QUERY_NO_VARS);
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		sparql.setRdfService(new RDFServiceModel(model));
		Parameter strParam = ParameterUtils.createStringLiteralParameter(STR_VAR);
		sparql.addOutputParameter(strParam);

		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(STR_VAR));
		if (dataStore.contains(STR_VAR)) {
			assertTrue(SimpleDataView.getStringRepresentation(STR_VAR, dataStore).equals("test"));
		}
	}

	@Test
	public void stringliteralInput() throws Exception {
		sparql.setQueryText(QUERY_OBJ_VAR);
		Parameter modelParam = ParameterUtils.createModelParameter(MODEL);
		sparql.setQueryModel(modelParam);
		Data modelData = new Data(modelParam);
		ParameterUtils.addStatement(model, "test:resource", "test:property", "alice");
		TestView.setObject(modelData, model);
		dataStore.addData(MODEL, modelData);
		Parameter strParam = ParameterUtils.createStringLiteralParameter(STR_VAR);
		sparql.addInputParameter(strParam);
		// Not enough params
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		Data strData = new Data(strParam);
		TestView.setObject(strData, "alice");
		dataStore.addData(STR_VAR, strData);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertFalse(dataStore.contains(S_VAR));
		Parameter sParam = ParameterUtils.createUriParameter(S_VAR);
		sparql.addOutputParameter(sParam);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(STR_VAR));
		if (dataStore.contains(STR_VAR)) {
			assertEquals("test:resource", SimpleDataView.getStringRepresentation(S_VAR, dataStore));
		}
	}

	@Test
	public void uriInput() throws Exception {
		sparql.setQueryText(QUERY_SUBJ_VAR);
		Parameter modelParam = ParameterUtils.createModelParameter(MODEL);
		sparql.setQueryModel(modelParam);
		Data modelData = new Data(modelParam);
		ParameterUtils.addStatement(model, "test:resource", "test:property", "alice");
		TestView.setObject(modelData, model);
		dataStore.addData(MODEL, modelData);
		Parameter strParam = ParameterUtils.createUriParameter(STR_VAR);
		sparql.addInputParameter(strParam);
		// Not enough params
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		Data strData = new Data(strParam);
		TestView.setObject(strData, "test:resource");
		dataStore.addData(STR_VAR, strData);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertFalse(dataStore.contains(O_VAR));
		Parameter sParam = ParameterUtils.createStringLiteralParameter(O_VAR);
		sparql.addOutputParameter(sParam);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(O_VAR));
		if (dataStore.contains(O_VAR)) {
			assertEquals("alice", SimpleDataView.getStringRepresentation(O_VAR, dataStore));
		}
	}

	@Test
	public void languageFiltering() throws Exception {
		sparql.setQueryText(QUERY_LABEL);
		Parameter modelParam = ParameterUtils.createModelParameter(MODEL);
		sparql.setQueryModel(modelParam);
		sparql.setLanguageFiltering(true);
		Data modelData = new Data(modelParam);
		ParameterUtils.addStatement(model, "test:resource", "http://www.w3.org/2000/01/rdf-schema#label", "Alicia@es");
		ParameterUtils.addStatement(model, "test:resource", "http://www.w3.org/2000/01/rdf-schema#label", "Alice@en-US");
		ParameterUtils.addStatement(model, "test:resource", "http://www.w3.org/2000/01/rdf-schema#label", "Алиса@ru-RU");
		TestView.setObject(modelData, model);
		dataStore.addData(MODEL, modelData);
		Parameter strParam = ParameterUtils.createUriParameter(STR_VAR);
		sparql.addInputParameter(strParam);
		// Not enough params
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		Data strData = new Data(strParam);
		TestView.setObject(strData, "test:resource");
		dataStore.addData(STR_VAR, strData);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertFalse(dataStore.contains(O_VAR));
		Parameter sParam = ParameterUtils.createStringLiteralParameter(O_VAR);
		sparql.addOutputParameter(sParam);
		dataStore.setAcceptLangs(Arrays.asList("ru-RU"));
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(O_VAR));
		if (dataStore.contains(O_VAR)) {
			assertEquals("Алиса@ru-RU", SimpleDataView.getStringRepresentation(O_VAR, dataStore));
		}
		dataStore.setAcceptLangs(Arrays.asList("es"));
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(O_VAR));
		if (dataStore.contains(O_VAR)) {
			assertEquals("Alicia@es", SimpleDataView.getStringRepresentation(O_VAR, dataStore));
		}
		dataStore.setAcceptLangs(Arrays.asList("Alice@en-US"));
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(O_VAR));
		if (dataStore.contains(O_VAR)) {
			assertEquals("Alice@en-US", SimpleDataView.getStringRepresentation(O_VAR, dataStore));
		}
	}

	@Test
	public void jsonOutput() throws Exception {
		sparql.setQueryText(QUERY_SUBJ_VAR);
		Parameter modelParam = ParameterUtils.createModelParameter(MODEL);
		sparql.setQueryModel(modelParam);
		Data modelData = new Data(modelParam);
		ParameterUtils.addStatement(model, "test:resource", "test:property", "alice");
		TestView.setObject(modelData, model);
		dataStore.addData(MODEL, modelData);
		Parameter strParam = ParameterUtils.createUriParameter(STR_VAR);
		sparql.addInputParameter(strParam);
		// Not enough params
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		Data strData = new Data(strParam);
		TestView.setObject(strData, "test:resource");
		dataStore.addData(STR_VAR, strData);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertFalse(dataStore.contains(ALL_VAR));
		Parameter outParam = ParameterUtils.createJsonParameter(ALL_VAR);
		sparql.addOutputParameter(outParam);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(ALL_VAR));
		if (dataStore.contains(ALL_VAR)) {
			assertEquals(
					"{\"head\":{\"vars\":[\"o\"]},\"results\":{\"bindings\":[{\"o\":{\"type\":\"literal\",\"value\":\"alice\"}}]}}",
					JsonView.getJsonString(dataStore, outParam));
		}
	}
	
   @Test
    public void orderWithLimit() throws Exception {
        sparql.setQueryText(QUERY_LIMIT_ORDER);
        Parameter modelParam = ParameterUtils.createModelParameter(MODEL);
        sparql.setQueryModel(modelParam);
        Data modelData = new Data(modelParam);
        ParameterUtils.addStatement(model, "test:resource", "test:property", "alice");
        ParameterUtils.addStatement(model, "test:resource", "test:property", "bob");
        TestView.setObject(modelData, model);
        dataStore.addData(MODEL, modelData);

        Parameter limitParam = ParameterUtils.createStringLiteralParameter(LIMIT_VAR);
        Data limitData = new Data(limitParam);
        TestView.setObject(limitData, "1");
        dataStore.addData(LIMIT_VAR, limitData);
        sparql.addInputSubstitutionParameter(limitParam);
        
        Parameter orderParam = ParameterUtils.createStringLiteralParameter(ORDER_VAR);
        Data orderData = new Data(orderParam);
        TestView.setObject(orderData, "ASC");
        dataStore.addData(ORDER_VAR, orderData);
        sparql.addInputSubstitutionParameter(orderParam);

        Parameter strParam = ParameterUtils.createUriParameter(STR_VAR);
        sparql.addInputParameter(strParam);
        // Not enough params
        assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
        Data strData = new Data(strParam);
        TestView.setObject(strData, "test:resource");
        dataStore.addData(STR_VAR, strData);
        assertEquals(OperationResult.ok(), sparql.run(dataStore));
        assertFalse(dataStore.contains(ALL_VAR));
        Parameter outParam = ParameterUtils.createJsonParameter(ALL_VAR);
        sparql.addOutputParameter(outParam);
        assertEquals(OperationResult.ok(), sparql.run(dataStore));
        assertTrue(dataStore.contains(ALL_VAR));
        if (dataStore.contains(ALL_VAR)) {
            assertEquals(
                    "{\"head\":{\"vars\":[\"limit_o\"]},\"results\":{\"bindings\":[{\"limit_o\":{\"type\":\"literal\",\"value\":\"alice\"}}]}}",
                    JsonView.getJsonString(dataStore, outParam));
        }
    }

}
