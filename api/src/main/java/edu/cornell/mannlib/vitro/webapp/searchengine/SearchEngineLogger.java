/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine;

import static edu.cornell.mannlib.vitro.webapp.utils.developer.Key.SEARCH_DELETIONS_ENABLE;
import static edu.cornell.mannlib.vitro.webapp.utils.developer.Key.SEARCH_ENGINE_ENABLE;
import static edu.cornell.mannlib.vitro.webapp.utils.developer.Key.SEARCH_INDEX_ENABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;
import edu.cornell.mannlib.vitro.webapp.utils.developer.loggers.StackTraceUtility;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.Formatter;

/**
 * Logging the SearchEngine, for the Developer's panel.
 */
public abstract class SearchEngineLogger implements AutoCloseable {
	private static final Log log = LogFactory.getLog(SearchEngineLogger.class);

	// ----------------------------------------------------------------------
	// Factory
	// ----------------------------------------------------------------------

	public static SearchEngineLogger doAdd(SearchInputDocument[] docs) {
		if (isEnabled(SEARCH_INDEX_ENABLE)) {
			return new AddLogger(Arrays.asList(docs));
		} else {
			return new DisabledLogger();
		}
	}

	public static SearchEngineLogger doAdd(Collection<SearchInputDocument> docs) {
		if (isEnabled(SEARCH_INDEX_ENABLE)) {
			return new AddLogger(docs);
		} else {
			return new DisabledLogger();
		}
	}

	public static SearchEngineLogger doDeleteById(String[] ids) {
		if (isEnabled(SEARCH_DELETIONS_ENABLE)) {
			return new DeleteIdsLogger(Arrays.asList(ids));
		} else {
			return new DisabledLogger();
		}
	}

	public static SearchEngineLogger doDeleteById(Collection<String> ids) {
		if (isEnabled(SEARCH_DELETIONS_ENABLE)) {
			return new DeleteIdsLogger(ids);
		} else {
			return new DisabledLogger();
		}
	}

	public static SearchEngineLogger doDeleteByQuery(String query) {
		if (isEnabled(SEARCH_DELETIONS_ENABLE)) {
			return new DeleteQueryLogger(query);
		} else {
			return new DisabledLogger();
		}
	}

	public static SearchEngineLogger doQuery(SearchQuery query) {
		if (isEnabled(SEARCH_ENGINE_ENABLE)) {
			return new QueryLogger(query);
		} else {
			return new DisabledLogger();
		}
	}

	public static SearchEngineLogger doCountQuery() {
		if (isEnabled(SEARCH_ENGINE_ENABLE)) {
			return new CountQueryLogger();
		} else {
			return new DisabledLogger();
		}
	}

	private static boolean isEnabled(Key enableKey) {
		return log.isInfoEnabled()
				&& DeveloperSettings.getInstance().getBoolean(enableKey);
	}

	// ----------------------------------------------------------------------
	// Abstract instance
	// ----------------------------------------------------------------------

	private final long startTime;

	public SearchEngineLogger() {
		this.startTime = System.currentTimeMillis();
	}

	protected float elapsedSeconds() {
		long endTime = System.currentTimeMillis();
		return (endTime - startTime) / 1000.0F;
	}

	@Override
	public void close() {
		try {
			writeToLog();
		} catch (Exception e) {
			log.error("Failed to write log record", e);
		}
	}

	@SuppressWarnings("unused")
	public void setSearchResponse(SearchResponse response) {
		throw new UnsupportedOperationException(this.getClass().getSimpleName()
				+ " does not support setSearchResponse()");
	}

	protected abstract void writeToLog();

	// ----------------------------------------------------------------------
	// Concrete sub-classes
	// ----------------------------------------------------------------------

	private static class AddLogger extends SearchEngineLogger {
		private final List<SearchInputDocument> docs;
		private final boolean passesRestrictions;

		AddLogger(Collection<SearchInputDocument> docs) {
			this.docs = restrictDocsByUriOrName(docs);
			this.passesRestrictions = passesDocumentRestriction()
					&& this.docs.size() > 0;
		}

		private List<SearchInputDocument> restrictDocsByUriOrName(
				Collection<SearchInputDocument> rawDocs) {
			String restriction = DeveloperSettings.getInstance().getString(
					Key.SEARCH_INDEX_URI_OR_NAME_RESTRICTION);
			if (restriction.isEmpty()) {
				return new ArrayList<>(rawDocs);
			}

			List<SearchInputDocument> list = new ArrayList<>();
			for (SearchInputDocument doc : rawDocs) {
				if (passesUriOrNameRestriction(doc, restriction)) {
					list.add(doc);
				}
			}
			return list;
		}

		private boolean passesUriOrNameRestriction(SearchInputDocument doc,
				String restriction) {
			try {
				return Pattern.matches(restriction, Formatter.format(doc));
			} catch (Exception e) {
				log.warn("Failed to test URI or Name restriction: '"
						+ restriction + "'", e);
				return true;
			}
		}

		private boolean passesDocumentRestriction() {
			String restriction = DeveloperSettings.getInstance().getString(
					Key.SEARCH_INDEX_DOCUMENT_RESTRICTION);
			if (!restriction.isEmpty()) {
				try {
					return Pattern.matches(restriction, docContents());
				} catch (Exception e) {
					log.warn("Failed to test document restriction: '"
							+ restriction + "'", e);
				}
			}
			return true;
		}

