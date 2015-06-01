/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparql;

import java.util.regex.Pattern;

/**
 * Holds the text of a SPARQL CONSTRUCT query, and allows you to perform some lexical
 * operations on it.
 * 
 * This is immutable, so don't forget to get the result of the operations.
 */
public class ConstructQueryHolder {
	private final String queryString;

	public ConstructQueryHolder(String queryString) {
		this.queryString = queryString;
	}

	public String getQueryString() {
		return queryString;
	}

	public boolean hasVariable(String name) {
		String regex = "\\?" + name + "\\b";
		return Pattern.compile(regex).matcher(queryString).find();
	}

	public ConstructQueryHolder bindToUri(String name, String uri) {
		String regex = "\\?" + name + "\\b";
		String replacement = "<" + uri + ">";
		String bound = queryString.replaceAll(regex, replacement);
		return new ConstructQueryHolder(bound);
	}

}
