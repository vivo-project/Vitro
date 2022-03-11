package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import org.apache.jena.datatypes.xsd.XSDDatatype;

public class AnyURIData extends StringData {

    public AnyURIData(String value) {
        super(value);
    }

    @Override
    public String getType() {
        return "anyURI";
    }

    @Override
    public boolean checkType(ParameterType parameterType) {
        return (parameterType.getRDFDataType().toString().equals(getRDFDataType().toString()))  ?
                true    :   parameterType.getRDFDataType().toString().equals(new XSDDatatype(super.getType()).toString());
    }

}
