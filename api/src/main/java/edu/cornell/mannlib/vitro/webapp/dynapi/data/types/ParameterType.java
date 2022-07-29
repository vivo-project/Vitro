package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.SerializationType;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ParameterType implements Removable {

    protected String name;
	private SerializationType serializationType;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#serializationType", minOccurs = 1, maxOccurs = 1)
    public void setSerializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }
    
    public SerializationType getSerializationType() {
    	return serializationType;
    }
    
	@Override
	public void dereference() {
	}
}
