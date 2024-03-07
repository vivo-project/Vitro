package edu.cornell.mannlib.vitro.webapp.dynapi.access;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class UserGroup {

    private String name;
    private String label;

    @Property(uri = "http://www.w3.org/2000/01/rdf-schema#label")
    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        if (label != null) {
            return label;
        }
        return name;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#name", minOccurs = 1, maxOccurs = 1)
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
