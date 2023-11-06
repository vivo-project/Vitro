/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.objects;

import java.util.Optional;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxObjectPropertyWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ObjectPropertyAccessObject extends AccessObject {

    private static final Log log = LogFactory.getLog(ObjectPropertyAccessObject.class);

    public ObjectPropertyAccessObject(ObjectProperty objectProperty) {
        setObjectProperty(objectProperty);
        debug(objectProperty);
    }

    @Override
    public Optional<String> getUri() {
        Optional<ObjectProperty> op = getObjectProperty();
        if (op.isPresent()) {
            String uri = op.get().getURI();
            if (uri == null) {
                return Optional.empty();
            }
            return Optional.of(uri);
        }
        return Optional.empty();
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.OBJECT_PROPERTY;
    }

    @Override
    public String toString() {
        Optional<ObjectProperty> op = getObjectProperty();
        return getClass().getSimpleName() + ": " + (!op.isPresent() ? "not present." : op.get().getURI());
    }

    private void debug(ObjectProperty property) {
        if (property instanceof FauxObjectPropertyWrapper) {
            Throwable t = new Throwable();
            log.error("FauxObjectPropertyWrapper provided in ObjectPropertyAccessObject constructor");
            log.error(t, t);
        }
        if (property == null) {
            Throwable t = new Throwable();
            log.error("null provided in ObjectPropertyAccessObject constructor");
            log.error(t, t);
        }
    }
}
