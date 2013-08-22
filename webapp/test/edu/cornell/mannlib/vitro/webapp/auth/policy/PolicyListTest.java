package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AllRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AnyRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.UnauthorizedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

public class PolicyListTest {

    @Test
    public void basicPolicyListTest() {

        List<PolicyIface> polis = new ArrayList<PolicyIface>();
        polis.add( new SimplePolicy() );
        PolicyIface policy = new PolicyList( polis );
        PolicyDecision decision = policy.isAuthorized(null, new UnauthorizedAction());
        Assert.assertEquals(Authorization.UNAUTHORIZED, decision.getAuthorized() );
        
        decision = policy.isAuthorized(null, new AuthorizedAction());
        Assert.assertEquals(Authorization.AUTHORIZED, decision.getAuthorized() );
    }
    
    /**
     * Tests the handling of the AnyRequestedAction by the PolicyList.
     */
    @Test 
    public void anyActionTest(){        
        List<PolicyIface> polis = new ArrayList<PolicyIface>();
        polis.add( new SimplePolicy() );
        PolicyIface policy = new PolicyList( polis );
        
        AnyRequestedAction act = new AnyRequestedAction( new UnauthorizedAction() );
        PolicyDecision decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.UNAUTHORIZED, decision.getAuthorized() );
        
        act = new AnyRequestedAction( new UnauthorizedAction() , new UnauthorizedAction());
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.UNAUTHORIZED, decision.getAuthorized() );
        
        act = new AnyRequestedAction( new UnauthorizedAction(),new UnauthorizedAction(),new UnauthorizedAction());
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.UNAUTHORIZED, decision.getAuthorized() );
        
        act = new AnyRequestedAction( new AuthorizedAction() );
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.AUTHORIZED, decision.getAuthorized() );
        
        act = new AnyRequestedAction( new AuthorizedAction(),new UnauthorizedAction() );
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.AUTHORIZED, decision.getAuthorized() );
        
        act = new AnyRequestedAction( new UnauthorizedAction(),new AuthorizedAction() );
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.AUTHORIZED, decision.getAuthorized() );
        
        act = new AnyRequestedAction( new UnauthorizedAction(),new UnauthorizedAction(),new AuthorizedAction());
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.AUTHORIZED, decision.getAuthorized() );
        
        act = new AnyRequestedAction( new UnauthorizedAction(),new AuthorizedAction(),new AuthorizedAction());
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.AUTHORIZED, decision.getAuthorized() );
        
        act = new AnyRequestedAction( new AuthorizedAction(),new AuthorizedAction(),new AuthorizedAction());
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.AUTHORIZED, decision.getAuthorized() );
    }
    
    /**
     * Tests the handling of the AllRequestedAction by the PolicyList.
     */
    @Test
    public void andActionTest(){
        List<PolicyIface> polis = new ArrayList<PolicyIface>();
        polis.add( new SimplePolicy() );
        PolicyIface policy = new PolicyList( polis );        
        
        AllRequestedAction act = new AllRequestedAction( new UnauthorizedAction(), new UnauthorizedAction(), new UnauthorizedAction());        
        PolicyDecision decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.UNAUTHORIZED, decision.getAuthorized() );
        
        act = new AllRequestedAction( new UnauthorizedAction() );        
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.UNAUTHORIZED, decision.getAuthorized() );
        
        act = new AllRequestedAction( new UnauthorizedAction() , new AuthorizedAction() );        
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.UNAUTHORIZED, decision.getAuthorized() );
        
        act = new AllRequestedAction( new AuthorizedAction() , new UnauthorizedAction() );        
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.UNAUTHORIZED, decision.getAuthorized() );
        
        act = new AllRequestedAction( new AuthorizedAction() , new AuthorizedAction() ,new UnauthorizedAction() );        
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.UNAUTHORIZED, decision.getAuthorized() );
        
        act = new AllRequestedAction( new AuthorizedAction()  );        
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.AUTHORIZED, decision.getAuthorized() );
        
        act = new AllRequestedAction( new AuthorizedAction() , new AuthorizedAction(), new AuthorizedAction() );        
        decision = policy.isAuthorized(null, act);
        Assert.assertNotNull( decision );
        Assert.assertEquals(Authorization.AUTHORIZED, decision.getAuthorized() );
    }

    
    /**
     * policy that only responds to Unauthorized and Authorized actions.
     */
    public class SimplePolicy implements PolicyIface {

        @Override
        public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
                RequestedAction whatToAuth) {
            if( whatToAuth instanceof UnauthorizedAction )
                return new BasicPolicyDecision( Authorization.UNAUTHORIZED, "SimplePolicy unauthorized");
            if( whatToAuth instanceof AuthorizedAction )
                return new BasicPolicyDecision( Authorization.AUTHORIZED, "SimplePolicy authorized");
            else
                return new BasicPolicyDecision(Authorization.INCONCLUSIVE, "SimplePolicy INCONCLUSIVE");
        }
        
        
    }
}
