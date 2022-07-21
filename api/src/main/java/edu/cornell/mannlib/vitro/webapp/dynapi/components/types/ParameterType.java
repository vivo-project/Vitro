package edu.cornell.mannlib.vitro.webapp.dynapi.components.types;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class ParameterType implements Removable {

    protected String name;
	private String serializedType = "string";

    public RDFDatatype getRDFDataType() {
        return new XSDDatatype(name);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void dereference() {
        // TODO Auto-generated method stub
    }

    public abstract String computePrefix(String fieldName);

	public String getName() {
		return name;
	}
	
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#serializedType", minOccurs = 0, maxOccurs = 1)
    public void setSerializedType(String serializedType) {
        this.serializedType = serializedType;
    }

	public String getSerializedType() {
		//TODO: temporary hacks. Implement in ontology
		if (name.contains("integer")) {
			return "integer";
		}
		return serializedType;
	}

}
