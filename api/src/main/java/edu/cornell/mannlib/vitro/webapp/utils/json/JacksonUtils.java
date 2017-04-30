/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.json;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

/**
 * Some utility methods to ease the transition from net.sf.json to Jackson.
 */
public class JacksonUtils {
	private static final String QUOTE = "\"";

	/**
	 * A "clean room" replacement for net.sf.json.util.JSONUtils.quote().
	 */
	public static String quote(String raw) {
		if (raw == null) {
			// Null string is treated like an empty string.
			return QUOTE + QUOTE;
		} else {
			return new StringBuilder(QUOTE)
					.append(JsonStringEncoder.getInstance().quoteAsString(raw))
					.append(QUOTE).toString();
		}
	}
}
