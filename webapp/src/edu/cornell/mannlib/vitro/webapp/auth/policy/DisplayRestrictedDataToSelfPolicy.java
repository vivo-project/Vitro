/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasAssociatedIndividual;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionPolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display.DisplayObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/**
 * Permit display of various data if it relates to the user's associated
 * individual.
 * 
 * This policy is only to handle the case where a user would not be able to see
 * data except for their self-editing status. If the data would be visible
 * without that status, we assume that some other policy will grant access.
 */
public class DisplayRestrictedDataToSelfPolicy implements PolicyIface {
	private static final Log log = LogFactory
			.getLog(DisplayRestrictedDataToSelfPolicy.class);

	private final ServletContext ctx;

	public DisplayRestrictedDataToSelfPolicy(ServletContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * If the requested action is to display a property or a property statement,
	 * we might authorize it based on their role level.
	 */
	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {
		if (whoToAuth == null) {
			return defaultDecision("whomToAuth was null");
		}
		if (whatToAuth == null) {
			return defaultDecision("whatToAuth was null");
		}

		Collection<String> associated = HasAssociatedIndividual
				.getIndividualUris(whoToAuth);
		if (associated.isEmpty()) {
			return defaultDecision("not self-editing for anyone");
		}

		if (whatToAuth instanceof DisplayDataProperty) {
			return defaultDecision("DataProperties have no associated 'self'");
		} else if (whatToAuth instanceof DisplayObjectProperty) {
			return defaultDecision("ObjectProperties have no associated 'self'");
		}

		PolicyDecision result;
		if (whatToAuth instanceof DisplayDataPropertyStatement) {
			result = isAuthorized((DisplayDataPropertyStatement) whatToAuth,
					associated);
		} else if (whatToAuth instanceof DisplayObjectPropertyStatement) {
			result = isAuthorized((DisplayObjectPropertyStatement) whatToAuth,
					associated);
		} else {
			result = defaultDecision("Unrecognized action");
		}

		log.debug("decision for '" + whatToAuth + "' is " + result);
		return result;
	}

	/**
	 * The user may see this data property statement if the subject and the
	 * predicate are both viewable by self-editors, and the subject is one of
	 * the associated individuals (the "selves").
	 */
	private PolicyDecision isAuthorized(DisplayDataPropertyStatement action,
			Collection<String> individuals) {
		DataPropertyStatement stmt = action.getDataPropertyStatement();
		String subjectUri = stmt.getIndividualURI();
		String predicateUri = stmt.getDatapropURI();
		if (canDisplayResource(subjectUri) && canDisplayPredicate(predicateUri)
				&& isAboutAssociatedIndividual(individuals, stmt)) {
			return authorized("user may view DataPropertyStatement "
					+ subjectUri + " ==> " + predicateUri);
		} else {
			return defaultDecision("user may not view DataPropertyStatement "
					+ subjectUri + " ==> " + predicateUri);
		}
	}

	/**
	 * The user may see this data property statement if the subject, the
	 * predicate, and the object are all viewable by self-editors, and either
	 * the subject or the object is one of the associated individuals (the
	 * "selves").
	 */
	private PolicyDecision isAuthorized(DisplayObjectPropertyStatement action,
			Collection<String> individuals) {
		ObjectPropertyStatement stmt = action.getObjectPropertyStatement();
		String subjectUri = stmt.getSubjectURI();
		String predicateUri = stmt.getPropertyURI();
		String objectUri = stmt.getObjectURI();
		if (canDisplayResource(subjectUri) && canDisplayPredicate(predicateUri)
				&& canDisplayResource(objectUri)
				&& isAboutAssociatedIndividual(individuals, stmt)) {
			return authorized("user may view ObjectPropertyStatement "
					+ subjectUri + " ==> " + predicateUri + " ==> " + objectUri);
		} else {
			return defaultDecision("user may not view ObjectPropertyStatement "
					+ subjectUri + " ==> " + predicateUri + " ==> " + objectUri);
		}
	}

	/** If the user is explicitly authorized, return this. */
	private PolicyDecision authorized(String message) {
		String className = this.getClass().getSimpleName();
		return new BasicPolicyDecision(Authorization.AUTHORIZED, className
				+ ": " + message);
	}

	/** If the user isn't explicitly authorized, return this. */
	private PolicyDecision defaultDecision(String message) {
		return new BasicPolicyDecision(Authorization.INCONCLUSIVE, message);
	}

	private boolean canDisplayResource(String uri) {
		return PropertyRestrictionPolicyHelper.getBean(ctx).canDisplayResource(
				uri, RoleLevel.SELF);
	}

	private boolean canDisplayPredicate(String uri) {
		return PropertyRestrictionPolicyHelper.getBean(ctx)
				.canDisplayPredicate(uri, RoleLevel.SELF);
	}

	private boolean isAboutAssociatedIndividual(Collection<String> selves,
			DataPropertyStatement stmt) {
		for (String self : selves) {
			if (self.equals(stmt.getIndividualURI())) {
				return true;
			}
		}
		return false;
	}

	private boolean isAboutAssociatedIndividual(Collection<String> selves,
			ObjectPropertyStatement stmt) {
		for (String self : selves) {
			if (self.equals(stmt.getSubjectURI())
					|| self.equals(stmt.getObjectURI())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " - " + hashCode();
	}

}