/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.rules;

import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Check;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public interface AccessRule {

    public boolean isAllowMatched();

    public void setAllowMatched(boolean allowMatched);

    public String getRuleUri();

    public void setRuleUri(String ruleUri);

    public List<Check> getChecks();

    public boolean match(AuthorizationRequest ar);

    public void addCheck(Check attr);

    public Set<String> getCheckUris();

    public boolean containsCheckUri(String uri);

    public Set<Check> getChecksByType(Attribute type);

    public long getChecksCount();

    public Check getCheck(String uri);
}
