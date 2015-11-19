/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.searchengine;

import java.util.Map;

/**
 * This can be used to filter the results of the search query.
 */
public interface SearchResponseFilter {
	boolean accept(Map<String, String> map);
}
