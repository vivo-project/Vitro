/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.developer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Keys for the DeveloperSettings. Each key holds these values:
 * 
 * A property name, which specifies the key in developer.properties, like
 * "this.thatThing"
 * 
 * A flag to say whether this key controls a boolean value. If false, then the
 * value is a string.
 * 
 * We can derive the element ID for each key by replacing the periods in the
 * property name with underscores.
 */
public enum Key {
	/**
	 * Developer mode and developer panel is enabled.
	 */
	ENABLED("developer.enabled", true),

	/**
	 * If the developer panel is enabled, may an anonymous user change the
	 * settings?
	 */
	PERMIT_ANONYMOUS_CONTROL("developer.permitAnonymousControl", true),

	/**
	 * Load Freemarker templates every time they are requested.
	 */
	DEFEAT_FREEMARKER_CACHE("developer.defeatFreemarkerCache", true),

	/**
	 * Show where each Freemarker template starts and stops.
	 */
	INSERT_FREEMARKER_DELIMITERS("developer.insertFreemarkerDelimiters", true),

	/**
	 * Load language property files every time they are requested.
	 */
	I18N_DEFEAT_CACHE("developer.i18n.defeatCache", true),

	/**
	 * Enable the I18nLogger to log each string request.
	 */
	I18N_LOG_STRINGS("developer.i18n.logStringRequests", true),

	/**
	 * Enable the LoggingRDFService
	 */
	LOGGING_RDF_ENABLE("developer.loggingRDFService.enable", true),

	/**
	 * When logging with the LoggingRDFService, include a stack trace
	 */
	LOGGING_RDF_STACK_TRACE("developer.loggingRDFService.stackTrace", true),

	/**
	 * Don't log with the LoggingRDFService unless the calling stack meets this
	 * restriction.
	 */
	LOGGING_RDF_QUERY_RESTRICTION(
			"developer.loggingRDFService.queryRestriction", false),

	/**
	 * Don't log with the LoggingRDFService unless the calling stack meets this
	 * restriction.
	 */
	LOGGING_RDF_STACK_RESTRICTION(
			"developer.loggingRDFService.stackRestriction", false),

	/**
	 * Tell the CustomListViewLogger to note the use of non-default custom list
	 * views.
	 */
	PAGE_CONTENTS_LOG_CUSTOM_LIST_VIEW(
			"developer.pageContents.logCustomListView", true),

	/**
	 * Tell the ShortViewLogger to note the use of non-default short views.
	 */
	PAGE_CONTENTS_LOG_CUSTOM_SHORT_VIEW(
			"developer.pageContents.logCustomShortView", true),

	/**
	 * Tell the SearchEngineLogger to log all indexing operations.
	 */
	SEARCH_INDEX_ENABLE("developer.searchIndex.enable", true),

	/**
	 * Add the document contents to the log of indexing operations.
	 */
	SEARCH_INDEX_SHOW_DOCUMENTS("developer.searchIndex.showDocuments", true),

	/**
	 * Log indexing operations only if one of the document identifiers match
	 * this regular expression.
	 */
	SEARCH_INDEX_URI_OR_NAME_RESTRICTION(
			"developer.searchIndex.uriOrNameRestriction", false),

	/**
	 * Log indexing operations only if the document contents match this regular
	 * expression.
	 */
	SEARCH_INDEX_DOCUMENT_RESTRICTION(
			"developer.searchIndex.documentRestriction", false),

	/**
	 * Accumulate breakdown timings for search indexing, and log them at the end
	 * of the indexing operation.
	 */
	SEARCH_INDEX_LOG_INDEXING_BREAKDOWN_TIMINGS(
			"developer.searchIndex.logIndexingBreakdownTimings", true),

	/**
	 * If set, don't pass model change events to the search indexer.
	 */
	SEARCH_INDEX_SUPPRESS_MODEL_CHANGE_LISTENER(
			"developer.searchIndex.suppressModelChangeListener", true),

	/**
	 * Tell the SearchEngineLogger to log all index deletions.
	 */
	SEARCH_DELETIONS_ENABLE("developer.searchDeletions.enable", true),

	/**
	 * Tell the SearchEngineLogger to log all search operations.
	 */
	SEARCH_ENGINE_ENABLE("developer.searchEngine.enable", true),

	/**
	 * Add the stack trace to the log of search operations.
	 */
	SEARCH_ENGINE_ADD_STACK_TRACE("developer.searchEngine.addStackTrace", true),

	/**
	 * Add the search results to the log of search operations.
	 */
	SEARCH_ENGINE_ADD_RESULTS("developer.searchEngine.addResults", true),

	/**
	 * Log search operations only if the query matches this regular expression.
	 */
	SEARCH_ENGINE_QUERY_RESTRICTION("developer.searchEngine.queryRestriction",
			false),

	/**
	 * Log search operations only if the stack trace matches this regular
	 * expression.
	 */
	SEARCH_ENGINE_STACK_RESTRICTION("developer.searchEngine.stackRestriction",
			false),

	/**
	 * Enable the PolicyDecisionLogger.
	 */
	AUTHORIZATION_LOG_DECISIONS_ENABLE(
			"developer.authorization.logDecisions.enable", true),

	/**
	 * When logging policy decisions, add the identifier bundle.
	 */
	AUTHORIZATION_LOG_DECISIONS_ADD_IDENTIFERS(
			"developer.authorization.logDecisions.addIdentifiers", true),

	/**
	 * Don't log policy decisions if the decision is INCONCLUSIVE.
	 */
	AUTHORIZATION_LOG_DECISIONS_SKIP_INCONCLUSIVE(
			"developer.authorization.logDecisions.skipInconclusive", true),

	/**
	 * Don't log policy decisions unless the requested action meets this
	 * restriction.
	 */
	AUTHORIZATION_LOG_DECISIONS_ACTION_RESTRICTION(
			"developer.authorization.logDecisions.actionRestriction", false),

	/**
	 * Don't log policy decisions unless the identifier bundle meets this
	 * restriction.
	 */
	AUTHORIZATION_LOG_DECISIONS_USER_RESTRICTION(
			"developer.authorization.logDecisions.userRestriction", false),

	/**
	 * Don't log policy decisions unless the policy meets this restriction.
	 */
	AUTHORIZATION_LOG_DECISIONS_POLICY_RESTRICTION(
			"developer.authorization.logDecisions.policyRestriction", false);

	private static final Log log = LogFactory.getLog(Key.class);
	private final String propertyName;
	private final boolean bool;

	// TODO create a private enum for KeyType to clarify these constructors.
	private Key(String propertyName, boolean bool) {
		this.propertyName = propertyName;
		this.bool = bool;
	}

	public String propertyName() {
		return propertyName;
	}

	public String elementId() {
		return propertyName.replace('.', '_');
	}

	boolean isBoolean() {
		return bool;
	}

	@Override
	public String toString() {
		return propertyName;
	}

	static Key fromElementId(String id) {
		return fromPropertyName(id.replace('_', '.'));
	}

	static Key fromPropertyName(String name) {
		for (Key k : Key.values()) {
			if (k.propertyName.equals(name)) {
				return k;
			}
		}
		log.error("Can't find key for property name: '" + name + "'");
		return null;
	}

}