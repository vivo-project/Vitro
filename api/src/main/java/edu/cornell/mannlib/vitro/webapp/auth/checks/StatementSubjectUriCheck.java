/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StatementSubjectUriCheck extends AbstractCheck {

    private static final Log log = LogFactory.getLog(StatementSubjectUriCheck.class);

    public StatementSubjectUriCheck(String uri, AttributeValueSet values) {
        super(uri, values);
    }

    @Override
    public boolean check(AuthorizationRequest ar) {
        AccessObject ao = ar.getAccessObject();
        final String inputValue = ao.getStatementSubject();
        if (AttributeValueChecker.test(this, ar, inputValue)) {
            log.debug("Attribute value match requested statement subject uri '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute value don't match requested statement subject uri '" + inputValue + "'");
        return false;
    }

    @Override
    public Attribute getAttributeType() {
        return Attribute.STATEMENT_SUBJECT_URI;
    }

}
