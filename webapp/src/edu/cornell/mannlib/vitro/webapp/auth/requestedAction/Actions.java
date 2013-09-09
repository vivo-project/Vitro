/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A list of RequiredAction objects.
 * 
 * Authorization is considered successful if ALL of the actions are
 * authorized, or if there are NO clauses.
 * 
 * A previous version of this class had a capability to do OR clauses but 
 * this feature was unused and hindered composition of Actions
 * objects. The ability to do an OR has been moved to AnyRequestedAction
 * and AllRequestedAction.
 * 
 */
public class Actions {
	private static final Log log = LogFactory.getLog(Actions.class);

	public static final Actions AUTHORIZED = new Actions();
	public static final Actions UNAUTHORIZED = new Actions(
			new UnauthorizedAction());

	public static Actions notNull(Actions actions) {
		return (actions == null) ? AUTHORIZED : actions;
	}

	/**
	 * This is a set of RequestedActions that get ANDed together.
	 *  
	 * If all of the RequestedAction objects from the
	 * Sets are authorized, then the Actions object should
	 * be considered authorized.   
	 */
	private Set<RequestedAction> requestedActions;
	
	public Actions(){
        requestedActions= Collections.emptySet();
	}	    
	
	/**
     * AND together all the RequestedAction from all the actions.
     */
    public Actions(Actions... actions){        
        Set<RequestedAction> newActs = new HashSet<RequestedAction>();
        
        for( Actions actionToAnd : actions){
            if( actionToAnd != null && actionToAnd.requestedActions != null ){
                newActs.addAll( actionToAnd.requestedActions );
            }
        }

        this.requestedActions = Collections.unmodifiableSet( newActs );
    }
    
	public Actions(RequestedAction... actions) {
		this(Arrays.asList(actions));
	}
		
	public Actions(Collection<RequestedAction> actions) {
		this(Collections.<RequestedAction> emptySet(), actions);
	}
	
	private Actions(Set<RequestedAction> oldList,
			         Collection<RequestedAction> newActions) {
	    
		Set<RequestedAction> newActs = new HashSet<RequestedAction>();
		
		if( oldList != null ){
		    newActs.addAll(oldList);
		}
		
		if ( newActions != null ) {
			newActs.addAll( newActions );
		}
		
		this.requestedActions = Collections.unmodifiableSet(newActs);
	}
	
	/** require all RequestedActions on this and the ones in newActions to authorize.*/
	public Actions and(RequestedAction... newActions){
	    return and(Arrays.asList( newActions));
	}
		
	/** require all RequestedActions on this and the ones in newActions to authorize.*/
	public Actions and(Collection<RequestedAction> newActions){
	    if( newActions == null || newActions.size() == 0)
	        return this;
	    else
	        return new Actions( this.requestedActions, newActions);
	}		
		
	/** require all RequestedActions on this and the ones in newActions to authorize.*/
	public Actions and(Actions newActions){
	    return new Actions( this.requestedActions, newActions.requestedActions);
	}
	
	public Actions or(RequestedAction... newActions) {
		return or(Arrays.asList(newActions));
	}

	/**
	 * OR together this.requestedActions and newActions. 
	 */
	public Actions or(Collection<RequestedAction> newActions) {	    
	    RequestedAction acts;
	    
	    if( newActions == null || newActions.size() == 0 ){
	        return this;
	    }
	    
	    int thisActionCount = this.requestedActions.size();
	    int newActionCount = newActions.size();
	    
	    /* This minimizes the number of extra RequestedActions
	     * that get created when there is only one in this
	     * or newActions.*/
	    if( thisActionCount == 1 && newActionCount == 1 ){
	            return new Actions( 
	                    new AnyRequestedAction( 
	                            this.requestedActions.iterator().next(),
	                            newActions.iterator().next() ));
	    }
	    
	    if( thisActionCount == 1 && newActionCount > 1 ){
	        return new Actions( 
	                new AnyRequestedAction(
	                        this.requestedActions.iterator().next(),
	                        new AllRequestedAction( newActions )));	            
	    }
	    
	    if( thisActionCount > 1 && newActionCount == 1){
	        return new Actions( new AnyRequestedAction( 
	                      new AllRequestedAction( this.requestedActions),
	                      newActions.iterator().next()));
	    }
	     
	    if( thisActionCount > 1 && newActionCount > 1 ){
	        return new Actions(
	                new AnyRequestedAction( 
	                        new AllRequestedAction( this.requestedActions ),
	                        new AllRequestedAction( newActions )));	        
	    }
	    //should never be reached.
	    log.error("Could not properly create disjunction");
        return null;
	}

