/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.solr;

import java.util.Map;

/**
 * This can be used to filter the results of the Solr query.
 */
public interface SolrResponseFilter {
	boolean accept(Map<String, String> map);
}
