package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public class ObjectUriAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(ObjectUriAttribute.class);

    public ObjectUriAttribute(String uri, String objectUri) {
        super(uri, objectUri);
    }

    @Override
    public boolean match(AuthorizationRequest ar) {
        AccessObject ao = ar.getAccessObject();
        final String inputValue = ao.getUri();
        if (AttributeValueTester.test(this, ar, inputValue)) {
            log.debug("Attribute match requested '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute don't match requested '" + inputValue + "'");
        return false;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.OBJECT_URI;
    }

}
