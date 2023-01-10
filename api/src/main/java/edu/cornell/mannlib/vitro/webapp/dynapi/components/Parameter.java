package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.Validator;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Parameter implements Removable {

	private String name;
	private String description;
	private Validators validators = new Validators();
	private ParameterType type;
	private String defaultValue;
	private Boolean internal;

    public String getName() {
		return name;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasType", minOccurs = 1, maxOccurs = 1)
	public void setType(ParameterType type) throws InitializationException {
		type.initialize();
		this.type = type;
	}

	public ParameterType getType() {
		return type;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#defaultValue", minOccurs = 0, maxOccurs = 1)
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
	
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#isInternal", minOccurs = 0, maxOccurs = 1)
	public void setInternal(boolean internal) {
	    this.internal = internal;
    }

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#description", maxOccurs = 1)
	public void setDescription(String description) {
		this.description = description;
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasValidator")
	public void addValidator(Validator validator) {
		validators.add(validator);
	}

	public boolean isValid(String name, Data data) {
		return validators.isAllValid(name, data);
	}

	@Override
	public void dereference() {

	}

	public boolean isInternal() {
	    if (internal != null) {
	        return internal;
	    }
		return type.isInternal();
	}

	public boolean isArray() {
		return type.isArray();
	}

	public boolean isString() {
	    return type.isString();
	}
	
	public boolean isPlainString() {
        return type.isPlainString();
    }
	
	public String getInputPath() {
		return "";
	}

	public String getOutputPath() {
		return "";
	}

	public boolean isOptional() {
		return false;
	}

	public boolean isJsonContainer() {
		return type.isJsonContainer();
	}

    public String getDefaultValue() {
        if (defaultValue != null) {
            return defaultValue;
        }
        return type.getImplementationType().getDefaultValue();
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Parameter)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        Parameter compared = (Parameter) object;

        return new EqualsBuilder()
                .append(getName(), compared.getName())
                .append(getType(), compared.getType())
                .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(43, 205)
                .append(getName())
                .append(getType())
                .toHashCode();
    }

}
