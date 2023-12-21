/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class AbstractCheck implements Check {

    private AttributeValueSet values;
    private String uri;
    private long computationalCost;

    private CheckType testType = CheckType.EQUALS;

    public AbstractCheck(String uri, AttributeValueSet values) {
        this.uri = uri;
        this.values = values;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public CheckType getType() {
        return testType;
    }

    public void setType(CheckType testType) {
        this.testType = testType;
        adjustComputationCost(testType);
    }

    @Override
    public void addValue(String value) {
        values.add(value);
    }

    @Override
    public AttributeValueSet getValues() {
        return values;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AbstractCheck)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        AbstractCheck compared = (AbstractCheck) object;

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

    private void adjustComputationCost(CheckType testType) {
        switch (testType) {
            case EQUALS:
                computationalCost = 1;
                return;
            case NOT_EQUALS:
                computationalCost = 2;
                return;
            case ONE_OF:
                computationalCost = 100;
                return;
            case NOT_ONE_OF:
                computationalCost = 101;
                return;
            case STARTS_WITH:
                computationalCost = 1000;
                return;
            case SPARQL_SELECT_QUERY_RESULTS_CONTAIN:
                computationalCost = 10000;
                return;
            case SPARQL_SELECT_QUERY_RESULTS_NOT_CONTAIN:
                computationalCost = 10001;
                return;
            default:
                return;
        }
    }
}
