package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.Lock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonObjectView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ModelView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RawData;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RdfView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SPARQLQuery extends Operation {

	private static final Log log = LogFactory.getLog(SPARQLQuery.class);

	private String queryText;
	private Parameters requiredParams = new Parameters();
	private Parameters providedParams = new Parameters();

	@Override
	public void dereference() {
		// TODO
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
	public void addRequiredParameter(Parameter param) {
		requiredParams.add(param);
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
	public void addProvidedParameter(Parameter param) {
		providedParams.add(param);
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#sparqlQueryText", minOccurs = 1, maxOccurs = 1)
	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasModel", minOccurs = 1, maxOccurs = 1)
	public void setQueryModel(Parameter model) {
		requiredParams.add(model);
	}

	@Override
	public Parameters getRequiredParams() {
		return requiredParams;
	}

	@Override
	public Parameters getProvidedParams() {
		return providedParams;
	}

	@Override
	public OperationResult run(DataStore dataStore) {
		if (!isInputValid(dataStore)) {
			return new OperationResult(500);
		}
		int resultCode = 200;
		Model queryModel = ModelView.getFirstModel(dataStore, requiredParams);
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		for (String paramName : RdfView.getLiteralNames(requiredParams)) {
			pss.setLiteral(paramName, SimpleDataView.getStringRepresentation(paramName, dataStore),
					requiredParams.get(paramName).getType().getRdfType().getRDFDataType());
		}
		pss.setCommandText(queryText);
		Map<String,ArrayNode> jsonArrays = JsonObjectView.getJsonArrays(providedParams);

		//Map<String, List> singleDimensionalArrays = ArrayView.getSingleDimensionalArrays(providedParams);
		List<String> simpleData = SimpleDataView.getNames(providedParams);
		queryModel.enterCriticalSection(Lock.READ);
		try {
			QueryExecution qexec = QueryExecutionFactory.create(pss.toString(), queryModel);
			try {
				ResultSet results = qexec.execSelect();
				int i = 1;
				List<String> vars = results.getResultVars();
				log.debug("Query vars: " + String.join(", ", vars));

				int j = 0;
				while (results.hasNext()) {
					QuerySolution solution = results.nextSolution();
					log.debug("Query solution " + i++);
					
					populateJsonArrayFromSolution(dataStore, jsonArrays, vars, solution);
					populateSimpleDataFromSolution(dataStore, simpleData, vars, solution);
					j++;
				}

			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				e.printStackTrace();
				resultCode = 500;
			} finally {
				qexec.close();
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			resultCode = 500;
		} finally {
			queryModel.leaveCriticalSection();
		}
		if (!isOutputValid(dataStore)) {
			return new OperationResult(500);
		}
		return new OperationResult(resultCode);
	}

	private void populateJsonArrayFromSolution(DataStore dataStore, Map<String, ArrayNode> jsonArrays, List<String> vars,
			QuerySolution solution) throws ConversionException {
		for (String key : jsonArrays.keySet()) {
			ArrayNode node = jsonArrays.get(key);
			if (!dataStore.contains(key)) {
				RawData data = new RawData(providedParams.get(key));
				data.setObject(node);
				dataStore.addData(key, data);
			}
			ObjectNode object = JsonObjectView.getObject(node);
			for (String var : vars) {
				RDFNode solVar = solution.get(var);
				object.put(var, solVar.toString());
			}
		}
	}
	
	private void populateSimpleDataFromSolution(DataStore dataStore, List<String> simpleData, List<String> vars,
			QuerySolution solution) throws ConversionException {
		for (String var : vars) {
			log.debug(var + " : " + solution.get(var));
			if (simpleData.contains(var) && solution.contains(var)) {
				RDFNode solVar = solution.get(var);
				RawData data = new RawData(providedParams.get(var));
				//TODO: new data should be created based on it's RDF type and parameter type
				data.setRawString(solVar.toString());
				data.earlyInitialization();
				dataStore.addData(var, data);
				simpleData.remove(var);
			} 
		}
	}

}
