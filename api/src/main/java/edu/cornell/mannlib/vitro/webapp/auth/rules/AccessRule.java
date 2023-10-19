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

    public List<Check> getAttributes();

    public boolean match(AuthorizationRequest ar);

    public void addAttribute(Check attr);

    public Set<String> getAttributeUris();

    public boolean containsAttributeUri(String uri);

    public Set<Check> getAttributesByType(Attribute type);

    public long getAttributesCount();

    public Check getAttribute(String uri);
}
