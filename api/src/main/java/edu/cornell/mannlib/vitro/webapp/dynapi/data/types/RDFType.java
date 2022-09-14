package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class RDFType implements Removable {
	
	public static final String ANY_URI = "http://www.w3.org/2001/XMLSchema#anyURI";

    protected String name;
    private RDFDatatype rdfDataType;

    public RDFDatatype getRDFDataType() {
    	if (rdfDataType == null) {
    		rdfDataType =  new XSDDatatype(name);
    	}
        return rdfDataType;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

	public boolean isLiteral() {
		RDFDatatype type = getRDFDataType();
		String uri = type.getURI();
		return !ANY_URI.equals(uri);
	}

	public boolean isUri() {
		RDFDatatype type = getRDFDataType();
		String uri = type.getURI();
		return ANY_URI.equals(uri);
	}
	
    @Override
    public void dereference() {
        // TODO Auto-generated method stub
    }
    
}
