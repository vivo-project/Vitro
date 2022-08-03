package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import java.math.BigInteger;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ImplementationType {

	private Class<?> className;
	private ImplementationConfig serializationConfig;
	private ImplementationConfig deserializationConfig;


	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#className", minOccurs = 1, maxOccurs = 1)
	public void setName(String className) throws ClassNotFoundException {
		this.className = Class.forName(className);
	}

	public Class<?> getClassName() {
		return className;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#serializationConfig", minOccurs = 1, maxOccurs = 1)
	public void setSerializationConfig(ImplementationConfig serializationConfig) {
			this.serializationConfig = serializationConfig;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#deserializationConfig", minOccurs = 1, maxOccurs = 1)
	public void setDeserializationConfig(ImplementationConfig deserializationConfig) {
		this.deserializationConfig = deserializationConfig;
	}
}
