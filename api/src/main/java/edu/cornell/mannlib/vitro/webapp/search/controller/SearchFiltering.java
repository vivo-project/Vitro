package edu.cornell.mannlib.vitro.webapp.search.controller;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.ROLE_PUBLIC_URI;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;

public class SearchFiltering {

    private static final Log log = LogFactory.getLog(SearchFiltering.class);

    private static final String FILTER_RANGE = "filter_range_";
    private static final String FILTER_INPUT_PREFIX = "filter_input_";
    private static final String FILTERS = "filters";
    public static final String ANY_VALUE = "[* TO *]";

    private static final String FILTER_QUERY = ""
            + "PREFIX search: <https://vivoweb.org/ontology/vitro-search#>\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "SELECT ?filter_id ?filter_type ?filter_label ?value_label ?value_id  ?field_name ?public ?filter_order "
            + "?value_order (STR(?isUriReq) as ?isUri ) ?multivalued ?input ?regex ?facet ?min ?max ?role "
            + "?value_public ?more_limit ?multilingual ?reverseFacetOrder\n"
            + "WHERE {\n"
            + "    ?filter rdf:type search:Filter .\n"
            + "    ?filter rdfs:label ?filter_label .\n"
            + "    ?filter search:id ?filter_id .\n"
            + "    ?filter a ?filter_type .\n"
            + "    ?filter search:filterField ?field .\n"
            + "    ?field search:indexField ?field_name .\n"
            + "    OPTIONAL {\n"
            + "        ?filter search:hasKnownValue ?value . \n"
            + "        ?value rdfs:label ?value_label .\n"
            + "        ?value search:id ?value_id .\n"
            + "        OPTIONAL {"
            + "            ?value search:order ?v_order .\n"
            + "            bind(?v_order as ?value_order_found).\n"
            + "        }\n"
            + "        OPTIONAL {\n"
            + "            ?value search:isDefaultForRole ?role .\n"
            + "        }\n"
            + "        OPTIONAL {\n"
            + "            ?value search:public ?value_public .\n"
            + "        }\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "        ?field search:isLanguageSpecific ?f_multilingual  .\n"
            + "        BIND(?f_multilingual as ?bind_multilingual) .\n"
            + "    }\n"
            + "    OPTIONAL {?field search:multivalued ?multivalued}\n"
            + "    OPTIONAL {?filter search:isUriValues ?isUriReq }\n"
            + "    OPTIONAL {?filter search:userInput ?input }\n"
            + "    OPTIONAL {?filter search:userInputRegex ?regex }\n"
            + "    OPTIONAL {?filter search:facetResults ?facet }\n"
            + "    OPTIONAL {?filter search:reverseFacetOrder ?reverseFacetOrder }\n"
            + "    OPTIONAL {?filter search:from ?min }\n"
            + "    OPTIONAL {?filter search:public ?public }\n"
            + "    OPTIONAL {?filter search:to ?max }\n"
            + "    OPTIONAL {?filter search:moreLimit ?more_limit }\n"
            + "    OPTIONAL {\n"
            + "        ?filter search:order ?f_order \n"
            + "        bind(?f_order as ?filter_order_found).\n"
            + "    }\n"
            + "    BIND(coalesce(?filter_order_found, 0) as ?filter_order)\n"
            + "    BIND(coalesce(?value_order_found, 0) as ?value_order)\n"
            + "    BIND(COALESCE(?bind_multilingual, false) as ?multilingual)\n"
            + "} ORDER BY ?filter_id ?filter_order ?value_order";

    private static final String FILTER_GROUPS_QUERY = ""
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX search: <https://vivoweb.org/ontology/vitro-search#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "SELECT ?group_id (STR(?group_l) AS ?group_label) ?filter_id ?order ?filter_order ?public\n"
            + "WHERE {\n"
            + "    ?filter_group rdf:type search:FilterGroup .\n"
            + "    ?filter_group search:contains ?filter .\n"
            + "    ?filter_group rdfs:label ?group_l .\n"
            + "    ?filter_group search:id ?group_id .\n"
            + "    OPTIONAL { ?filter_group search:order ?order . } \n"
            + "    ?filter search:id ?filter_id .\n"
            + "    OPTIONAL {?filter_group search:public ?public }\n"
            + "    OPTIONAL{ ?filter search:order ?f_order .\n"
            + "        bind(?f_order as ?filter_order_found).\n"
            + "    }\n"
            + "    BIND(coalesce(?filter_order_found, 0) as ?filter_order)\n"
            + "}  ORDER BY ?order ?group_label ?filter_order";

