package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * This action should be authorized if all of its subActions are authorized.
 * @author bdc34 
 */
public class AllRequestedAction extends RequestedAction{
    private final Collection<RequestedAction> subActions ;
    
    public AllRequestedAction(RequestedAction... actions ){
        this( Arrays.asList( actions ));
    }
    public AllRequestedAction(Collection <RequestedAction> subActions){
        this.subActions = Collections.unmodifiableCollection( subActions );
    }    

    public Collection<RequestedAction> getRequestedActions(){
        return subActions;
    }
}
