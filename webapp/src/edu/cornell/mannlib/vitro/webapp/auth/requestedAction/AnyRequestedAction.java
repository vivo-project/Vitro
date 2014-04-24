/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
