package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionMethod;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ImplementationType {

	private Class<?> className;
	private ImplementationConfig serializationConfig;
	private ImplementationConfig deserializationConfig;

	public ImplementationConfig getSerializationConfig() {
		return serializationConfig;
	}

	public ImplementationConfig getDeserializationConfig() {
		return deserializationConfig;
	}

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
	
	public Object serialize(ParameterType type, Object input) throws ConversionException {
		if (!serializationConfig.isMethodInitialized()) {
			try {
				serializationConfig.setConversionMethod(new ConversionMethod(type, true));
			} catch (InitializationException e) {
				e.printStackTrace();
			}
		}
		return serializationConfig.getConversionMethod().invoke(type, input);
	}
	
	public Object deserialize(ParameterType type, Object input) throws ConversionException {
		if (!deserializationConfig.isMethodInitialized()) {
			try {
				deserializationConfig.setConversionMethod(new ConversionMethod(type, false));
			} catch (InitializationException e) {
				e.printStackTrace();
			}
		}
		return deserializationConfig.getConversionMethod().invoke(type, input);
	}
}
