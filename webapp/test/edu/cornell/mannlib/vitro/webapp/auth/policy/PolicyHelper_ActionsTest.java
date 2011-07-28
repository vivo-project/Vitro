/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * Test the function of PolicyHelper in authorizing simple actions.
 */
public class PolicyHelper_ActionsTest extends AbstractTestClass {
	private ServletContextStub ctx;
	private HttpSessionStub session;
	private HttpServletRequestStub req;

	@Before
	public void setLogging() {
		setLoggerLevel(ServletPolicyList.class, Level.WARN);
	}

	@Before
	public void setup() {
		ctx = new ServletContextStub();

		session = new HttpSessionStub();
		session.setServletContext(ctx);

		req = new HttpServletRequestStub();
		req.setSession(session);
	}

	// ----------------------------------------------------------------------
	// Action-level tests
	// ----------------------------------------------------------------------

	@Test
	public void authorizedForActionsNull() {
		createPolicy();
		assertEquals("null actions", true,
				PolicyHelper.isAuthorizedForActions(req, (Actions) null));
	}

	@Test
	public void authorizedForActionsEmpty() {
		createPolicy();
		assertEquals("empty actions", true,
				PolicyHelper.isAuthorizedForActions(req, new Actions()));
	}

	@Test
	public void authorizedForActionsOneClausePass() {
		createPolicy(new Action1(), new Action2());
		assertEquals("one clause pass", true,
				PolicyHelper.isAuthorizedForActions(req, new Actions(
						new Action1(), new Action2())));
	}

	@Test
	public void authorizedForActionsOneClauseFail() {
		createPolicy(new Action2());
		assertEquals("one clause fail", false,
				PolicyHelper.isAuthorizedForActions(req, new Actions(
						new Action1(), new Action2())));
	}

	@Test
	public void authorizedForActionsMultipleClausesPass() {
		createPolicy(new Action3());
		assertEquals("multiple clauses pass", true,
				PolicyHelper.isAuthorizedForActions(req, new Actions(
						new Action1(), new Action2()).or(new Action3())));
	}

	@Test
	public void authorizedForActionsMultipleClausesFail() {
		createPolicy(new Action1());
		assertEquals("multiple clauses fail", false,
				PolicyHelper.isAuthorizedForActions(req, new Actions(
						new Action1(), new Action2()).or(new Action3())));
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void createPolicy(RequestedAction... authorizedActions) {
		ServletPolicyList.addPolicy(ctx, new MySimplePolicy(authorizedActions));
	}

	// ----------------------------------------------------------------------
	// Helper Classes
	// ----------------------------------------------------------------------

	public static class Action1 extends RequestedAction {
		// actions must be public, with public constructor
	}

	public static class Action2 extends RequestedAction {
		// actions must be public, with public constructor
	}

	public static class Action3 extends RequestedAction {
		// actions must be public, with public constructor
	}

	private static class MySimplePolicy implements PolicyIface {
		private final Set<RequestedAction> authorizedActions;

		public MySimplePolicy(RequestedAction... authorizedActions) {
			this.authorizedActions = new HashSet<RequestedAction>(
					Arrays.asList(authorizedActions));
		}

		@Override
		public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
				RequestedAction whatToAuth) {
			for (RequestedAction authorized : authorizedActions) {
				if (authorized.getClass().equals(whatToAuth.getClass())) {
					return new BasicPolicyDecision(Authorization.AUTHORIZED,
							"matched " + authorized.getClass().getSimpleName());
				}

			}
			return new BasicPolicyDecision(Authorization.INCONCLUSIVE, "nope");
		}

	}
}
