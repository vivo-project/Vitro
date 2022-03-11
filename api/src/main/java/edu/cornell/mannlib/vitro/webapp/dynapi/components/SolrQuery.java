package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SolrQuery extends Operation {

    private static final Log log = LogFactory.getLog(SolrQuery.class);

    private Parameters requiredParams = new Parameters();
    private Parameters providedParams = new Parameters();

    private String queryText;
    private String offset;
    private String limit;
    private ArrayList<String> fields = new ArrayList<>();
    private ArrayList<String> filters = new ArrayList<>();
    private ArrayList<String> facets = new ArrayList<>();
    private ArrayList<String> sorts = new ArrayList<>();

    // region @Property Setters

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
    public void addRequiredParameter(Parameter param) {
        requiredParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addProvidedParameter(Parameter param) {
        providedParams.add(param);
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
    public Parameters getRequiredParams() {
        return requiredParams;
    }

    @Override
    public Parameters getProvidedParams() {
        return providedParams;
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
    public OperationResult run(OperationData input) {
        if (!isInputValid(input)) {
            return new OperationResult(400);
        }

        SearchQuery searchQuery;
        try {
            searchQuery = createSearchQuery(input);
        } catch (Exception e) {
            log.error("Error while parsing input data for query");
            log.error(e);
            return new OperationResult(400);
        }

        SearchEngine searchEngine = ApplicationUtils.instance().getSearchEngine();
        SearchResponse response;
        try {
            response = searchEngine.query(searchQuery);
        } catch (SearchEngineException e) {
            log.error("Error while executing Solr Query:");
            log.error(e.getMessage());
            return new OperationResult(500);
        }

        return new OperationResult(200);
    }

    private SearchQuery createSearchQuery(OperationData input)
            throws InputMismatchException, IllegalArgumentException {
        SearchQuery searchQuery = ApplicationUtils.instance().getSearchEngine().createQuery();

        if (queryText != null) {
            searchQuery = searchQuery.setQuery(replaceVariablesWithInput(queryText, input));
        }
        if (offset != null) {
            searchQuery = searchQuery.setStart(Integer.parseInt(replaceVariablesWithInput(offset, input)));
        }
        if (limit != null) {
            searchQuery = searchQuery.setRows(Integer.parseInt(replaceVariablesWithInput(limit, input)));
        }
        for (String field : fields) {
            searchQuery = searchQuery.addFields(replaceVariablesWithInput(field, input));
        }
        for (String filter : filters) {
            searchQuery = searchQuery.addFilterQuery(replaceVariablesWithInput(filter, input));
        }
        for (String sort : sorts) {
            sort = replaceVariablesWithInput(sort, input);
            String[] sortTokens = sort.trim().split(" ");
            searchQuery = searchQuery.addSortField(sortTokens[0], SearchQuery.Order.valueOf(sortTokens[sortTokens.length - 1].toUpperCase()));
        }
        return searchQuery;
    }

    private String replaceVariablesWithInput(String property, OperationData input)
            throws InputMismatchException {

        String[] propertyVariables = Arrays.stream(property.split(":| |,"))
                .filter(propertySegment -> propertySegment.startsWith("?"))
                .map(propertyVariable -> propertyVariable.substring(1))
                .toArray(String[]::new);

        for (String propertyVar : propertyVariables) {
            if (!input.has(propertyVar)) {
                throw new InputMismatchException();
            }
            property = property.replace("?" + propertyVar, input.get(propertyVar));
        }

        return property;
    }

    @Override
    public void dereference() {

    }

}
