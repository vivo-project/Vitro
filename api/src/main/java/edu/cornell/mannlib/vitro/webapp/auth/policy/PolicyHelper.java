/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject.SOME_URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasAssociatedIndividual;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IdentifierPermissionSetProvider;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.DataPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.ObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest.WRAP_TYPE;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * A collection of static methods to help determine whether requested actions
 * are authorized by current policy.
 */
public class PolicyHelper {
	private static final Log log = LogFactory.getLog(PolicyHelper.class);

	public static boolean isAuthorizedForActions(HttpServletRequest req, AccessObject ar, AccessOperation operation) {
		IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(req);
		return actionRequestIsAuthorized(ids, ar, operation);
	}
	
    public static boolean isAuthorizedForActions(IdentifierBundle ids, AccessObject ar, AccessOperation op) {
        return actionRequestIsAuthorized(ids, ar, op);
    }
    
    public static boolean isAuthorizedForActions(IdentifierBundle ids, AuthorizationRequest ar) {
        if (ar == null) {
            log.error("AuthorizationRequest is null");
            return false;
        }
        if (ar.getPredefinedDecision() != DecisionResult.INCONCLUSIVE){
            return ar.getPredefinedDecision() == DecisionResult.AUTHORIZED;
        }
        if (ar.getWrapType() != null) {
            return processUnwrappedAuthorizationRequest(ar, ids);
        }
        return actionRequestIsAuthorized(ids, ar.getAccessObject(), ar.getAccessOperation());
    }

    public static boolean isAuthorizedForActions(HttpServletRequest req, AuthorizationRequest ar) {
        if (ar == null) {
            log.error("AuthorizationRequest is null");
            return false;
        }
        if (ar.getPredefinedDecision() != DecisionResult.INCONCLUSIVE){
            return ar.getPredefinedDecision() == DecisionResult.AUTHORIZED;
        }
        IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(req);
        if (ar.getWrapType() != null) {
            return processUnwrappedAuthorizationRequest(ar, ids);
        }
        return actionRequestIsAuthorized(ids, ar.getAccessObject(), ar.getAccessOperation());
    }

    private static boolean processUnwrappedAuthorizationRequest(AuthorizationRequest ar, IdentifierBundle ids) {
        List<AuthorizationRequest> items = ar.getItems();
        boolean result = false;
        if (WRAP_TYPE.OR == ar.getWrapType()) {
            for (AuthorizationRequest item : items ) {
                result = result || isAuthorizedForActions(ids, item);
            }    
        } else {
            result = true;
            for (AuthorizationRequest item : items ) {
                result = result && isAuthorizedForActions(ids, item);
            } 
        }
        return result;
    }

	private static boolean actionRequestIsAuthorized(IdentifierBundle ids, AccessObject ao, AccessOperation operation) {
	    if (operation == null) {
	        log.error("Opeartion is null, accessObject " + ao );
	        return false;
	    }
        if (ao == null) {
            log.error("Access object is null, operation " + operation);
            return false;
        }
        AuthorizationRequest ar = new SimpleAuthorizationRequest(ao, operation);
        Collection<String> uris = IdentifierPermissionSetProvider.getPermissionSetUris(ids);
        if (uris.isEmpty()) {
            uris.add(VitroVocabulary.VITRO_AUTH + "PUBLIC");
        }
        ar.setRoleUris(new ArrayList<String>(uris));
        
        ar.setIds(ids);
        ar.setEditorUris(new ArrayList<String>(HasAssociatedIndividual.getIndividualUris(ids)));
	    PolicyDecision decision = PolicyDecisionPoint.decide(ar);
	    debug(ar, decision);
        return decision.getDecisionResult() == DecisionResult.AUTHORIZED;
	}

	private static void debug(AuthorizationRequest ar, PolicyDecision decision) {
	    if (true) {//log.isDebugEnabled()
	        AccessObject ao = ar.getAccessObject();
            log.error(String.format("Request for %s on object %s resulted in decision %s", ar.getAccessOperation(), ao, decision.getDecisionResult()));
	        if (ao == null) {
	            Throwable t = new Throwable();
                log.error(t, t);
	        }
	    }
    }

