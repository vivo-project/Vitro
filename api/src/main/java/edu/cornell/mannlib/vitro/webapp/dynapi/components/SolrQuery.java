package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpNode;
import edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore.DumpParser;
import edu.cornell.mannlib.vitro.webapp.dynapi.ActionPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchQuery;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class SolrQuery extends AbstractOperation{

    private static final Log log = LogFactory.getLog(ActionPool.class);

    private String queryText;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#solrQueryText", minOccurs = 1, maxOccurs = 1)
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getQueryText(){
        return this.queryText;
    }

    @Override
    public void dereference() {

    }

    @Override
    public OperationResult run(OperationData input) {
        if (!isInputValid(input)) {
            return new OperationResult(400);
        }

        Map<String,Object> parsedQuery = parseQuery(input);

        SearchQuery searchQuery;
        try {
            searchQuery = createSearchQuery(parsedQuery);
        } catch (Exception e) {
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

    private Map<String, Object> parseQuery(OperationData input){
        String query=queryText;
        for(Parameter parameter: providedParams.params.values()){
            String[] parameterInput = input.get(parameter.getName());
            query=queryText.replaceAll("?"+parameter.getName(),String.join(",",parameterInput));
        }
        Type type = new TypeToken<Map<String,Object>>(){}.getType();
        return new Gson().fromJson(query, type);
    }

    private SearchQuery createSearchQuery(Map<String,Object> parsedQuery) throws Exception {
        SearchQuery searchQuery = ApplicationUtils.instance().getSearchEngine().createQuery();

        for(Map.Entry<String, Object> queryParam : parsedQuery.entrySet()){
            switch(queryParam.getKey()){
                case "query":
                    searchQuery.setQuery((String) queryParam.getValue());
                    break;
                case "filter":
                    searchQuery.addFilterQueries(convertJsonValueToStringArray(queryParam.getValue()));
                    break;
                case "fields":
                    searchQuery.addFields(convertJsonValueToStringArray(queryParam.getValue()));
                    break;
                case "offset":
                    searchQuery.setStart(Integer.parseInt((String)queryParam.getValue()));
                case "limit":
                    searchQuery.setRows(Integer.parseInt((String)queryParam.getValue()));
                case "sort":
                    String[] sortingFields = convertJsonValueToStringArray(queryParam.getValue());
                    for(String sortingField: sortingFields){
                        String sortingFiledName = sortingField.trim().split(" ")[0];
                        SearchQuery.Order order = SearchQuery.Order.valueOf(
                                sortingField.trim().split(" ")[0].toUpperCase()
                        );
                        searchQuery.addSortField(sortingFiledName, order);
                    }
                    break;
                case "facet":
                    //TODO to be implemented
                    break;
                default:
                    log.warn("Unknown field '"+queryParam.getKey()+"' found in Solr text query.");
            }
        }

        return searchQuery;
    }

    /*
    Some fields within the Solr Query JSON, such as 'filter', 'fields' and 'sort' could
    either be represented as an array of string, or one string containing key-value
    pairs separated by commas. This function converts them to an array of key:value,
    pair strings, which can later be passed to SearchQuery.
    */
    private String[] convertJsonValueToStringArray(Object fieldValue) throws Exception {
        String[] fieldValues;
        if(fieldValue instanceof ArrayList){
            fieldValues = ((ArrayList<String>)fieldValue).stream().map(
                     value -> value.replaceAll("\"","")
            ).toArray(String[]::new);
        }else if (fieldValue instanceof String){
            fieldValues = ((String)fieldValue).split(",");
        }else{
            throw new Exception("Field value must be a string or an array");
        }
        return fieldValues;
    }
}
