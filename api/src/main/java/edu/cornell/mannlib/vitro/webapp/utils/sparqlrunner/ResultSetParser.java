/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Define the interface for parsing a result set, and provide some helpful
 * methods as well.
 */
public abstract class ResultSetParser<T> {
	protected abstract T parseResults(String queryStr, ResultSet results);

	protected abstract T defaultValue();

	protected String ifResourcePresent(QuerySolution solution,
			String variableName, String defaultValue) {
		RDFNode node = solution.get(variableName);
		if (node == null || !node.isURIResource()) {
			return defaultValue;
		}
		return node.asResource().getURI();
	}

	protected String ifLiteralPresent(QuerySolution solution,
			String variableName, String defaultValue) {
		Literal literal = solution.getLiteral(variableName);
		if (literal == null) {
			return defaultValue;
		} else {
			return literal.getString();
		}
	}

	protected long ifLongPresent(QuerySolution solution, String variableName,
			long defaultValue) {
		Literal literal = solution.getLiteral(variableName);
		if (literal == null) {
			return defaultValue;
		} else {
			return literal.getLong();
		}
	}

	protected int ifIntPresent(QuerySolution solution, String variableName,
			int defaultValue) {
		Literal literal = solution.getLiteral(variableName);
		if (literal == null) {
			return defaultValue;
		} else {
			return literal.getInt();
		}
	}
}
