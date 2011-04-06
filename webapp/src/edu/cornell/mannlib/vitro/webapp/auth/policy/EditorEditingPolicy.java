/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.EditorEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionPolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.OntoRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.AbstractResourceAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.AddResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.DropResource;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;

/**
 * Policy to use for Vivo non-privileged but user accouunt-based editing All
 * methods in this class should be thread safe and side effect free.
 */
public class EditorEditingPolicy implements PolicyIface {

	private final ServletContext ctx;

	public EditorEditingPolicy(ServletContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * Indicates which Authorization to use when the user isn't explicitly
	 * authorized.
	 */
	private PolicyDecision defaultDecision(String message) {
		return new BasicPolicyDecision(Authorization.INCONCLUSIVE, message);
	}

	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whomToAuth,
			RequestedAction whatToAuth) {
		if (whomToAuth == null) {
			return defaultDecision("whomToAuth was null");
		}
		if (whatToAuth == null) {
			return defaultDecision("whatToAuth was null");
		}
		if (!isEditor(whomToAuth)) {
			return defaultDecision("IdBundle does not include an Editor identifier");
		}

		if (whatToAuth instanceof OntoRequestedAction) {
			return defaultDecision("EditorEditingPolicy doesn't authorize OntoRequestedActions");
		} else if (whatToAuth instanceof AdminRequestedAction) {
			return defaultDecision("EditorEditingPolicy doesn't authorize AdminRequestedActions");
		}

		if (whatToAuth instanceof AddDataPropStmt) {
			return isAuthorized((AddDataPropStmt) whatToAuth);
		} else if (whatToAuth instanceof DropDataPropStmt) {
			return isAuthorized((DropDataPropStmt) whatToAuth);
		} else if (whatToAuth instanceof EditDataPropStmt) {
			return isAuthorized((EditDataPropStmt) whatToAuth);
		} else if (whatToAuth instanceof AddObjectPropStmt) {
			return isAuthorized((AddObjectPropStmt) whatToAuth);
		} else if (whatToAuth instanceof DropObjectPropStmt) {
			return isAuthorized((DropObjectPropStmt) whatToAuth);
		} else if (whatToAuth instanceof EditObjPropStmt) {
			return isAuthorized((EditObjPropStmt) whatToAuth);
		} else if (whatToAuth instanceof AddResource) {
			return isAuthorized((AddResource) whatToAuth);
		} else if (whatToAuth instanceof DropResource) {
			return isAuthorized((DropResource) whatToAuth);
		} else {
			return defaultDecision("unrecognized requested action: "
					+ whatToAuth);
		}
	}

	private boolean isEditor(IdentifierBundle whomToAuth) {
		for (Identifier id : whomToAuth) {
			if (id instanceof EditorEditingIdentifierFactory.EditorEditingId) {
				return true;
			}
		}
		return false;
	}

	private boolean canModifyResource(String uri) {
		return PropertyRestrictionPolicyHelper.getBean(ctx).canModifyResource(
				uri, RoleLevel.EDITOR);
	}

	private boolean canModifyPredicate(String uri) {
		return PropertyRestrictionPolicyHelper.getBean(ctx).canModifyPredicate(
				uri, RoleLevel.EDITOR);
	}

	/**
	 * Check authorization for Adding, Editing or Dropping a DataProperty.
	 */
	private PolicyDecision isAuthorized(AbstractDataPropertyAction action) {
		if (!canModifyResource(action.getSubjectUri())) {
			return defaultDecision("EditorEditingPolicy does not grant access to admin resources; "
					+ "may not modify " + action.getSubjectUri());
		}

		if (!canModifyPredicate(action.getPredicateUri())) {
			return defaultDecision("EditorEditingPolicy does not grant access to admin predicates; "
					+ "may not modify " + action.getPredicateUri());
		}

		return new BasicPolicyDecision(Authorization.AUTHORIZED,
				"EditorEditingPolicy: user may modify '"
						+ action.getSubjectUri() + "' ==> '"
						+ action.getPredicateUri() + "'");
	}

	/**
	 * Check authorization for Adding, Editing or Dropping an ObjectProperty.
	 */
	private PolicyDecision isAuthorized(AbstractObjectPropertyAction action) {
		if (!canModifyResource(action.uriOfSubject)) {
			return defaultDecision("EditorEditingPolicy does not grant access to admin resources; "
					+ "may not modify " + action.uriOfSubject);
		}

		if (!canModifyPredicate(action.uriOfPredicate)) {
			return defaultDecision("EditorEditingPolicy does not grant access to admin predicates; "
					+ "may not modify " + action.uriOfPredicate);
		}

		if (!canModifyResource(action.uriOfObject)) {
			return defaultDecision("EditorEditingPolicy does not grant access to admin resources; "
					+ "may not modify " + action.uriOfObject);
		}

		return new BasicPolicyDecision(Authorization.AUTHORIZED,
				"EditorEditingPolicy: user may modify '" + action.uriOfSubject
						+ "' ==> '" + action.uriOfPredicate + "' ==> '"
						+ action.uriOfObject + "'");
	}

	/**
	 * Check authorization for Adding or Dropping a Resource.
	 */
	private PolicyDecision isAuthorized(AbstractResourceAction action) {
		if (!canModifyResource(action.getSubjectUri())) {
			return defaultDecision("EditorEditingPolicy does not grant access to admin resources; "
					+ "may not modify " + action.getSubjectUri());
		}

		return new BasicPolicyDecision(Authorization.AUTHORIZED,
				"EditorEditingPolicy: may add or remove resource: "
						+ action.getSubjectUri());
	}

	@Override
	public String toString() {
		return "EditorEditingPolicy - " + hashCode();
	}
}
