/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.tasks;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.searchindex.indexing.IndexingUriFinderList;

/**
 * Ask all of the URI Finders to find URIs that might be affected by this
 * statement.
 */
public class FindUrisForStatementWorkUnit implements Runnable {
	private final Statement stmt;
	private final IndexingUriFinderList finders;
	private final Set<String> uris;

	public FindUrisForStatementWorkUnit(Statement stmt,
			IndexingUriFinderList finders) {
		this.stmt = stmt;
		this.finders = finders;
		this.uris = new HashSet<>();
	}

	@Override
	public void run() {
		uris.addAll(finders.findAdditionalUris(stmt));
	}

	public Statement getStatement() {
		return stmt;
	}

	public Set<String> getUris() {
		return uris;
	}

}
