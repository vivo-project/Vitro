/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

/**
 * Some utility methods that help when dealing with SPARQL queries.
 */
public class SparqlQueryUtils {
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
	 * 
	 * Also escape a single quote ('), but only with a single backslash, since 
	 * this one is for the SPARQL parser itself (single quote is not a special
	 * character to REGEX).
	 */
	public static String escapeForRegex(String raw) {
		StringBuilder clean = new StringBuilder();
		outer: for (char c : raw.toCharArray()) {
			for (char special : REGEX_SPECIAL_CHARACTERS) {
				if (c == special) {
					clean.append('\\').append('\\').append(c);
					continue outer;
				} 
			}
			if (c == '\'') {
				clean.append('\\').append(c);
				continue outer;
			}
			clean.append(c);
		}
		return clean.toString();
	}

}
