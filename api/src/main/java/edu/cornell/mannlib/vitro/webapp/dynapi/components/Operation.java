package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Operation implements RunnableComponent, ParameterInfo {

    private static final Log log = LogFactory.getLog(Operation.class);

    public boolean isInputValid(OperationData inputOutput) {
        Parameters requiredParams = getRequiredParams();
        if(requiredParams!=null) {
            for (String name : requiredParams.getNames()) {
                if (!inputOutput.has(name)) {
                    log.error("Parameter " + name + " not found");
                    return false;
                }
                Parameter param = requiredParams.get(name);
                String[] inputValues = inputOutput.get(name);
                if (!param.isValid(name, inputValues)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isOutputValid(OperationData inputOutput) {
        Parameters providedParams = getProvidedParams();
        if(providedParams!=null) {
            for (String name : providedParams.getNames()) {
                if (!inputOutput.has(name)) {
                    log.error("Parameter " + name + " not found");
                    return false;
                }
            }
        }
        return true;
    }

    public String computeProvidedFieldName(String partOfName){
        String retVal = partOfName;
        Parameters providedParams = getProvidedParams();
        if(providedParams!=null) {
            for (String name : providedParams.getNames()) {
                Parameter parameter = providedParams.get(name);
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