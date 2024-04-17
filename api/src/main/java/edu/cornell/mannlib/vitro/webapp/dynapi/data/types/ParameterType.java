/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.SerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionMethod;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ParameterType implements Removable {

    private String name;
    private SerializationType serializationType;
    private RDFType rdftype;
    private ImplementationType implementationType;
    protected ParameterType nestedType = NullParameterType.getInstance();
    private boolean isInternal = false;
    private Set<Class<?>> interfaces = new HashSet<Class<?>>();

    public String getName() {
        return name;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#serializationType", minOccurs = 1, maxOccurs = 1)
    public void setSerializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#rdfType", minOccurs = 0, maxOccurs = 1)
    public void setRdfType(RDFType rdftype) {
        this.rdftype = rdftype;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#isInternal", maxOccurs = 1)
    public void setIsInternal(boolean isInternal) {
        this.isInternal = isInternal;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#implementationType", minOccurs = 1, maxOccurs = 1)
    public void setImplementationType(ImplementationType implementationType) {
        this.implementationType = implementationType;
    }

    public ImplementationType getImplementationType() {
        return implementationType;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#nestedType", minOccurs = 0, maxOccurs = 1)
    public void setValuesType(ParameterType nestedType) {
        this.nestedType = nestedType;
    }

    public ParameterType getNestedType() {
        return nestedType;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#interface", minOccurs = 1)
    public void addInterface(String className) throws ClassNotFoundException {
        interfaces.add(Class.forName(className));
    }

    public boolean hasInterface(Class<?> clazz) {
        return interfaces.contains(clazz);
    }

    public Set<Class<?>> getInterfaces() {
        return new HashSet<Class<?>>(interfaces);
    }

    public boolean isLiteral() {
        if (!isRdfType()) {
            return false;
        }
        return rdftype.isLiteral();
    }

    public boolean isUri() {
        if (!isRdfType()) {
            return false;
        }
        return rdftype.isUri();
    }

    public boolean isRdfType() {
        if (rdftype != null) {
            return true;
        }
        return false;
    }

    public RDFType getRdfType() {
        return rdftype;
    }

    public SerializationType getSerializationType() {
        return serializationType;
    }

    @Override
    public void dereference() {
    }

    public boolean isString() {
        return hasInterface(String.class);
    }

    public boolean isBoolean() {
        return hasInterface(Boolean.class);
    }

    public boolean isPlainString() {
        return isString() && !isRdfType();
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void initialize() throws InitializationException {
        if (implementationType == null) {
            throw new InitializationException("implementation type is null");
        }
        ConversionConfiguration deserializationConfig = implementationType.getDeserializationConfig();
        if (deserializationConfig != null) {
            deserializationConfig.setConversionMethod(new ConversionMethod(deserializationConfig));
        }
        ConversionConfiguration serializationConfig = implementationType.getSerializationConfig();
        if (serializationConfig != null) {
            serializationConfig.setConversionMethod(new ConversionMethod(serializationConfig));
        }

    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParameterType)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        ParameterType compared = (ParameterType) object;

        return new EqualsBuilder()
                .append(getName(), compared.getName())
                .append(getSerializationType(), compared.getSerializationType())
                .append(getImplementationType(), compared.getImplementationType())
                .append(getRdfType(), compared.getRdfType())
                .append(getNestedType(), compared.getNestedType())
                .append(getInterfaces(), compared.getInterfaces())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(43, 205)
                .append(getName())
                .append(getSerializationType())
                .append(getImplementationType())
                .append(getRdfType())
                .append(getNestedType())
                .append(getInterfaces())
                .toHashCode();
    }
}
