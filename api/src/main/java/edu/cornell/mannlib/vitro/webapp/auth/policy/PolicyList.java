/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;

/**
 * This is a List of Policy Objects that implements PolciyIface.  The intent
 * is to make it easy to query a list of policies for a PolicyDecision.
 *
 *  The Policy objects in the PolicyList are queried for authorization in order
 *  and return the first AUTHORIZED or UNAUTHROIZED decision.  INCONCLUSIVE
 *  or null decisions will be ignored and the next policy on the list will
 *  be queried.
 */
public class PolicyList extends ArrayList<PolicyIface> implements PolicyIface{
    private static final Log log = LogFactory.getLog(PolicyList.class.getName());

    public PolicyList(){
        super();
    }

	public PolicyList(Collection<PolicyIface> policies) {
		super(policies);
	}

	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth, RequestedAction whatToAuth) {
	    PolicyDecision pd = null;
	    PolicyDecisionLogger logger = new PolicyDecisionLogger(whoToAuth, whatToAuth);
	    for(PolicyIface policy : this){
            try{
                pd = policy.isAuthorized(whoToAuth, whatToAuth);
                logger.log(policy, pd);
                if( pd != null ){
                    if(  pd.getAuthorized() == Authorization.AUTHORIZED )
                        return pd;
                    if( pd.getAuthorized() == Authorization.UNAUTHORIZED )
                        return pd;
                    if( pd.getAuthorized() == Authorization.INCONCLUSIVE )
                        continue;
                } else{
                    log.debug("policy " + policy.toString() + " returned a null PolicyDecision");
                }
            }catch(Throwable th){
                log.error("ignoring exception in policy " + policy.toString(), th );
            }
        }

		pd = new BasicPolicyDecision(Authorization.INCONCLUSIVE,
				"No policy returned a conclusive decision on " + whatToAuth);
		logger.logNoDecision(pd);
		return pd;
	}

}
