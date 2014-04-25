/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

/**
 * Test the ability of the Policy Helper to authorize a variety of simple or complex AuthorizationRequests
 */
public class PolicyHelper_AuthorizationRequestTest {

/**Test plan
 * <pre>
 * isAuthorizedForActions with array, including empty, null, contains one or more, or contains nulls.
 * isAuthorizedForActions with collection, including empty, null, contains one or more, or contains nulls.
 *   All of this is tested by AuthorizationRequestTest?
 *   
 * isAuthorizedForActions with simple and complete AuthorizationRequests
 *   Simple success or failure (perhaps by INCONCLUSIVE) against one or more policies.
 *   AND satisfied by one policy or by two policies
 *   OR satisfied by one UNAUTHORIZED and one AUTHORIZED
 *   Complex structure of AND and OR satisfied in different ways by different policies.
 * </pre>
 */
}
