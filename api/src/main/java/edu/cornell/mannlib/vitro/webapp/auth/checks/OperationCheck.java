/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OperationCheck extends AbstractCheck {

    private static final Log log = LogFactory.getLog(OperationCheck.class);

    public OperationCheck(String uri, AttributeValueSet values) {
        super(uri, values);
    }

    @Override
    public boolean check(AuthorizationRequest ar) {
        AccessOperation aop = ar.getAccessOperation();
        final String inputValue = aop.toString();
        if (AttributeValueChecker.test(this, ar, inputValue)) {
            log.debug("Checked operation match requested operation '" + inputValue + "'");
            return true;
        }
        log.debug("Checked operation don't match requested operation '" + inputValue + "'");
        return false;
    }

    @Override
    public Attribute getAttributeType() {
        return Attribute.OPERATION;
    }

}
