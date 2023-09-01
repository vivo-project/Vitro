/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AccessObjectUriAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(AccessObjectUriAttribute.class);

    public AccessObjectUriAttribute(String uri, String objectUri) {
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
        return AttributeType.ACCESS_OBJECT_URI;
    }

}
