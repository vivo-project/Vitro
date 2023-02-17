package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;

public abstract class AbstractOperation implements Operation {

    private static final Log log = LogFactory.getLog(AbstractOperation.class);
    protected Parameters inputParams = new Parameters();
    protected Parameters outputParams = new Parameters();
    
    public Parameters getInputParams() {
        return inputParams;
    }

    public Parameters getOutputParams() {
        return outputParams;
    }
    
    public OperationResult run(DataStore dataStore) {
        OperationResult result = OperationResult.ok();
        if (!isInputValid(dataStore)) {
            return OperationResult.internalServerError();
        }
        try {
            result = runOperation(dataStore);    
        } catch(Exception t) {
            log.error(t,t);
            return OperationResult.internalServerError();
        }
        if (!isOutputValid(dataStore)) {
            return OperationResult.internalServerError();
        }
        return result;
    }

    protected abstract OperationResult runOperation(DataStore dataStore) throws Exception;

    public boolean isInputValid(DataStore dataStore) {
        if (!isValid()) {
            log.debug("Component configuration is invalid");
            return false;
        }
        if (dataStore == null) {
            log.error("data store is null");
            return false;
        }
        Parameters inputParams = getInputParams();
        for (String name : inputParams.getNames()) {
            if (!dataStore.contains(name)) {
                log.error("Parameter " + name + " not found");
                return false;
            }
            Parameter param = inputParams.get(name);
            Data data = dataStore.getData(name);
            if (!param.isValid(name, data)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isOutputValid(DataStore dataStore) {
        Parameters providedParams = getOutputParams();
        if (providedParams != null) {
            for (String name : providedParams.getNames()) {
                if (!dataStore.contains(name)) {
                    log.error("Parameter " + name + " not found");
                    return false;
                }
            }
        }
        return true;
    }
    
    public Map<String, ProcedureDescriptor> getDependencies(){
        return Collections.emptyMap();
    }
    
    public void dereference() {}
}
