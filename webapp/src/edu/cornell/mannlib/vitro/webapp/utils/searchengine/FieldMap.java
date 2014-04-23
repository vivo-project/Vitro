/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.searchengine;

import java.util.HashMap;
import java.util.Map;

/**
 * A builder object that can assemble a map of search result field names to JSON
 * field names.
 * 
 * Use like this:
 * 
 * m = SearchQueryUtils.fieldMap().put("this", "that").put("2nd", "row").map();
 * 
 */
public class FieldMap {
	private final Map<String, String> m = new HashMap<String, String>();

	/**
	 * Add a row to the map
	 */
	public FieldMap put(String searchResultFieldName, String jsonFieldName) {
		if (searchResultFieldName == null) {
			throw new NullPointerException(
					"searchResultFieldName may not be null.");
		}
		if (jsonFieldName == null) {
			throw new NullPointerException("jsonFieldName may not be null.");
		}
		m.put(searchResultFieldName, jsonFieldName);

		return this;
	}

	/**
	 * Release the map for use.
	 */
	public Map<String, String> map() {
		return new HashMap<String, String>(m);
	}
}
