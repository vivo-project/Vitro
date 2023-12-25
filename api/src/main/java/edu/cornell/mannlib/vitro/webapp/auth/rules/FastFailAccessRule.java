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

import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.checks.Check;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FastFailAccessRule implements AccessRule {
    private static final Log log = LogFactory.getLog(FastFailAccessRule.class);
    protected Map<String, Check> checksMap = new HashMap<>();
    protected List<Check> checks = new ArrayList<Check>();
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

    public List<Check> getChecks() {
        return checks;
    }

    public boolean match(AuthorizationRequest ar) {
        for (Check check : checks) {
            if (!check.check(ar)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Check %s didn't match request %s", check.getUri(), ar));
                }
                return false;
            }
        }
        return true;
    }

    public void addCheck(Check attr) {
        if (checksMap.containsKey(attr.getUri())) {
            log.error(String.format("Check %s already exists in the rule", attr.getUri()));
        }
        checks.add(attr);
        Collections.sort(checks, comparator);
        checksMap.put(attr.getUri(), attr);
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
                .append(getChecks(), compared.getChecks())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 101)
                .append(getChecks())
                .append(getRuleUri())
                .toHashCode();
    }

    public Set<String> getCheckUris() {
        return checksMap.keySet();
    }

    public boolean containsCheckUri(String uri) {
        return checksMap.containsKey(uri);
    }

    public Set<Check> getChecksByType(Attribute type) {
        return getChecks().stream().filter(a -> a.getAttributeType().equals(type)).collect(Collectors.toSet());
    }

    public long getChecksCount() {
        return checks.size();
    }

    public Check getCheck(String uri) {
        return checksMap.get(uri);
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
