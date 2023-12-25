/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import java.util.Optional;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxObjectPropertyWrapper;

public class FauxObjectPropertyAccessObject extends AccessObject {

    public FauxObjectPropertyAccessObject(ObjectProperty objectProperty) {
        setObjectProperty(objectProperty);
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.FAUX_OBJECT_PROPERTY;
    }

    public Optional<String> getUri() {
        Optional<ObjectProperty> op = getObjectProperty();
        if (!op.isPresent()) {
            return Optional.empty();
        }
        ObjectProperty property = op.get();
        if (property instanceof FauxObjectPropertyWrapper) {
            String uri = ((FauxObjectPropertyWrapper) property).getConfigUri();
            if (uri == null) {
                return Optional.empty();
            }
            return Optional.of(uri);
        }
        String uri = property.getURI();
        if (uri == null) {
            return Optional.empty();
        }
        return Optional.of(uri);
    }

    @Override
    public String toString() {
        Optional<ObjectProperty> op = getObjectProperty();
        if (!op.isPresent()) {
            return getClass().getSimpleName() + ": Object property is not present.";
        }
        ObjectProperty property = op.get();
        if (property instanceof FauxObjectPropertyWrapper) {
            return getClass().getSimpleName() + ": " + ((FauxObjectPropertyWrapper) property).getConfigUri();
        }
        return getClass().getSimpleName() + ": " + (property == null ? "not present." : property.getURI());
    }
}