    private static final String LABEL_QUERY = ""
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "SELECT ?label\n"
            + "WHERE {\n"
            + "    ?uri rdfs:label ?label .\n"
            + "} LIMIT 1";

    private static final String SORT_QUERY = ""
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX search: <https://vivoweb.org/ontology/vitro-search#> \n"
            + "SELECT ( STR(?sort_label) as ?label ) ?id ?searchField " +
            "?multilingual ?isAsc ?sort_order ?fallback ?display\n"
            + "WHERE {\n"
            + "    ?sort rdf:type search:Sort . \n"
            + "    ?sort rdfs:label ?sort_label .\n"
            + "    OPTIONAL {\n"
            + "        ?sort search:sortField ?field .\n"
            + "        ?field search:indexField ?searchField  .\n"
            + "        OPTIONAL {\n"
            + "            ?field search:isLanguageSpecific ?f_multilingual  .\n"
            + "            BIND(?f_multilingual as ?bind_multilingual) .\n"
            + "        }\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "        ?sort search:id ?id .\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "        ?sort search:isAscending ?f_ord  .\n"
            + "        BIND(?f_ord as ?f_order) .\n"
            + "    }\n"
            + "    OPTIONAL {\n"
            + "        ?sort search:hasFallback/search:id ?fallback .\n"
            + "    }\n"
            + "    OPTIONAL{ "
            + "        ?sort search:order ?s_order .\n"
            + "        BIND(?s_order as ?sort_order_found).\n"
            + "    }\n"
            + "    OPTIONAL {?sort search:display ?display }\n"
            + "    BIND(coalesce(?sort_order_found, 0) as ?sort_order)\n"
            + "    BIND(COALESCE(?f_order, false) as ?isAsc)\n"
            + "    BIND(COALESCE(?bind_multilingual, false) as ?multilingual)\n"
            + "} ORDER BY ?sort_order ?label ";

    protected static void addFiltersToQuery(SearchQuery query, Map<String, SearchFilter> filters) {
        for (SearchFilter searchFilter : filters.values()) {
            if (searchFilter.isInput()) {
                SearchFiltering.addInputFilter(query, searchFilter);
            } else if (searchFilter.isRange()) {
                SearchFiltering.addRangeFilter(query, searchFilter);
            }
            for (FilterValue fv : searchFilter.getValues().values()) {
                if (fv.isDefault() || fv.getSelected()) {
                    if (ANY_VALUE.equals(fv.getId())) {
                        query.addFilterQuery(searchFilter.getField() + ":" + ANY_VALUE );
                    } else {
                        query.addFilterQuery(searchFilter.getField() + ":\"" + fv.getId() + "\"");
                    }
                }
            }
        }
    }

