/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.Collection;
import java.util.Map;

/**
 * The concrete representation of a document in the search index. Obtained in
 * response to a query.
 */
public interface SearchResultDocument {

	/**
	 * A document identifier that can be used in SearchEngine.deleteById();
	 * Never null.
	 */
	public String getUniqueId();

	/**
	 * May return an empty collection, but never null.
	 */
	public Collection<String> getFieldNames();

	/**
	 * May return null.
	 */
	public Object getFirstValue(String name);

	/**
	 * Gets the first value for the named field, and converts it to a String.
	 * May return null.
	 */
	public String getStringValue(String name);

	/**
	 * Get the values for the named field. May return an empty collection, but
	 * never null.
	 */
	public Collection<Object> getFieldValues(String name);

	/**
	 * May return an empty map, but never null. The values collection for any
	 * key may be empty, but never null.
	 */
	public Map<String, Collection<Object>> getFieldValuesMap();
}