		@Override
		public void writeToLog() {
			if (!passesRestrictions) {
				return;
			}
			if (showDocumentContents()) {
				log.info(String.format("%8.3f added %d documents: \n%s",
						elapsedSeconds(), docs.size(), docContents()));
			} else {
				log.info(String.format("%8.3f added %d documents: \n%s",
						elapsedSeconds(), docs.size(), docUris()));
			}
		}

		private boolean showDocumentContents() {
			return DeveloperSettings.getInstance().getBoolean(
					Key.SEARCH_INDEX_SHOW_DOCUMENTS);
		}

		private String docUris() {
			StringBuilder sb = new StringBuilder();
			for (SearchInputDocument doc : docs) {
				sb.append(Formatter.getValueFromField(doc, "URI"))
						.append(" - ")
						.append(Formatter.getValueFromField(doc, "nameRaw"))
						.append("\n");
			}
			return sb.toString();
		}

		private String docContents() {
			StringBuilder sb = new StringBuilder();
			for (SearchInputDocument doc : docs) {
				sb.append(Formatter.format(doc));
			}
			return sb.toString();
		}

	}

	private static class DeleteIdsLogger extends SearchEngineLogger {
		private final List<String> ids;

		DeleteIdsLogger(Collection<String> ids) {
			this.ids = new ArrayList<>(ids);
		}

		@Override
		public void writeToLog() {
			log.info(String.format(
					"%8.3f deleted these %d search documents: %s",
					elapsedSeconds(), ids.size(), StringUtils.join(ids, ", ")));
		}
	}

	private static class DeleteQueryLogger extends SearchEngineLogger {
		private final String query;

		DeleteQueryLogger(String query) {
			this.query = query;
		}

		@Override
		public void writeToLog() {
			log.info(String.format(
					"%8.3f delete documents as found by this query: %s\n",
					elapsedSeconds(), query));
		}
	}

	public static class QueryLogger extends SearchEngineLogger {
		private final SearchQuery query;
		private final StackTraceUtility stackTrace;
		private final boolean passesRestrictions;

		private SearchResponse response;

		QueryLogger(SearchQuery query) {
			this.query = query;
			this.stackTrace = new StackTraceUtility(
					InstrumentedSearchEngineWrapper.class, true);
			this.passesRestrictions = passesQueryRestriction()
					&& passesStackRestriction();
			log.debug("QueryLogger: query=" + query + ", passes="
					+ passesRestrictions);
		}

		private boolean passesStackRestriction() {
			return stackTrace.passesStackRestriction(DeveloperSettings
					.getInstance().getString(
							Key.SEARCH_ENGINE_STACK_RESTRICTION));
		}

		private boolean passesQueryRestriction() {
			String restriction = DeveloperSettings.getInstance().getString(
					Key.SEARCH_ENGINE_QUERY_RESTRICTION);
			if (StringUtils.isEmpty(restriction)) {
				return true;
			}
			try {
				return Pattern.matches(restriction, Formatter.format(query));
			} catch (Exception e) {
				log.warn("Failed to test query restriction: '" + restriction
						+ "'", e);
				return true;
			}
		}

		@Override
		public void setSearchResponse(SearchResponse response) {
			this.response = response;
		}

		@Override
		public void writeToLog() {
			if (!passesRestrictions) {
				return;
			}
			String results = (showSearchResults()) ? Formatter.format(response)
					: "   returned " + response.getResults().size()
							+ " results.\n";
			String trace = stackTrace.format(showStackTrace());
			log.info(String.format("%8.3f %s%s%s", elapsedSeconds(),
					Formatter.format(query), results, trace));
		}

		private boolean showSearchResults() {
			return DeveloperSettings.getInstance().getBoolean(
					Key.SEARCH_ENGINE_ADD_RESULTS);
		}

		private boolean showStackTrace() {
			return DeveloperSettings.getInstance().getBoolean(
					Key.SEARCH_ENGINE_ADD_STACK_TRACE);
		}
	}

	public static class CountQueryLogger extends SearchEngineLogger {
		private final StackTraceUtility stackTrace;
		private final boolean passesRestrictions;

		private long count;

		CountQueryLogger() {
			this.stackTrace = new StackTraceUtility(
					InstrumentedSearchEngineWrapper.class, true);
			this.passesRestrictions = passesQueryRestriction()
					&& passesStackRestriction();
			log.debug("CountQueryLogger: passes=" + passesRestrictions);
		}

		private boolean passesStackRestriction() {
			return stackTrace.passesStackRestriction(DeveloperSettings
					.getInstance().getString(
							Key.SEARCH_ENGINE_STACK_RESTRICTION));
		}

		/** Only passes if there is no restriction. */
		private boolean passesQueryRestriction() {
			String restriction = DeveloperSettings.getInstance().getString(
					Key.SEARCH_ENGINE_QUERY_RESTRICTION);
			return StringUtils.isEmpty(restriction);
		}

		@Override
		public void setSearchResponse(SearchResponse response) {
			this.count = response.getResults().getNumFound();
		}

		@Override
		public void writeToLog() {
			if (!passesRestrictions) {
				return;
			}
			String results = "Document count query found " + count + " documents.\n";
			String trace = stackTrace.format(showStackTrace());
			log.info(String.format("%8.3f %s%s", elapsedSeconds(), results,
					trace));
		}

		private boolean showStackTrace() {
			return DeveloperSettings.getInstance().getBoolean(
					Key.SEARCH_ENGINE_ADD_STACK_TRACE);
		}
	}

	private static class DisabledLogger extends SearchEngineLogger {
		@Override
		public void setSearchResponse(SearchResponse response) {
			// Does nothing.
		}

		@Override
		protected void writeToLog() {
			// Does nothing.
		}
	}
}
