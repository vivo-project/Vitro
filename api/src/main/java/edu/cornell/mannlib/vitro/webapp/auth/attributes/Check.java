/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public interface Check {

    public void setUri(String uri);

    public String getUri();

    public boolean check(AuthorizationRequest ar);

    public Attribute getAttributeType();

    public CheckType getType();

    Set<String> getValues();

    public void addValue(String value);

    public void setType(CheckType valueOf);

    public long getComputationalCost();

}
