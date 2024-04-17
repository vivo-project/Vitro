/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class ParameterConverter {

    public static Object serialize(ParameterType type, Object input) throws ConversionException,
            InitializationException {
        ConversionConfiguration config = type.getDefaultFormat().getSerializationConfig();
        if (!config.isMethodInitialized()) {
            config.setConversionMethod(new ConversionMethod(config));
        }
        return config.getConversionMethod().invoke(type, input).toString();
    }

    public static Object deserialize(ParameterType type, Object input) throws ConversionException,
            InitializationException {
        ConversionConfiguration config = type.getDefaultFormat().getDeserializationConfig();
        if (!config.isMethodInitialized()) {
            config.setConversionMethod(new ConversionMethod(config));
        }
        return config.getConversionMethod().invoke(type, input);
    }
}
