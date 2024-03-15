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
import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
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
    private static final String PROFILE_URI = "profileUri";
    private static final String EXTERNAL_AUTH_ID = "externalAuthId";
    private static final String MATCHING_PROPERTY_URI = "matchingPropertyUri";
    private static final String OBJECT_URI = "objectUri";
    private static final Log log = LogFactory.getLog(SparqlSelectQueryResultsChecker.class);

    public static boolean sparqlSelectQueryResultsContain(Check check, AuthorizationRequest ar, String[] inputValues) {
        String query = check.getConfiguration();
        if (StringUtils.isBlank(query)) {
            query = check.getValues().getSingleValue();
            if (StringUtils.isBlank(query)) {
                log.error("Sparql query is empty.");
                return false;
            }
        }

        AccessObject ao = ar.getAccessObject();
        Model m = ao.getModel();
        if (m == null) {
            log.error("Model not provided");
            return false;
        }

        Set<String> comparedValues = new HashSet<>();

        if (isQueryNotProvidedInConfiguration(check)) {
            addRelatedUrisToComparedValues(ao, comparedValues);
        } else {
            addValuesToComparedValues(check.getValues(), comparedValues);
        }

        if (isProfileUriRelatedQuery(query)) {
            return makeProfileUriMatchQuery(ar, query, m, comparedValues);
        }

        if (query.contains("?" + EXTERNAL_AUTH_ID) && externalAuthIdIsNotAvailable(ar)) {
            logVariableNotAvailable(EXTERNAL_AUTH_ID);
            return false;
        }

        if (query.contains("?" + MATCHING_PROPERTY_URI) && matchingPropertyUriIsNotAvailable()) {
            logVariableNotAvailable(MATCHING_PROPERTY_URI);
            return false;
        }

        Set<String> sparqlResults = getSparqlSelectResults(m, "", query, ar);
        sparqlResults.retainAll(comparedValues);
        if (!sparqlResults.isEmpty()) {
            return true;
        }

        return false;
    }

    private static boolean makeProfileUriMatchQuery(AuthorizationRequest ar, String queryTemplate, Model m,
            Set<String> comparedValues) {
        boolean result = false;
        Set<String> profileUris = new HashSet<String>(ar.getEditorUris());
        if (profileUris.isEmpty()) {
            log.debug("Subject has no person Uri, nothing to substitute.");
            result = false;
        }
        for (String profileUri : profileUris) {
            Set<String> sparqlResults = getSparqlSelectResults(m, profileUri, queryTemplate, ar);
            // Return true if intersection is not empty
            sparqlResults.retainAll(comparedValues);
            if (!sparqlResults.isEmpty()) {
                result = true;
            }
        }
        return result;
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
        HashSet<String> results = new HashSet<>();
        if (queryMap.containsKey(queryMapKey)) {
            results.addAll(queryMap.get(queryMapKey));
            return results;
        }
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
        Set<String> queryMapValue = new HashSet<>();
        queryMapValue.addAll(results);
        queryMap.put(queryMapKey, queryMapValue);
        QueryResultsMapCache.update(queryMap);
        return results;
    }

    private static void setVariables(String profileUri, AuthorizationRequest ar, ParameterizedSparqlString pss) {
        pss.setIri(PROFILE_URI, profileUri);
        AccessObject object = ar.getAccessObject();
        Optional<String> uri = object.getUri();
        if (uri.isPresent()) {
            pss.setIri(OBJECT_URI, uri.get());
        }
        String externalAuthId = ar.getExternalAuthId();
        if (!StringUtils.isBlank(externalAuthId)) {
            pss.setLiteral(EXTERNAL_AUTH_ID, externalAuthId);
        }
        String matchingPropertyUri = SelfEditingConfiguration.getInstance().getMatchingPropertyUri();
        if (!StringUtils.isBlank(matchingPropertyUri)) {
            pss.setIri(MATCHING_PROPERTY_URI, matchingPropertyUri);
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
        if (queryTemplate.contains("?" + OBJECT_URI)) {
            AccessObject object = ar.getAccessObject();
            Optional<String> uri = object.getUri();
            if (uri.isPresent()) {
                mapKey += "." + uri.get();
            }
        }
        return mapKey;
    }

    private static void logVariableNotAvailable(String variable) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Query contains ?%s, but authorization request doesn't provide it.", variable));
        }
    }

    private static boolean externalAuthIdIsNotAvailable(AuthorizationRequest ar) {
        return StringUtils.isBlank(ar.getExternalAuthId());
    }

    private static boolean matchingPropertyUriIsNotAvailable() {
        return StringUtils.isBlank(SelfEditingConfiguration.getInstance().getMatchingPropertyUri());
    }

    private static boolean isProfileUriRelatedQuery(String queryTemplate) {
        return queryTemplate.contains("?" + PROFILE_URI);
    }

}
