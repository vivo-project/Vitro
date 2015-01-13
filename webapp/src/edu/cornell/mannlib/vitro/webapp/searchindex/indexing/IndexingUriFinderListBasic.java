/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * The basic implementation.
 */
public class IndexingUriFinderListBasic implements IndexingUriFinderList {
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
			uris.addAll(uriFinder.findAdditionalURIsToIndex(stmt));
		}
		return uris;
	}

}
