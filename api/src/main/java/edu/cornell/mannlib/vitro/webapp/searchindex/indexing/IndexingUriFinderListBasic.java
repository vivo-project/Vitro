/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Statement;

/**
 * The basic implementation.
 */
public class IndexingUriFinderListBasic implements IndexingUriFinderList {
	private static final Log log = LogFactory
			.getLog(IndexingUriFinderListBasic.class);

	private final List<IndexingUriFinder> finders;

	public IndexingUriFinderListBasic(
			Collection<? extends IndexingUriFinder> finders) {
		this.finders = Collections.synchronizedList(new ArrayList<>(finders));
	}

	@Override
	public void startIndexing() {
		for (IndexingUriFinder finder : finders) {
			finder.startIndexing();
		}
	}

	@Override
	public void stopIndexing() {
		for (IndexingUriFinder finder : finders) {
			finder.endIndexing();
		}
	}

	@Override
	public Set<String> findAdditionalUris(Statement stmt) {
		Set<String> uris = new HashSet<>();
		for (IndexingUriFinder uriFinder : finders) {
			List<String> additions = uriFinder.findAdditionalURIsToIndex(stmt);
			if (log.isDebugEnabled() && !additions.isEmpty()) {
				log.debug(uriFinder + " found " + additions.size()
						+ " additions " + additions + " for this statement "
						+ stmt);
			}
			for (String addition : additions) {
				if (addition == null) {
					log.warn("Finder " + uriFinder + " returned a null URI.");
				} else {
					uris.add(addition);
				}
			}
		}
		return uris;
	}

}
