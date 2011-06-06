/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasRoleLevel;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionPolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyAction;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;

/**
 * Permit adding, editing, or deleting of various data based on the user's Role
 * level and the restrictions in the ontology.
 * 
 * This policy only authorizes users who are Editors, Curators or DBAs.
 * Self-editors and users who are not logged in must look elsewhere for
 * authorization.
 */
public class EditRestrictedDataByRoleLevelPolicy implements PolicyIface {
	private static final Log log = LogFactory
			.getLog(EditRestrictedDataByRoleLevelPolicy.class);

	private final ServletContext ctx;

	public EditRestrictedDataByRoleLevelPolicy(ServletContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * If the requested action is to edit a property statement, we might
	 * authorize it based on their role level.
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

		RoleLevel userRole = HasRoleLevel.getUsersRoleLevel(whoToAuth);
		if (!userRoleIsHighEnough(userRole)) {
			return defaultDecision("insufficient role level: " + userRole);
		}

		PolicyDecision result;
		if (whatToAuth instanceof AbstractDataPropertyAction) {
			result = isAuthorized((AbstractDataPropertyAction) whatToAuth,
					userRole);
		} else if (whatToAuth instanceof AbstractObjectPropertyAction) {
			result = isAuthorized((AbstractObjectPropertyAction) whatToAuth,
					userRole);
		} else {
			result = defaultDecision("Unrecognized action");
		}

		log.debug("whoToAuth: " + whoToAuth);
		log.debug("decision for '" + whatToAuth + "' is " + result);
		return result;
	}

	/**
	 * We only consider Editors, Curators and DBAs.
	 */
	private boolean userRoleIsHighEnough(RoleLevel userRole) {
		return (userRole == RoleLevel.EDITOR)
				|| (userRole == RoleLevel.CURATOR)
				|| (userRole == RoleLevel.DB_ADMIN);
	}

	/**
	 * The user may add, edit, or delete this data property if they are allowed
	 * to modify its subject and its predicate.
	 */
	private PolicyDecision isAuthorized(AbstractDataPropertyAction action,
			RoleLevel userRole) {
		String subjectUri = action.getSubjectUri();
		String predicateUri = action.getPredicateUri();
		if (canModifyResource(subjectUri, userRole)
				&& canModifyPredicate(predicateUri, userRole)) {
			return authorized("user may modify DataPropertyStatement "
					+ subjectUri + " ==> " + predicateUri);
		} else {
			return defaultDecision("user may not modify DataPropertyStatement "
					+ subjectUri + " ==> " + predicateUri);
		}
	}

	/**
	 * The user may add, edit, or delete this data property if they are allowed
	 * to modify its subject, its predicate, and its object.
	 */
	private PolicyDecision isAuthorized(AbstractObjectPropertyAction action,
			RoleLevel userRole) {
		String subjectUri = action.getUriOfSubject();
		String predicateUri = action.getUriOfPredicate();
		String objectUri = action.getUriOfObject();
		if (canModifyResource(subjectUri, userRole)
				&& canModifyPredicate(predicateUri, userRole)
				&& canModifyResource(objectUri, userRole)) {
			return authorized("user may modify ObjectPropertyStatement "
					+ subjectUri + " ==> " + predicateUri + " ==> " + objectUri);
		} else {
			return defaultDecision("user may not modify ObjectPropertyStatement "
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

	private boolean canModifyResource(String uri, RoleLevel userRole) {
		return PropertyRestrictionPolicyHelper.getBean(ctx).canModifyResource(
				uri, userRole);
	}

	private boolean canModifyPredicate(String uri, RoleLevel userRole) {
		return PropertyRestrictionPolicyHelper.getBean(ctx).canModifyPredicate(
				uri, userRole);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " - " + hashCode();
	}
}
