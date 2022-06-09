package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ConditionIsNotEmpty implements Condition {

    private Parameters requiredParams = new Parameters();
    
    @Override
    public boolean isSatisfied(OperationData data) {
        for (String name : requiredParams.getNames()) {
            String[] values = data.get(name);
            if (values.length == 0) {
                return false;
            }
            for (String value :values ) {
                if (StringUtils.isBlank(value)) {
                    return false;
                }
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
