/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.PrimitiveSerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IntegerParam extends Parameter {

    private static final String TYPE_NAME = "integer";
    private static final Log log = LogFactory.getLog(IntegerParam.class);

    public IntegerParam(String var) {
        this.setName(var);
        try {
            ParameterType type = new ParameterType();
            type.setName(TYPE_NAME);
            ImplementationType implType = new ImplementationType();
            type.setImplementationType(implType);
            type.setSerializationType(createSerializationType());
            implType.setSerializationConfig(getSerializationConfig());
            implType.setDeserializationConfig(getDeserializationConfig());
            implType.setClassName(Integer.class.getCanonicalName());
            this.setType(type);
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private PrimitiveSerializationType createSerializationType() {
        PrimitiveSerializationType stype = new PrimitiveSerializationType();
        stype.setName("integer");
        return stype;
    }

    private ConversionConfiguration getSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClassName(Integer.class.getCanonicalName());
        serializationConfig.setMethodName("toString");
        serializationConfig.setMethodArguments("");
        serializationConfig.setStaticMethod(false);
        return serializationConfig;
    }

    private ConversionConfiguration getDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClassName(Integer.class.getCanonicalName());
        serializationConfig.setMethodName("parseInt");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        return serializationConfig;
    }

}
