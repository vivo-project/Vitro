/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ArrayParameterType extends ParameterType {

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#containsType", minOccurs = 1, maxOccurs = 1)
    public void setValuesType(ParameterType valuesType) {
        this.valuesType = valuesType;
    }
}
