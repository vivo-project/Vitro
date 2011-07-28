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
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;

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
		return Actions.notNull(actions).isAuthorized(policy, ids);
	}

	/**
	 * Do the current policies authorize the current user to add all of the
	 * statements in this model?
	 */
	public static boolean isAuthorizedToAdd(HttpServletRequest req, Model model) {
		if ((req == null) || (model == null)) {
			return false;
		}

		StmtIterator stmts = model.listStatements();
		try {
			while (stmts.hasNext()) {
				if (!isAuthorizedToAdd(req, stmts.next())) {
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
	public static boolean isAuthorizedToAdd(HttpServletRequest req,
			Statement stmt) {
		if ((req == null) || (stmt == null)) {
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
			action = new AddObjectPropStmt(subject.getURI(),
					predicate.getURI(), objectNode.asResource().getURI());
		} else {
			action = new AddDataPropStmt(subject.getURI(), predicate.getURI(),
					objectNode.asLiteral());
		}
		return isAuthorizedForActions(req, action);
	}

	/**
	 * Do the current policies authorize the current user to drop all of the
	 * statements in this model?
	 */
	public static boolean isAuthorizedToDrop(HttpServletRequest req, Model model) {
		if ((req == null) || (model == null)) {
			return false;
		}

		StmtIterator stmts = model.listStatements();
		try {
			while (stmts.hasNext()) {
				if (!isAuthorizedToDrop(req, stmts.next())) {
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
	public static boolean isAuthorizedToDrop(HttpServletRequest req,
			Statement stmt) {
		if ((req == null) || (stmt == null)) {
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
			action = new DropObjectPropStmt(subject.getURI(),
					predicate.getURI(), objectNode.asResource().getURI());
		} else {
			action = new DropDataPropStmt(subject.getURI(), predicate.getURI(),
					objectNode.asLiteral());
		}
		return isAuthorizedForActions(req, action);
	}

	/**
	 * No need to instantiate this helper class - all methods are static.
	 */
	private PolicyHelper() {
		// nothing to do.
	}

}
