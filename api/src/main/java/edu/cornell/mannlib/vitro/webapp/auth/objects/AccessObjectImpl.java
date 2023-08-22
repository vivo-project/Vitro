/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;

/**
 * A RequestedAction that can be recognized by a SimplePermission.
 */
public class AccessObjectImpl extends AccessObject {
    private final String uri;
    private AccessObjectType type;

    public AccessObjectImpl() {
        this.uri = "";
        this.type = AccessObjectType.NAMED_OBJECT;
    }

    public AccessObjectImpl(String uri, AccessObjectType type) {
        this.uri = uri;
        this.type = type;
    }

    public AccessObjectImpl(String uri) {
        this.uri = uri;
        this.type = AccessObjectType.NAMED_OBJECT;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AccessObjectImpl) {
            AccessObjectImpl that = (AccessObjectImpl) o;
            return equivalent(this.uri, that.uri);
        }
        return false;
    }

    private boolean equivalent(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    @Override
    public String toString() {
        return "SimpleRequestedAction['" + uri + "']";
    }

    @Override
    public AccessObjectType getType() {
        return type;
    }

}
