/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributor.MissingParametersException;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.ConstructQueryContext;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.SelectQueryContext;

/**
 * Start with a parameter map, like from an HTTPServletRequest.
 * 
 * Given a query context and a list of names, find values for each name, and
 * bind it to the variable of that name, either as a URI or as a plain literal.
 * 
 * If a parameter is not found, or has multiple values, throw an exception.
 */
public class VariableBinder {
	private final Map<String, String[]> parameters;

	public VariableBinder(Map<String, String[]> parameters) {
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

	public SelectQueryContext bindUriParameters(Set<String> names,
			SelectQueryContext queryContext) throws MissingParametersException {
		for (String name : names) {
			queryContext = queryContext.bindVariableToUri(name,
					getOneParameter(name));
		}
		return queryContext;
	}

	public SelectQueryContext bindLiteralParameters(Set<String> names,
			SelectQueryContext queryContext) throws MissingParametersException {
		for (String name : names) {
			queryContext = queryContext.bindVariableToPlainLiteral(name,
					getOneParameter(name));
		}
		return queryContext;
	}

	public ConstructQueryContext bindUriParameters(Collection<String> names,
			ConstructQueryContext queryContext)
			throws MissingParametersException {
		for (String name : names) {
			queryContext = queryContext.bindVariableToUri(name,
					getOneParameter(name));
		}
		return queryContext;
	}

	public ConstructQueryContext bindLiteralParameters(Set<String> names,
			ConstructQueryContext queryContext)
			throws MissingParametersException {
		for (String name : names) {
			queryContext = queryContext.bindVariableToPlainLiteral(name,
					getOneParameter(name));
		}
		return queryContext;
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
