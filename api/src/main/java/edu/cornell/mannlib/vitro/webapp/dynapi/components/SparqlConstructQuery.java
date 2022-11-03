package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ModelView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SparqlConstructQuery extends SparqlQuery {

	private static final Log log = LogFactory.getLog(SparqlConstructQuery.class);
	private static final String ERROR_MESSAGE = "Only model supported as an output parameter";
	private Parameter outputParam;

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter", maxOccurs = 1)
	public void addOutputParameter(Parameter param) throws InitializationException {
		if (!ModelView.isModel(param)) {
			throw new InitializationException(ERROR_MESSAGE);
		}
		outputParam = param;
		outputParams.add(param);
	}

	@Override
	public OperationResult run(DataStore dataStore) {
		if (!isValid(dataStore)) {
			return OperationResult.internalServerError();
		}
		OperationResult result = OperationResult.ok();
		final String preparedQueryString = prepareQuery(dataStore);
		RDFService localRdfService = getRDFService(dataStore);

		try {
			List<Model> models = ModelView.getExistingModels(outputParams, dataStore);
			if (models.isEmpty()) {
				Model resultModel = new OntModelImpl(OntModelSpec.OWL_DL_MEM);
				localRdfService.sparqlConstructQuery(preparedQueryString, resultModel);
				ModelView.addModel(dataStore, resultModel, outputParam);
			} else {
				// Extend existing model
				localRdfService.sparqlConstructQuery(preparedQueryString, models.get(0));
			}

		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			result = OperationResult.internalServerError();
		}

		if (!isOutputValid(dataStore)) {
			return OperationResult.internalServerError();
		}
		return result;
	}

	protected boolean isValid(DataStore dataStore) {
		if (!isValid()) {
			return false;
		}
		if (!super.isValid(dataStore)) {
			return false;
		}
		return true;
	}

	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		if (outputParams.size() != 1) {
			return false;
		}
		return true;
	}
}
