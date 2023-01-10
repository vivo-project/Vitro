package edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class JsonContainerSerializationType extends SerializationType {

    private Parameters internalElements = new Parameters();

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasInternalElement")
    public void addInternalElement(Parameter param) {
        internalElements.add(param);
    }

    public Parameters getInternalElements() {
        return internalElements;
    }
 
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JsonContainerSerializationType)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        JsonContainerSerializationType compared = (JsonContainerSerializationType) object;

        return new EqualsBuilder()
                .append(getName(), compared.getName())
                .append(getInternalElements(), compared.getInternalElements())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 97)
                .append(getName())
                .append(getInternalElements())
                .toHashCode();
    }
}
