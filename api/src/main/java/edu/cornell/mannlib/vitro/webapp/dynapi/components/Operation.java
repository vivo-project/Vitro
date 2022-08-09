package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.RawData;

public abstract class Operation implements RunnableComponent, ParameterInfo {

    private static final Log log = LogFactory.getLog(Operation.class);

    public boolean isInputValid(DataStore inputOutput) {
        Parameters requiredParams = getRequiredParams();
        for (String name : requiredParams.getNames()) {
            if (!inputOutput.contains(name)) {
                log.error("Parameter " + name + " not found");
                return false;
            }
            Parameter param = requiredParams.get(name);
            RawData data = inputOutput.getData(name);
            if (!param.isValid(name, data)) {
                return false;
            }
        }
        return true;
    }

    public boolean isOutputValid(DataStore inputOutput) {
        Parameters providedParams = getProvidedParams();
        if (providedParams != null) {
            for (String name : providedParams.getNames()) {
                if (!inputOutput.contains(name)) {
                    log.error("Parameter " + name + " not found");
                    return false;
                }
            }
        }

        return true;
    }

}