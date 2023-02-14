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
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.ModelWriter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.TestView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiInMemoryOntModel;

public class ModelWriterTest {

	private static final String RETRACTION = "retraction";
	private static final String TARGET = "target";
	private static final String ADDITION = "addition";
	public static final String MODEL_CONVERSION_CLASS = DynapiInMemoryOntModel.class.getCanonicalName();
	private OntModelImpl targetModel;
	private Parameter addition;
	private Parameter target;
	private Parameter retraction;
	private DataStore dataStore;
	private ModelWriter writer;

	@Before
	public void init() throws Exception {
		writer = new ModelWriter();
		dataStore = new DataStore();
		targetModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
		target = ParameterUtils.createModelParameter(TARGET);
		addition = ParameterUtils.createModelParameter(ADDITION);
		retraction = ParameterUtils.createModelParameter(RETRACTION);
	}

	@Test
	public void noTargetModel() {
		assertEquals(writer.run(dataStore), OperationResult.internalServerError());
	}

	@Test
	public void notEnoughInput() throws InitializationException {
		writer.addTarget(target);
		assertEquals(writer.run(dataStore), OperationResult.internalServerError());
	}

	@Test
	public void emptyModels() throws InitializationException {
		writer.addTarget(target);
		Data data = new Data(target);
		TestView.setObject(data, targetModel);
		dataStore.addData(TARGET, data);
		assertTrue(targetModel.isEmpty());
		assertFalse(writer.run(dataStore).hasError());
		assertTrue(targetModel.isEmpty());
	}

	@Test
	public void additions() throws InitializationException {
		writer.addTarget(target);
		writer.addAdditions(addition);
		
		Data data = new Data(target);
		TestView.setObject(data, targetModel);
		dataStore.addData(TARGET, data);

		data = new Data(addition);
		OntModelImpl additionModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
		Statement stmt = ParameterUtils.addStatement(additionModel, "test:resource", "test:property", "literal");
		TestView.setObject(data, additionModel);
		dataStore.addData(ADDITION, data);

		assertTrue(targetModel.isEmpty());
		assertFalse(writer.run(dataStore).hasError());
		assertTrue(targetModel.contains(stmt));
		assertTrue(targetModel.size() == 1);
	}

	@Test
	public void retractionsOnEmptyModel() throws InitializationException {
		writer.addTarget(target);
		writer.addRetractions(retraction);
		
		Data data = new Data(target);
		TestView.setObject(data, targetModel);
		dataStore.addData(TARGET, data);

		data = new Data(retraction);
		OntModelImpl retractionModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
		ParameterUtils.addStatement(retractionModel, "test:resource", "test:property", "literal");
		TestView.setObject(data, retractionModel);
		dataStore.addData(RETRACTION, data);

		assertTrue(targetModel.isEmpty());
		assertFalse(writer.run(dataStore).hasError());
		assertTrue(targetModel.isEmpty());
	}
	
	@Test
	public void additionsAndRetractions() throws InitializationException {
		writer.addTarget(target);
		writer.addRetractions(retraction);
		writer.addAdditions(addition);
		
		Data data = new Data(target);
		TestView.setObject(data, targetModel);
		dataStore.addData(TARGET, data);

		data = new Data(retraction);
		OntModelImpl retractionModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
		ParameterUtils.addStatement(retractionModel, "test:resource", "test:property", "literal");
		TestView.setObject(data, retractionModel);
		dataStore.addData(RETRACTION, data);
		
		data = new Data(addition);
		OntModelImpl additionModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
		ParameterUtils.addStatement(additionModel, "test:resource", "test:property", "literal");
		TestView.setObject(data, additionModel);
		dataStore.addData(ADDITION, data);

		assertTrue(targetModel.isEmpty());
		assertFalse(writer.run(dataStore).hasError());
		assertTrue(targetModel.isEmpty());
	}
	
	@Test
	public void retractionsOnData() throws InitializationException {
		writer.addTarget(target);
		writer.addRetractions(retraction);
		
		Data data = new Data(target);
		TestView.setObject(data, targetModel);
		dataStore.addData(TARGET, data);

		data = new Data(retraction);
		OntModelImpl retractionModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
		Statement stmt = ParameterUtils.addStatement(retractionModel, "test:resource", "test:property", "literal");
		targetModel.add(stmt);
		TestView.setObject(data, retractionModel);
		dataStore.addData(RETRACTION, data);

		assertTrue(targetModel.size() == 1);
		assertFalse(writer.run(dataStore).hasError());
		assertTrue(targetModel.isEmpty());
	}
}
