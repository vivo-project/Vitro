/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public interface Check {

    void setUri(String uri);

    String getUri();

    boolean check(AuthorizationRequest ar);

    Attribute getAttributeType();

    CheckType getType();

    AttributeValueSet getValues();

    void addValue(String value);

    void setType(CheckType valueOf);

    long getComputationalCost();

}
