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

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Check;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AccessRuleImpl implements AccessRule {
    private static final Log log = LogFactory.getLog(AccessRuleImpl.class);
    protected Map<String, Check> attributeMap = new HashMap<>();
    protected List<Check> attributes = new ArrayList<Check>();
    private static final Comparator<Check> comparator = getAttributeComparator();

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

    public List<Check> getAttributes() {
        return attributes;
    }

    public boolean match(AuthorizationRequest ar) {
        for (Check attribute : attributes) {
            if (!attribute.check(ar)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Attribute %s didn't match", attribute.getUri()));
                }
                return false;
            }
        }
        return true;
    }

    public void addAttribute(Check attr) {
        if (attributeMap.containsKey(attr.getUri())) {
            log.error(String.format("attribute %s already exists in the rule", attr.getUri()));
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

    public Set<Check> getAttributesByType(Attribute type) {
        return getAttributes().stream().filter(a -> a.getAttributeType().equals(type)).collect(Collectors.toSet());
    }

    public long getAttributesCount() {
        return attributes.size();
    }

    public Check getAttribute(String uri) {
        return attributeMap.get(uri);
    }

    private static Comparator<Check> getAttributeComparator() {
        return new Comparator<Check>() {
            @Override
            public int compare(Check latt, Check ratt) {
                if (latt.getComputationalCost() > ratt.getComputationalCost()) {
                    return -1;
                } else if (latt.getComputationalCost() < ratt.getComputationalCost()) {
                    return 1;
                }
                return latt.getUri().compareTo(latt.getUri());
            }
        };
    }

}
