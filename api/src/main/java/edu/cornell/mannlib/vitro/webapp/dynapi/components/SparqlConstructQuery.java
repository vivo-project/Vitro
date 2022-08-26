package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ModelView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SparqlConstructQuery extends SparqlQuery {

	private static final Log log = LogFactory.getLog(SparqlConstructQuery.class);
	private static final String ERROR_MESSAGE = "Only model supported as an output parameter";

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter", maxOccurs = 1)
	public void addOutputParameter(Parameter param) throws InitializationException {
		if (!ModelView.isModel(param)) {
			throw new InitializationException(ERROR_MESSAGE);
		}
		outputParams.add(param);
	}
	
	@Override
	public OperationResult run(DataStore dataStore) {
		if(!isValid(dataStore)) {
			return OperationResult.internalServerError();
		}
		OperationResult result = OperationResult.ok();
		Model queryModel = ModelView.getModel(dataStore, queryModelParam);
		final String preparedQueryString = prepareQuery(dataStore);

		queryModel.enterCriticalSection(Lock.READ);
		try {
			QueryExecution qexec = QueryExecutionFactory.create(preparedQueryString, queryModel);
			try {
				List<Model> models = ModelView.getExistingModels(outputParams, dataStore);
				if (models.isEmpty()) {
					Model results = qexec.execConstruct();
					ModelView.addModel(dataStore, results, outputParams.getFirst());	
				} else {
					//Extend existing model
					qexec.execConstruct(models.get(0));
				}
				
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				e.printStackTrace();
				result = OperationResult.internalServerError();
			} finally {
				qexec.close();
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			result = OperationResult.internalServerError();
		} finally {
			queryModel.leaveCriticalSection();
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
