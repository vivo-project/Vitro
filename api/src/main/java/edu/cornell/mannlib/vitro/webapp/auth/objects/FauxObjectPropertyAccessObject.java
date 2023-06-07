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

    @Override
    public String toString() {
        ObjectProperty op = getObjectProperty();
        if (op instanceof FauxObjectPropertyWrapper) {
            return getClass().getSimpleName() + ": " + ((FauxObjectPropertyWrapper) op).getConfigUri();
        }
        return getClass().getSimpleName() + ": " + (op == null ? op : op.getURI());
    }
}