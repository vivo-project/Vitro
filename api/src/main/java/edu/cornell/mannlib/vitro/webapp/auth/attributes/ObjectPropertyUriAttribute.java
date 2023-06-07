package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

public class ObjectPropertyUriAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(ObjectPropertyUriAttribute.class);

    public ObjectPropertyUriAttribute(String uri, String value) {
        super(uri, value);
    }

    @Override
    public boolean match(AuthorizationRequest ar) {
        AccessObject ao = ar.getAccessObject();
        ObjectProperty objectProperty = ao.getObjectProperty();
        String inputValue = objectProperty.getURI();
        if (objectProperty != null) {
            inputValue = objectProperty.getURI();
        }
        if (AttributeValueTester.test(this, ar, inputValue)) {
            log.debug("Attribute value match requested statement object property uri '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute value don't match requested statement object property uri '" + inputValue + "'");
        return false;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.OBJECT_PROPERTY_URI;
    }

}
