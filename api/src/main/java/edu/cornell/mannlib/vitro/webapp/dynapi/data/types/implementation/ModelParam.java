package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.PrimitiveSerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiInMemoryOntModel;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.DynapiModelFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;

public class ModelParam extends Parameter {

	private static final Log log = LogFactory.getLog(ModelParam.class);

	public ModelParam(String name, boolean internal, boolean autocreate) {
		this.setName(name);
		try {
			ParameterType type = new ParameterType();
			type.setIsInternal(autocreate);
			if (internal) {
			    type.setName("internal model");
			} else {
			    type.setName("model");    
			}
			ImplementationType implType = new ImplementationType();
			type.setImplementationType(implType);
			PrimitiveSerializationType serializationType = new PrimitiveSerializationType();
			serializationType.setName("string");
            type.setSerializationType(serializationType);
			implType.setSerializationConfig(getSerializationConfig());
			implType.setDeserializationConfig(getDeserializationConfig(internal));	
			implType.setClassName(Model.class.getCanonicalName());
			if (internal) {
			    this.setDefaultValue(name);
			}
			this.setType(type);
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	private ImplementationConfig getSerializationConfig() throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		serializationConfig.setClassName(DynapiInMemoryOntModel.class.getCanonicalName());
		serializationConfig.setMethodName("serializeN3");
		serializationConfig.setMethodArguments("input");
		serializationConfig.setStaticMethod(true);
		return serializationConfig;
	}
	
	private ImplementationConfig getDeserializationConfig(boolean internal) throws ClassNotFoundException {
		ImplementationConfig serializationConfig = new ImplementationConfig();
		if (internal) {
		      serializationConfig.setClassName(DynapiModelFactory.class.getCanonicalName());
              serializationConfig.setMethodName("getModel");
		} else {
		      serializationConfig.setClassName(DynapiInMemoryOntModel.class.getCanonicalName());
		      serializationConfig.setMethodName("deserialize");
		}
		serializationConfig.setMethodArguments("input");
		serializationConfig.setStaticMethod(true);
		
		return serializationConfig;
	}
}
