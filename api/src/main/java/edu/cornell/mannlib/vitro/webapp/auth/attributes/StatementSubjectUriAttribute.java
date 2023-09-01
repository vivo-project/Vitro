/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StatementSubjectUriAttribute extends AbstractAttribute {

    private static final Log log = LogFactory.getLog(StatementSubjectUriAttribute.class);

    public StatementSubjectUriAttribute(String uri, String value) {
        super(uri, value);
    }

    @Override
    public boolean match(AuthorizationRequest ar) {
        AccessObject ao = ar.getAccessObject();
        final String inputValue = ao.getStatementSubject();
        if (AttributeValueTester.test(this, ar, inputValue)) {
            log.debug("Attribute value match requested statement subject uri '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute value don't match requested statement subject uri '" + inputValue + "'");
        return false;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.STATEMENT_SUBJECT_URI;
    }

}
