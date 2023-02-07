package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.List;

import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ModelView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.AdditionsAndRetractions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ModelWriter extends AbstractOperation{

	
	private static final String ERROR_MESSAGE = "ModelWriter accept only model parameters";
	Parameters additionModelParams = new Parameters();
	Parameters retractionModelParams = new Parameters();
	Parameters inputParams = new Parameters();
	private Parameter targetModelParam;
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#additionModel")
	public void addAdditions(Parameter param) throws InitializationException {
		if (!ModelView.isModel(param)) {
			throw new InitializationException(ERROR_MESSAGE);
		}
		additionModelParams.add(param);
		inputParams.add(param);
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#retractionModel")
	public void addRetractions(Parameter param) throws InitializationException {
		if (!ModelView.isModel(param)) {
			throw new InitializationException(ERROR_MESSAGE);
		}
		retractionModelParams.add(param);
		inputParams.add(param);
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#targetModel", minOccurs = 1, maxOccurs = 1)
	public void addTarget(Parameter param) throws InitializationException {
		if (!ModelView.isModel(param)) {
			throw new InitializationException(ERROR_MESSAGE);
		}
		targetModelParam = param;
		inputParams.add(param);
	}
	
	@Override
	public OperationResult run(DataStore dataStore) {
		if (targetModelParam == null) {
			return OperationResult.internalServerError();
		}
		if (!isInputValid(dataStore)) {
			return OperationResult.internalServerError();
		}
		
		List<Model> additions = ModelView.getExistingModels(additionModelParams, dataStore);
		List<Model> retractions = ModelView.getExistingModels(retractionModelParams, dataStore);
		Model target = ModelView.getModel(dataStore, targetModelParam);
		AdditionsAndRetractions changes = new AdditionsAndRetractions(additions, retractions);
		//TODO: set editor uri instead of ""
		ProcessRdfForm.applyChangesToWriteModel(changes, null, target, dataStore.getUserUri());
		
		return OperationResult.ok();
	}

	@Override
	public void dereference() {
	}

	@Override
	public Parameters getInputParams() {
		return inputParams;
	}

	@Override
	public Parameters getOutputParams() {
		return new Parameters();
	}

}
