/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

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

    public String getUri() {
        ObjectProperty op = getObjectProperty();
        if (op instanceof FauxObjectPropertyWrapper) {
            return ((FauxObjectPropertyWrapper) op).getConfigUri();
        }
        if (op != null) {
            return op.getURI();
        }
        return null;
    }

    @Override
    public String toString() {
        ObjectProperty op = getObjectProperty();
        if (op instanceof FauxObjectPropertyWrapper) {
            return getClass().getSimpleName() + ": " + ((FauxObjectPropertyWrapper) op).getConfigUri();
        }
        return getClass().getSimpleName() + ": " + (op == null ? op : op.getURI());
    }
}
