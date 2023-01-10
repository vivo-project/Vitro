package edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class SerializationType implements Removable {

    protected String name;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void dereference() {}

	public String getName() {
		return name;
	}

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SerializationType)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        SerializationType compared = (SerializationType) object;

        return new EqualsBuilder()
                .append(getName(), compared.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 103)
                .append(getName())
                .toHashCode();
    }
	
}
