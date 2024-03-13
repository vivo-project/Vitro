/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionMethod;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImplementationType {

    private static final Log log = LogFactory.getLog(ImplementationType.class);

    private Class<?> className;
    private ImplementationConfig serializationConfig;
    private ImplementationConfig deserializationConfig;
    private String defaultValue = "";

    public ImplementationConfig getSerializationConfig() {
        return serializationConfig;
    }

    public ImplementationConfig getDeserializationConfig() {
        return deserializationConfig;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#className", minOccurs = 1, maxOccurs = 1)
    public void setClassName(String className) throws ClassNotFoundException {
        this.className = Class.forName(className);
    }

    public Class<?> getClassName() {
        return className;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#defaultValue", minOccurs = 0, maxOccurs = 1)
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#serializationConfig", minOccurs = 1, maxOccurs = 1)
    public void setSerializationConfig(ImplementationConfig serializationConfig) {
        this.serializationConfig = serializationConfig;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#deserializationConfig", minOccurs = 1,
            maxOccurs = 1)
    public void setDeserializationConfig(ImplementationConfig deserializationConfig) {
        this.deserializationConfig = deserializationConfig;
    }

    public Object serialize(ParameterType type, Object input) {
        final ImplementationConfig config = serializationConfig;
        final boolean serialize = true;
        if (!config.isMethodInitialized()) {
            initializeMethod(type, config, serialize);
        }
        return invoke(config, type, input);
    }

    private void initializeMethod(ParameterType type, final ImplementationConfig config, final boolean serialize) {
        try {
            config.setConversionMethod(new ConversionMethod(type, serialize));
        } catch (InitializationException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public Object deserialize(ParameterType type, Object input) {
        final ImplementationConfig config = deserializationConfig;
        final boolean serialize = false;
        if (!config.isMethodInitialized()) {
            initializeMethod(type, config, serialize);
        }
        return invoke(config, type, input);
    }

    private Object invoke(ImplementationConfig config, ParameterType type, Object input) {
        try {
            return config.getConversionMethod().invoke(type, input);
        } catch (ConversionException e) {
            log.error("input: " + input);
            log.error("type: " + type);
            log.error(e, e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImplementationType)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        ImplementationType compared = (ImplementationType) object;

        return new EqualsBuilder()
                .append(className, compared.className)
                .append(serializationConfig, compared.serializationConfig)
                .append(deserializationConfig, compared.deserializationConfig)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(59, 103)
                .append(className)
                .append(serializationConfig)
                .append(deserializationConfig)
                .toHashCode();
    }
}