	public boolean isEmpty() {
	    return this.requestedActions.isEmpty();
	}

	
	/**
	 * Are the RequestedAction objects for this Actions authorized
	 * with the ids and policy? 
	 */
	public boolean isAuthorized(PolicyIface policy, IdentifierBundle ids) {
	    /* No clauses means everything is authorized */
		if (requestedActions.isEmpty()) {
			log.debug("Empty Actions is authorized");
			return true;
		}
				
		/* Are all the RequestedAction object authorized? */
		List<PolicyDecision> decisions = new ArrayList<PolicyDecision>();
		for (RequestedAction action : requestedActions) {
		    PolicyDecision decision = policy.isAuthorized(ids, action);
		    log.debug("decision for '" + action.getClass().getSimpleName() + "' was: "
	                + decision);
		    decisions.add( decision );
		}
		return areAllAuthorized( decisions );
	}

	private boolean areAllAuthorized( List<PolicyDecision> decisions ){
	    for( PolicyDecision dec : decisions){
	        if( dec == null || dec.getAuthorized() != Authorization.AUTHORIZED ){
	            return false;
	        }
	    }
	    return true;
	}
	
//	/** All actions in a clause must be authorized. */
//	private static boolean isAuthorizedForClause(PolicyIface policy,
//			IdentifierBundle ids, Set<RequestedAction> clause) {
//		for (RequestedAction action : clause) {
//			if (!isAuthorizedForAction(policy, ids, action)) {
//				log.debug("not authorized");
//				return false;
//			}
//		}
//		return true;
//	}
//
//	/** Is this action authorized? */
//	private static boolean isAuthorizedForAction(PolicyIface policy,
//			IdentifierBundle ids, RequestedAction action) {
//		PolicyDecision decision = policy.isAuthorized(ids, action);
//		log.debug("decision for '" + action.getClass().getSimpleName() + "' was: "
//				+ decision);
//		return (decision != null)
//				&& (decision.getAuthorized() == Authorization.AUTHORIZED);
//	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Actions[");
		Iterator<RequestedAction> it = this.requestedActions.iterator();
		while( it.hasNext() ){
		    RequestedAction act = it.next();
			sb.append( act.toString() );
			if (it.hasNext()) {
			    sb.append(", ");			
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * AND for Actions.
	 * ANDing with an Action with multiple disjoint clauses is not supported.
	 *  
	 * To do the AND, we take each ORed clause, and add all of the RequestedActions
	 * so now in each of the alternative clauses, all of the singleClauseToAnd
	 * RequestedActions are required.
	 * 
	 * @throws Exception  when multiple disjoint clauses are present on both Actions.
	 */
	//private void andWithAction( Actions otherAct ) throws Exception{
//	    Set<RequestedAction> singleClauseToAnd;
//	    List<Set<RequestedAction>> clauses;
//	    
//	    if( otherAct.singleAndClause() ){
//	        clauses = this.requestedActions; 
//	        singleClauseToAnd = otherAct.requestedActions.get(0);
//	    }else if( this.singleAndClause() ){
//	        clauses = new ArrayList<Set<RequestedAction>>( otherAct.requestedActions );
//	        singleClauseToAnd = this.requestedActions.get(0);
//	    }else{
//	        //both have multiple ORed clauses, give up
//	       throw new Exception("ANDing with an Action with multiple disjoint clauses is not supported.");
//	    }
//	    
//	    // 
//	    for( Set<RequestedAction> clause : clauses){
//	        clause.addAll( singleClauseToAnd );
//	    }	    
//	    this.requestedActions = clauses;
	//}
	
//	private boolean singleAndClause(){
//	    return requestedActions.size() == 1;
//	}
	
//	/**
//	 * Nobody knows about this action class, so only the root user should be
//	 * authorized for it.
//	 */
//	private static class UnauthorizedAction extends RequestedAction {
//		// no members
//	}
}
