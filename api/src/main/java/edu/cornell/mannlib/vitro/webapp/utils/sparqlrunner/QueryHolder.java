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
		String bound = replaceWithinBraces(regex, replacement);
		return new QueryHolder(bound);
	}

	public QueryHolder bindToPlainLiteral(String name, String value) {
		String regex = "\\?" + name + "\\b";
		String replacement = '"' + value + '"';
		String bound = replaceWithinBraces(regex, replacement);
		return new QueryHolder(bound);
	}

	private String replaceWithinBraces(String regex, String replacement) {
		int openBrace = queryString.indexOf('{');
		int closeBrace = queryString.lastIndexOf('}');
		if (openBrace == -1 || closeBrace == -1) {
			return queryString;
		} else {
			String prefix = queryString.substring(0, openBrace);
			String suffix = queryString.substring(closeBrace);
			String patterns = queryString.substring(openBrace, closeBrace);
			return prefix + patterns.replaceAll(regex, replacement) + suffix;
		}
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
