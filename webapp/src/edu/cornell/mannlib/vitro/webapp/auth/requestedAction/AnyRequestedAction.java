package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

public class AnyRequestedAction extends RequestedAction {
    private final Collection<RequestedAction> subActions ;
    
    public AnyRequestedAction(RequestedAction... acts){
        this( Arrays.asList( acts) );
    }
    
    public AnyRequestedAction(Collection<RequestedAction> subActions){
        this.subActions = Collections.unmodifiableCollection( subActions );
    }    
    
    public Collection<RequestedAction> getRequestedActions(){
        return subActions;
    }
}
