package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;

public class LoopOperationExecution {

    private static final Log log = LogFactory.getLog(LoopOperationExecution.class.getName());
    private List<ProcedureDescriptor> conditionDescriptors;
    private List<ProcedureDescriptor> inputDescriptors;
    private List<ProcedureDescriptor> outputDescriptors;
    Map<ProcedureDescriptor, DataStore> conditions = new HashMap<>();

    private ProcedureDescriptor executableDescriptor;
    private DataStore loopStore = new DataStore();
    private DataStore dataStore;
    private Parameters loopInternalParams;
    private Parameters loopInputParams;
    private Parameters loopOutputParams;

    public LoopOperationExecution(DataStore dataStore, LoopOperation loopOperation) throws ConversionException {
        conditionDescriptors = loopOperation.getConditionDescriptors();
        inputDescriptors = loopOperation.getInputDescriptors();
        outputDescriptors = loopOperation.getOutputDescriptors();
        executableDescriptor = loopOperation.getExecutableDescriptor();
        loopInternalParams = loopOperation.getInternalParams();
        loopOutputParams = loopOperation.getOutputParams();
        loopInputParams = loopOperation.getInputParams();
        for ( ProcedureDescriptor pd : conditionDescriptors) {
            DataStore expectedData = new DataStore();
            Converter.convertInternalParams(pd.getOutputParams(), expectedData);
            conditions.put(pd, expectedData);
        }
        this.dataStore = dataStore;
    }
    
    public OperationResult executeLoop() throws ConversionException {
        initilaizeLocalStore(dataStore, loopStore, loopInputParams, loopInternalParams);
        if (areConditionsSatisfied()) {
            for( ProcedureDescriptor inputDescriptor : inputDescriptors) {
                execute(inputDescriptor);
            }
            execute(executableDescriptor);
            for( ProcedureDescriptor outputDescriptor : outputDescriptors) {
                execute(outputDescriptor);
            }    
        }
        copyDataToStore(loopStore, dataStore, loopOutputParams);
        return OperationResult.ok();
    }

    private boolean areConditionsSatisfied() throws ConversionException {
        for( ProcedureDescriptor conditionDescriptor : conditionDescriptors) {
            if (!isConditionSatisfied(conditionDescriptor)) {
                log.debug("Condition uri:'" + conditionDescriptor.getUri() + "' is not satisfied.");
                return false;
            }
        }
        return true;
    }

    private boolean isConditionSatisfied(ProcedureDescriptor conditionDescriptor) throws ConversionException {
        DataStore localStore = new DataStore();
        String uri = conditionDescriptor.getUri();
        Action conditionCheck = loopStore.getDependency(uri);
        initilaizeLocalStore(loopStore, localStore, conditionDescriptor.getInputParams(), conditionCheck.getInternalParams());
        conditionCheck.run(localStore);
        DataStore expectedData = conditions.get(conditionDescriptor);
        if (localStore.containsData(expectedData)) {
           return true;
        }
        return false;
    }

    private void execute(ProcedureDescriptor procedureDescriptor) throws ConversionException {
        DataStore localStore = new DataStore();
        String uri = procedureDescriptor.getUri();
        Action procedure = loopStore.getDependency(uri);
        initilaizeLocalStore(loopStore, localStore, procedureDescriptor.getInputParams(), procedure.getInternalParams());
        procedure.run(localStore);
        copyDataToStore(localStore, loopStore, procedureDescriptor.getOutputParams());
    }
    
    private void initilaizeLocalStore(DataStore externalStore, DataStore localStore, Parameters paramsToCopy,
            Parameters localParams) throws ConversionException {
        copyDataToStore(externalStore, localStore, paramsToCopy);
        localStore.putDependencies(externalStore.getDependencies());
        Converter.convertInternalParams(localParams, localStore);
    }

    private void copyDataToStore(DataStore fromStore, DataStore toStore, Parameters params) {
        for (String name : params.getNames()) {
            Data data = fromStore.getData(name);
            toStore.addData(name, data);
        }
    }

}
