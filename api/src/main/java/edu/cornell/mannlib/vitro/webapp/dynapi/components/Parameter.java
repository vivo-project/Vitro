package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ObjectParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ArrayData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ObjectData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.PrimitiveData;
import org.apache.jena.datatypes.RDFDatatype;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.PrimitiveParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.validators.Validator;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class Parameter implements Removable {

    private String name;
    private String description;
    private Validators validators = new Validators();
    private ParameterType type;

    public String getName() {
        return name;
    }

    public RDFDatatype getRDFDataType() {
        return type.getRDFDataType();
    }

    public ParameterType getType() {
        return type;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasType", minOccurs = 1, maxOccurs = 1)
    public void setParamType(ParameterType type) {
        this.type = type;
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

    public Validators getValidators() {
        return validators;
    }

    public boolean isValid(Data data) {
        return (data != null) && (data.checkType(type)) && (data.isAllValid(name, validators, type));
    }

    public String computePrefix(String fieldName) {
        String retVal = "";
        if (type instanceof PrimitiveParameterType) {
            retVal = (name.equals(fieldName)) ? "" : null;
        } else if (!(fieldName.contains("."))) {
            retVal = null;
        } else {
            String fieldNameFirstPart = fieldName.substring(0, fieldName.indexOf("."));
            if (!(name.equals(fieldNameFirstPart))) {
                String restOfPrefix = type.computePrefix(fieldName);
                retVal = (restOfPrefix != null) ? name + "." + restOfPrefix: null;
            } else {
                retVal = type.computePrefix(fieldName.substring(fieldName.indexOf(".") + 1));
            }
        }

        return retVal;
    }

    @Override
    public void dereference() {

    }

}
