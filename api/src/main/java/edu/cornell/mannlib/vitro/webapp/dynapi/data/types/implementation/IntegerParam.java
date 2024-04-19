/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types.implementation;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.PrimitiveSerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DataFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DefaultFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IntegerParam extends Parameter {

    private static final Log log = LogFactory.getLog(IntegerParam.class);

    public IntegerParam(String var) {
        this.setName(var);
        try {
            ParameterType type = new ParameterType();
            DataFormat defaultFormat = new DefaultFormat();
            type.addFormat(defaultFormat);
            type.setSerializationType(createSerializationType());
            defaultFormat.setSerializationConfig(getSerializationConfig());
            defaultFormat.setDeserializationConfig(getDeserializationConfig());
            type.addInterface(Integer.class);
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
        serializationConfig.setClass(Integer.class);
        serializationConfig.setMethodName("toString");
        serializationConfig.setMethodArguments("");
        serializationConfig.setStaticMethod(false);
        return serializationConfig;
    }

    private ConversionConfiguration getDeserializationConfig() throws ClassNotFoundException {
        ConversionConfiguration serializationConfig = new ConversionConfiguration();
        serializationConfig.setClass(Integer.class);
        serializationConfig.setMethodName("parseInt");
        serializationConfig.setMethodArguments("input");
        serializationConfig.setStaticMethod(true);
        return serializationConfig;
    }

}
