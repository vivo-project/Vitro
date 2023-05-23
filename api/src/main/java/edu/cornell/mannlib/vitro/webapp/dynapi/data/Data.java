package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class Data {

    private String rawString = null;
    private Object object = null;
    private Parameter param = null;

    public Data(Parameter param) {
        this.param = param;
    }

    protected void setObject(Object object) {
        this.object = object;
    }

    protected Object getObject() {
        return object;
    }

    public void setParam(Parameter param) {
        this.param = param;
    }
    
    public Parameter getParam() {
        return param;
    }

    public void setRawString(String rawString) {
        this.rawString = rawString;
    }

    protected String getRawString() {
        return rawString;
    }

    public void earlyInitialization() {
        initialization();
    }
    
    public boolean isInitialized() {
        return object != null;
    }

    public void initialization() {
        if(isOnlyInternalParameter() || isOptionalWithoutValue()) {
            initializeDefault();
            return;
        }
        initializeFromString();
    }

    private boolean isOnlyInternalParameter() {
        return param.isInternal() && !param.isOptional();
    }

    private boolean isOptionalWithoutValue() {
        return param.isOptional() && rawString == null;
    }
    
    public void initializeFromString() {
        final ParameterType type = param.getType();
        final ImplementationType implementationType = type.getImplementationType();
        object = implementationType.deserialize(type, rawString);
    }

    public String getSerializedValue() {
        final ParameterType type = param.getType();
        final ImplementationType implementationType = type.getImplementationType();
        if (object == null) {
            object = implementationType.deserialize(type, rawString);
        }
        return implementationType.serialize(type, object).toString();
    }

    public void initializeDefault() {
        ParameterType type = param.getType();
        ImplementationType implementationType = type.getImplementationType();
        String defaultValue = param.getDefaultValue();
        object = implementationType.deserialize(type, defaultValue);
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Data)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        Data d = (Data) object;
        
        return new EqualsBuilder()
                .append(param, d.param)
                .append(this.object, d.object)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(67, 392)
                .append(object)
                .append(param)
                .toHashCode();
    }

    public void copyObject(Data assignableData) {
        this.object = assignableData.getObject();
    }

}
