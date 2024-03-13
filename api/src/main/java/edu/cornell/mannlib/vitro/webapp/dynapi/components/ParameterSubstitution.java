/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ParameterSubstitution {

    private Parameter source;
    private Parameter target;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#substitutionSource", minOccurs = 1, maxOccurs = 1)
    public void setSource(Parameter source) {
        this.source = source;
    }

    public Parameter getSource() {
        return source;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#substitutionTarget", minOccurs = 1, maxOccurs = 1)
    public void setTarget(Parameter param) {
        target = param;
    }

    public Parameter getTarget() {
        return target;
    }
}
