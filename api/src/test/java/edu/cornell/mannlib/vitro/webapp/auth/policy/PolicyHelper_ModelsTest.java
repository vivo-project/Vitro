/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.NiceIterator;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.AbstractPropertyStatementAction;

/**
 * Test the function of PolicyHelper in authorizing models of additions and
 * retractions.
 * 
 * It is vital that these methods work even if the statements are presented in
 * the "wrong" order: one statement being authorized by a statement that appears
 * later in the list.
 * 
 * In order to test that, we need to create a Model that will list the
 * statements in an order that we can predict, vis. the order in which they were
 * added.
 * 
 * To avoid creating a SortedModel that implements dozens of methods, we instead
 * create a Proxy class that keeps the statements in order and lists them on
 * demand.
 */
public class PolicyHelper_ModelsTest extends AbstractTestClass {
	private static final String PRIMARY_RESOURCE_URI = "http://primaryResource";
	private static final String OTHER_RESOURCE_URI = "http://otherResource";
	private static final String FRIEND_PREDICATE_URI = "http://friend";
	private static final String SOME_PREDICATE_URI = "http://something";

	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;
	private OntModel ontModel = ModelFactory
			.createOntologyModel(OntModelSpec.OWL_MEM);

	private Model additions;
	private Model retractions;

	@Before
	public void setup() {
		ctx = new ServletContextStub();

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);

