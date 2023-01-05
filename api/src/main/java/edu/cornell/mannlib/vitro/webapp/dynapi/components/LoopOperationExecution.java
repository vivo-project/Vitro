package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.ActionPool;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public class LoopOperationExecution {

    private List<ProcedureDescriptor> conditionDescriptors;
    private List<ProcedureDescriptor> inputDescriptors;
    private List<ProcedureDescriptor> outputDescriptors;

    private ProcedureDescriptor executableDescriptor;
    private DataStore localStore;

    public LoopOperationExecution(DataStore localStore, LoopOperation loopOperation) {
        conditionDescriptors = loopOperation.getConditionDescriptors();
        inputDescriptors = loopOperation.getInputDescriptors();
        outputDescriptors = loopOperation.getOutputDescriptors();
        executableDescriptor = loopOperation.getExecutableDescriptor();
        this.localStore = localStore;
    }

    public OperationResult execute() {
        ActionPool pool = ActionPool.getInstance();

        Action executable = null;
        List<Action> inputConverters = new LinkedList<>();
        List<Action> outputConverters = new LinkedList<>();
        List<Action> conditions = new LinkedList<>();
        for ( ProcedureDescriptor pd : conditionDescriptors) {
            Action condition = pool.getByUri(pd.getUri());
            conditions.add(condition);
        }
        executable = pool.getByUri(executableDescriptor.getUri());
        for ( ProcedureDescriptor pd : inputDescriptors) {
            Action inputConverter = pool.getByUri(pd.getUri());
            inputConverters.add(inputConverter);
        }
        for ( ProcedureDescriptor pd : outputDescriptors) {
            Action outputConverter = pool.getByUri(pd.getUri());
            outputConverters.add(outputConverter);
        }

        for (Action inputConverter : inputConverters) {
            inputConverter.run(localStore);
        }
        executable.run(localStore);
        for (Action outputConverter : outputConverters) {
            outputConverter.run(localStore);
        }
        
        return OperationResult.ok();
    }
   

}
