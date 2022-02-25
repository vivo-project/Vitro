package edu.cornell.mannlib.vitro.webapp.dynapi.components;


import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.LogFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import org.apache.commons.logging.Log;

public abstract class AbstractOperation implements Operation{

    protected Parameters requiredParams = new Parameters();

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
    public void addRequiredParameter(Parameter param) {
        requiredParams.add(param);
    }

    public Parameters getRequiredParams() {
        return requiredParams;
    }

    protected boolean isInputValid(OperationData input) {
        Log log = LogFactory.getLog(this.getClass().getName());
        for (String name : requiredParams.getNames()) {
            if (!input.has(name)) {
                log.error("Parameter " + name + " not found");
                return false;
            }
            Parameter param = requiredParams.get(name);
            String[] inputValues = input.get(name);
            if (!param.isValid(name, inputValues)){
                return false;
            }
        }
        return true;
    }
}
