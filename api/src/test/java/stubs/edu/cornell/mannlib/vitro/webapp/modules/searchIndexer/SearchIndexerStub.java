/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.modules.searchIndexer;

import java.util.Collection;
import java.util.List;

import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus;

/**
 * TODO
 */
public class SearchIndexerStub implements SearchIndexer {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------
	
	private boolean paused = true;

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public void pause() {
		paused = true;
	}

    @Override
	public void unpause() {
		paused = false;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		throw new RuntimeException(
				"SearchIndexerStub.startup() not implemented.");
	}

	@Override
	public void scheduleUpdatesForStatements(List<Statement> changes) {
		throw new RuntimeException(
				"SearchIndexerStub.scheduleUpdatesForStatements() not implemented.");
	}

	@Override
	public void scheduleUpdatesForUris(Collection<String> uris) {
		throw new RuntimeException(
				"SearchIndexerStub.scheduleUpdatesForUris() not implemented.");
	}

	@Override
	public void rebuildIndex() {
		throw new RuntimeException(
				"SearchIndexerStub.rebuildIndex() not implemented.");
	}

	@Override
	public SearchIndexerStatus getStatus() {
		throw new RuntimeException(
				"SearchIndexerStub.getStatus() not implemented.");
	}

	@Override
	public void addListener(Listener listener) {
		throw new RuntimeException(
				"SearchIndexerStub.addListener() not implemented.");
	}

	@Override
	public void removeListener(Listener listener) {
		throw new RuntimeException(
				"SearchIndexerStub.removeListener() not implemented.");
	}

	@Override
	public void shutdown(Application app) {
		throw new RuntimeException(
				"SearchIndexerStub.shutdown() not implemented.");
	}

}
