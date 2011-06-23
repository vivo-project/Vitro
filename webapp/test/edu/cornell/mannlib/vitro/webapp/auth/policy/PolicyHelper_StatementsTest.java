/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractDataPropertyAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractObjectPropertyAction;

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

	@Before
	public void setup() {
		ctx = new ServletContextStub();

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);

		setLoggerLevel(ServletPolicyList.class, Level.WARN);
		ServletPolicyList.addPolicy(ctx, new MySimplePolicy());
	}

	// ----------------------------------------------------------------------
	// Statement-level tests.
	// ----------------------------------------------------------------------

	@Test
	public void addNullStatement() {
		assertEquals("null statement", false,
				PolicyHelper.isAuthorizedToAdd(req, (Statement) null));
	}

	@Test
	public void addStatementWithNullRequest() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("null request", false,
				PolicyHelper.isAuthorizedToAdd(null, stmt));
	}

	@Test
	public void addAuthorizedStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToAdd(req, stmt));
	}

	@Test
	public void addUnauthorizedStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToAdd(req, stmt));
	}

	@Test
	public void dropNullStatement() {
		assertEquals("null statement", false,
				PolicyHelper.isAuthorizedToDrop(req, (Statement) null));
	}

	@Test
	public void dropStatementWithNullRequest() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("null request", false,
				PolicyHelper.isAuthorizedToDrop(null, stmt));
	}

	@Test
	public void dropAuthorizedStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				APPROVED_PREDICATE_URI);
		assertEquals("authorized", true,
				PolicyHelper.isAuthorizedToDrop(req, stmt));
	}

	@Test
	public void dropUnauthorizedStatement() {
		Statement stmt = dataStatement(APPROVED_SUBJECT_URI,
				UNAPPROVED_PREDICATE_URI);
		assertEquals("not authorized", false,
				PolicyHelper.isAuthorizedToDrop(req, stmt));
	}

	// ----------------------------------------------------------------------
	// Model-level tests
	// ----------------------------------------------------------------------

	@Test
	public void addNullModel() {
		assertEquals("null statement", false,
				PolicyHelper.isAuthorizedToAdd(req, (Model) null));
	}

	@Test
	public void addModelWithNullRequest() {
		assertEquals("empty model", false,
				PolicyHelper.isAuthorizedToAdd(null, model()));
	}

	@Test
	public void addEmptyModel() {
		assertEquals("empty model", true,
				PolicyHelper.isAuthorizedToAdd(req, model()));
	}

	@Test
	public void addAuthorizedModel() {
		Model model = model(
				dataStatement(APPROVED_SUBJECT_URI, APPROVED_PREDICATE_URI),
				objectStatement(APPROVED_SUBJECT_URI, APPROVED_PREDICATE_URI,
						APPROVED_OBJECT_URI));
		assertEquals("authorized model", true,
				PolicyHelper.isAuthorizedToAdd(req, model));
	}

	@Test
	public void addUnauthorizedModel() {
		Model model = model(
				dataStatement(APPROVED_SUBJECT_URI, APPROVED_PREDICATE_URI),
				objectStatement(APPROVED_SUBJECT_URI, UNAPPROVED_PREDICATE_URI,
						APPROVED_OBJECT_URI));
		assertEquals("unauthorized model", false,
				PolicyHelper.isAuthorizedToAdd(req, model));
	}

	@Test
	public void dropNullModel() {
		assertEquals("null statement", false,
				PolicyHelper.isAuthorizedToDrop(req, (Model) null));
	}

	@Test
	public void dropModelWithNullRequest() {
		assertEquals("empty model", false,
				PolicyHelper.isAuthorizedToDrop(null, model()));
	}

	@Test
	public void dropEmptyModel() {
		assertEquals("empty model", true,
				PolicyHelper.isAuthorizedToDrop(req, model()));
	}

	@Test
	public void dropAuthorizedModel() {
		Model model = model(
				dataStatement(APPROVED_SUBJECT_URI, APPROVED_PREDICATE_URI),
				objectStatement(APPROVED_SUBJECT_URI, APPROVED_PREDICATE_URI,
						APPROVED_OBJECT_URI));
		assertEquals("authorized model", true,
				PolicyHelper.isAuthorizedToDrop(req, model));
	}

	@Test
	public void dropUnauthorizedModel() {
		Model model = model(
				dataStatement(APPROVED_SUBJECT_URI, UNAPPROVED_PREDICATE_URI),
				objectStatement(APPROVED_SUBJECT_URI, APPROVED_PREDICATE_URI,
						APPROVED_OBJECT_URI));
		assertEquals("unauthorized model", false,
				PolicyHelper.isAuthorizedToDrop(req, model));
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	/** Build a data statement. */
	private Statement dataStatement(String subjectUri, String predicateUri) {
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.createResource(subjectUri);
		Property predicate = model.createProperty(predicateUri);
		return model.createStatement(subject, predicate, "whoCares?");
	}

	/** Build a object statement. */
	private Statement objectStatement(String subjectUri, String predicateUri,
			String objectUri) {
		Model model = ModelFactory.createDefaultModel();
		Resource subject = model.createResource(subjectUri);
		Resource object = model.createResource(objectUri);
		Property predicate = model.createProperty(predicateUri);
		return model.createStatement(subject, predicate, object);
	}

	/** Build a model. */
	private Model model(Statement... stmts) {
		Model model = ModelFactory.createDefaultModel();
		model.add(stmts);
		return model;
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class MySimplePolicy implements PolicyIface {
		@Override
		public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
				RequestedAction whatToAuth) {
			if (whatToAuth instanceof AbstractDataPropertyAction) {
				return isAuthorized((AbstractDataPropertyAction) whatToAuth);
			} else if (whatToAuth instanceof AbstractObjectPropertyAction) {
				return isAuthorized((AbstractObjectPropertyAction) whatToAuth);
			} else {
				return inconclusive();
			}
		}

		private PolicyDecision isAuthorized(
				AbstractDataPropertyAction whatToAuth) {
			if ((APPROVED_SUBJECT_URI.equals(whatToAuth.getSubjectUri()))
					&& (APPROVED_PREDICATE_URI.equals(whatToAuth
							.getPredicateUri()))) {
				return authorized();
			} else {
				return inconclusive();
			}
		}

		private PolicyDecision isAuthorized(
				AbstractObjectPropertyAction whatToAuth) {
			if ((APPROVED_SUBJECT_URI.equals(whatToAuth.uriOfSubject))
					&& (APPROVED_PREDICATE_URI
							.equals(whatToAuth.uriOfPredicate))
					&& (APPROVED_OBJECT_URI.equals(whatToAuth.uriOfObject))) {
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
