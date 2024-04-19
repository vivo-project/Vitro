/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DataFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DefaultFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringParam extends Parameter {

    private static final Log log = LogFactory.getLog(StringParam.class);

    public StringParam(String var) {
        this.setName(var);
        try {
            ParameterType type = new ParameterType();
            DataFormat defaultFormat = new DefaultFormat();
            type.addFormat(defaultFormat);
            defaultFormat.setSerializationConfig(getSerializationConfig());
            defaultFormat.setDeserializationConfig(getDeserializationConfig());
            type.addInterface(String.class);
            this.setType(type);
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private ConversionConfiguration getSerializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClass(String.class);
        serializationConfig.setMethodName("toString");
        serializationConfig.setMethodArguments("");
        serializationConfig.setStaticMethod(false);
        serializationConfig.setInputInterface(String.class);
        return serializationConfig;
    }

    private ConversionConfiguration getDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClass(String.class);
        serializationConfig.setMethodName("toString");
        serializationConfig.setMethodArguments("");
        serializationConfig.setStaticMethod(false);
        return serializationConfig;
    }

}
