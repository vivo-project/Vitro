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
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.AddNewUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.LoadOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RemoveUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UpdateTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UploadFile;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.OntoRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.CreateOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.RemoveOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.AddResource;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource.DropResource;

/**
 * Policy to use for Vivo Self-Editing based on NetId for use at Cornell. All
 * methods in this class should be thread safe and side effect free.
 */
public class SelfEditingPolicy implements VisitingPolicyIface {
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
		if (whatToAuth instanceof OntoRequestedAction) {
			return defaultDecision("Won't authorize OntoRequestedActions");
		}
		if (whatToAuth instanceof AdminRequestedAction) {
			return defaultDecision("Won't authorize AdminRequestedActions");
		}
		if (getUrisOfSelfEditor(whoToAuth).isEmpty()) {
			return defaultDecision("no non-blacklisted SelfEditing Identifier "
					+ "found in IdentifierBundle");
		}

		// kick off the visitor pattern
		return whatToAuth.accept(this, whoToAuth);
	}

	// ----------------------------------------------------------------------
	// Visitor methods.
	// ----------------------------------------------------------------------

	public PolicyDecision visit(IdentifierBundle ids, AddResource action) {
		PolicyDecision pd = checkNullArguments(ids, action);
		if (pd == null)
			pd = checkRestrictedResource(action.getSubjectUri());
		if (pd == null)
			pd = authorizedDecision("May add resource.");
		return pd;
	}

	public PolicyDecision visit(IdentifierBundle ids, DropResource action) {
		PolicyDecision pd = checkNullArguments(ids, action);
		if (pd == null)
			pd = checkRestrictedResource(action.getSubjectUri());
		if (pd == null)
			pd = authorizedDecision("May remove resource.");
		return pd;
	}

	public PolicyDecision visit(IdentifierBundle ids, AddObjectPropStmt action) {
		PolicyDecision pd = checkNullArguments(ids, action);
		if (pd == null)
			pd = checkRestrictedResource(action.uriOfSubject);
		if (pd == null)
			pd = checkRestrictedResource(action.uriOfObject);
		if (pd == null)
			pd = checkRestrictedPredicate(action.uriOfPredicate);
		if (pd == null)
			pd = checkUserEditsAsSubjectOrObjectOfStmt(ids,
					action.uriOfSubject, action.uriOfObject);
		if (pd == null)
			pd = defaultDecision("No basis for decision.");
		return pd;
	}

	public PolicyDecision visit(IdentifierBundle ids, EditObjPropStmt action) {
		PolicyDecision pd = checkNullArguments(ids, action);
		if (pd == null)
			pd = checkRestrictedResource(action.uriOfSubject);
		if (pd == null)
			pd = checkRestrictedResource(action.uriOfObject);
		if (pd == null)
			pd = checkRestrictedPredicate(action.uriOfPredicate);
		if (pd == null)
			pd = checkUserEditsAsSubjectOrObjectOfStmt(ids,
					action.uriOfSubject, action.uriOfObject);
		if (pd == null)
			pd = defaultDecision("No basis for decision.");
		return pd;
	}

	public PolicyDecision visit(IdentifierBundle ids, DropObjectPropStmt action) {
		PolicyDecision pd = checkNullArguments(ids, action);
		if (pd == null)
			pd = checkRestrictedResource(action.uriOfSubject);
		if (pd == null)
			pd = checkRestrictedResource(action.uriOfObject);
		if (pd == null)
			pd = checkRestrictedPredicate(action.uriOfPredicate);
		if (pd == null)
			pd = checkUserEditsAsSubjectOrObjectOfStmt(ids,
					action.uriOfSubject, action.uriOfObject);
		if (pd == null)
			pd = defaultDecision("No basis for decision.");
		return pd;
	}

	public PolicyDecision visit(IdentifierBundle ids, AddDataPropStmt action) {
		PolicyDecision pd = checkNullArguments(ids, action);
		if (pd == null)
			pd = checkRestrictedResource(action.getSubjectUri());
		if (pd == null)
			pd = checkRestrictedPredicate(action.getPredicateUri());
		if (pd == null)
			pd = checkUserEditsAsSubjectOfStmt(ids, action.getSubjectUri());
		if (pd == null)
			pd = defaultDecision("No basis for decision.");
		return pd;
	}

	public PolicyDecision visit(IdentifierBundle ids, EditDataPropStmt action) {
		PolicyDecision pd = checkNullArguments(ids, action);
		if (pd == null)
			pd = checkRestrictedResource(action.getSubjectUri());
		if (pd == null)
			pd = checkRestrictedPredicate(action.getPredicateUri());
		if (pd == null)
			pd = checkUserEditsAsSubjectOfStmt(ids, action.getSubjectUri());
		if (pd == null)
			pd = defaultDecision("No basis for decision.");
		return pd;
	}

	public PolicyDecision visit(IdentifierBundle ids, DropDataPropStmt action) {
		PolicyDecision pd = checkNullArguments(ids, action);
		if (pd == null)
			pd = checkRestrictedResource(action.getSubjectUri());
		if (pd == null)
			pd = checkRestrictedPredicate(action.getPredicateUri());
		if (pd == null)
			pd = checkUserEditsAsSubjectOfStmt(ids, action.getSubjectUri());
		if (pd == null)
			pd = defaultDecision("No basis for decision.");
		return pd;
	}

	public PolicyDecision visit(IdentifierBundle ids, AddNewUser action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids, RemoveUser action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids, LoadOntology action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids, RebuildTextIndex action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids, UpdateTextIndex action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids, ServerStatus action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids, CreateOwlClass action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids, RemoveOwlClass action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids, DefineDataProperty action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids,
			DefineObjectProperty action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	public PolicyDecision visit(IdentifierBundle ids, UploadFile action) {
		return defaultDecision("does not authorize administrative modifications");
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private PolicyDecision checkNullArguments(IdentifierBundle ids,
			RequestedAction action) {
		if (ids == null || action == null) {
			return defaultDecision("Null action or ids.");
		}
		return null;
	}

	private PolicyDecision checkRestrictedResource(String uri) {
		if (!restrictor.canModifyResource(uri)) {
			return defaultDecision("No access to admin resources; "
					+ "cannot modify " + uri);
		}
		return null;
	}

	private PolicyDecision checkRestrictedPredicate(String uri) {
		if (!restrictor.canModifyPredicate(uri)) {
			return defaultDecision("No access to admin predicates; "
					+ "cannot modify " + uri);
		}
		return null;
	}

	private PolicyDecision checkUserEditsAsSubjectOfStmt(IdentifierBundle ids,
			String uriOfSubject) {
		List<String> userUris = getUrisOfSelfEditor(ids);
		for (String userUri : userUris) {
			if (userUri.equals(uriOfSubject)) {
				return authorizedDecision("User is subject of statement.");
			}
		}
		return null;
	}

	private PolicyDecision checkUserEditsAsSubjectOrObjectOfStmt(
			IdentifierBundle ids, String uriOfSubject, String uriOfObject) {
		List<String> userUris = getUrisOfSelfEditor(ids);
		for (String userUri : userUris) {
			if (userUri.equals(uriOfSubject)) {
				return authorizedDecision("User is subject of statement.");
			}
			if (userUri.equals(uriOfObject)) {
				return authorizedDecision("User is subject of statement.");
			}
		}
		return null;
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
