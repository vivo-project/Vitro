/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsRootUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubjectTypeCheck extends AbstractCheck {

    private static final Log log = LogFactory.getLog(SubjectTypeCheck.class);

    public SubjectTypeCheck(String uri, AttributeValueSet values) {
        super(uri, values);
    }

    @Override
    public boolean check(AuthorizationRequest ar) {
        IdentifierBundle ac_subject = ar.getIds();
        String inputValue = IsRootUser.isRootUser(ac_subject) ? "ROOT_USER" : "";
        if (AttributeValueChecker.test(this, ar, inputValue)) {
            log.debug("Attribute subject type match requested object type '");
            return true;
        } else {
            log.debug("Attribute subject type don't match requested object type '");
            return false;
        }
    }

    @Override
    public Attribute getAttributeType() {
        return Attribute.SUBJECT_TYPE;
    }
}
