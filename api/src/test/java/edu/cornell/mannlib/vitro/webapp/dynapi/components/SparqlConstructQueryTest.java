package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.ParameterUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.SparqlConstructQuery;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

public class SparqlConstructQueryTest {
	private static final String MODEL = "model";
	private static final String OUT_MODEL = "outmodel";
	private static final String STR_VAR = "str";
	private static final String QUERY_NO_VARS = "CONSTRUCT { <test:uri> <test:property> ?str . } WHERE { BIND(\"test\" as ?str) } ";
	private static final String QUERY_OBJ_VAR = "CONSTRUCT { ?s <test:property> \"bob\" . }  WHERE { ?s <test:property> ?str . } " ;
	private static final String QUERY_SUBJ_VAR = "CONSTRUCT { <test:uri> <test:property> ?o . } WHERE { ?str <test:property> ?o . } " ;
    private static final String QUERY_LABEL = "CONSTRUCT { <test:uri> <http://www.w3.org/2000/01/rdf-schema#label> ?o . } WHERE { ?str <http://www.w3.org/2000/01/rdf-schema#label> ?o . } " ;
	private OntModelImpl model;
	private DataStore dataStore;
	private SparqlConstructQuery sparql;

	@Before
	public void init() throws Exception {
		sparql = new SparqlConstructQuery();
		dataStore = new DataStore();
		model = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
	}
	
	@Test
	public void emptyConfiguration() throws Exception {
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		sparql.setQueryText(QUERY_NO_VARS);
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		sparql.setRdfService(new RDFServiceModel(model));
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		Parameter output = ParameterUtils.createModelParameter(OUT_MODEL);
		sparql.addOutputParameter(output);
		
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(OUT_MODEL));
		if (dataStore.contains(OUT_MODEL)) {
			Model outModel = (Model) TestView.getObject(dataStore.getData(OUT_MODEL));
			assertTrue(outModel.size() == 1);
			assertTrue(outModel.getGraph().contains(
	                NodeFactory.createURI("test:uri"),
	                NodeFactory.createURI("test:property"),
	                NodeFactory.createLiteral("test"))
	        );
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
		Parameter outputParam = ParameterUtils.createModelParameter(OUT_MODEL);
		sparql.addOutputParameter(outputParam);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(OUT_MODEL));
		if (dataStore.contains(OUT_MODEL)) {
			Model outModel = (Model) TestView.getObject(dataStore.getData(OUT_MODEL));
			assertTrue(outModel.size() == 1);
			assertTrue(outModel.getGraph().contains(
	                NodeFactory.createURI("test:resource"),
	                NodeFactory.createURI("test:property"),
	                NodeFactory.createLiteral("bob"))
	        );
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
		Parameter outputParam = ParameterUtils.createModelParameter(OUT_MODEL);
		sparql.addOutputParameter(outputParam);
		assertTrue(!dataStore.contains(OUT_MODEL));
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(OUT_MODEL));
		if (dataStore.contains(OUT_MODEL)) {
			Model outModel = (Model) TestView.getObject(dataStore.getData(OUT_MODEL));
			assertTrue(outModel.size() == 1);
			assertTrue(outModel.getGraph().contains(
	                NodeFactory.createURI("test:uri"),
	                NodeFactory.createURI("test:property"),
	                NodeFactory.createLiteral("alice"))
	        );
		}
	}
	
    @Test
    public void languageFiltering() throws Exception {
        sparql.setQueryText(QUERY_LABEL);
        sparql.setRdfService(new RDFServiceModel(model));
        ParameterUtils.addStatement(model, "test:uri", "http://www.w3.org/2000/01/rdf-schema#label", "Alicia@es");
        ParameterUtils.addStatement(model, "test:uri", "http://www.w3.org/2000/01/rdf-schema#label", "Alice@en-US");
        ParameterUtils.addStatement(model, "test:uri", "http://www.w3.org/2000/01/rdf-schema#label", "Алиса@ru-RU");
        Parameter output = ParameterUtils.createModelParameter(OUT_MODEL);
        sparql.addOutputParameter(output);
        sparql.setLanguageFiltering(true);
        dataStore.setAcceptLangs(Arrays.asList("es"));
        assertEquals(OperationResult.ok(), sparql.run(dataStore));
        assertTrue(dataStore.contains(OUT_MODEL));
        if (dataStore.contains(OUT_MODEL)) {
            Model outModel = (Model) TestView.getObject(dataStore.getData(OUT_MODEL));
            assertTrue(outModel.size() == 1);
            assertTrue(outModel.getGraph().contains(
                    NodeFactory.createURI("test:uri"),
                    NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label"),
                    NodeFactory.createLiteral("Alicia","es"))
            );
        }
    }
	@Test
	public void addToProvidedModelUriInput() throws Exception {
		sparql.setQueryText(QUERY_SUBJ_VAR);
		Parameter modelParam = ParameterUtils.createModelParameter(MODEL);
		sparql.setQueryModel(modelParam);
		Data modelData = new Data(modelParam);
		ParameterUtils.addStatement(model, "test:resource1", "test:property", "alice" );
		ParameterUtils.addStatement(model, "test:resource2", "test:property", "bob" );

		TestView.setObject(modelData, model);
		dataStore.addData(MODEL, modelData);
		Parameter strParam = ParameterUtils.createUriParameter(STR_VAR);
		sparql.addInputParameter(strParam);
		//Not enough params
		assertEquals(OperationResult.internalServerError(), sparql.run(dataStore));
		Data strData = new Data(strParam);
		TestView.setObject(strData, "test:resource1");
		dataStore.addData(STR_VAR, strData);
		Parameter outputModelParam = ParameterUtils.createModelParameter(OUT_MODEL);
		sparql.addOutputParameter(outputModelParam);
		sparql.addInputParameter(outputModelParam);
		Data writeModelData = new Data(outputModelParam);
		OntModelImpl writeModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
		TestView.setObject(writeModelData, writeModel);
		dataStore.addData(OUT_MODEL, writeModelData);
		assertTrue(writeModel.size() == 0);
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(OUT_MODEL));
		if (dataStore.contains(OUT_MODEL)) {
			Model outModel = (Model) TestView.getObject(dataStore.getData(OUT_MODEL));
			assertTrue(outModel.size() == 1);
			assertTrue(outModel.getGraph().contains(
	                NodeFactory.createURI("test:uri"),
	                NodeFactory.createURI("test:property"),
	                NodeFactory.createLiteral("alice"))
	        );
		}
		TestView.setObject(strData, "test:resource2");
		assertEquals(OperationResult.ok(), sparql.run(dataStore));
		assertTrue(dataStore.contains(OUT_MODEL));
		if (dataStore.contains(OUT_MODEL)) {
			Model outModel = (Model) TestView.getObject(dataStore.getData(OUT_MODEL));
			assertTrue(outModel.size() == 2);
			assertTrue(outModel.getGraph().contains(
	                NodeFactory.createURI("test:uri"),
	                NodeFactory.createURI("test:property"),
	                NodeFactory.createLiteral("alice"))
	        );
			assertTrue(outModel.getGraph().contains(
	                NodeFactory.createURI("test:uri"),
	                NodeFactory.createURI("test:property"),
	                NodeFactory.createLiteral("bob"))
	        );
		}
	}
	
}
