/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import java.util.Collections;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ConditionHasValueTrue implements Condition {

    private static final String TRUE_STRING_REPRESENTATION = "true";
    private Parameters inputParams = new Parameters();

    @Override
    public boolean isSatisfied(DataStore data) {
        for (String name : SimpleDataView.getNames(inputParams)) {
            String value = SimpleDataView.getStringRepresentation(name, data);
            if (!TRUE_STRING_REPRESENTATION.equals(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Parameters getInputParams() {
        return inputParams;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter", minOccurs = 1)
    public void addInputParameter(Parameter param) {
        inputParams.add(param);
    }

    @Override
    public Map<String, ProcedureDescriptor> getDependencies() {
        return Collections.emptyMap();
    }
}
