/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

public class AccessRuleImpl implements AccessRule {
    private static final Log log = LogFactory.getLog(AccessRuleImpl.class);
    protected Map<String,Attribute> attributeMap = new HashMap<>();
    protected List<Attribute> attributes = new ArrayList<Attribute>();
    private static final Comparator<Attribute> comparator = getAttributeComparator();


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
    
    public List<Attribute> getAttributes() {
        return attributes;
    }
    
    public boolean match(AuthorizationRequest ar) {
       for (Attribute attribute : attributes) {
           if (!attribute.match(ar)) {
               return false;
           }
       }
       return true;
    }

    public void addAttribute(Attribute attr) {
        if (attributeMap.containsKey(attr.getUri())) {
            log.error(String.format("attribute %s already exists in the rule",attr.getUri()));
        }
        attributes.add(attr);
        Collections.sort(attributes, comparator);
        attributeMap.put(attr.getUri(), attr);
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
        return attributeMap.keySet();
    }
    
    public boolean containsAttributeUri(String uri) {
        return attributeMap.containsKey(uri);
    }

    public Set<Attribute> getAttributesByType(AttributeType type){
        return getAttributes().stream().filter(a -> a.getAttributeType().equals(type)).collect(Collectors.toSet());
    }
    
    public long getAttributesCount() {
        return attributes.size();
    }
    
    public Attribute getAttribute(String uri) {
        return attributeMap.get(uri);
    }
    
    private static Comparator<Attribute> getAttributeComparator() {
        return new Comparator<Attribute>() {
            @Override
            public int compare(Attribute latt, Attribute ratt) {
                if ( latt.getComputationalCost() > ratt.getComputationalCost() ) {
                    return -1;
                } else 
                if (latt.getComputationalCost() < ratt.getComputationalCost()) {
                    return 1;
                }
                return latt.getUri().compareTo(latt.getUri()); 
            }
        };
    }

    
}
