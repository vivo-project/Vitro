package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.ParameterizedSparqlString;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ModelView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RdfView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class SparqlQuery extends Operation{
	
	private static final Log log = LogFactory.getLog(SparqlQuery.class);
	protected String queryText;
	protected Parameters inputParams = new Parameters();
	protected Parameters outputParams = new Parameters();
	protected Parameter queryModelParam;

	@Override
	public Parameters getOutputParams() {
		return outputParams;
	}
	
	@Override
	public void dereference() {
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
	public void addInputParameter(Parameter param) {
		inputParams.add(param);
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#sparqlQueryText", minOccurs = 1, maxOccurs = 1)
	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasModel", minOccurs = 1, maxOccurs = 1)
	public void setQueryModel(Parameter model) throws InitializationException {
		if (!ModelView.isModel(model)) {
			throw new InitializationException("Only model parameters accepted on setQueryModel");
		}
		queryModelParam = model;
		inputParams.add(model);
	}

	@Override
	public Parameters getInputParams() {
		return inputParams;
	}
	
	protected void setUris(DataStore dataStore, ParameterizedSparqlString pss) {
		for (String paramName : RdfView.getUriNames(inputParams)) {
			pss.setIri(paramName, SimpleDataView.getStringRepresentation(paramName, dataStore));
		}
	}

	protected void setLiterals(DataStore dataStore, ParameterizedSparqlString pss) {
		for (String paramName : RdfView.getLiteralNames(inputParams)) {
			pss.setLiteral(paramName, SimpleDataView.getStringRepresentation(paramName, dataStore),
					inputParams.get(paramName).getType().getRdfType().getRDFDataType());
		}
	}

	protected String prepareQuery(DataStore dataStore) {
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		pss.setCommandText(queryText);
		setLiterals(dataStore, pss);
		setUris(dataStore, pss);
		final String preparedQueryString = pss.toString();
		return preparedQueryString;
	}
	
	protected boolean isValid(DataStore dataStore) {
		boolean valid = isValid();
		if (valid && !ModelView.hasModel(dataStore, queryModelParam)) {
			log.error("Model not found in input parameters");
			valid = false;
		}
		if (!isInputValid(dataStore)) {
			log.error("Input data is invalid");
			valid = false;
		}
		return valid;
	}

	public boolean isValid() {
		boolean valid = true;
		if (StringUtils.isBlank(queryText)) {
			log.error("Query text is not set");
			valid = false;
		}
		if (queryModelParam == null) {
			log.error("Model param is not provided in configuration");
			valid = false;
		}
		return valid;
	}
}
