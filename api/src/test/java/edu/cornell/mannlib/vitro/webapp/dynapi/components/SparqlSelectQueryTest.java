package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.ParameterUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonView;

public class SparqlSelectQueryTest {
	private static final String MODEL = "model";
	private static final String STR_VAR = "str";
	private static final String S_VAR = "s";
	private static final String O_VAR = "o";
    private static final String ALL_VAR = "all";
	private static final String QUERY_NO_VARS = "SELECT ?str WHERE { BIND(\"test\" as ?str) } ";
	private static final String QUERY_OBJ_VAR = "SELECT ?s WHERE { ?s <test:property> ?str . } " ;
	private static final String QUERY_SUBJ_VAR = "SELECT ?o WHERE { ?str <test:property> ?o . } " ;
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
		Parameter modelParam = ParameterUtils.createModelParameter(MODEL);
		sparql.setQueryModel(modelParam);
		Data modelData = new Data(modelParam);
		TestView.setObject(modelData, model);
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		dataStore.addData(MODEL, modelData);
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
		ParameterUtils.addStatement(model, "test:resource", "test:property", "alice" );
		TestView.setObject(modelData, model);
		dataStore.addData(MODEL, modelData);
		Parameter strParam = ParameterUtils.createStringLiteralParameter(STR_VAR);
		sparql.addInputParameter(strParam);
		//Not enough params
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
		ParameterUtils.addStatement(model, "test:resource", "test:property", "alice" );
		TestView.setObject(modelData, model);
		dataStore.addData(MODEL, modelData);
		Parameter strParam = ParameterUtils.createUriParameter(STR_VAR);
		sparql.addInputParameter(strParam);
		//Not enough params
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
            assertEquals("{\"head\":{\"vars\":[\"o\"]},\"results\":{\"bindings\":[{\"o\":{\"type\":\"literal\",\"value\":\"alice\"}}]}}", JsonView.getJsonString(dataStore, outParam));
        }
    }
	    
}
