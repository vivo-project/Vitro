package edu.cornell.mannlib.vitro.webapp.auth.objects;

import java.util.Optional;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;

public class IndividualAccessObject extends AccessObject {

    private final String uri;

    public IndividualAccessObject(String uri) {
        this.uri = uri;
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.INDIVIDUAL;
    }

    @Override
    public Optional<String> getUri() {
        if (uri == null) {
            return Optional.empty();
        }
        return Optional.of(uri);
    }

}
