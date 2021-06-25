/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds a registry of all relationship checkers that have been defined by the application.
 *
 * Allows the initialisation of supported relationship types (which is linked from the startup configuration),
 * that can be used by objects deep within the authorisation code.
 */
public abstract class RelationshipChecker {
    private final static String PERSON_RESOURCE_MAP_KEY = RelationshipChecker.class.getSimpleName() + "_personResourceMap";

    private static String relationshipQuery = null;

    public static QueryBuilder getQueryBuilder() {
        return new QueryBuilder();
    }

    public static boolean anyRelated(HttpServletRequest request, OntModel ontModel, List<String> resourceUris, List<String> personUris) {
        for (String personUri : personUris) {
            List<String> connectedResourceUris = getResourcesForPersonUri(request, ontModel, personUri);
            for (String connectedResourceUri : connectedResourceUris) {
                if (resourceUris.contains(connectedResourceUri)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static List<String> getResourcesForPersonUri(HttpServletRequest request, OntModel ontModel, String personUri) {
        Map<String, List<String>> personResourceMap = null;

        if (request != null) {
            personResourceMap = (Map<String, List<String>>) request.getAttribute(PERSON_RESOURCE_MAP_KEY);
            if (personResourceMap != null && personResourceMap.containsKey(personUri)) {
                return personResourceMap.get(personUri);
            }
        }

        List<String> resourceUris = new ArrayList<>();

        if (!StringUtils.isEmpty(relationshipQuery)) {
            ParameterizedSparqlString pss = new ParameterizedSparqlString();
            pss.setCommandText(relationshipQuery);

            pss.setIri("personUri", personUri);
            Query query = QueryFactory.create(pss.toString());
            QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
            try {
                ResultSet resultSet = queryExecution.execSelect();
                while (resultSet.hasNext()) {
                    QuerySolution qs = resultSet.nextSolution();
                    resourceUris.add(qs.getResource("resourceUri").getURI());
                }
            } finally {
                queryExecution.close();
            }
        }

        if (request != null) {
            if (personResourceMap == null) {
                personResourceMap = new HashMap<>();
            }

            personResourceMap.put(personUri, resourceUris);
            request.setAttribute(PERSON_RESOURCE_MAP_KEY, personResourceMap);
        }

        return resourceUris;
    }

    public static class QueryBuilder {
        private StringBuilder queryString = new StringBuilder();

        public QueryBuilder addRelationshipThroughContext(String personToContext, String contextType, String contextToResource, String resourceType) {
            if (queryString.length() > 0) {
                queryString.append("\nUNION\n");
            } else {
                queryString.append("SELECT ?resourceUri WHERE {\n");
            }
            queryString.append("{\n");
            queryString.append("  ?personUri <" + personToContext + "> ?roleUri .\n");
            queryString.append("  ?roleUri a <" + contextType + "> .\n");
            if (contextToResource != null) {
                queryString.append("  ?roleUri <" + contextToResource + "> ?resourceUri .\n");
                if (resourceType != null) {
                    queryString.append("  ?resourceUri a <" + resourceType + "> .\n");
                }
            }
            queryString.append("}\n");

            return this;
        }

        public void finish() {
            if (queryString.length() > 0) {
                queryString.append("\n}\n");
            }

            relationshipQuery = queryString.toString();
        }
    }
}
