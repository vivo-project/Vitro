/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueContainer;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public interface Check {

    public void setUri(String uri);

    public String getUri();

    public boolean check(AuthorizationRequest ar);

    public Attribute getAttributeType();

    public CheckType getType();

    public AttributeValueContainer getValues();

    public void addValue(String value);

    public void setType(CheckType valueOf);

    public long getComputationalCost();

}
