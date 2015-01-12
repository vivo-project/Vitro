/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.tasks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.searchindex.indexing.IndexingUriFinder;

/**
 * Ask all of the URI Finders to find URIs that might be affected by this
 * statement.
 */
public class FindUrisForStatementWorkUnit implements Runnable {
	private final Statement stmt;
	private final Collection<IndexingUriFinder> uriFinders;
	private final Set<String> uris;

	public FindUrisForStatementWorkUnit(Statement stmt,
			Collection<IndexingUriFinder> uriFinders) {
		this.stmt = stmt;
		this.uriFinders = uriFinders;
		this.uris = new HashSet<>();
	}

	@Override
	public void run() {
		for (IndexingUriFinder uriFinder : uriFinders) {
			uris.addAll(uriFinder.findAdditionalURIsToIndex(stmt));
		}
	}

	public Statement getStatement() {
		return stmt;
	}

	public Set<String> getUris() {
		return uris;
	}

}
