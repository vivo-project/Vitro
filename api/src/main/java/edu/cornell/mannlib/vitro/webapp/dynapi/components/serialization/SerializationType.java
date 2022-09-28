package edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Removable;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class SerializationType implements Removable {

    protected String name;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void dereference() {
        // TODO Auto-generated method stub
    }

	public String getName() {
		return name;
	}

}
