/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubjectRoleCheck extends AbstractCheck {

    private static final Log log = LogFactory.getLog(SubjectRoleCheck.class);

    public SubjectRoleCheck(String uri, AttributeValueSet values) {
        super(uri, values);
    }

    @Override
    public boolean check(AuthorizationRequest ar) {
        final List<String> inputValues = ar.getRoleUris();
        if (AttributeValueChecker.test(this, ar, inputValues.toArray(new String[0]))) {
            log.debug("Attribute match requested '" + inputValues + "'");
            return true;
        }
        log.debug("Attribute don't match requested '" + inputValues + "'");
        return false;
    }

    @Override
    public Attribute getAttributeType() {
        return Attribute.SUBJECT_ROLE_URI;
    }

}
