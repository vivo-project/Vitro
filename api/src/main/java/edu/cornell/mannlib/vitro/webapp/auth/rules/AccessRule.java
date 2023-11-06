/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.rules;

import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.checks.Check;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public interface AccessRule {

    boolean isAllowMatched();

    void setAllowMatched(boolean allowMatched);

    String getRuleUri();

    void setRuleUri(String ruleUri);

    List<Check> getChecks();

    boolean match(AuthorizationRequest ar);

    void addCheck(Check attr);

    Set<String> getCheckUris();

    boolean containsCheckUri(String uri);

    Set<Check> getChecksByType(Attribute type);

    long getChecksCount();

    Check getCheck(String uri);
}
