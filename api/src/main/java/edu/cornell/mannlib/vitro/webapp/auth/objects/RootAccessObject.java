/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;

/**
 * Should we allow the user to edit or delete the root account?
 */
public class RootAccessObject extends AccessObjectImpl {
    
    @Override
    public AccessObjectType getType() {
        return AccessObjectType.ROOT_USER;
    }
}
