package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsRootUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubjectTypeAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(SubjectTypeAttribute.class);

    public SubjectTypeAttribute(String uri, String value) {
        super(uri, value);
    }

    @Override
    public boolean match(AuthorizationRequest ar) {
        IdentifierBundle ac_subject = ar.getIds();
        String inputValue = IsRootUser.isRootUser(ac_subject) ? "ROOT_USER" : "";
        if (AttributeValueTester.test(this, ar, inputValue)) {
            log.debug("Attribute subject type match requested object type '");
            return true;
        } else {
            log.debug("Attribute subject type don't match requested object type '");
            return false;
        }
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.SUBJECT_TYPE;
    }
}
