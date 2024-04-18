/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Format implements DataFormat {

    private static final Log log = LogFactory.getLog(Format.class);

    private ConversionConfiguration serializationConfig;
    private ConversionConfiguration deserializationConfig;
    private String defaultValue = "";

    public ConversionConfiguration getSerializationConfig() {
        return serializationConfig;
    }

    public ConversionConfiguration getDeserializationConfig() {
        return deserializationConfig;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#defaultValue", maxOccurs = 1)
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#serializationConfig", maxOccurs = 1)
    public void setSerializationConfig(ConversionConfiguration serializationConfig) {
        this.serializationConfig = serializationConfig;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#deserializationConfig", maxOccurs = 1)
    public void setDeserializationConfig(ConversionConfiguration deserializationConfig) {
        this.deserializationConfig = deserializationConfig;
    }

    public Object serialize(ParameterType type, Object input) {
        return invoke(serializationConfig, type, input);
    }

    private void initializeMethod(ConversionConfiguration config) {
        try {
            config.initialize();
        } catch (InitializationException e) {
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public Object deserialize(ParameterType type, Object input) {
        return invoke(deserializationConfig, type, input);
    }

    private Object invoke(ConversionConfiguration config, ParameterType type, Object input) {
        if (!config.isMethodInitialized()) {
            initializeMethod(config);
        }
        try {
            return config.getConversionMethod().invoke(type, input);
        } catch (ConversionException e) {
            log.error("input: " + input);
            log.error("type: " + type);
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public boolean equals(Object object) {
        if (!(object instanceof Format)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        Format compared = (Format) object;

        return new EqualsBuilder()
                .append(getSerializationConfig(), compared.getSerializationConfig())
                .append(getDeserializationConfig(), compared.getDeserializationConfig())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder(59, 103)
                .append(serializationConfig)
                .append(deserializationConfig)
                .toHashCode();
    }

}
