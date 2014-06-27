/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasAssociatedIndividual;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyStatementAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyStatementAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.AbstractResourceAction;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * Policy to use for Vivo Self-Editing based on NetId for use at Cornell. All
 * methods in this class should be thread safe and side effect free.
 */
public class SelfEditingPolicy extends BaseSelfEditingPolicy implements
		PolicyIface {
	public SelfEditingPolicy(ServletContext ctx) {
		super(ctx, RoleLevel.SELF);
	}

	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {
		if (whoToAuth == null) {
			return inconclusiveDecision("whoToAuth was null");
		}
		if (whatToAuth == null) {
			return inconclusiveDecision("whatToAuth was null");
		}

		List<String> userUris = new ArrayList<String>(
				HasAssociatedIndividual.getIndividualUris(whoToAuth));

		if (userUris.isEmpty()) {
			return inconclusiveDecision("Not self-editing.");
		}

		if (whatToAuth instanceof AbstractObjectPropertyStatementAction) {
			return isAuthorizedForObjectPropertyAction(userUris,
					(AbstractObjectPropertyStatementAction) whatToAuth);
		}

		if (whatToAuth instanceof AbstractDataPropertyStatementAction) {
			return isAuthorizedForDataPropertyAction(userUris,
					(AbstractDataPropertyStatementAction) whatToAuth);
		}

		if (whatToAuth instanceof AbstractResourceAction) {
			return isAuthorizedForResourceAction((AbstractResourceAction) whatToAuth);
		}

		return inconclusiveDecision("Does not authorize "
				+ whatToAuth.getClass().getSimpleName() + " actions");
	}

	/**
	 * The user can edit a object property if it is not restricted and if it is
	 * about him.
	 */
	private PolicyDecision isAuthorizedForObjectPropertyAction(
			List<String> userUris, AbstractObjectPropertyStatementAction action) {
		String subject = action.getSubjectUri();
		Property predicate = action.getPredicate();
		String object = action.getObjectUri();

		if (!canModifyResource(subject)) {
			return cantModifyResource(subject);
		}
		if (!canModifyPredicate(predicate)) {
			return cantModifyPredicate(predicate);
		}
		if (!canModifyResource(object)) {
			return cantModifyResource(object);
		}

		if (userCanEditAsSubjectOrObjectOfStmt(userUris, subject, object)) {
			return authorizedDecision("User is subject or object of statement.");
		} else {
			return userNotAuthorizedToStatement();
		}
	}

	/**
	 * The user can edit a data property if it is not restricted and if it is
	 * about him.
	 */
	private PolicyDecision isAuthorizedForDataPropertyAction(
			List<String> userUris, AbstractDataPropertyStatementAction action) {
		String subject = action.getSubjectUri();
		Property predicate = action.getPredicate();

		if (!canModifyResource(subject)) {
			return cantModifyResource(subject);
		}
		if (!canModifyPredicate(predicate)) {
			return cantModifyPredicate(predicate);
		}

		if (userCanEditAsSubjectOfStmt(userUris, subject)) {
			return authorizedDecision("User is subject of statement.");
		} else {
			return userNotAuthorizedToStatement();
		}
	}

	/**
	 * The user can add or remove resources if they are not restricted.
	 */
	private PolicyDecision isAuthorizedForResourceAction(
			AbstractResourceAction action) {
		String uri = action.getSubjectUri();
		if (!canModifyResource(uri)) {
			return cantModifyResource(uri);
		} else {
			return authorizedDecision("May add/remove resource.");
		}
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private boolean userCanEditAsSubjectOfStmt(List<String> userUris,
			String subject) {
		for (String userUri : userUris) {
			if (userUri.equals(subject)) {
				return true;
			}
		}
		return false;
	}

	private boolean userCanEditAsSubjectOrObjectOfStmt(List<String> userUris,
			String subject, String object) {
		for (String userUri : userUris) {
			if (userUri.equals(subject)) {
				return true;
			}
			if (userUri.equals(object)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "SelfEditingPolicy - " + hashCode();
	}

}
