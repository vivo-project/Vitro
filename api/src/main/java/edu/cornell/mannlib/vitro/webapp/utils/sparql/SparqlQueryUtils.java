/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparql;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;

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
	 * A list of SPARQL syntaxes to try when parsing queries
	 */
    public static final List<Syntax> SUPPORTED_SYNTAXES = Arrays.asList(
            Syntax.syntaxARQ , Syntax.syntaxSPARQL_11);

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

	/**
	 * A convenience method to attempt parsing a query string with various syntaxes
	 * @param queryString Query String
	 * @return Query
	 */
	public static Query create(String queryString) {
	    boolean parseSuccess = false;
        Iterator<Syntax> syntaxIt = SUPPORTED_SYNTAXES.iterator();
        Query query = null;
        while (!parseSuccess && syntaxIt.hasNext()) {
            Syntax syntax = syntaxIt.next();
            try {
                query = QueryFactory.create(queryString, syntax);
                parseSuccess = true;
            } catch (QueryParseException e) {
                if (!syntaxIt.hasNext()) {
                    throw e;
                }
            }
        }
        return query;
	}

}
