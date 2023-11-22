/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StatementPredicateUriCheck extends AbstractCheck {

    private static final Log log = LogFactory.getLog(StatementPredicateUriCheck.class);

    public StatementPredicateUriCheck(String uri, AttributeValueSet values) {
        super(uri, values);
    }

    @Override
    public boolean check(AuthorizationRequest ar) {
        AccessObject ao = ar.getAccessObject();
        final String inputValue = ao.getStatementPredicateUri();
        if (AttributeValueChecker.test(this, ar, inputValue)) {
            log.debug("Attribute value match requested statement predicate uri '" + inputValue + "'");
            return true;
        }
        log.debug("Attribute value don't match requested statement predicate uri '" + inputValue + "'");
        return false;
    }

    @Override
    public Attribute getAttributeType() {
        return Attribute.STATEMENT_PREDICATE_URI;
    }

}
