package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.PrimitiveSerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.SerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.Validator;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Parameter implements Removable {

    private boolean isArray = false;
	private String name;
    private String description;
    private Validators validators = new Validators();
    private ParameterType type;
    
    private boolean local;

    public String getName() {
        return name;
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasType", minOccurs = 1, maxOccurs = 1)
    public void setType(ParameterType type) {
        this.type = type;
    }
    
    public ParameterType getType() {
    	return type;
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

    public boolean isValid(String name, String[] values) {
        return validators.isAllValid(name, values);
    }

    public String computePrefix(String fieldName) {
        String retVal = "";
        SerializationType serializationType = type.getSerializationType();
        if (serializationType instanceof PrimitiveSerializationType) {
            retVal = (name.equals(fieldName)) ? "" : null;
        } else if (!(fieldName.contains("."))) {
            retVal = null;
        } else {
            String fieldNameFirstPart = fieldName.substring(0, fieldName.indexOf("."));
            if (!(name.equals(fieldNameFirstPart))) {
                String restOfPrefix = serializationType.computePrefix(fieldName);
                retVal = (restOfPrefix != null) ? name + "." + restOfPrefix: null;
            } else {
                retVal = serializationType.computePrefix(fieldName.substring(fieldName.indexOf(".") + 1));
            }
        }

        return retVal;
    }

    @Override
    public void dereference() {

    }

    public boolean isLocal() {
        return local;
    }

	public boolean isArray() {
		return isArray ;
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

	public boolean isJsonObject() {
		// TODO Auto-generated method stub
		return false;
	}

}
