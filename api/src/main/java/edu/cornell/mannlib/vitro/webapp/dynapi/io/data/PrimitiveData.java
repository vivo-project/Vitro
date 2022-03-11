package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Validators;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;

public abstract class PrimitiveData<T> implements Data {

    protected T value;

    protected PrimitiveData(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    protected RDFDatatype getRDFDataType(){
        return new XSDDatatype(getType());
    }

    @Override
    public boolean checkType(ParameterType parameterType) {
        return parameterType.getRDFDataType().toString().equals(getRDFDataType().toString());
    }

    @Override
    public boolean isAllValid(String name, Validators validators, ParameterType type) {
        return validators.isAllValid(name, this.toString());
    }

    @Override
    public Data getElement(String fieldName) {
        return null;
    }

    @Override
    public boolean setElement(String fieldName, Data newData) {
        return false;
    }
}
