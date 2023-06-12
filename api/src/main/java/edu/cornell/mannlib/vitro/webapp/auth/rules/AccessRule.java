/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.rules;

import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeType;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public interface AccessRule {

    public boolean isAllowMatched();

    public void setAllowMatched(boolean allowMatched);

    public String getRuleUri();

    public void setRuleUri(String ruleUri);
    
	public List<Attribute> getAttributes();
	
	public boolean match(AuthorizationRequest ar);
	
	public void addAttribute(Attribute attr);

    public Set<String> getAttributeUris();
    
    public boolean containsAttributeUri(String uri);

    public Set<Attribute> getAttributesByType(AttributeType type);
    
    public long getAttributesCount();
    
    public Attribute getAttribute(String uri);
}
