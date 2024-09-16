/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.utils.dataGetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.google.common.base.Optional;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.search.controller.FilterValue;
import edu.cornell.mannlib.vitro.webapp.search.controller.PagedSearchController;
import edu.cornell.mannlib.vitro.webapp.search.controller.SearchFilter;
import edu.cornell.mannlib.vitro.webapp.search.controller.SearchFiltering;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;
import org.apache.tika.utils.StringUtils;

public class SearchFilterValuesDataGetter extends DataGetterBase implements DataGetter {
    private static final String defaultTemplate = "browseSearchFilterValues.ftl";
    private static final String searchFilterUri = "<" + DisplayVocabulary.SEARCH_FILTER_VALUE + ">";
    private static final Log log = LogFactory.getLog(SearchFilterValuesDataGetter.class);

    String dataGetterURI;
    String searchFilter;
    VitroRequest vreq;
    ServletContext context;

    /**
     * Constructor with display model and data getter URI that will be called by reflection.
     */
    public SearchFilterValuesDataGetter(VitroRequest vreq, Model displayModel, String dataGetterURI) {
        this.configure(vreq, displayModel, dataGetterURI);
    }

    @Override
    public Map<String, Object> getData(Map<String, Object> pageData) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.putAll(vreq.getParameterMap());
        Map<String, List<String>> requestFilters = Collections.emptyMap();
        Map<String, Object> defaultSearchResults = PagedSearchController.process(vreq, requestFilters).getMap();
        responseMap.put("filterGenericInfo", defaultSearchResults);
        requestFilters = SearchFiltering.getRequestFilters(vreq);
        Optional<FilterValue> filterValue = Optional.absent();
        if (!isValidFilterValueProvided(requestFilters, defaultSearchResults)) {
            // get first filter value from emptyRequestMap and apply it for next request.
            filterValue = getFirstFilterValue(defaultSearchResults);
            if (filterValue.isPresent()) {
                requestFilters.put(searchFilter, new ArrayList<String>(Arrays.asList(filterValue.get().getId())));
            }
        }
        responseMap.putAll(PagedSearchController.process(vreq, requestFilters).getMap());
        responseMap.put("searchFilter", this.searchFilter);
        responseMap.put("bodyTemplate", defaultTemplate);
        responseMap.put("languageAware", isLanguageAwarenessEnabled());
        return responseMap;
    }

    private Boolean isLanguageAwarenessEnabled() {
        ConfigurationProperties cp = ConfigurationProperties.getInstance();
        return Boolean.valueOf(cp.getProperty("RDFService.languageFilter", "false"));
    }

    private boolean isValidFilterValueProvided(Map<String, List<String>> requestFilters,
            Map<String, Object> defaultSearchResults) {
        String mainFilterValue = vreq.getParameter("filters_main");
        List<String> requestedValues = requestFilters.get(searchFilter);
        if (StringUtils.isBlank(mainFilterValue) || requestedValues == null || requestedValues.isEmpty()
                || StringUtils.isBlank(requestedValues.iterator().next())) {
            return false;
        }
        String requestedValue = requestedValues.iterator().next();
        try {
            Map<String, SearchFilter> filterMap = (Map<String, SearchFilter>) defaultSearchResults.get("filters");
            SearchFilter f = filterMap.get(searchFilter);
            Map<String, FilterValue> values = f.getValues();
            if (values.containsKey(requestedValue)) {
                return true;
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return false;
    }

    private Optional<FilterValue> getFirstFilterValue(Map<String, Object> defaultSearchResults) {
        try {
            Map<String, SearchFilter> filterMap = (Map<String, SearchFilter>) defaultSearchResults.get("filters");
            SearchFilter f = filterMap.get(searchFilter);
            Collection<FilterValue> values = f.getValues().values();
            if (!values.isEmpty()) {
                return Optional.of(values.iterator().next());
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        return Optional.absent();
    }

    /**
     * Configure this instance based on the URI and display model.
     */
    protected void configure(VitroRequest vreq, Model displayModel, String dataGetterURI) {
        if (vreq == null) {
            throw new IllegalArgumentException("VitroRequest  may not be null.");
        }
        if (displayModel == null) {
            throw new IllegalArgumentException("Display Model may not be null.");
        }
        if (dataGetterURI == null) {
            throw new IllegalArgumentException("PageUri may not be null.");
        }

        this.vreq = vreq;
        this.context = vreq.getSession().getServletContext();
        this.dataGetterURI = dataGetterURI;

        QuerySolutionMap initBindings = new QuerySolutionMap();
        initBindings.add("dataGetterURI", ResourceFactory.createResource(this.dataGetterURI));

        Query configurationQuery = QueryFactory.create(dataGetterQuery);
        displayModel.enterCriticalSection(Lock.READ);
        try (QueryExecution qexec = QueryExecutionFactory.create(configurationQuery, displayModel, initBindings)) {
            ResultSet res = qexec.execSelect();
            while (res.hasNext()) {
                QuerySolution soln = res.next();
                this.searchFilter = soln.getLiteral("id").toString();
            }
        } finally {
            displayModel.leaveCriticalSection();
        }
    }

    public static final String defaultVarNameForResults = "results";

    /**
     * Query to get search filter id for a given URI.
     */
    private static final String dataGetterQuery =
        "PREFIX display: <" + DisplayVocabulary.DISPLAY_NS + "> \n" +
        "PREFIX search: <https://vivoweb.org/ontology/vitro-search#>\n" +
        "SELECT ?id WHERE { \n" +
        "?dataGetterURI " + searchFilterUri + " ?searchFilter .\n" +
        "?searchFilter search:id ?id .\n" +
        "}";

}
