package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptorCall;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.Converter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;

public class LoopOperationExecution extends ProcedureDescriptorCall {

    private static final Log log = LogFactory.getLog(LoopOperationExecution.class.getName());
    private List<ProcedureDescriptor> conditionDescriptors;
    private List<ProcedureDescriptor> inputDescriptors;
    private List<ProcedureDescriptor> outputDescriptors;
    Map<ProcedureDescriptor, DataStore> conditions = new HashMap<>();
    private ProcedureDescriptor executableDescriptor;
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
        for (ProcedureDescriptor pd : conditionDescriptors) {
            DataStore expectedData = new DataStore();
            Converter.convertInternalParams(pd.getOutputParams(), expectedData);
            conditions.put(pd, expectedData);
        }
        this.dataStore = dataStore;
    }

    public OperationResult executeLoop() throws ConversionException, InitializationException {
        DataStore loopStore = new DataStore();
        initilaizeLocalStore(dataStore, loopStore, loopInputParams, loopInternalParams);
        while (allConditionsSatisfied(loopStore)) {
            for (ProcedureDescriptor inputDescriptor : inputDescriptors) {
                execute(inputDescriptor, loopStore);
            }
            execute(executableDescriptor, loopStore);
            for (ProcedureDescriptor outputDescriptor : outputDescriptors) {
                execute(outputDescriptor, loopStore);
            }
        }
        copyData(loopStore, dataStore, loopOutputParams);
        return OperationResult.ok();
    }

    private boolean allConditionsSatisfied(DataStore loopStore) throws ConversionException {
        for (ProcedureDescriptor conditionDescriptor : conditionDescriptors) {
            if (!isConditionSatisfied(conditionDescriptor, loopStore)) {
                log.debug("Condition uri:'" + conditionDescriptor.getUri() + "' is not satisfied.");
                return false;
            }
        }
        return true;
    }

    private boolean isConditionSatisfied(ProcedureDescriptor conditionDescriptor, DataStore loopStore) throws ConversionException {
        DataStore localStore = new DataStore();
        String uri = conditionDescriptor.getUri();
        Procedure conditionCheck = loopStore.getDependency(uri);
        initilaizeLocalStore(loopStore, localStore, conditionDescriptor.getInputParams(),
                conditionCheck.getInternalParams());
        OperationResult result = conditionCheck.run(localStore);
        if (result.hasError()) {
            throw new RuntimeException(formatErrorMessage(conditionDescriptor, uri));
        }
        DataStore expectedData = conditions.get(conditionDescriptor);
        if (localStore.containsData(expectedData)) {
            return true;
        }
        return false;
    }


}
