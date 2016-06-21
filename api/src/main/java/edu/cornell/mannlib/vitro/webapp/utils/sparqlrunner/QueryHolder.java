/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Holds the text of a SPARQL query, and allows you to perform some lexical
 * operations on it.
 * 
 * This is immutable, so don't forget to get the result of the operations.
 */
public class QueryHolder {
	private final String queryString;

	public QueryHolder(String queryString) {
		this.queryString = Objects.requireNonNull(queryString);
	}

	public String getQueryString() {
		return queryString;
	}

	public boolean hasVariable(String name) {
		String regex = "\\?" + name + "\\b";
		return Pattern.compile(regex).matcher(queryString).find();
	}

	public QueryHolder bindToUri(String name, String uri) {
		String regex = "\\?" + name + "\\b";
		String replacement = "<" + uri + ">";
		String bound = queryString.replaceAll(regex, replacement);
		return new QueryHolder(bound);
	}

	public QueryHolder bindToPlainLiteral(String name, String value) {
		String regex = "\\?" + name + "\\b";
		String replacement = '"' + value + '"';
		String bound = queryString.replaceAll(regex, replacement);
		return new QueryHolder(bound);
	}

	@Override
	public int hashCode() {
		return queryString.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		QueryHolder that = (QueryHolder) obj;
		return Objects.equals(this.queryString, that.queryString);
	}

	@Override
	public String toString() {
		return "QueryHolder[" + queryString + "]";
	}
}
