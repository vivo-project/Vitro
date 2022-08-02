package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.SerializationType;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ParameterType implements Removable {

    protected String name;
	private SerializationType serializationType;
	private RDFType rdftype;
	private ImplementationType implementationType;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#serializationType", minOccurs = 1, maxOccurs = 1)
    public void setSerializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#rdfType", minOccurs = 0, maxOccurs = 1)
    public void setRdfType(RDFType rdftype) {
        this.rdftype = rdftype;
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#implementationType", minOccurs = 1, maxOccurs = 1)
    public void setImplementationType(ImplementationType implementationType) {
        this.implementationType = implementationType;
    }
    
    public ImplementationType getImplementationType() {
    	return implementationType;
    }
    
    public boolean isLiteral() {
    	if (!isRdfType()) {
    		return false;
    	}
    	return rdftype.isLiteral();
    }
    
    public boolean isUri() {
    	if (!isRdfType()) {
    		return false;
    	}
    	return rdftype.isUri();
    }
    
    public boolean isRdfType() {
    	if (rdftype != null) {
    		return true;
    	}
    	return false;
    }
    
    public RDFType getRdfType() {
    	return rdftype;
    }
    
    public SerializationType getSerializationType() {
    	return serializationType;
    }
    
	@Override
	public void dereference() {
	}
}
