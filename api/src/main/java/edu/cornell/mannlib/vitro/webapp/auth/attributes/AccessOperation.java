/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Arrays;
import java.util.List;

public enum AccessOperation {
    EXECUTE,
    PUBLISH,
    UPDATE,
    DISPLAY,
    ADD,
    DROP,
    EDIT;

    public static List<AccessOperation> getOperations(AccessObjectType aot) {
        if (aot.equals(AccessObjectType.DATA_PROPERTY)
                || aot.equals(AccessObjectType.OBJECT_PROPERTY)
                || aot.equals(AccessObjectType.FAUX_DATA_PROPERTY)
                || aot.equals(AccessObjectType.FAUX_OBJECT_PROPERTY)) {
            return Arrays.asList(AccessOperation.DISPLAY, AccessOperation.PUBLISH, AccessOperation.ADD,
                    AccessOperation.DROP, AccessOperation.EDIT);
        }
        return Arrays.asList(AccessOperation.DISPLAY, AccessOperation.PUBLISH, AccessOperation.UPDATE);
    }
}
