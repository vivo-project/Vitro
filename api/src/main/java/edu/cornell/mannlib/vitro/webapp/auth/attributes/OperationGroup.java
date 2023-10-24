/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.EnumSet;
import java.util.Set;

public enum OperationGroup {
    DISPLAY_GROUP,
    UPDATE_GROUP,
    PUBLISH_GROUP;

    public static Set<AccessOperation> getOperations(OperationGroup og) {
        if (og.equals(UPDATE_GROUP)) {
            return EnumSet.of(AccessOperation.ADD, AccessOperation.DROP, AccessOperation.EDIT);
        }
        if (og.equals(PUBLISH_GROUP)) {
            return EnumSet.of(AccessOperation.PUBLISH);
        }
        return EnumSet.of(AccessOperation.DISPLAY);

    }
}
