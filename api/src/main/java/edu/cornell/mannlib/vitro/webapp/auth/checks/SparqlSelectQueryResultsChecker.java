/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.lang3.StringUtils;
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

public class SparqlSelectQueryResultsChecker {
    private static final Log log = LogFactory.getLog(SparqlSelectQueryResultsChecker.class);

    public static boolean sparqlSelectQueryResultsContain(Check check, AuthorizationRequest ar, String[] inputValues) {
        String queryTemplate = check.getConfiguration();
        if (StringUtils.isBlank(queryTemplate)) {
            queryTemplate = check.getValues().getSingleValue();
        }
        if (StringUtils.isBlank(queryTemplate)) {
            log.error("SparqlQueryContains template is empty");
            return false;
        }
        AccessObject ao = ar.getAccessObject();
        Model m = ao.getModel();
        if (m == null) {
            log.debug("SparqlQueryContains model is not provided");
            return false;
        }
        Set<String> profileUris = new HashSet<String>(ar.getEditorUris());
        if (profileUris.isEmpty()) {
            if (queryTemplate.contains("?profileUri")) {
                log.debug("Subject has no person URIs");
                return false;
            } else {
                profileUris.add("");
            }
        }
        Set<String> comparedValues = new HashSet<>();
        if (isQueryNotProvidedInConfiguration(check)) {
            addRelatedUrisToComparedValues(ao, comparedValues);
        } else {
            addValuesToComparedValues(check.getValues(), comparedValues);
        }
        for (String profileUri : profileUris) {
            Set<String> sparqlSelectResults = getSparqlSelectResults(m, profileUri, queryTemplate, ar);
            // Return true if intersection is not empty
            comparedValues.retainAll(sparqlSelectResults);
            if (!comparedValues.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void addValuesToComparedValues(AttributeValueSet values, Set<String> comparedValues) {
        comparedValues.addAll(values.getValues());
    }

    private static void addRelatedUrisToComparedValues(AccessObject ao, Set<String> comparedValues) {
        comparedValues.addAll(Arrays.asList(ao.getResourceUris()));
    }

    private static boolean isQueryNotProvidedInConfiguration(Check check) {
        return StringUtils.isBlank(check.getConfiguration());
    }

    private static Set<String> getSparqlSelectResults(Model model, String profileUri, String queryTemplate,
            AuthorizationRequest ar) {
        HashMap<String, Set<String>> queryMap = QueryResultsMapCache.get();
        String queryMapKey = createQueryMapKey(profileUri, queryTemplate, ar);
        if (queryMap.containsKey(queryMapKey)) {
            return queryMap.get(queryMapKey);
        }
        Set<String> results = new HashSet<>();
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(queryTemplate);
        setVariables(profileUri, ar, pss);

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
        } catch (Exception e) {
            log.error(e, e);
        } finally {
            queryExecution.close();
        }
        debug("query results: " + results);
        queryMap.put(queryMapKey, results);
        QueryResultsMapCache.update(queryMap);
        return results;
    }

    private static void setVariables(String profileUri, AuthorizationRequest ar, ParameterizedSparqlString pss) {
        pss.setIri("profileUri", profileUri);
        AccessObject object = ar.getAccessObject();
        Optional<String> uri = object.getUri();
        if (uri.isPresent()) {
            pss.setIri("objectUri", uri.get());
        }
    }

    private static void addSolutionValues(Set<String> results, QuerySolution qs) {
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

    private static String createQueryMapKey(String profileUri, String queryTemplate, AuthorizationRequest ar) {
        String mapKey = queryTemplate + "." + profileUri;
        if (queryTemplate.contains("?objectUri")) {
            AccessObject object = ar.getAccessObject();
            Optional<String> uri = object.getUri();
            if (uri.isPresent()) {
                mapKey += "." + uri.get();
            }
        }
        return mapKey;
    }

}
