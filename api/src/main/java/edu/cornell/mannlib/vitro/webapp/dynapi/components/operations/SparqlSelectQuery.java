package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DefaultDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SparqlSelectQuery extends SparqlQuery {

	public static final Log log = LogFactory.getLog(SparqlSelectQuery.class);

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
	public void addOutputParameter(Parameter param) {
		outputParams.add(param);
	}

	@Override
	public OperationResult runOperation(DataStore dataStore) {
		OperationResult result = OperationResult.ok();
		RDFService localRdfService = getRDFService(dataStore);
		final String preparedQueryString = prepareQuery(dataStore);
		DefaultDataView.createDefaultOutput(dataStore, outputParams);
		try {
			ResultSet results = RDFServiceUtils.sparqlSelectQuery(preparedQueryString, localRdfService);
			JsonView.addSparqlSelectResult(dataStore, outputParams, results);
			int i = 1;
			List<String> vars = results.getResultVars();
			log.debug("Query vars: " + String.join(", ", vars));

			while (results.hasNext()) {
				QuerySolution solution = results.nextSolution();
				log.debug("Query solution " + i++);
				JsonContainerView.addSolutionRow(dataStore, vars, solution, outputParams);
				SimpleDataView.addFromSolution(dataStore, vars, solution, outputParams);
			}
		} catch (Exception e) {
			log.error(e,e);
			result = OperationResult.internalServerError();
		}

		if (!isOutputValid(dataStore)) {
			return OperationResult.internalServerError();
		}
		return result;
	}

}
