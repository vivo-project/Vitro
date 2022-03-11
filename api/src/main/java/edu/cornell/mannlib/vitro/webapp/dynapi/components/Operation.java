package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;

public abstract class Operation implements RunnableComponent, ParameterInfo {

    private static final Log log = LogFactory.getLog(Operation.class);

    public boolean isInputValid(OperationData inputOutput) {
        Parameters requiredParams = getRequiredParams();
        if (requiredParams != null) {
            for (String name : requiredParams.getNames()) {
                if (!inputOutput.has(name)) {
                    log.error("Parameter " + name + " not found");
                    return false;
                }
                Parameter param = requiredParams.get(name);
                Data data = inputOutput.getData(name);
                if (!param.isValid(data)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isOutputValid(OperationData inputOutput) {
        Parameters providedParams = getProvidedParams();
        if (providedParams != null) {
            for (String name : providedParams.getNames()) {
                if (!inputOutput.has(name)) {
                    log.error("Parameter " + name + " not found");
                    return false;
                }
            }
        }

        return true;
    }

    public String computeProvidedFieldName(String partOfName) {
        return computeFieldName(partOfName, getProvidedParams());
    }

    public String computeRequiredFieldName(String partOfName) {
        return computeFieldName(partOfName, getRequiredParams());
    }

    private String computeFieldName(String partOfName, Parameters params) {
        String retVal = partOfName;
        if (params != null) {
            for (String name : params.getNames()) {
                Parameter parameter = params.get(name);
                String prefix = parameter.computePrefix(partOfName);
                if (prefix != null) {
                    retVal = prefix + partOfName;
                    break;
                }
            }
        }

        return retVal;
    }

}