/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyStatementAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyStatementAction;

/**
 * Test the function of PolicyHelper in authorizing statements and models.
 */
public class PolicyHelper_StatementsTest extends AbstractTestClass {
	private static final String APPROVED_SUBJECT_URI = "test://approvedSubjectUri";
	private static final String APPROVED_PREDICATE_URI = "test://approvedPredicateUri";
	private static final String UNAPPROVED_PREDICATE_URI = "test://bogusPredicateUri";
	private static final String APPROVED_OBJECT_URI = "test://approvedObjectUri";

	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;
	private OntModel ontModel;

	@Before
	public void setup() {
		ctx = new ServletContextStub();

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);

		ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		setLoggerLevel(ServletPolicyList.class, Level.WARN);
		ServletPolicyList.addPolicy(ctx, new MySimplePolicy());
	}

	// ----------------------------------------------------------------------
	// The tests.
	// ----------------------------------------------------------------------

	@Test
	public void addNullStatement() {
		assertEquals("null statement", false,
				PolicyHelper.isAuthorizedToAdd(req, null, ontModel));
	}

	@Test
	public void addStatementWithNullRequest() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("null request", false,
				PolicyHelper.isAuthorizedToAdd(null, stmt, ontModel));
	}

	@Test
	public void addStatementToNullModel() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", false,
				PolicyHelper.isAuthorizedToAdd(req, stmt, null));
	}

	@Test
	public void addAuthorizedDataStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToAdd(req, stmt, ontModel));
	}

	@Test
	public void addAuthorizedObjectStatement() {
		Statement stmt = objectStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI, APPROVED_OBJECT_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToAdd(req, stmt, ontModel));
	}

	@Test
	public void addUnauthorizedDataStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToAdd(req, stmt, ontModel));
	}

	@Test
	public void addUnauthorizedObjectStatement() {
		Statement stmt = objectStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI, APPROVED_OBJECT_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToAdd(req, stmt, ontModel));
	}

	@Test
	public void dropNullStatement() {
		assertEquals("null statement", false, PolicyHelper.isAuthorizedToDrop(
				req, (Statement) null, ontModel));
	}

	@Test
	public void dropStatementWithNullRequest() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("null request", false,
				PolicyHelper.isAuthorizedToDrop(null, stmt, ontModel));
	}

	@Test
	public void dropStatementFromNullModel() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", false,
				PolicyHelper.isAuthorizedToDrop(req, stmt, null));
	}

	@Test
	public void dropAuthorizedDataStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToDrop(req, stmt, ontModel));
	}

	@Test
	public void dropAuthorizedObjectStatement() {
		Statement stmt = objectStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI, APPROVED_OBJECT_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToDrop(req, stmt, ontModel));
	}

	@Test
	public void dropUnauthorizedDataStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToDrop(req, stmt, ontModel));
	}

	@Test
	public void dropUnauthorizedObjectStatement() {
		Statement stmt = objectStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI, APPROVED_OBJECT_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToDrop(req, stmt, ontModel));
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/** Build a data statement. */
	private Statement dataStatement(String subjectUri, String predicateUri) {
		Resource subject = ontModel.createResource(subjectUri);
		Property predicate = ontModel.createProperty(predicateUri);
		return ontModel.createStatement(subject, predicate, "whoCares?");
	}

	/** Build a object statement. */
	private Statement objectStatement(String subjectUri, String predicateUri,
			String objectUri) {
		Resource subject = ontModel.createResource(subjectUri);
		Resource object = ontModel.createResource(objectUri);
		Property predicate = ontModel.createProperty(predicateUri);
		return ontModel.createStatement(subject, predicate, object);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class MySimplePolicy implements PolicyIface {
		@Override
		public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
				RequestedAction whatToAuth) {
			if (whatToAuth instanceof AbstractDataPropertyStatementAction) {
				return isAuthorized((AbstractDataPropertyStatementAction) whatToAuth);
			} else if (whatToAuth instanceof AbstractObjectPropertyStatementAction) {
				return isAuthorized((AbstractObjectPropertyStatementAction) whatToAuth);
			} else {
				return inconclusive();
			}
		}

		private PolicyDecision isAuthorized(
				AbstractDataPropertyStatementAction whatToAuth) {
			if ((APPROVED_SUBJECT_URI.equals(whatToAuth.getSubjectUri()))
					&& (APPROVED_PREDICATE_URI.equals(whatToAuth
							.getPredicateUri()))) {
				return authorized();
			} else {
				return inconclusive();
			}
		}

		private PolicyDecision isAuthorized(
				AbstractObjectPropertyStatementAction whatToAuth) {
			if ((APPROVED_SUBJECT_URI.equals(whatToAuth.getSubjectUri()))
					&& (APPROVED_PREDICATE_URI.equals(whatToAuth
							.getPredicateUri()))
					&& (APPROVED_OBJECT_URI.equals(whatToAuth.getObjectUri()))) {
				return authorized();
			} else {
				return inconclusive();
			}
		}

		private PolicyDecision authorized() {
			return new BasicPolicyDecision(Authorization.AUTHORIZED, "");
		}

		private PolicyDecision inconclusive() {
			return new BasicPolicyDecision(Authorization.INCONCLUSIVE, "");
		}
	}

}