    public static Map<String, List<String>> getRequestFilters(VitroRequest vreq) {
        Map<String, List<String>> requestFilters = new HashMap<>();
        Enumeration<String> paramNames = vreq.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramFilterName = paramNames.nextElement();
            if (paramFilterName.startsWith(SearchFiltering.FILTERS)) {
                String[] filterValues = vreq.getParameterValues(paramFilterName);
                if (filterValues != null && filterValues.length > 0) {
                    for (String filter : filterValues) {
                        String[] pair = filter.split(":", 2);
                        if (pair.length == 2) {
                            String filterId = pair[0].replace("\"", "");
                            String value = pair[1].replace("\"", "");
                            if (requestFilters.containsKey(filterId)) {
                                List<String> list = requestFilters.get(filterId);
                                list.add(value);
                            } else {
                                requestFilters.put(filterId, new LinkedList<String>(Arrays.asList(value)));
                            }
                        }
                    }
                }
            }
            if (paramFilterName.startsWith(SearchFiltering.FILTER_RANGE)) {
                String[] values = vreq.getParameterValues(paramFilterName);
                if (values != null && values.length > 0) {
                    String filterId = paramFilterName.replace(SearchFiltering.FILTER_RANGE, "");
                    requestFilters.put(filterId, new LinkedList<String>(Arrays.asList(values[0])));
                }
            }
            if (paramFilterName.startsWith(SearchFiltering.FILTER_INPUT_PREFIX)) {
                String[] values = vreq.getParameterValues(paramFilterName);
                if (values != null && values.length > 0) {
                    String filterId = paramFilterName.replace(SearchFiltering.FILTER_INPUT_PREFIX, "");
                    requestFilters.put(filterId, new LinkedList<String>(Arrays.asList(values[0])));
                }
            }
        }
        return requestFilters;
    }

    public static Map<String, SearchFilter> readFilterConfigurations(Set<String> currentRoles, VitroRequest vreq) {
        long startTime = System.nanoTime();
        Map<String, SearchFilter> filtersByField = new LinkedHashMap<>();
        Model model;
        if (vreq != null) {
            model = ModelAccess.on(vreq).getOntModelSelector().getDisplayModel();
        } else {
            model = ModelAccess.getInstance().getOntModelSelector().getDisplayModel();
        }
        if (model == null) {
            return filtersByField;
        }
        model.enterCriticalSection(Lock.READ);
        try {
            Query facetQuery = QueryFactory.create(FILTER_QUERY);
            QueryExecution qexec = QueryExecutionFactory.create(facetQuery, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                if (solution.get("filter_id") == null || solution.get("field_name") == null
                        || solution.get("filter_type") == null) {
                    continue;
                }
                String resultFilterId = solution.get("filter_id").toString();
                String resultFieldName = solution.get("field_name").toString();

                SearchFilter filter = null;
                if (filtersByField.containsKey(resultFieldName)) {
                    filter = filtersByField.get(resultFieldName);
                } else {
                    Optional<Locale> locale = vreq != null ? Optional.of(vreq.getLocale()) : Optional.empty();
                    filter = createSearchFilter(filtersByField, solution, resultFilterId, resultFieldName, locale);
                }
                filter.setMulitlingual(solution.get("multilingual").asLiteral().getBoolean());
                if (solution.get("value_id") == null) {
                    continue;
                }
                String valueId = solution.get("value_id").toString();
                FilterValue value;
                if (!filter.contains(valueId)) {
                    value = new FilterValue(valueId);
                    value.setName(solution.get("value_label"));
                    value.setOrder(solution.get("value_order"));
                    value.setPubliclyAvailable(solution.get("value_public"));
                    filter.addValue(value);
                }
                value = filter.getValue(valueId);
                RDFNode role = solution.get("role");
                if (role != null && role.isResource()) {
                    String roleUri = role.asResource().getURI();
                    if (currentRoles.contains(roleUri)) {
                        value.setDefault(true);
                    }
                }
            }
        } finally {
            model.leaveCriticalSection();
        }
        if (log.isDebugEnabled()) {
            log.debug(getSpentTime(startTime) + "ms spent after FILTER QUERY request.");
        }
        return sortFilters(filtersByField);
    }

    public static void addDefaultFilters(SearchQuery query, Set<String> currentRoles) {
        Map<String, SearchFilter> filtersByField = SearchFiltering.readFilterConfigurations(currentRoles, null);
        for (SearchFilter searchFilter : filtersByField.values()) {
            if (searchFilter.isInput()) {
                SearchFiltering.addInputFilter(query, searchFilter);
            } else if (searchFilter.isRange()) {
                SearchFiltering.addRangeFilter(query, searchFilter);
            }
            for (FilterValue fv : searchFilter.getValues().values()) {
                if (fv.isDefault() || fv.getSelected()) {
                    query.addFilterQuery(searchFilter.getField() + ":\"" + fv.getId() + "\"");
                }
            }
        }
    }

    public static Map<String, SearchFilter> sortFilters(Map<String, SearchFilter> filters) {
        List<Entry<String, SearchFilter>> list = new LinkedList<>(filters.entrySet());
        list.sort(new FilterComparator());
        return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    public static class FilterComparator implements Comparator<Map.Entry<String, SearchFilter>> {
        public int compare(Entry<String, SearchFilter> obj1, Entry<String, SearchFilter> obj2) {
            SearchFilter filter1 = obj1.getValue();
            SearchFilter filter2 = obj2.getValue();
            int result = filter1.getOrder().compareTo(filter2.getOrder());
            if (result == 0) {
                // order are equal, sort by name
                return filter1.getName().toLowerCase().compareTo(filter2.getName().toLowerCase());
            } else {
                return result;
            }
        }
    }

    public static List<SearchFilterGroup> readFilterGroupsConfigurations(VitroRequest vreq,
            Map<String, SearchFilter> filtersById) {
        Map<String, SearchFilterGroup> groups = new LinkedHashMap<>();
        Model model = ModelAccess.on(vreq).getOntModelSelector().getDisplayModel();
        model.enterCriticalSection(Lock.READ);
        try {
            Query facetQuery = QueryFactory.create(FILTER_GROUPS_QUERY);
            QueryExecution qexec = QueryExecutionFactory.create(facetQuery, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                if (solution.get("filter_id") == null || solution.get("group_label") == null
                        || solution.get("group_id") == null) {
                    continue;
                }
                String filterId = solution.get("filter_id").toString();
                String groupId = solution.get("group_id").toString();
                String groupLabel = solution.get("group_label").toString();
                SearchFilterGroup group = null;
                if (groups.containsKey(groupId)) {
                    group = groups.get(groupId);
                } else {
                    group = new SearchFilterGroup(groupId, groupLabel);
                    RDFNode publicNode = solution.get("public");
                    if (publicNode != null) {
                        group.setPublic(publicNode.asLiteral().getBoolean());
                    }
                    groups.put(groupId, group);
                }
                group.addFilterId(filterId);
                SearchFilter filter = filtersById.get(filterId);
                if (filter != null && !filter.isHidden()) {
                    group.setHidden(false);
                }
            }
        } finally {
            model.leaveCriticalSection();
        }
        return new LinkedList<SearchFilterGroup>(groups.values());
    }

    public static Map<String, SortConfiguration> getSortConfigurations(VitroRequest vreq) {
        Map<String, SortConfiguration> sortConfigurations = new LinkedHashMap<>();
        Model model = ModelAccess.on(vreq).getOntModelSelector().getDisplayModel();
        model.enterCriticalSection(Lock.READ);
        try {
            Query facetQuery = QueryFactory.create(SORT_QUERY);
            QueryExecution qexec = QueryExecutionFactory.create(facetQuery, model);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                RDFNode searchFieldNode = solution.get("searchField");
                RDFNode idNode = solution.get("id");
                if (solution.get("label") == null) {
                    continue;
                }
                String field = searchFieldNode == null ? "" : searchFieldNode.toString();
                String id = idNode == null ? "" : idNode.toString();
                String label = solution.get("label").asLiteral().getLexicalForm();

                if (!sortConfigurations.containsKey(id)) {
                    SortConfiguration config = new SortConfiguration(id, label, field);

                    RDFNode multilingual = solution.get("multilingual");
                    if (multilingual != null) {
                        config.setMultilingual(multilingual.asLiteral().getBoolean());
                    }
                    RDFNode isAsc = solution.get("isAsc");
                    if (isAsc != null) {
                        config.setAscOrder(isAsc.asLiteral().getBoolean());
                    }
                    RDFNode fallback = solution.get("fallback");
                    if (fallback != null && fallback.isLiteral()) {
                        config.setFallback(fallback.asLiteral().toString());
                    }
                    RDFNode order = solution.get("sort_order");
                    if (order != null) {
                        config.setOrder(order.asLiteral().getInt());
                    }
                    RDFNode display = solution.get("display");
                    if (display != null) {
                        config.setDisplay(display.asLiteral().getBoolean());
                    }
                    sortConfigurations.put(id, config);
                }
            }
        } finally {
            model.leaveCriticalSection();
        }
        return sortConfigurations;
    }

    private static SearchFilter createSearchFilter(Map<String, SearchFilter> filtersByField,
            QuerySolution solution, String resultFilterId, String resultFieldName, Optional<Locale> locale) {
        SearchFilter filter;
        filter = new SearchFilter(resultFilterId, locale);
        filtersByField.put(resultFieldName, filter);
        filter.setName(solution.get("filter_label"));
        filter.setOrder(solution.get("filter_order"));
        filter.setType(solution.get("filter_type"));
        if (solution.get("isUri") != null && "true".equals(solution.get("isUri").toString())) {
            filter.setLocalizationRequired(true);
        }
        RDFNode min = solution.get("min");
        if (min != null) {
            filter.setMin(min.asLiteral().toString());
        }
        RDFNode max = solution.get("max");
        if (max != null) {
            filter.setMax(max.asLiteral().toString());
        }
        filter.setField(resultFieldName);
        RDFNode multivalued = solution.get("multivalued");
        if (multivalued != null) {
            filter.setMultivalued(multivalued.asLiteral().getBoolean());
        }
        RDFNode input = solution.get("input");
        if (input != null) {
            filter.setInput(input.asLiteral().getBoolean());
        }
        RDFNode inputRegex = solution.get("regex");
        if (inputRegex != null) {
            filter.setInputRegex(inputRegex.asLiteral().getBoolean());
        }

        RDFNode publicNode = solution.get("public");
        if (publicNode != null) {
            filter.setPublic(publicNode.asLiteral().getBoolean());
        }

        RDFNode facet = solution.get("facet");
        if (facet != null) {
            filter.setFacetsRequired(facet.asLiteral().getBoolean());
        }

        RDFNode reverseFacetOrder = solution.get("reverseFacetOrder");
        if (reverseFacetOrder != null) {
            filter.setReverseFacetOrder(reverseFacetOrder.asLiteral().getBoolean());
        }

        RDFNode moreLimit = solution.get("more_limit");
        if (moreLimit != null && moreLimit.isLiteral()) {
            filter.setMoreLimit(moreLimit.asLiteral().getInt());
        }
        return filter;
    }

    private static void addRangeFilter(SearchQuery query, SearchFilter searchFilter) {
        String rangeText = searchFilter.getRangeText();
        if (StringUtils.isBlank(rangeText)) {
            return;
        }
        query.addFilterQuery(searchFilter.getField() + ":\"" + rangeText + "\"");
    }

    private static void addInputFilter(SearchQuery query, SearchFilter searchFilter) {
        if (StringUtils.isBlank(searchFilter.getInputText())
                || PagedSearchController.PARAM_QUERY_TEXT.equals(searchFilter.getId())) {
            return;
        }
        String searchText = searchFilter.getInputText();
        if (searchFilter.isInputRegex()) {
            query.addFilterQuery(searchFilter.getField() + ":/" + searchText + "/");
        } else {
            query.addFilterQuery(searchFilter.getField() + ":\"" + searchText + "\"");
        }
    }

    static String getFilterInputText(VitroRequest vreq, String name) {
        if (PagedSearchController.PARAM_QUERY_TEXT.equals(name)) {
            return PagedSearchController.getQueryText(vreq);
        }
        String[] values = vreq.getParameterValues(SearchFiltering.FILTER_INPUT_PREFIX + name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return "";
    }

    static String getFilterRangeText(VitroRequest vreq, String name) {
        String[] values = vreq.getParameterValues(SearchFiltering.FILTER_RANGE + name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return "";
    }

    public static void setSelectedFilters(Map<String, SearchFilter> filters, Map<String, List<String>> requestFilters) {
        for (SearchFilter filter : filters.values()) {
            if (requestFilters.containsKey(filter.getId())) {
                List<String> requestValues = requestFilters.get(filter.getId());
                if (!SearchFiltering.isEmptyValues(requestValues)) {
                    filter.setSelected(true);
                    if (filter.isRange()) {
                        filter.setRangeValues(requestValues.iterator().next());
                    } else if (filter.isInput()) {
                        filter.setInputText(requestValues.iterator().next());
                    } else {
                        for (String requestValue : requestValues) {
                            if (filter.getValues().containsKey(requestValue)) {
                                FilterValue value = filter.getValue(requestValue);
                                value.setSelected(true);
                            } else {
                                FilterValue value = new FilterValue(requestValue);
                                value.setSelected(true);
                                filter.addValue(value);
                            }
                        }
                    }
                }
            }
        }
    }

    public static Set<String> getCurrentUserRoles(VitroRequest vreq) {
        UserAccount user = LoginStatusBean.getCurrentUser(vreq);
        if (user == null) {
            return Collections.singleton(ROLE_PUBLIC_URI);
        }
        return user.getPermissionSetUris();
    }

    static boolean isEmptyValues(List<String> requestedValues) {
        if (requestedValues.isEmpty()) {
            return true;
        }
        for (String value : requestedValues) {
            if (!StringUtils.isBlank(value)) {
                return false;
            }
        }
        return true;
    }

    static String getUriLabel(String uri, VitroRequest vreq) {
        String result = "";
        Model model = ModelAccess.on(vreq).getOntModelSelector().getFullModel();
        model.enterCriticalSection(Lock.READ);
        try {
            QuerySolutionMap initialBindings = new QuerySolutionMap();
            initialBindings.add("uri", ResourceFactory.createResource(uri));
            Query facetQuery = QueryFactory.create(SearchFiltering.LABEL_QUERY);
            QueryExecution qexec = QueryExecutionFactory.create(facetQuery, model, initialBindings);
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                RDFNode rdfNode = solution.get("label");
                Literal literal = rdfNode.asLiteral();
                result = literal.getLexicalForm();
            } else {
                result = uri;
            }
        } catch (Exception e) {
            log.error(e, e);
        } finally {
            model.leaveCriticalSection();
        }
        return result;
    }

    static void addFacetFieldsToQuery(Map<String, SearchFilter> filters, SearchQuery query) {
        for (String fieldId : filters.keySet()) {
            SearchFilter filter = filters.get(fieldId);
            if (filter.isFacetsRequired()) {
                query.addFacetFields(fieldId);
            }
        }
    }

    public static Map<String, SearchFilter> getFiltersById(Map<String, SearchFilter> filtersByField) {
        Map<String, SearchFilter> filtersById =
                filtersByField.values().stream().collect(Collectors.toMap(SearchFilter::getId, Function.identity()));
        return filtersById;
    }

    static Map<String, SearchFilter> getFiltersForTemplate(Map<String, SearchFilter> filtersByField) {
        Iterator<Entry<String, SearchFilter>> iterator = filtersByField.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, SearchFilter> entry = iterator.next();
            SearchFilter searchFilter = entry.getValue();
            searchFilter.removeValuesWithZeroCount();
            if (searchFilter.isEmpty()) {
                searchFilter.setHidden(true);
            }

        }
        return filtersByField.values().stream().collect(Collectors.toMap(SearchFilter::getId, Function.identity()));
    }

    static void addFiltersToPageLinks(VitroRequest vreq, ParamMap pagingLinkParams, Enumeration<String> paramNames) {
        while (paramNames.hasMoreElements()) {
            String paramFilterName = paramNames.nextElement();
            if (!StringUtils.isBlank(paramFilterName) && (paramFilterName.startsWith(FILTERS)
                    || paramFilterName.startsWith(FILTER_RANGE) || paramFilterName.startsWith(FILTER_INPUT_PREFIX)
                    || paramFilterName.startsWith(PagedSearchController.PARAM_QUERY_SORT_BY))) {
                String[] values = vreq.getParameterValues(paramFilterName);
                if (values.length > 0) {
                    pagingLinkParams.put(paramFilterName, values[0]);
                }
            }
        }
    }

    private static long getSpentTime(long startTime) {
        return (System.nanoTime() - startTime) / 1000000;
    }
}
