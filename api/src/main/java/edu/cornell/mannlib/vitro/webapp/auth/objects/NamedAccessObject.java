/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import java.util.Optional;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;

/**
 * A NamedAccessObject to be used for SimplePermission.
 */
public class NamedAccessObject extends AccessObject {
    private final String uri;
    private AccessObjectType type;

    public NamedAccessObject() {
        this.uri = "";
        this.type = AccessObjectType.NAMED_OBJECT;
    }

    public NamedAccessObject(String uri, AccessObjectType type) {
        this.uri = uri;
        this.type = type;
    }

    public NamedAccessObject(String uri) {
        this.uri = uri;
        this.type = AccessObjectType.NAMED_OBJECT;
    }

    @Override
    public Optional<String> getUri() {
        if (uri == null) {
            return Optional.empty();
        }
        return Optional.of(uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NamedAccessObject) {
            NamedAccessObject that = (NamedAccessObject) o;
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
