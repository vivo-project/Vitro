/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.exclusions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;

/**
 * The basic implementation.
 */
public class SearchIndexExcluderListBasic implements SearchIndexExcluderList {
	private static final Log log = LogFactory
			.getLog(SearchIndexExcluderListBasic.class);

	private final List<SearchIndexExcluder> excluders;

	public SearchIndexExcluderListBasic(
			Collection<? extends SearchIndexExcluder> excluders) {
		this.excluders = Collections
				.unmodifiableList(new ArrayList<>(excluders));
	}

	@Override
	public void startIndexing() {
		// Nothing to do.
	}

	@Override
	public void stopIndexing() {
		// Nothing to do.
	}

	@Override
	public boolean isExcluded(Individual ind) {
		for (SearchIndexExcluder excluder : excluders) {
			String message = excluder.checkForExclusion(ind);
			if (message != SearchIndexExcluder.DONT_EXCLUDE) {
				log.debug("Excluded " + ind + " because " + message);
				return true;
			}
		}
		return false;
	}
}
