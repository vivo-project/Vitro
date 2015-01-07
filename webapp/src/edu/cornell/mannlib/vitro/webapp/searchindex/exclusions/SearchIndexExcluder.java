/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.exclusions;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * Interface for classes that check to see if an individual
 * should be excluded from the search index.
 */
public interface SearchIndexExcluder {
	public static final String DONT_EXCLUDE = null;

    /**
     * REturn a string message if the individual should
     * be excluded from the index.
     * 
     * Return null if ind should not be excluded.
     */
    public String checkForExclusion(Individual ind); 
}
