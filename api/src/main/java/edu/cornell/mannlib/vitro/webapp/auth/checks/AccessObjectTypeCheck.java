/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AccessObjectTypeCheck extends AbstractCheck {

    private static final Log log = LogFactory.getLog(AccessObjectTypeCheck.class);

    public AccessObjectTypeCheck(String uri, AttributeValueSet values) {
        super(uri, values);
    }

    @Override
    public boolean check(AuthorizationRequest ar) {
        AccessObject ao = ar.getAccessObject();
        final String inputValue = ao.getType().toString();
        if (AttributeValueChecker.test(this, ar, inputValue)) {
            log.debug("Attribute match requested object type '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute don't match requested object type '" + inputValue + "'");
        return false;
    }

    @Override
    public Attribute getAttributeType() {
        return Attribute.ACCESS_OBJECT_TYPE;
    }
}
