package edu.cornell.mannlib.vitro.webapp.auth.policy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Policy;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public class PolicyDecisionPoint {

    private static final Log log = LogFactory.getLog(PolicyDecisionPoint.class.getName());

    public static PolicyDecision decide(AuthorizationRequest ar) {
        PolicyDecision pd = null;
        PolicyDecisionLogger logger = new PolicyDecisionLogger(ar.getIds(), ar.getAccessObject());
        PolicyStore store = PolicyStore.getInstance();
        for(Policy policy : store.getList()){
            try{
                pd = policy.decide(ar);
                logger.log(policy, pd);
                if( pd != null ){
                    if(  pd.getDecisionResult() == DecisionResult.AUTHORIZED )
                        return pd;
                    if( pd.getDecisionResult() == DecisionResult.UNAUTHORIZED )
                        return pd;
                    if( pd.getDecisionResult() == DecisionResult.INCONCLUSIVE )
                        continue;
                } else{
                    log.debug("policy " + policy.toString() + " returned a null PolicyDecision");
                }
            }catch(Throwable th){
                log.error("ignoring exception in policy " + policy.toString(), th );
            }
        }

        pd = new BasicPolicyDecision(DecisionResult.INCONCLUSIVE,
                "No policy returned a conclusive decision on " + ar.getAccessObject());
        logger.logNoDecision(pd);
        return pd;
    }
}
