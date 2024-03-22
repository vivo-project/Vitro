package edu.cornell.mannlib.vitro.webapp.auth.objects;

import java.util.Optional;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;

public class IndividualAccessObject extends NamedAccessObject {

    public IndividualAccessObject(String uri) {
        super(uri);
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.INDIVIDUAL;
    }

    public String[] getResourceUris() {
        Optional<String> optionalUri = getUri();
        if (optionalUri.isPresent()) {
            return new String[] { optionalUri.get() };
        } else {
            return new String[0];
        }
    }
}
