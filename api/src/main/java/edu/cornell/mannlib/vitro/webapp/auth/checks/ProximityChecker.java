/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

public class ProximityChecker {
    private static final Log log = LogFactory.getLog(ProximityChecker.class);

    public static boolean isAnyRelated(Model ontModel, List<String> resourceUris, List<String> personUris,
            String query) {
        for (String personUri : personUris) {
            List<String> connectedResourceUris = getRelatedUris(ontModel, personUri, query);
            for (String connectedResourceUri : connectedResourceUris) {
                if (resourceUris.contains(connectedResourceUri)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> getRelatedUris(Model model, String personUri, String queryTemplate) {
        HashMap<String, List<String>> queryMap = QueryResultsMapCache.get();
        String queryMapKey = createQueryMapKey(personUri, queryTemplate);
        if (queryMap.containsKey(queryMapKey)) {
            return queryMap.get(queryMapKey);
        }
        List<String> results = new ArrayList<>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(queryTemplate);
        pss.setIri("personUri", personUri);
        String queryText = pss.toString();
        debug("queryText: " + queryText);
        Query query = QueryFactory.create(queryText);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        try {
            ResultSet resultSet = queryExecution.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.nextSolution();
                addSolutionValues(results, qs);
            }
        } finally {
            queryExecution.close();
        }
        debug("query results: " + results);
        queryMap.put(queryMapKey, results);
        QueryResultsMapCache.update(queryMap);
        return results;
    }

    private static void addSolutionValues(List<String> results, QuerySolution qs) {
        Iterator<String> names = qs.varNames();
        while (names.hasNext()) {
            String name = names.next();
            RDFNode node = qs.get(name);
            if (node.isURIResource()) {
                results.add(node.asResource().getURI());
            } else if (node.isLiteral()) {
                results.add(node.asLiteral().toString());
            }
        }
    }

    private static void debug(String queryText) {
        if (log.isDebugEnabled()) {
            log.debug(queryText);
        }
    }

    private static String createQueryMapKey(String personUri, String queryTemplate) {
        return queryTemplate + "." + personUri;
    }
}
