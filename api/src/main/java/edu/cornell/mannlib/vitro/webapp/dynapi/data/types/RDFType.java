package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class RDFType implements Removable {
	
	public static final String ANY_URI = "http://www.w3.org/2001/XMLSchema#anyURI";
	public static final String LANG_STRING = "langString";
	

    protected String name;
    private RDFDatatype rdfDataType;

    public RDFDatatype getRDFDataType() {
    	if (rdfDataType == null) {
    		createRDFDataType();
    	}
        return rdfDataType;
    }

	private void createRDFDataType() {
		if (LANG_STRING.equals(name)) {
			rdfDataType = RDFLangString.rdfLangString;
			return;		
		}
		rdfDataType = new XSDDatatype(name);
	}

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }
    
    private String getName() {
        return name;
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
    public void dereference() {}
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RDFType)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        RDFType compared = (RDFType) object;

        return new EqualsBuilder()
                .append(getName(), compared.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(47, 201)
                .append(getName())
                .toHashCode();
    }
}
