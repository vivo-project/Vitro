package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubjectRoleAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(SubjectRoleAttribute.class);

    public SubjectRoleAttribute(String uri, String roleValue) {
        super(uri, roleValue);
    }

    @Override
    public boolean match(AuthorizationRequest ar) {
        final List<String> inputValues = ar.getRoleUris();
        if (AttributeValueTester.test(this, ar, inputValues.toArray(new String[0]))) {
            log.debug("Attribute match requested '" + inputValues + "'");
            return true;
        }
        log.debug("Attribute don't match requested '" + inputValues + "'");
        return false;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.SUBJECT_ROLE_URI;
    }

}