    /**
	 * Is the email/password authorized for these actions? This should be used
	 * when a controller or something needs allow actions if the user passes in
	 * their email and password.
	 *
	 * It may be better to check this as part of a servlet Filter and add an
	 * identifier bundle.
	 */
	public static boolean isAuthorizedForActions(HttpServletRequest req, String email, String password, AuthorizationRequest ar) {
		if (password == null || email == null || password.isEmpty()
				|| email.isEmpty()) {
			return false;
		}

		try {
			Authenticator auth = Authenticator.getInstance(req);
			UserAccount user = auth.getAccountForInternalAuth(email);
			if (user == null) {
				log.debug("No account for '" + email + "'");
				return false;
			}

			String uri = user.getUri();
			log.debug("userAccount is '" + uri + "'");

			if (!auth.isCurrentPasswordArgon2(user, password)) {
				log.debug(String.format("UNAUTHORIZED, password not accepted "
						+ "for %s, account URI: %s", email, uri));
				return false;
			}
			log.debug(String.format("password accepted for %s, "
					+ "account URI: %s", email, uri));

			// figure out if that account can do the actions
			IdentifierBundle ids = ActiveIdentifierBundleFactories.getUserIdentifierBundle(user);
			return isAuthorizedForActions(ids, ar);
		} catch (Exception ex) {
			log.error("Error while attempting to authorize actions " + ar, ex);
			return false;
		}
	}

	/**
	 * Do the current policies authorize the current user to add this statement
	 * to this model?
	 *
	 * The statement is expected to be fully-populated, with no null fields.
	 */
	public static boolean isAuthorizedToAdd(HttpServletRequest req,	Statement stmt, OntModel modelToBeModified) {
		if ((req == null) || (stmt == null) || (modelToBeModified == null)) {
			return false;
		}

		Resource subject = stmt.getSubject();
		org.apache.jena.rdf.model.Property predicate = stmt.getPredicate();
		RDFNode objectNode = stmt.getObject();
		if ((subject == null) || (predicate == null) || (objectNode == null)) {
			return false;
		}

		AccessObject action;
		if (objectNode.isResource()) {
			Property property = new Property(predicate.getURI());
			property.setDomainVClassURI(SOME_URI);
			property.setRangeVClassURI(SOME_URI);
			action = new ObjectPropertyStatementAccessObject(modelToBeModified,
					subject.getURI(), property, objectNode.asResource()
							.getURI());
		} else {
			action = new DataPropertyStatementAccessObject(modelToBeModified,
					subject.getURI(), predicate.getURI(), objectNode
							.asLiteral().getString());
		}
		return isAuthorizedForActions(req, action, AccessOperation.ADD);
	}

	/**
	 * Do the current policies authorize the current user to drop this statement
	 * from this model?
	 *
	 * The statement is expected to be fully-populated, with no null fields.
	 */
	public static boolean isAuthorizedToDrop(HttpServletRequest req, Statement stmt, OntModel modelToBeModified) {
		if ((req == null) || (stmt == null) || (modelToBeModified == null)) {
			return false;
		}

		Resource subject = stmt.getSubject();
		org.apache.jena.rdf.model.Property predicate = stmt.getPredicate();
		RDFNode objectNode = stmt.getObject();
		if ((subject == null) || (predicate == null) || (objectNode == null)) {
			return false;
		}

		AccessObject action;
		if (objectNode.isResource()) {
			Property property = new Property(predicate.getURI());
			property.setDomainVClassURI(SOME_URI);
			property.setRangeVClassURI(SOME_URI);
			action = new ObjectPropertyStatementAccessObject(modelToBeModified,
					subject.getURI(), property, objectNode.asResource()
							.getURI());
		} else {
			action = new DataPropertyStatementAccessObject(modelToBeModified,
					subject.getURI(), predicate.getURI(), objectNode
							.asLiteral().getString());
		}
		return isAuthorizedForActions(req, action, AccessOperation.DROP);
	}

