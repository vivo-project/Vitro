package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.ArrayView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.StringView;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SolrQuery extends Operation {

    private static final Log log = LogFactory.getLog(SolrQuery.class);

    private Parameters inputParams = new Parameters();
    private Parameters outputParams = new Parameters();

    private String queryText;
    private String offset;
    private String limit;
    private ArrayList<String> fields = new ArrayList<>();
    private ArrayList<String> filters = new ArrayList<>();
    private ArrayList<String> facets = new ArrayList<>();
    private ArrayList<String> sorts = new ArrayList<>();

    // region @Property Setters

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
    public void addInputParameter(Parameter param) {
        inputParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addOutputParameter(Parameter param) {
        outputParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#solrQueryText", maxOccurs = 1)
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#solrFilter")
    public void addFilter(String filter) {
        this.filters.add(filter);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#solrField")
    public void addField(String field) {
        this.fields.add(field);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#solrLimit", maxOccurs = 1)
    public void setLimit(String limit) {
        this.limit = limit;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#solrOffset", maxOccurs = 1)
    public void setOffset(String offset) {
        this.offset = offset;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#solrSort")
    public void addSort(String sort) {
        this.sorts.add(sort);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#solrFacet")
    public void addFacet(String facet) {
        this.facets.add(facet);
    }

    // endregion

    // region Getters

    @Override
    public Parameters getInputParams() {
        return inputParams;
    }

    @Override
    public Parameters getOutputParams() {
        return outputParams;
    }

    public String getQueryText() {
        return queryText;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    public ArrayList<String> getFilters() {
        return filters;
    }

    public String getOffset() {
        return offset;
    }

    public String getLimit() {
        return limit;
    }

    public ArrayList<String> getFacets() {
        return facets;
    }

    public ArrayList<String> getSorts() {
        return sorts;
    }

    // endregion

    @Override
    public OperationResult run(DataStore input) {
        if (!isInputValid(input)) {
            return OperationResult.badRequest();
        }

        SearchQuery searchQuery;
        try {
            searchQuery = createSearchQuery(input);
        } catch (Exception e) {
            log.error("Error while parsing input data for query");
            log.error(e);
            return OperationResult.badRequest();
        }

        SearchEngine searchEngine = ApplicationUtils.instance().getSearchEngine();
        SearchResponse response;
        try {
            response = searchEngine.query(searchQuery);
        } catch (SearchEngineException e) {
            log.error("Error while executing Solr Query:");
            log.error(e.getMessage());
            return OperationResult.internalServerError();
        }

        return OperationResult.ok();
    }

    private SearchQuery createSearchQuery(DataStore dataStore)
            throws InputMismatchException, IllegalArgumentException {
        SearchQuery searchQuery = ApplicationUtils.instance().getSearchEngine().createQuery();

        if (queryText != null) {
            searchQuery = searchQuery.setQuery(replaceVariablesWithInput(queryText, dataStore));
        }
        if (offset != null) {
            searchQuery = searchQuery.setStart(Integer.parseInt(replaceVariablesWithInput(offset, dataStore)));
        }
        if (limit != null) {
            searchQuery = searchQuery.setRows(Integer.parseInt(replaceVariablesWithInput(limit, dataStore)));
        }
        for (String field : fields) {
            searchQuery = searchQuery.addFields(replaceVariablesWithInput(field, dataStore));
        }
        for (String filter : filters) {
            searchQuery = searchQuery.addFilterQuery(replaceVariablesWithInput(filter, dataStore));
        }
        for (String sort : sorts) {
            sort = replaceVariablesWithInput(sort, dataStore);
            String[] sortTokens = sort.trim().split(" ");
            searchQuery = searchQuery.addSortField(sortTokens[0], SearchQuery.Order.valueOf(sortTokens[sortTokens.length - 1].toUpperCase()));
        }
        return searchQuery;
    }

    private String replaceVariablesWithInput(String property, DataStore dataStore)
            throws InputMismatchException {

        String[] propertyVariables = Arrays.stream(property.split(":| |,"))
                .filter(propertySegment -> propertySegment.startsWith("?"))
                .map(propertyVariable -> propertyVariable.substring(1))
                .toArray(String[]::new);

        for (String propertyVar : propertyVariables) {
            if (!dataStore.contains(propertyVar) ) {
                throw new InputMismatchException("Data store doesn't contain value " + propertyVar);
            }
            if ( ArrayView.isMultiValuedArray(dataStore, propertyVar)) {
            	throw new InputMismatchException(propertyVar + " is multivalued array");
            }
            property = property.replace("?" + propertyVar, StringView.getFirstStringValue(dataStore, propertyVar));
        }

        return property;
    }

    @Override
    public void dereference() {

    }
}
