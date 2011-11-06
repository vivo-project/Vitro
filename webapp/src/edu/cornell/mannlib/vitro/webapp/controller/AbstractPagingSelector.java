/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

/**
 * A base class with some methods for building a selector.
 */
public abstract class AbstractPagingSelector {
	/**
	 * If the user enters any of these characters in a search term, escape it
	 * with a backslash.
	 */
	private static final char[] REGEX_SPECIAL_CHARACTERS = "[\\^$.|?*+()]"
			.toCharArray();


	/**
	 * Escape any regex special characters in the string.
	 * 
	 * Note that the SPARQL parser requires two backslashes, in order to pass a
	 * single backslash to the REGEX function.
	 */
	protected String escapeForRegex(String raw) {
		StringBuilder clean = new StringBuilder();
		outer: for (char c : raw.toCharArray()) {
			for (char special : REGEX_SPECIAL_CHARACTERS) {
				if (c == special) {
					clean.append('\\').append('\\').append(c);
					continue outer;
				}
			}
			clean.append(c);
		}
		return clean.toString();
	}


}
