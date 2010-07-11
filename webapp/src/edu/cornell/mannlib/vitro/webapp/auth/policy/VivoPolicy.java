/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DefaultInconclusivePolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

public class VivoPolicy extends DefaultInconclusivePolicy{

	private static String AUTHORSHIP =  "http://vivoweb.org/ontology/core#informationResourceInAuthorship"; 
	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {

		if( whatToAuth instanceof DropObjectPropStmt ){
			DropObjectPropStmt dops = (DropObjectPropStmt)whatToAuth;
			
			/* Do not offer the user the option to delete so they will use the custom form instead */ 
			/* see issue NIHVIVO-739 */
			if( AUTHORSHIP.equals( dops.getUriOfPredicate() )) {
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
						"Use the custom edit form for core:informationResourceInAuthorship");
			}
		}
		
		return super.isAuthorized(whoToAuth, whatToAuth);						
	}

	
}
