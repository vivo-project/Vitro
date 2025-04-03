/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField.Count;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SearchFacetsController generates provides autocomplete options from the
 * search index.
 */

@WebServlet(name = "SearchFacetsController", urlPatterns = { SearchFacetsController.SEARCH_FACETS_URL })
public class SearchFacetsController extends VitroAjaxController {

    public static final String SEARCH_FACETS_URL = "/searchfacets";
    private static final String APPLICATION_JSON = "application/json";
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(SearchFacetsController.class);
    private static final String PARAM_TERM = "term";
    private static final String PARAM_FILTER = "facet_filter";
    private static final int MAX_QUERY_LENGTH = 50;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doRequest(VitroRequest vreq, HttpServletResponse response) throws IOException, ServletException {

        try {
            String term = vreq.getParameter(PARAM_TERM);
            String filterParam = vreq.getParameter(PARAM_FILTER);
            String field = null;
            Set<String> currentRoles = SearchFiltering.getCurrentUserRoles(vreq);
            Map<String, SearchFilter> filters = SearchFiltering.readFilterConfigurations(currentRoles, vreq);
            if (StringUtils.isNotBlank(filterParam)) {
                SearchFilter filter = filters.get(filterParam);
                if (filter != null && filter.isDisplayed()) {
                    field = filter.getField();
                }
            }
            if (StringUtils.isBlank(field)) {
                log.debug("field '" + field + "' is empty.");
                setEmptySearchResults(response);
                return;
            }
            if (term == null) {
                log.error("There was no parameter '" + PARAM_TERM + "' in the request.");
                setEmptySearchResults(response);
                return;
            } else if (term.length() > MAX_QUERY_LENGTH) {
                log.debug("The search was too long. The maximum " + "query length is " + MAX_QUERY_LENGTH);
                setEmptySearchResults(response);
                return;
            }
            Map<String, List<String>> requestFilters = SearchFiltering.getRequestFilters(vreq);
            SearchFiltering.setSelectedFilters(filters, requestFilters);
            String queryText = PagedSearchController.getQueryText(vreq);
            Map<String, SortConfiguration> sortConfigurations = SearchFiltering.getSortConfigurations(vreq);
            SearchQuery query = PagedSearchController.getQuery(queryText, 0, 0, vreq, filters, sortConfigurations);
            log.debug("query for '" + term + "' is " + query.toString());

            query.addFacetFields(field);
            query.setFacetMinCount(1);
            query.setFacetTextToMatch(term);
            query.setFacetTextCompareCaseInsensitive(true);

            SearchEngine search = ApplicationUtils.instance().getSearchEngine();
            SearchResponse queryResponse = search.query(query);

            if (queryResponse == null) {
                log.error("Query response for a search was null");
                setEmptySearchResults(response);
                return;
            }

            SearchFacetField facetField = queryResponse.getFacetField(field);
            List<Count> values = facetField.getValues();

            if (values == null) {
                log.error("Facet values for a search was null");
                setEmptySearchResults(response);
                return;
            }

            ArrayNode results = objectMapper.createArrayNode();
            for (Count doc : values) {
                String name = doc.getName();
                results.add(name);
            }
            response.setContentType(APPLICATION_JSON);
            response.getWriter().write(results.toString());
        } catch (Throwable e) {
            log.error(e, e);
            setEmptySearchResults(response);
        }
    }

    private void setEmptySearchResults(HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        response.getWriter().write("[]");
    }

}
