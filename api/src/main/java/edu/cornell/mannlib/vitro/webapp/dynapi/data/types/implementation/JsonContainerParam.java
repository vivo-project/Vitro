/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DataFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DefaultFormat;
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
            DataFormat defaultFormat = new DefaultFormat();
            type.addFormat(defaultFormat);
            defaultFormat.setSerializationConfig(getSerializationConfig());
            defaultFormat.setDeserializationConfig(getDeserializationConfig());
            type.addInterface(JsonContainer.class);
            this.setType(type);
            this.setDefaultValue(getContainerDefaultValue());
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private ConversionConfiguration getSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClass(JsonFactory.class);
        serializationConfig.setMethodName("serialize");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        serializationConfig.setInputInterface(JsonContainer.class);
        return serializationConfig;
    }

    private ConversionConfiguration getDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClass(JsonFactory.class);
        serializationConfig.setMethodName("deserialize");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        return serializationConfig;
    }

    protected abstract String getContainerDefaultValue();

    public void setValuesType(ParameterType valuesType) {
        type.setValuesType(valuesType);
    }
}
