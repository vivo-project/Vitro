/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.EnumSet;
import java.util.Set;

public enum AccessOperation {
    EXECUTE,
    PUBLISH,
    UPDATE,
    DISPLAY,
    ADD,
    DROP,
    EDIT;

    public static Set<AccessOperation> getUserInterfaceSet() {
        return EnumSet.of(AccessOperation.DISPLAY, AccessOperation.ADD, AccessOperation.DROP, AccessOperation.EDIT,
                AccessOperation.PUBLISH);
    }
}
