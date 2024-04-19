/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.SerializationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ParameterType implements Removable {

    private SerializationType serializationType;
    private RDFType rdftype;
    protected ParameterType nestedType = NullParameterType.getInstance();
    private boolean isInternal = false;
    private Set<Class<?>> interfaces = new HashSet<Class<?>>();
    private Map<FormatName, DataFormat> formats = new HashMap<FormatName, DataFormat>();

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

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasFormat", minOccurs = 1)
    public void addFormat(DataFormat format) {
        formats.put(format.getName(), format);
    }

    public DataFormat getDefaultFormat() {
        return formats.get(FormatName.DEFAULT);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#nestedType", minOccurs = 0, maxOccurs = 1)
    public void setValuesType(ParameterType nestedType) {
        this.nestedType = nestedType;
    }

    public ParameterType getNestedType() {
        return nestedType;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#interface", minOccurs = 1)
    public void addInterfaceName(String interfaceName) throws ClassNotFoundException {
        interfaces.add(Class.forName(interfaceName));
    }

    public void addInterface(Class<?> clazz) {
        interfaces.add(clazz);
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
        if (getDefaultFormat() == null) {
            throw new InitializationException("Default format is null");
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
                .append(getSerializationType(), compared.getSerializationType())
                .append(getDefaultFormat(), compared.getDefaultFormat())
                .append(getRdfType(), compared.getRdfType())
                .append(getNestedType(), compared.getNestedType())
                .append(getInterfaces(), compared.getInterfaces())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(43, 205)
                .append(getSerializationType())
                .append(getDefaultFormat())
                .append(getRdfType())
                .append(getNestedType())
                .append(getInterfaces())
                .toHashCode();
    }
}
