/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

public interface DataFormat {

    FormatName getName();

    ConversionConfiguration getSerializationConfig();

    ConversionConfiguration getDeserializationConfig();

    void setDefaultValue(String defaultValue);

    String getDefaultValue();

    void setSerializationConfig(ConversionConfiguration serializationConfig);

    void setDeserializationConfig(ConversionConfiguration deserializationConfig);

    Object serialize(ParameterType type, Object input);

    Object deserialize(ParameterType type, Object input);

}