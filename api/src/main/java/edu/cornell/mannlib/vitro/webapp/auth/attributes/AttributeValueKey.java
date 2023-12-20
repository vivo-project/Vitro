package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AttributeValueKey {

    private AccessOperation ao;
    private AccessObjectType aot;
    private String role;
    private String type;
    private Set<String> namedKeyComponents = new HashSet<>();

    public Set<String> getNamedKeyComponents() {
        return namedKeyComponents;
    }

    public AttributeValueKey() {
    }

    public AttributeValueKey(AccessOperation ao, AccessObjectType aot, String role, String type,
            String... namedKeyComponents) {
        this.ao = ao;
        this.aot = aot;
        this.role = role;
        this.type = type;
        this.namedKeyComponents = new HashSet<>(Arrays.asList(namedKeyComponents));
    }

    public AttributeValueKey(AccessOperation ao, AccessObjectType aot, String role, String type,
            Set<String> namedKeyComponents) {
        this.ao = ao;
        this.aot = aot;
        this.role = role;
        this.type = type;
        this.namedKeyComponents = namedKeyComponents;
    }

    public AccessOperation getAccessOperation() {
        return ao;
    }

    public void setOperation(AccessOperation ao) {
        this.ao = ao;
    }

    public AccessObjectType getObjectType() {
        return aot;
    }

    public void setObjectType(AccessObjectType aot) {
        this.aot = aot;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addNamedKey(String key) {
        namedKeyComponents.add(key);
    }

    public AttributeValueKey clone() {
        return new AttributeValueKey(ao, aot, role, type, namedKeyComponents);
    }

    public boolean isEmpty() {
        return ao == null && aot == null && role == null && type == null;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AttributeValueKey)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        AttributeValueKey compared = (AttributeValueKey) object;

        return new EqualsBuilder().append(getAccessOperation(), compared.getAccessOperation())
                .append(getObjectType(), compared.getObjectType()).append(getRole(), compared.getRole())
                .append(getType(), compared.getType()).append(getNamedKeyComponents(), compared.getNamedKeyComponents())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(151, 1017).append(getAccessOperation()).append(getObjectType()).append(getRole())
                .append(getType()).append(getNamedKeyComponents()).toHashCode();
    }
}
