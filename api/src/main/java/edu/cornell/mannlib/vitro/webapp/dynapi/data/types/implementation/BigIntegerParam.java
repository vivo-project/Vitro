package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BigIntegerParam extends Parameter {

	private static final String TYPE_NAME = "big integer";
	private static final Log log = LogFactory.getLog(BigIntegerParam.class);

	public BigIntegerParam(String var) {
		this.setName(var);
		try {
			ParameterType type = new ParameterType();
			type.setName(TYPE_NAME);
			ImplementationType implType = new ImplementationType();
			type.setImplementationType(implType);
			implType.setSerializationConfig(getSerializationConfig());
			implType.setDeserializationConfig(getDeserializationConfig());	
			implType.setClassName(BigInteger.class.getCanonicalName());
			this.setType(type);
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	private ImplementationConfig getSerializationConfig() throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		serializationConfig.setClassName(BigInteger.class.getCanonicalName());
		serializationConfig.setMethodName("toString");
		serializationConfig.setMethodArguments("");
		serializationConfig.setStaticMethod(false);
		return serializationConfig;
	}
	
	private ImplementationConfig getDeserializationConfig() throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		serializationConfig.setClassName(BigInteger.class.getCanonicalName());
		serializationConfig.setMethodName("");
		serializationConfig.setMethodArguments("input");
		serializationConfig.setStaticMethod(true);
		return serializationConfig;
	}
	
}
