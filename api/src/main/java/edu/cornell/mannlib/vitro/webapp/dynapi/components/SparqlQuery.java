package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ModelView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RdfView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.filter.LanguageFilteringRDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ContextModelsUser;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class SparqlQuery extends AbstractOperation implements ContextModelsUser {

	private static final Log log = LogFactory.getLog(SparqlQuery.class);
	protected String queryText;
	protected Parameter queryModelParam;
	protected boolean langFiltering = false;
	protected RDFService rdfService;

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
	public void addInputParameter(Parameter param) {
		inputParams.add(param);
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#languageFiltering", minOccurs = 0, maxOccurs = 1)
	public void setLanguageFiltering(boolean langFiltering) {
		this.langFiltering = langFiltering;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#sparqlQueryText", minOccurs = 1, maxOccurs = 1)
	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasModel", minOccurs = 0, maxOccurs = 1)
	public void setQueryModel(Parameter model) throws InitializationException {
		if (!ModelView.isModel(model)) {
			throw new InitializationException("Only model parameters accepted on setQueryModel");
		}
		queryModelParam = model;
		inputParams.add(model);
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

	public boolean isValid() {
		boolean valid = true;
		if (StringUtils.isBlank(queryText)) {
			log.error("Query text is not set");
			valid = false;
		}
		if (queryModelParam == null && rdfService == null) {
			log.error("Either query model param or rdfService should be set");
			valid = false;
		}
		return valid;
	}

	public void setContextModels(ContextModelAccess models) {
		rdfService = models.getRDFService();
	}

	protected RDFService getRDFService(DataStore dataStore) {
		RDFService localRdfService = null;
		if (queryModelParam != null) {
			Model queryModel = ModelView.getModel(dataStore, queryModelParam);
			localRdfService = new RDFServiceModel(queryModel);
		} else {
			localRdfService = rdfService;
		}
		if (langFiltering) {
			return new LanguageFilteringRDFService(localRdfService, dataStore.getAcceptLangs());
		}
		return localRdfService;
	}
}
