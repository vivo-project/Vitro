package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public class StatementObjectUriAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(StatementObjectUriAttribute.class);

    public StatementObjectUriAttribute(String uri, String objectUri) {
        super(uri, objectUri);
    }

    @Override
    public boolean match(AuthorizationRequest ar) {
        AccessObject ao = ar.getAccessObject();
        final String inputValue = ao.getStatementObject();
        if (AttributeValueTester.test(this, ar, inputValue)) {
            log.debug("Attribute value match requested statement object uri '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute value don't match requested statement object uri '" + inputValue + "'");
        return false;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.STATEMENT_OBJECT_URI;
    }

}
