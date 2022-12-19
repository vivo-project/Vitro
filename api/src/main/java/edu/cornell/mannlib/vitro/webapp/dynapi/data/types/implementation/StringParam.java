package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringParam extends Parameter {

	private static final String TYPE_NAME = "plain string";
	private static final Log log = LogFactory.getLog(StringParam.class);

	public StringParam(String var) {
		this.setName(var);
		try {
			ParameterType type = new ParameterType();
			type.setName(TYPE_NAME);
			ImplementationType implType = new ImplementationType();
			type.setImplementationType(implType);
			implType.setSerializationConfig(getSerializationConfig());
			implType.setDeserializationConfig(getDeserializationConfig());	
			implType.setClassName(String.class.getCanonicalName());
			this.setType(type);
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	private ImplementationConfig getSerializationConfig() throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		serializationConfig.setClassName(String.class.getCanonicalName());
		serializationConfig.setMethodName("toString");
		serializationConfig.setMethodArguments("");
		serializationConfig.setStaticMethod(false);
		return serializationConfig;
	}
	
	private ImplementationConfig getDeserializationConfig() throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		serializationConfig.setClassName(String.class.getCanonicalName());
		serializationConfig.setMethodName("toString");
		serializationConfig.setMethodArguments("");
		serializationConfig.setStaticMethod(false);
		return serializationConfig;
	}
	
}
