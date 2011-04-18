/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.RequiresAuthorizationFor;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.RequiresAuthorizationFor.Or;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;

/**
 * Test the basic top-level function of PolicyHelper.
 */
public class PolicyHelperTest extends AbstractTestClass {
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

	@Test
	public void noAnnotation() {
		createPolicy();
		assertExpectedAuthorization("no actions required",
				NoAnnotationServlet.class, true);
	}

	@Test
	public void noRequirements() {
		createPolicy();
		assertExpectedAuthorization("no actions required",
				NoRequirementsServlet.class, true);
	}

	@Test
	public void oneRequirementFail() {
		createPolicy();
		assertExpectedAuthorization("requires Action1", Action1Servlet.class,
				false);
	}

	@Test
	public void oneRequirementSucceed() {
		createPolicy(new Action1());
		assertExpectedAuthorization("requires Action1", Action1Servlet.class,
				true);
	}

	@Test
	public void twoRequirementsFailOne() {
		createPolicy(new Action1());
		assertExpectedAuthorization("requires Actions 1 and 2",
				Action1AndAction2Servlet.class, false);
	}

	@Test
	public void twoRequirementsFailTwo() {
		createPolicy(new Action2());
		assertExpectedAuthorization("requires Actions 1 and 2",
				Action1AndAction2Servlet.class, false);
	}

	@Test
	public void twoRequirementsSucceed() {
		createPolicy(new Action2(), new Action1());
		assertExpectedAuthorization("requires Actions 1 and 2",
				Action1AndAction2Servlet.class, true);
	}

	@Test
	public void oneOrTwoFail() {
		createPolicy();
		assertExpectedAuthorization("requires Action 1 or 2",
				Action1OrAction2Servlet.class, false);
	}

	@Test
	public void oneOrTwoSucceedOne() {
		createPolicy(new Action1());
		assertExpectedAuthorization("requires Action 1 or 2",
				Action1OrAction2Servlet.class, true);
	}

	@Test
	public void oneOrTwoSucceedTwo() {
		createPolicy(new Action2());
		assertExpectedAuthorization("requires Action 1 or 2",
				Action1OrAction2Servlet.class, true);
	}

	@Test
	public void oneOrTwoOrThreeFail() {
		createPolicy();
		assertExpectedAuthorization("requires Action 1 or 2 or 3",
				Action1OrAction2OrAction3Servlet.class, false);
	}

	@Test
	public void oneOrTwoOrThreeSucceedOne() {
		createPolicy(new Action1());
		assertExpectedAuthorization("requires Action 1 or 2 or 3",
				Action1OrAction2OrAction3Servlet.class, true);
	}

	@Test
	public void oneOrTwoOrThreeSucceedTwo() {
		createPolicy(new Action2());
		assertExpectedAuthorization("requires Action 1 or 2 or 3",
				Action1OrAction2OrAction3Servlet.class, true);
	}

	@Test
	public void oneOrTwoOrThreeSucceedThree() {
		createPolicy(new Action3());
		assertExpectedAuthorization("requires Action 1 or 2 or 3",
				Action1OrAction2OrAction3Servlet.class, true);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void createPolicy(RequestedAction... authorizedActions) {
		ServletPolicyList.addPolicy(ctx, new MySimplePolicy(authorizedActions));
	}

	private void assertExpectedAuthorization(String label,
			Class<? extends VitroHttpServlet> servletClass, boolean expected) {
		boolean actual = PolicyHelper.areRequiredAuthorizationsSatisfied(req,
				servletClass);
		assertEquals(label, expected, actual);
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

	// no annotation
	private static class NoAnnotationServlet extends VitroHttpServlet {
		/* no body */
	}

	@RequiresAuthorizationFor
	private static class NoRequirementsServlet extends VitroHttpServlet {
		/* no body */
	}

	@RequiresAuthorizationFor(Action1.class)
	private static class Action1Servlet extends VitroHttpServlet {
		/* no body */
	}

	@RequiresAuthorizationFor({ Action1.class, Action2.class })
	private static class Action1AndAction2Servlet extends VitroHttpServlet {
		/* no body */
	}

	@RequiresAuthorizationFor(value = Action1.class, or = @Or(Action2.class))
	private static class Action1OrAction2Servlet extends VitroHttpServlet {
		/* no body */
	}

	@RequiresAuthorizationFor(value = Action1.class, or = { @Or(Action2.class),
			@Or(Action3.class) })
	private static class Action1OrAction2OrAction3Servlet extends
			VitroHttpServlet {
		/* no body */
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