		setLoggerLevel(ServletPolicyList.class, Level.WARN);
		ServletPolicyList.addPolicy(ctx, new MySimplePolicy());

//		setLoggerLevel(PolicyHelper.class, Level.DEBUG);
	}

	// ----------------------------------------------------------------------
	// The tests.
	// ----------------------------------------------------------------------

	@Test
	public void rejectNullRequest() {
		setLoggerLevel(PolicyHelper.class, Level.ERROR); // suppress warning
		req = null;
		additions = model();
		retractions = model();
		assertAuthorized("reject null request", false);
	}

	@Test
	public void rejectNullAdditions() {
		setLoggerLevel(PolicyHelper.class, Level.ERROR); // suppress warning
		additions = null;
		retractions = model();
		assertAuthorized("reject null additions", false);
	}

	@Test
	public void rejectNullRetractions() {
		setLoggerLevel(PolicyHelper.class, Level.ERROR); // suppress warning
		additions = model();
		retractions = null;
		assertAuthorized("reject null retractions", false);
	}

	@Test
	public void rejectNullOntModel() {
		setLoggerLevel(PolicyHelper.class, Level.ERROR); // suppress warning
		additions = model();
		retractions = model();
		ontModel = null;
		assertAuthorized("reject null OntModel", false);
	}

	@Test
	public void acceptEmptyChanges() {
		additions = model();
		retractions = model();
		assertAuthorized("accept empty changes add", true);
	}

	@Test
	public void acceptSimpleAdd() {
		additions = model(dataStatement(PRIMARY_RESOURCE_URI,
				SOME_PREDICATE_URI));
		retractions = model();
		assertAuthorized("accept simple add", true);
	}

	@Test
	public void acceptSimpleDrop() {
		additions = model();
		retractions = model(dataStatement(PRIMARY_RESOURCE_URI,
				SOME_PREDICATE_URI));
		assertAuthorized("accept simple add", true);
	}

	@Test
	public void rejectSimpleAdd() {
		setLoggerLevel(PolicyHelper.class, Level.ERROR); // suppress warning
		additions = model(dataStatement(OTHER_RESOURCE_URI, SOME_PREDICATE_URI));
		retractions = model();
		assertAuthorized("reject simple add", false);
	}

	@Test
	public void rejectSimpleDrop() {
		setLoggerLevel(PolicyHelper.class, Level.ERROR); // suppress warning
		additions = model();
		retractions = model(dataStatement(OTHER_RESOURCE_URI,
				SOME_PREDICATE_URI));
		assertAuthorized("reject simple drop", false);
	}

	@Test
	public void acceptAddBecauseOfExistingStatement() {
		ontModel.add(objectStatement(PRIMARY_RESOURCE_URI,
				FRIEND_PREDICATE_URI, OTHER_RESOURCE_URI));
		additions = model(dataStatement(OTHER_RESOURCE_URI, SOME_PREDICATE_URI));
		retractions = model();
		assertAuthorized("accept add because of existing statement", true);
	}

	@Test
	public void acceptDropBecauseOfExistingStatement() {
		ontModel.add(objectStatement(PRIMARY_RESOURCE_URI,
				FRIEND_PREDICATE_URI, OTHER_RESOURCE_URI));
		additions = model();
		retractions = model(dataStatement(OTHER_RESOURCE_URI,
				SOME_PREDICATE_URI));
		assertAuthorized("accept drop because of existing statement", true);
	}

	/**
	 * This test is the whole reason for the funky model that lists statements
	 * in a known order. We need to know that the DataStatement is authorized
	 * even though it relies on an ObjectStatement that appears later in the
	 * list.
	 */
	@Test
	public void acceptAddBecauseOfOtherAdd() {
		additions = model(
				dataStatement(OTHER_RESOURCE_URI, SOME_PREDICATE_URI),
				objectStatement(PRIMARY_RESOURCE_URI, FRIEND_PREDICATE_URI,
						OTHER_RESOURCE_URI));
		retractions = model();
		assertAuthorized("accept add because of other add", true);
	}

	@Test
	public void acceptDropBecauseOfAdd() {
		additions = model(objectStatement(PRIMARY_RESOURCE_URI,
				FRIEND_PREDICATE_URI, OTHER_RESOURCE_URI));
		retractions = model(dataStatement(OTHER_RESOURCE_URI,
				SOME_PREDICATE_URI));
		assertAuthorized("accept drop because of add", true);
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

	/** Build an object statement. */
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
		Model innerModel = ModelFactory.createDefaultModel();
		Model proxy = (Model) Proxy.newProxyInstance(
				OrderedModelInvocationHandler.class.getClassLoader(),
				new Class[] { Model.class }, new OrderedModelInvocationHandler(
						innerModel));
		proxy.add(stmts);
		return proxy;
	}

	/**
	 * Check whether we are authorized to make the additions and retractions to
	 * the model.
	 */
	private void assertAuthorized(String message, boolean expected) {
		boolean actual = PolicyHelper.isAuthorizedAsExpected(req, additions,
				retractions, ontModel);
		assertEquals(message, expected, actual);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * A model Proxy object built around this will list statements in the order
	 * they were added.
	 * 
	 * This only works if the statements were added by a call to
	 * add(Statement[]), and if they are listed by a call to listStatements().
	 * If the test used other methods to add statements, or if the PolicyHelper
	 * used a different method to query the model, we would not be assured of
	 * the order of the statements from the iterator.
	 */
	public static class OrderedModelInvocationHandler implements
			InvocationHandler {
		private final Model proxied;
		private final List<Statement> stmts = new ArrayList<Statement>();

		public OrderedModelInvocationHandler(Model proxied) {
			this.proxied = proxied;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (method.getName().equals("add") && (args.length == 1)
					&& (args[0] instanceof Statement[])) {
				stmts.addAll(Arrays.asList((Statement[]) args[0]));
				return method.invoke(proxied, args);
			}
			if (method.getName().equals("listStatements")
					&& ((args == null) || (args.length == 0))) {
				return new StatementListIterator(stmts);
			}

			return method.invoke(proxied, args);
		}
	}

	/**
	 * A StmtIterator that iterates over a list of statements.
	 */
	public static class StatementListIterator extends NiceIterator<Statement>
			implements StmtIterator {
		private final Iterator<Statement> innerIterator;

		public StatementListIterator(List<Statement> stmts) {
			this.innerIterator = new ArrayList<Statement>(stmts).iterator();
		}

		@Override
		public Statement nextStatement() throws NoSuchElementException {
			return next();
		}

		@Override
		public boolean hasNext() {
			return innerIterator.hasNext();
		}

		@Override
		public Statement next() {
			return innerIterator.next();
		}

	}

	/**
	 * A Policy that authorizes a statement iff (1) The subject is the primary
	 * resource, or (2) The subject is related to the primary resource by a
	 * "friend" property statement.
	 */
	private class MySimplePolicy implements PolicyIface {
		@Override
		public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
				RequestedAction whatToAuth) {
			if (!(whatToAuth instanceof AbstractPropertyStatementAction)) {
				return inconclusive();
			}

			AbstractPropertyStatementAction action = (AbstractPropertyStatementAction) whatToAuth;

			String subjectUri = action.getResourceUris()[0];
			if (PRIMARY_RESOURCE_URI.equals(subjectUri)) {
				return authorized();
			}

			Statement friendStmt = objectStatement(PRIMARY_RESOURCE_URI,
					FRIEND_PREDICATE_URI, subjectUri);
			if (statementExists(action.getOntModel(), friendStmt)) {
				return authorized();
			}

			return inconclusive();
		}

		private boolean statementExists(OntModel oModel, Statement stmt) {
			StmtIterator stmts = oModel.listStatements(stmt.getSubject(),
					stmt.getPredicate(), stmt.getObject());
			try {
				return stmts.hasNext();
			} finally {
				stmts.close();
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
