/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.exclusions;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * An ordered list of SearxchIndexExcluder objects, in a handy package.
 * 
 * Implementations should make a protective copy of the list of
 * SearxchIndexExcluders. Implementations must be thread-safe.
 * 
 * The life-cycle is:
 * 
 * <pre>
 * startIndexing(), 
 * 0 or more isExcluded() by multiple threads, 
 * stopIndexing().
 * </pre>
 */
public interface SearchIndexExcluderList {

	/**
	 * Do any required setup on the individual Excluders.
	 */
	void startIndexing();

	/**
	 * Do any required teardown on the individual Excluders.
	 */
	void stopIndexing();

	/**
	 * Poll the list of excluders regarding this individual.
	 * 
	 * If any returns non-null, the individual is excluded. If all return null,
	 * the individual is not excluded.
	 */
	boolean isExcluded(Individual ind);

}
