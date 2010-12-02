/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory.SelfEditing;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.AbstractResourceAction;

/**
 * Policy to use for Vivo Self-Editing based on NetId for use at Cornell. All
 * methods in this class should be thread safe and side effect free.
 */
public class SelfEditingPolicy implements PolicyIface {
	protected static Log log = LogFactory.getLog(SelfEditingPolicy.class);

	protected final OntModel model;
	private final AdministrativeUriRestrictor restrictor;

	public SelfEditingPolicy(Set<String> prohibitedProperties,
			Set<String> prohibitedResources, Set<String> prohibitedNamespaces,
			Set<String> editableVitroUris, OntModel model) {
		this.model = model;
		this.restrictor = new AdministrativeUriRestrictor(prohibitedProperties,
				prohibitedResources, prohibitedNamespaces, editableVitroUris);
	}

	private static final Authorization DEFAULT_AUTHORIZATION = Authorization.INCONCLUSIVE;

	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {
		if (whoToAuth == null) {
			return defaultDecision("whoToAuth was null");
		}
		if (whatToAuth == null) {
			return defaultDecision("whatToAuth was null");
		}

		List<String> userUris = getUrisOfSelfEditor(whoToAuth);

		if (userUris.isEmpty()) {
			return defaultDecision("Not self-editing.");
		}

		if (whatToAuth instanceof AbstractObjectPropertyAction) {
			return isAuthorizedForObjectPropertyAction(userUris,
					(AbstractObjectPropertyAction) whatToAuth);
		}
		
		if (whatToAuth instanceof AbstractDataPropertyAction) {
			return isAuthorizedForDataPropertyAction(userUris,
					(AbstractDataPropertyAction) whatToAuth);
		}
		
		if (whatToAuth instanceof AbstractResourceAction) {
			return isAuthorizedForResourceAction((AbstractResourceAction) whatToAuth);
		}

		return defaultDecision("Does not authorize "
				+ whatToAuth.getClass().getSimpleName() + " actions");
	}

	/**
	 * The user can edit a object property if it is not restricted and if it is
	 * about him.
	 */
	private PolicyDecision isAuthorizedForObjectPropertyAction(
			List<String> userUris, AbstractObjectPropertyAction action) {
		String subject = action.getUriOfSubject();
		String predicate = action.getUriOfPredicate();
		String object = action.getUriOfObject();

		if (!restrictor.canModifyResource(subject)) {
			return cantModifyResource(subject);
		}
		if (!restrictor.canModifyPredicate(predicate)) {
			return cantModifyPredicate(predicate);
		}
		if (!restrictor.canModifyResource(object)) {
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
			List<String> userUris, AbstractDataPropertyAction action) {
		String subject = action.getSubjectUri();
		String predicate = action.getPredicateUri();

		if (!restrictor.canModifyResource(subject)) {
			return cantModifyResource(subject);
		}
		if (!restrictor.canModifyPredicate(predicate)) {
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
		if (!restrictor.canModifyResource(uri)) {
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

	private List<String> getUrisOfSelfEditor(IdentifierBundle ids) {
		List<String> uris = new ArrayList<String>();
		if (ids != null) {
			for (Identifier id : ids) {
				if (id instanceof SelfEditing) {
					SelfEditing selfEditId = (SelfEditing) id;
					if (selfEditId.getBlacklisted() == null) {
						uris.add(selfEditId.getValue());
					}
				}
			}
		}
		return uris;
	}

	protected PolicyDecision cantModifyResource(String uri) {
		return defaultDecision("No access to admin resources; cannot modify "
				+ uri);
	}

	protected PolicyDecision cantModifyPredicate(String uri) {
		return defaultDecision("No access to admin predicates; cannot modify "
				+ uri);
	}

	private PolicyDecision userNotAuthorizedToStatement() {
		return defaultDecision("User has no access to this statement.");
	}

	private PolicyDecision defaultDecision(String message) {
		return new BasicPolicyDecision(DEFAULT_AUTHORIZATION,
				"SelfEditingPolicy: " + message);
	}

	private PolicyDecision authorizedDecision(String message) {
		return new BasicPolicyDecision(Authorization.AUTHORIZED,
				"SelfEditingPolicy: " + message);
	}

	@Override
	public String toString() {
		return "SelfEditingPolicy " + hashCode() + "[" + restrictor + "]";
	}

}
