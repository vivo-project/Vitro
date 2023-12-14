package edu.cornell.mannlib.vitro.webapp.auth.objects;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;

public class IndividualAccessObject extends NamedAccessObject {

    public IndividualAccessObject(String uri) {
        super(uri);
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.INDIVIDUAL;
    }
}
