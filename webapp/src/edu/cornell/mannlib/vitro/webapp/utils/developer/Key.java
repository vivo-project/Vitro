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
	 * Enable the PolicyDecisionLogger.
	 */
	AUTHORIZATION_LOG_DECISIONS_ENABLE(
			"developer.authorization.logDecisions.enable", true),

	/**
	 * Enable the PolicyDecisionLogger.
	 */
	AUTHORIZATION_LOG_DECISIONS_ADD_IDENTIFERS(
			"developer.authorization.logDecisions.addIdentifiers", true),

	/**
	 * Enable the PolicyDecisionLogger.
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