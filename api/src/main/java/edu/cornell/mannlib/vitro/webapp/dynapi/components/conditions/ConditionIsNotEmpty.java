package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ConditionIsNotEmpty implements Condition {

    private Parameters inputParams = new Parameters();
    
    @Override
    public boolean isSatisfied(DataStore data) {
        for (String name : SimpleDataView.getNames(inputParams)) {
            //RawData values = data.getData(name);
        	String value = SimpleDataView.getStringRepresentation(name, data);
			if (StringUtils.isBlank(value)) {
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
