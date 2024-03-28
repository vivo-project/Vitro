/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class JsonContainerParam extends Parameter {

    private static final Log log = LogFactory.getLog(JsonContainerParam.class);
    private ParameterType type;

    public JsonContainerParam(String var) {
        this.setName(var);
        try {
            type = new ParameterType();
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

    private ConversionConfiguration getSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClassName(JsonFactory.class.getCanonicalName());
        serializationConfig.setMethodName("serialize");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        return serializationConfig;
    }

    private ConversionConfiguration getDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClassName(JsonFactory.class.getCanonicalName());
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
