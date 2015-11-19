/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;

/**
 * Test the functions of the base class.
 */
public class AuthorizationRequestTest extends AbstractTestClass {
	private MyAuth one = new MyAuth("one");
	private MyAuth two = new MyAuth("two");

	@Test
	public void and() {
		assertEquals("and", "(MyAuth[one] && MyAuth[two])", one.and(two)
				.toString());
	}

	@Test
	public void andNull() {
		assertEquals("andNull", "MyAuth[one]", one.and(null).toString());
	}

	@Test
	public void or() {
		assertEquals("or", "(MyAuth[one] || MyAuth[two])", one.or(two)
				.toString());
	}

	@Test
	public void orNull() {
		assertEquals("orNull", "MyAuth[one]", one.or(null).toString());
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class MyAuth extends AuthorizationRequest {
		private final String name;

		public MyAuth(String name) {
			this.name = name;
		}

		@Override
		public boolean isAuthorized(IdentifierBundle ids, PolicyIface policy) {
			throw new RuntimeException(
					"AuthorizationRequest.isAuthorized() not implemented.");
		}

		@Override
		public String toString() {
			return "MyAuth[" + name + "]";
		}

	}
}
