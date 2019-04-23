/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.MissingParametersException;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

/**
 * Start with a parameter map, like from an HTTPServletRequest.
 * 
 * Given a query holder and a list of names, find values for each name, and bind
 * it to the variable of that name, either as a URI or as a plain literal.
 * 
 * If a parameter is not found, or has multiple values, throw an exception.
 */
public class VariableBinder {
    private final Map<String, String[]> parameters;

    public VariableBinder(Map<String, String[]> parameters) {
        Objects.requireNonNull(parameters, "'parameters' must not be null.");
        this.parameters = Collections.unmodifiableMap(deepCopy(parameters));
    }

    private Map<String, String[]> deepCopy(Map<String, String[]> original) {
        Map<String, String[]> copy = new HashMap<>();
        for (String key : original.keySet()) {
            String[] values = original.get(key);
            copy.put(key, Arrays.copyOf(values, values.length));
        }
        return copy;
    }

    public QueryHolder bindValuesToQuery(Set<String> uriBindingNames,
            Set<String> literalBindingNames, QueryHolder query)
            throws MissingParametersException {
        Objects.requireNonNull(uriBindingNames,
                "'uriBindingNames' must not be null.");
        Objects.requireNonNull(literalBindingNames,
                "'literalBindingNames' must not be null.");
        Objects.requireNonNull(query, "'query' must not be null.");
        return bindUriParameters(uriBindingNames,
                bindLiteralParameters(literalBindingNames, query));
    }

    private QueryHolder bindUriParameters(Set<String> names,
            QueryHolder queryHolder) throws MissingParametersException {
        for (String name : names) {
            queryHolder = queryHolder.bindToUri(name, getOneParameter(name));
        }
        return queryHolder;
    }

    private QueryHolder bindLiteralParameters(Set<String> names,
            QueryHolder queryHolder) throws MissingParametersException {
        for (String name : names) {
            queryHolder = queryHolder.bindToPlainLiteral(name,
                    getOneParameter(name));
        }
        return queryHolder;
    }

    private String getOneParameter(String name)
            throws MissingParametersException {
        String[] uris = parameters.get(name);
        if (uris == null || uris.length == 0) {
            throw new MissingParametersException(
                    "A '" + name + "' parameter is required.");
        } else if (uris.length > 1) {
            throw new MissingParametersException(
                    "Unexpected multiple values for '" + name + "' parameter.");
        }
        return uris[0];
    }

}