	/**
	 * Do the current policies authorize the current user to modify this model
	 * by adding all of the statments in the additions model and dropping all of
	 * the statements in the retractions model?
	 *
	 * This differs from the other calls to "isAuthorized..." because we always
	 * expect the answer to be true. If the answer is false, it should be logged
	 * as an error.
	 *
	 * Even if a statement fails the test, continue to test the others, so the
	 * log will contain a full record of all failures. This is no more expensive
	 * than if all statements succeeded.
	 */
	public static boolean isAuthorizedAsExpected(HttpServletRequest req, Model additions, Model retractions, OntModel modelBeingModified) {
		if (req == null) {
			log.warn("Can't evaluate authorization if req is null");
			return false;
		}
		if (additions == null) {
			log.warn("Can't evaluate authorization if additions model is null");
			return false;
		}
		if (retractions == null) {
			log.warn("Can't evaluate authorization if retractions model is null");
			return false;
		}
		if (modelBeingModified == null) {
			log.warn("Can't evaluate authorization if model being modified is null");
			return false;
		}

		/*
		 * The naive way to test the additions is to test each statement against
		 * the JenaOntModel. However, some of the statements may not be
		 * authorized unless others are added first. The client code should not
		 * need to know which sequence will be successful. The client code only
		 * cares that such a sequence does exist.
		 *
		 * There are 3 obvious ways to test this, ranging from the most rigorous
		 * (and most costly) to the least costly (and least rigorous).
		 *
		 * 1. Try all sequences to find one that works. First, try to add each
		 * statement to the modelBeingModified. If any statement succeeds,
		 * construct a temporary model that joins that statement to the
		 * modelBeingModified. Now try the remaining statements against that
		 * temporary model, adding the statement each time we are successful. If
		 * we eventually find all of the statements authorized, declare success.
		 * This is logically rigorous, but could become geometrically expensive
		 * as statements are repeatedly tried against incremented models. O(n!).
		 *
		 * 2. Try each statement on the assumption that all of the others have
		 * already been added. So for each statement we create a temporary
		 * modeol that joins the other additions to the JenaOntModel. If all
		 * statements pass this test, declare success. This is logically flawed
		 * since it is possible that two statements would circularly authorize
		 * each other, but that neither statement could be added first. However,
		 * that seems like a small risk, and the algorithm is considerably less
		 * expensive. O(n).
		 *
		 * 3. Try each statement on the assumption that all of the statements
		 * (including itself) have already been added. If all statements pass
		 * this test, declare success. This has the additional minor flaw of
		 * allowing a statement to authorize its own addition, but this seems
		 * very unlikely. This is about as expensive as choice 2., but much
		 * simpler to code.
		 *
		 * For now, I am going with choice 3.
		 */

		boolean result = true;

		OntModel modelToTestAgainst = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM);
		modelToTestAgainst.addSubModel(additions);
		modelToTestAgainst.addSubModel(modelBeingModified);

		StmtIterator addStmts = additions.listStatements();
		try {
			while (addStmts.hasNext()) {
				Statement stmt = addStmts.next();
				if (isAuthorizedToAdd(req, stmt, modelToTestAgainst)) {
					if (log.isDebugEnabled()) {
						log.debug("Last-chance authorization check: "
								+ "authorized to add statement: "
								+ formatStatement(stmt));
					}
				} else {
					log.warn("Last-chance authorization check reveals not "
							+ "authorized to add statement: "
							+ formatStatement(stmt));
					result = false;
				}
			}
		} finally {
			addStmts.close();
		}

		/*
		 * For retractions, there is no such conundrum. Assume that all of the
		 * additions have been added, and check the authorization of each
		 * retraction.
		 */

		StmtIterator dropStmts = retractions.listStatements();
		try {
			while (dropStmts.hasNext()) {
				Statement stmt = dropStmts.next();
				if (isAuthorizedToDrop(req, stmt, modelToTestAgainst)) {
					if (log.isDebugEnabled()) {
						log.debug("Last-chance authorization check: "
								+ "authorized to drop statement: "
								+ formatStatement(stmt));
					}
				} else {
					log.warn("Last-chance authorization check reveals not "
							+ "authorized to drop statement: "
							+ formatStatement(stmt));
					result = false;
				}
			}
		} finally {
			dropStmts.close();
		}

		return result;
	}

	private static String formatStatement(Statement stmt) {
		if (stmt == null) {
			return "null statement";
		}
		return "<" + stmt.getSubject() + "> <" + stmt.getPredicate() + "> <"
				+ stmt.getObject() + ">";
	}

	/**
	 * No need to instantiate this helper class - all methods are static.
	 */
	private PolicyHelper() {
		// nothing to do.
	}


}
