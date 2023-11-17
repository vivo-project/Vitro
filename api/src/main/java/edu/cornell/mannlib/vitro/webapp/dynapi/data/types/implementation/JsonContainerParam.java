package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class JsonContainerParam extends Parameter {

	private static final Log log = LogFactory.getLog(JsonContainerParam.class);
    private ArrayParameterType type;

	public JsonContainerParam(String var) {
		this.setName(var);
		try {
		    type = new ArrayParameterType();
			type.setName(getContainerTypeName());
			ImplementationType implType = new ImplementationType();
			type.setImplementationType(implType);
			implType.setSerializationConfig(getSerializationConfig());
			implType.setDeserializationConfig(getDeserializationConfig());	
			implType.setClassName(JsonContainer.class.getCanonicalName());
			this.setType(type);
			this.setDefaultValue(getContainerDefaultValue());
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	private ImplementationConfig getSerializationConfig() throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		serializationConfig.setClassName(JsonContainer.class.getCanonicalName());
		serializationConfig.setMethodName("serialize");
		serializationConfig.setMethodArguments("input");
		serializationConfig.setStaticMethod(true);
		return serializationConfig;
	}
	
	private ImplementationConfig getDeserializationConfig() throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		serializationConfig.setClassName(JsonContainer.class.getCanonicalName());
		serializationConfig.setMethodName("deserialize");
		serializationConfig.setMethodArguments("input");
		serializationConfig.setStaticMethod(true);
		return serializationConfig;
	}
	
	protected abstract String getContainerTypeName();
	
	protected abstract String getContainerDefaultValue();
	
	public void setValuesType(ParameterType valuesType) {
	    type.setValuesType(valuesType);
	}
}
