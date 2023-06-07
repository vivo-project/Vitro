package edu.cornell.mannlib.vitro.webapp.auth.objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectType;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxObjectPropertyWrapper;

public class ObjectPropertyAccessObject extends AccessObject {

    private static final Log log = LogFactory.getLog(ObjectPropertyAccessObject.class);

    public ObjectPropertyAccessObject(ObjectProperty objectProperty) {
        setObjectProperty(objectProperty);
        debug(objectProperty);
    }
    
    public String getUri() {
        ObjectProperty op = getObjectProperty();
        if (op != null) {
            return op.getURI();
        }
        return null;
    }

    @Override
    public AccessObjectType getType() {
        return AccessObjectType.OBJECT_PROPERTY;
    }

    @Override
    public String toString() {
        ObjectProperty op = getObjectProperty();
        return getClass().getSimpleName() + ": " + (op == null ? op : op.getURI());
    }
    
    private void debug(ObjectProperty property) {
        if (true) {
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
}