/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeType;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

/**
 * A class of simple access rules.
 */
public class AccessRuleImpl implements AccessRule {
    private static final Log log = LogFactory.getLog(AccessRuleImpl.class);
    protected Map<String,Attribute> attributes = new HashMap<>();
    private boolean allowMatched = true;
    private String ruleUri;

    public boolean isAllowMatched() {
        return allowMatched;
    }

    public void setAllowMatched(boolean allowMatched) {
        this.allowMatched = allowMatched;
    }

    public String getRuleUri() {
        return ruleUri;
    }

    public void setRuleUri(String ruleUri) {
        this.ruleUri = ruleUri;
    }
    
    public Map<String, Attribute> getAttributes() {
        return attributes;
    }
    
    public boolean match(AuthorizationRequest ar) {
       for (Attribute a : attributes.values()) {
           if (!a.match(ar)) {
               return false;
           }
       }
       return true;
    }

    public void addAttribute(Attribute attr) {
        if (attributes.containsKey(attr.getUri())) {
            log.error(String.format("attribute %s already exists in the rule",attr.getUri()));
        }
        attributes.put(attr.getUri(), attr);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AccessRule)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        AccessRule compared = (AccessRule) object;

        return new EqualsBuilder()
                .append(getRuleUri(), compared.getRuleUri())
                .append(getAttributes(), compared.getAttributes())
                .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 101)
                .append(getAttributes())
                .append(getRuleUri())
                .toHashCode();
    }

    public Set<String> getAttributeUris() {
        return attributes.keySet();
    }
    
    public boolean containsAttributeUri(String uri) {
        return attributes.containsKey(uri);
    }

    public Set<Attribute> getAttributesByType(AttributeType type){
        return getAttributes().values().stream().filter(a -> a.getAttributeType().equals(type)).collect(Collectors.toSet());
    }
    
    public long getAttributesCount() {
        return attributes.size();
    }
    
    public Attribute getAttribute(String uri) {
        return attributes.get(uri);
    }
    
}
