package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ConditionIsNotEmpty implements Condition {

    private Parameters requiredParams = new Parameters();
    
    @Override
    public boolean isSatisfied(DataStore data) {
        for (String name : SimpleDataView.getNames(requiredParams)) {
            //RawData values = data.getData(name);
        	String value = SimpleDataView.getStringRepresentation(name, data);
			if (StringUtils.isBlank(value)) {
				return false;
			}
        }
        return true;
    }

    @Override
    public Parameters getRequiredParams() {
        return requiredParams;
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter", minOccurs = 1)
    public void addRequiredParameter(Parameter param) {
        requiredParams.add(param);
    }
}
