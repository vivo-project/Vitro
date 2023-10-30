package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AttributeValueKey {

    private AccessOperation ao;
    private AccessObjectType aot;
    private String role;
    private String containerType;

    public AttributeValueKey() {
    }

    public AttributeValueKey(AccessOperation ao, AccessObjectType aot, String role, String containerType) {
        this.ao = ao;
        this.aot = aot;
        this.role = role;
        this.containerType = containerType;
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

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public AttributeValueKey clone() {
        return new AttributeValueKey(ao, aot, role, containerType);
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

        return new EqualsBuilder()
                .append(getAccessOperation(), compared.getAccessOperation())
                .append(getObjectType(), compared.getObjectType())
                .append(getRole(), compared.getRole())
                .append(getContainerType(), compared.getContainerType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(151, 1017)
                .append(getAccessOperation())
                .append(getObjectType())
                .append(getRole())
                .append(getContainerType())
                .toHashCode();
    }
}
