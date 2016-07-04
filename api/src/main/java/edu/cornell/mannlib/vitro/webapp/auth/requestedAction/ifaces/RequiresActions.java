/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Interface to objects that provide a list of Actions that are 
 * required for the object to be used.
 * 
 * This is intended to provide a way to setup DataGetter
 * objects to be used with the FreemarkerHttpServlet.requiredActions()
 * method.
 * 
 * @author bdc34 
 */
public interface RequiresActions {
    
    /**
     * Returns Actions that are required to be authorized for
     * the object to be used. 
     * 
     * The code that is calling this method
     * could use methods from PolicyHelper to check if the 
     * request has authorization to do these Actions. The code
     * calling this method would then have the ability to
     * deny the action if it is not authorized. 
     * 
     * @param vreq Vitro request
     * @return Should not be null. Return Actions.AUTHORIZED
     * if no authorization is required to do use the object.
     */
    public AuthorizationRequest requiredActions(VitroRequest vreq) ;
    
}
