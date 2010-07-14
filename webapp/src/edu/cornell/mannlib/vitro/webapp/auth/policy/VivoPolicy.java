/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DefaultInconclusivePolicy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

public class VivoPolicy extends DefaultInconclusivePolicy{

	private static String AUTHORSHIP_FROM_PUB =  "http://vivoweb.org/ontology/core#informationResourceInAuthorship"; 
	private static String AUTHORSHIP_FROM_PERSON =  "http://vivoweb.org/ontology/core#authorInAuthorship";
	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {

		if( whatToAuth instanceof DropObjectPropStmt ){
			DropObjectPropStmt dops = (DropObjectPropStmt)whatToAuth;
			
			/* Do not offer the user the option to delete so they will use the custom form instead */ 
			/* see issue NIHVIVO-739 */
			if( AUTHORSHIP_FROM_PUB.equals( dops.getUriOfPredicate() )) {
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
						"Use the custom edit form for core:informationResourceInAuthorship");
			}
			
			if( AUTHORSHIP_FROM_PERSON.equals( dops.getUriOfPredicate() )) {
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
						"Use the custom edit form for core:authorInAuthorship");
			}
			 
			if( "http://vivoweb.org/ontology/core#linkedAuthor".equals( dops.getUriOfPredicate())){
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
					"Use the custom edit form for on information resource to edit authors.");
			}
			
			if( "http://vivoweb.org/ontology/core#linkedInformationResource".equals( dops.getUriOfPredicate())){
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
					"Use the custom edit form for on information resource to edit authors.");
			}			
		}
		if( whatToAuth instanceof AddObjectPropStmt ){
			AddObjectPropStmt aops = (AddObjectPropStmt)whatToAuth;
			if( "http://vivoweb.org/ontology/core#linkedAuthor".equals( aops.getUriOfPredicate())){
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
					"Use the custom edit form for on information resource to edit authors.");
			}
			
			if( "http://vivoweb.org/ontology/core#linkedInformationResource".equals( aops.getUriOfPredicate())){
				return new BasicPolicyDecision(Authorization.UNAUTHORIZED, 
					"Use the custom edit form for on information resource to edit authors.");
			}	
		}
		
		return super.isAuthorized(whoToAuth, whatToAuth);						
	}

	
}
