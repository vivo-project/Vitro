package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class AbstractAttribute implements Attribute {
    
    private Set<String> values = new HashSet<>();
    private String uri;
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
}
