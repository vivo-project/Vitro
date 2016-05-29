/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine;

import static edu.cornell.mannlib.vitro.webapp.modules.Application.Component.LifecycleState.ACTIVE;
import static edu.cornell.mannlib.vitro.webapp.modules.Application.Component.LifecycleState.NEW;
import static edu.cornell.mannlib.vitro.webapp.modules.Application.Component.LifecycleState.STOPPED;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Manages the life-cycle of the SearchEngine. Adds logging, controlled by
 * DeveloperSettings.
 */
public class InstrumentedSearchEngineWrapper implements SearchEngine {
	private static final Log log = LogFactory
			.getLog(InstrumentedSearchEngineWrapper.class);

	private SearchEngine innerEngine;

	private volatile LifecycleState lifecycleState = NEW;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#wraps", minOccurs = 1, maxOccurs = 1)
	public void setInnerEngine(SearchEngine inner) {
		innerEngine = inner;
	}

	/**
	 * Complain unless ACTIVE.
	 */
	private void confirmActive() {
		if (lifecycleState == NEW) {
			throw new IllegalStateException(
					"Search engine has not been started.");
		} else if (lifecycleState == STOPPED) {
			throw new IllegalStateException("Search engine has stopped.");
		}
	}

	// ----------------------------------------------------------------------
	// Overridden methods.
	// ----------------------------------------------------------------------

	/**
	 * If NEW, do startup. If STOPPED, throw an exception. If ACTIVE, just
	 * complain.
	 */
	@Override
	public void startup(Application application, ComponentStartupStatus css) {
		if (application == null) {
			throw new NullPointerException("application may not be null.");
		}
		switch (lifecycleState) {
		case NEW:
			innerEngine.startup(application, css);
			lifecycleState = ACTIVE;
			break;
		case STOPPED:
			throw new IllegalStateException(
					"startup called when already STOPPED");
		default: // ACTIVE:
			try {
				throw new IllegalStateException();
			} catch (Exception e) {
				log.warn("startup called when already ACTIVE", e);
			}
			break;
		}
	}

	/**
	 * If ACTIVE, do shutdown. Otherwise, complain and do nothing.
	 */
	@Override
	public void shutdown(Application application) {
		if (application == null) {
			throw new NullPointerException("application may not be null.");
		}
		switch (lifecycleState) {
		case ACTIVE:
			innerEngine.shutdown(application);
			lifecycleState = STOPPED;
			break;
		default: // NEW, STOPPED:
			try {
				throw new IllegalStateException();
			} catch (Exception e) {
				log.warn("shutdown called when state was " + lifecycleState, e);
			}
			break;
		}
	}

	@Override
	public void ping() throws SearchEngineException {
		confirmActive();
		innerEngine.ping();
	}

	@Override
	public SearchInputDocument createInputDocument() {
		confirmActive();
		return innerEngine.createInputDocument();
	}

	@Override
	public void add(SearchInputDocument... docs) throws SearchEngineException {
		try (SearchEngineLogger l = SearchEngineLogger.doAdd(docs)) {
			confirmActive();
			innerEngine.add(docs);
		}
	}

	@Override
	public void add(Collection<SearchInputDocument> docs)
			throws SearchEngineException {
		try (SearchEngineLogger l = SearchEngineLogger.doAdd(docs)) {
			confirmActive();
			innerEngine.add(docs);
		}
	}

	@Override
	public void commit() throws SearchEngineException {
		confirmActive();
		innerEngine.commit();
	}

	@Override
	public void commit(boolean wait) throws SearchEngineException {
		confirmActive();
		innerEngine.commit(wait);
	}

	@Override
	public void deleteById(String... ids) throws SearchEngineException {
		try (SearchEngineLogger l = SearchEngineLogger.doDeleteById(ids)) {
			confirmActive();
			innerEngine.deleteById(ids);
		}
	}

	@Override
	public void deleteById(Collection<String> ids) throws SearchEngineException {
		try (SearchEngineLogger l = SearchEngineLogger.doDeleteById(ids)) {
			confirmActive();
			innerEngine.deleteById(ids);
		}
	}

	@Override
	public void deleteByQuery(String query) throws SearchEngineException {
		try (SearchEngineLogger l = SearchEngineLogger.doDeleteByQuery(query)) {
			confirmActive();
			innerEngine.deleteByQuery(query);
		}
	}

	@Override
	public SearchQuery createQuery() {
		confirmActive();
		return innerEngine.createQuery();
	}

	@Override
	public SearchQuery createQuery(String queryText) {
		confirmActive();
		return innerEngine.createQuery(queryText);
	}

	@Override
	public SearchResponse query(SearchQuery query) throws SearchEngineException {
		try (SearchEngineLogger l = SearchEngineLogger.doQuery(query)) {
			confirmActive();
			SearchResponse response = innerEngine.query(query);
			l.setSearchResponse(response);
			return response;
		}
	}

	@Override
	public int documentCount() throws SearchEngineException {
		try (SearchEngineLogger l = SearchEngineLogger.doCountQuery()) {
			confirmActive();
			int count = innerEngine.documentCount();
			l.setSearchResponse(new SearchResponseForDocumentCount(count));
			return count;
		}
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class SearchResponseForDocumentCount implements
			SearchResponse {
		private final int count;

		public SearchResponseForDocumentCount(int count) {
			this.count = count;
		}

		@Override
		public SearchResultDocumentList getResults() {
			return new EmptyDocumentListWithCount();
		}

		@Override
		public Map<String, Map<String, List<String>>> getHighlighting() {
			return Collections.emptyMap();
		}

		@Override
		public SearchFacetField getFacetField(String name) {
			return null;
		}

		@Override
		public List<SearchFacetField> getFacetFields() {
			return Collections.emptyList();
		}

		private class EmptyDocumentListWithCount implements
				SearchResultDocumentList {
			@Override
			public Iterator<SearchResultDocument> iterator() {
				return Collections.emptyIterator();
			}

			@Override
			public int size() {
				return 0;
			}

			@Override
			public long getNumFound() {
				return count;
			}

			@Override
			public SearchResultDocument get(int i) {
				throw new ArrayIndexOutOfBoundsException(i);
			}
		}
	}

}
