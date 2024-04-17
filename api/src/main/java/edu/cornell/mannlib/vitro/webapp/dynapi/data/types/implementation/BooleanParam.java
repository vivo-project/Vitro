/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BooleanParam extends Parameter {

    private static final String TYPE_NAME = "boolean";
    private static final Log log = LogFactory.getLog(BooleanParam.class);

    public BooleanParam(String var) {
        this.setName(var);
        try {
            ParameterType type = new ParameterType();
            type.setName(TYPE_NAME);
            ImplementationType implType = new ImplementationType();
            type.setImplementationType(implType);
            implType.setSerializationConfig(getSerializationConfig());
            implType.setDeserializationConfig(getDeserializationConfig());
            type.addInterface(Boolean.class.getCanonicalName());
            this.setType(type);
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private ConversionConfiguration getSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClassName(Boolean.class.getCanonicalName());
        serializationConfig.setMethodName("toString");
        serializationConfig.setMethodArguments("");
        serializationConfig.setStaticMethod(false);
        return serializationConfig;
    }

    private ConversionConfiguration getDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClassName(Boolean.class.getCanonicalName());
        serializationConfig.setMethodName("parseBoolean");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        return serializationConfig;

    }
}
