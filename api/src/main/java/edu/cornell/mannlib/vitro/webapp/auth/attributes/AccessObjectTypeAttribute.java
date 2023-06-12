package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public class AccessObjectTypeAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(AccessObjectTypeAttribute.class);

    public AccessObjectTypeAttribute(String uri, String value) {
        super(uri, value);
    }

    @Override
    public boolean match(AuthorizationRequest ar) {
        AccessObject ao = ar.getAccessObject();
        final String inputValue = ao.getType().toString();
        if (AttributeValueTester.test(this, ar, inputValue)) {
            log.debug("Attribute match requested object type '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute don't match requested object type '" + inputValue + "'");
        return false;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.ACCESS_OBJECT_TYPE;
    }
}
