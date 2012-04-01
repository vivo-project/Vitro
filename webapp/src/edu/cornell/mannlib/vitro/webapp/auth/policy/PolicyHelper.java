/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * A collection of static methods to help determine whether requested actions
 * are authorized by current policy.
 */
public class PolicyHelper {
	private static final Log log = LogFactory.getLog(PolicyHelper.class);

	/**
	 * Are these actions authorized for the current user by the current
	 * policies?
	 */
	public static boolean isAuthorizedForActions(HttpServletRequest req,
			RequestedAction... actions) {
		return isAuthorizedForActions(req, new Actions(actions));
	}

	/**
	 * Are these actions authorized for the current user by the current
	 * policies?
	 */
	public static boolean isAuthorizedForActions(HttpServletRequest req,
			Actions actions) {
		PolicyIface policy = ServletPolicyList.getPolicies(req);
		IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(req);
		return isAuthorizedForActions(ids, policy, actions);
	}

	/**
	 * Are these actions authorized for these identifiers by these policies?
	 */
	public static boolean isAuthorizedForActions(IdentifierBundle ids,
			PolicyIface policy, Actions actions) {
		return Actions.notNull(actions).isAuthorized(policy, ids);
	}

	/**
	 * Do the current policies authorize the current user to add all of the
	 * statements in this model?
	 */
	public static boolean isAuthorizedToAdd(VitroRequest vreq, Model model) {
		if ((vreq == null) || (model == null)) {
			return false;
		}

		StmtIterator stmts = model.listStatements();
		try {
			while (stmts.hasNext()) {
				if (!isAuthorizedToAdd(vreq, stmts.next())) {
					return false;
				}
			}
			return true;
		} finally {
			stmts.close();
		}
	}

	/**
	 * Do the current policies authorize the current user to add this statement?
	 * 
	 * The statement is expected to be fully-populated, with no null fields.
	 */
	public static boolean isAuthorizedToAdd(VitroRequest vreq, Statement stmt) {
		if ((vreq == null) || (stmt == null)) {
			return false;
		}

		Resource subject = stmt.getSubject();
		Property predicate = stmt.getPredicate();
		RDFNode objectNode = stmt.getObject();
		if ((subject == null) || (predicate == null) || (objectNode == null)) {
			return false;
		}

		RequestedAction action;
		if (objectNode.isResource()) {
			action = new AddObjectPropertyStatement(vreq.getJenaOntModel(),
					subject.getURI(), predicate.getURI(), objectNode
							.asResource().getURI());
		} else {
			action = new AddDataPropertyStatement(vreq.getJenaOntModel(),
					subject.getURI(), predicate.getURI());
		}
		return isAuthorizedForActions(vreq, action);
	}

	/**
	 * Do the current policies authorize the current user to drop all of the
	 * statements in this model?
	 */
	public static boolean isAuthorizedToDrop(VitroRequest vreq, Model model) {
		if ((vreq == null) || (model == null)) {
			return false;
		}

		StmtIterator stmts = model.listStatements();
		try {
			while (stmts.hasNext()) {
				if (!isAuthorizedToDrop(vreq, stmts.next())) {
					return false;
				}
			}
			return true;
		} finally {
			stmts.close();
		}
	}

	/**
	 * Do the current policies authorize the current user to drop this
	 * statement?
	 * 
	 * The statement is expected to be fully-populated, with no null fields.
	 */
	public static boolean isAuthorizedToDrop(VitroRequest vreq, Statement stmt) {
		if ((vreq == null) || (stmt == null)) {
			return false;
		}

		Resource subject = stmt.getSubject();
		Property predicate = stmt.getPredicate();
		RDFNode objectNode = stmt.getObject();
		if ((subject == null) || (predicate == null) || (objectNode == null)) {
			return false;
		}

		RequestedAction action;
		if (objectNode.isResource()) {
			action = new DropObjectPropertyStatement(vreq.getJenaOntModel(),
					subject.getURI(), predicate.getURI(), objectNode
							.asResource().getURI());
		} else {
			action = new DropDataPropertyStatement(vreq.getJenaOntModel(),
					subject.getURI(), predicate.getURI());
		}
		return isAuthorizedForActions(vreq, action);
	}

	/**
	 * No need to instantiate this helper class - all methods are static.
	 */
	private PolicyHelper() {
		// nothing to do.
	}

}
