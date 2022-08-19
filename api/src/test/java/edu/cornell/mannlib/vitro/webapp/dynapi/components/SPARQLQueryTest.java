package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Statement;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.ParameterUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;

public class SPARQLQueryTest {
	private static final String MODEL = "model";
	private static final String STR_VAR = "str";
	private static final String S_VAR = "s";
	private static final String QUERY_NO_VARS = "SELECT ?str WHERE { BIND(\"test\" as ?str) } ";
	private static final String QUERY_OBJ_VAR = "SELECT ?s WHERE { ?s <test:property> ?str . } " ;
	private static final String QUERY_SUBJ_VAR = "SELECT ?o WHERE { ?s <test:property> ?o . } " ;
	private OntModelImpl model;
	private DataStore dataStore;
	private SPARQLQuery sparql;

	@Before
	public void init() throws Exception {
		sparql = new SPARQLQuery();
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
		Parameter strParam = ParameterUtils.createStringParameter(STR_VAR);
		sparql.addOutputParameter(strParam);
		
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(STR_VAR));
		if (dataStore.contains(STR_VAR)) {
			Data data = dataStore.getData(STR_VAR);
			assertTrue(SimpleDataView.getStringRepresentation(STR_VAR, dataStore).equals("test"));
		}
	}
	
	@Test
	public void literalInput() throws Exception {
		sparql.setQueryText(QUERY_OBJ_VAR);
		Parameter modelParam = ParameterUtils.createModelParameter(MODEL);
		sparql.setQueryModel(modelParam);
		Data modelData = new Data(modelParam);
		Statement stmt = ParameterUtils.addStatement(model, "test:resource", "test:property", "alice" );
		TestView.setObject(modelData, model);
		dataStore.addData(MODEL, modelData);
		Parameter strParam = ParameterUtils.createStringParameter(STR_VAR);
		sparql.addInputParameter(strParam);
		//Not enough params
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		Data strData = new Data(strParam);
		TestView.setObject(strData, "alice");
		dataStore.addData(STR_VAR, strData);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertFalse(dataStore.contains(S_VAR));
		Parameter sParam = ParameterUtils.createStringParameter(S_VAR);
		sparql.addOutputParameter(sParam);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		if (dataStore.contains(STR_VAR)) {
			Data data = dataStore.getData(STR_VAR);
			assertEquals("test:resource", SimpleDataView.getStringRepresentation(S_VAR, dataStore));
		}

	}
	
}
