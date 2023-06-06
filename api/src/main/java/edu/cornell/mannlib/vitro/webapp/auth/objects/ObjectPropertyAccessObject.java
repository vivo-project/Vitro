package edu.cornell.mannlib.vitro.webapp.auth.objects;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

public class ObjectPropertyAccessObject extends AccessObject {

    public ObjectPropertyAccessObject(ObjectProperty objectProperty) {
        setObjectProperty(objectProperty);
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.OBJECT_PROPERTY;
    }
}