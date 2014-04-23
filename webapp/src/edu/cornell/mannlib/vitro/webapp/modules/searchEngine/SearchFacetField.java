/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.List;

/**
 * Holds the faceting information from a query result.
 */
public interface SearchFacetField {

	/**
	 * The name of the field that was faceted. Never null.
	 */
	String getName();

	/**
	 * The different facet values. May return an empty list, but never null.
	 */
	List<Count> getValues();

	/**
	 * Holds one facet from this field.
	 */
	public interface Count {

		/**
		 * The value of this facet. Never null.
		 */
		String getName();

		/**
		 * The number of times that the value occurs in the results.
		 */
		long getCount();

	}

}
