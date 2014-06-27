/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization.INCONCLUSIVE;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;

/**
 * If enabled in the developer settings (and log levels), log each
 * PolicyDecision (subject to restrictions).
 * 
 * Some restrictions apply to the logger as a whole. Others apply to the
 * particular policy or the particular decision.
 */
public class PolicyDecisionLogger {
	private static final Log log = LogFactory
			.getLog(PolicyDecisionLogger.class);

	private static final Pattern NEVER_MATCHES = Pattern.compile("^__NEVER__$");

	private static final BasicPolicyDecision NULL_DECISION = new BasicPolicyDecision(
			INCONCLUSIVE, "The decision was null.");

	private final DeveloperSettings settings;
	private final RequestedAction whatToAuth;
	private final IdentifierBundle whoToAuth;

	private final boolean enabled;

	private final Pattern policyRestriction;
	private final boolean skipInconclusive;
	private final boolean includeIdentifiers;

	public PolicyDecisionLogger(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {
		this.settings = DeveloperSettings.getInstance();
		this.whoToAuth = whoToAuth;
		this.whatToAuth = whatToAuth;

		this.enabled = figureEnabled();

		this.policyRestriction = figurePolicyRestriction();
		this.skipInconclusive = figureSkipInconclusive();
		this.includeIdentifiers = figureIncludeIdentifiers();
	}

	private boolean figureEnabled() {
		return log.isInfoEnabled()
				&& settings.getBoolean(Key.AUTHORIZATION_LOG_DECISIONS_ENABLE)
				&& passesUserRestriction() && passesActionRestriction();
	}

	/**
	 * The identifier bundle passes if there is no restriction, or if the
	 * restriction pattern is found within concatenated string of the identifier
	 * bundle.
	 * 
	 * If the restriction is invalid, the action fails.
	 */
	private boolean passesUserRestriction() {
		Pattern userRestriction = compilePatternFromSetting(Key.AUTHORIZATION_LOG_DECISIONS_USER_RESTRICTION);
		return userRestriction == null
				|| userRestriction.matcher(String.valueOf(whoToAuth)).find();
	}

	/**
	 * The requested action passes if there is no restriction, or if the
	 * restriction pattern is found within the class name of the action.
	 * 
	 * If the restriction is invalid, the action fails.
	 */
	private boolean passesActionRestriction() {
		Pattern actionRestriction = compilePatternFromSetting(Key.AUTHORIZATION_LOG_DECISIONS_ACTION_RESTRICTION);
		return actionRestriction == null
				|| actionRestriction.matcher(String.valueOf(whatToAuth)).find();
	}

	/**
	 * Only compile the policy restriction pattern once.
	 */
	private Pattern figurePolicyRestriction() {
		return compilePatternFromSetting(Key.AUTHORIZATION_LOG_DECISIONS_POLICY_RESTRICTION);
	}

	/**
	 * Do we log inconclusive decisions?
	 */
	private boolean figureSkipInconclusive() {
		return settings
				.getBoolean(Key.AUTHORIZATION_LOG_DECISIONS_SKIP_INCONCLUSIVE);
	}

	/**
	 * Do we include Identifiers in the log record?
	 */
	private boolean figureIncludeIdentifiers() {
		return settings
				.getBoolean(Key.AUTHORIZATION_LOG_DECISIONS_ADD_IDENTIFERS);
	}

	/**
	 * If no pattern was provided, return null. If an invalid pattern was
	 * provided, return a pattern that never matches.
	 */
	private Pattern compilePatternFromSetting(Key key) {
		String setting = settings.getString(key);
		if (setting.isEmpty()) {
			return null;
		} else {
			try {
				return Pattern.compile(setting);
			} catch (Exception e) {
				return NEVER_MATCHES;
			}
		}
	}

	/**
	 * If the logger and the policy and the decision all pass the restrictions,
	 * write to the log. A null decision is treated as inconclusive.
	 */
	public void log(PolicyIface policy, PolicyDecision pd) {
		if (passesRestrictions(String.valueOf(policy), pd)) {
			if (this.includeIdentifiers) {
				log.info(String.format(
						"Decision on %s by %s was %s; user is %s",
						this.whatToAuth, policy, pd, this.whoToAuth));
			} else {
				log.info(String.format("Decision on %s by %s was %s",
						this.whatToAuth, policy, pd));
			}
		}
	}

	private boolean passesRestrictions(String policyString, PolicyDecision pd) {
		if (pd == null) {
			pd = NULL_DECISION;
		}
		return enabled && passesPolicyRestriction(policyString)
				&& passesConclusiveRestriction(pd);
	}

	private boolean passesPolicyRestriction(String policyString) {
		return this.policyRestriction == null
				|| this.policyRestriction.matcher(policyString).find();
	}

	private boolean passesConclusiveRestriction(PolicyDecision pd) {
		return !(skipInconclusive && isInconclusive(pd));
	}

	private boolean isInconclusive(PolicyDecision pd) {
		return pd == null || pd.getAuthorized() == INCONCLUSIVE;
	}

	public void logNoDecision(PolicyDecision pd) {
		if (enabled) {
			if (this.includeIdentifiers) {
				log.info(pd.getMessage() + "; user is " + this.whoToAuth);
			} else {
				log.info(pd.getMessage());
			}
		}
	}
}
