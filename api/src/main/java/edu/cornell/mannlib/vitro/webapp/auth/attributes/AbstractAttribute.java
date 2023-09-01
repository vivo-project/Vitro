/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class AbstractAttribute implements Attribute {

    private Set<String> values = new HashSet<>();
    private String uri;
    private long computationalCost;

    private TestType testType = TestType.EQUALS;

    public AbstractAttribute(String uri, String value) {
        this.uri = uri;
        values.add(value);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public TestType getTestType() {
        return testType;
    }

    public void setTestType(TestType testType) {
        this.testType = testType;
        adjustComputationCost(testType);
    }

    @Override
    public void addValue(String value) {
        values.add(value);
    }

    @Override
    public Set<String> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AbstractAttribute)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        AbstractAttribute compared = (AbstractAttribute) object;

        return new EqualsBuilder()
                .append(getUri(), compared.getUri())
                .append(getValues(), compared.getValues())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 101)
                .append(getUri())
                .append(getValues())
                .toHashCode();
    }

    public long getComputationalCost() {
        return computationalCost;
    }

    private void adjustComputationCost(TestType testType) {
        switch (testType) {
            case ONE_OF:
                computationalCost += 100;
                return;
            case NOT_ONE_OF:
                computationalCost += 100;
                return;
            case STARTS_WITH:
                computationalCost += 1000;
                return;
            case SPARQL_SELECT_QUERY_CONTAINS:
                computationalCost += 10000;
                return;
            default:
                return;
        }
    }
}
