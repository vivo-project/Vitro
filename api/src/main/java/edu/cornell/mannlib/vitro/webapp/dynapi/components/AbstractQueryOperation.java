package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public abstract class AbstractQueryOperation extends AbstractOperation{

    protected Parameters providedParams = new Parameters();

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addProvidedParameter(Parameter param) {
        providedParams.add(param);
    }

    public Parameters getProvidedParams() {
        return providedParams;
    }
}
