package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OperationAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(OperationAttribute.class);

    public OperationAttribute(String uri, String operation) {
        super(uri, operation);
    }

    @Override
    public boolean match(AuthorizationRequest ar) {
        AccessOperation aop = ar.getAccessOperation();
        final String inputValue = aop.toString();
        if (AttributeValueTester.test(this, ar, inputValue)) {
            log.debug("Attribute match requested operation '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute don't match requested operation '" + inputValue + "'");
        return false;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.OPERATION;
    }

}
