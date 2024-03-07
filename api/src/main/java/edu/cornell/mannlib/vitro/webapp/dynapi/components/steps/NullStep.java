package edu.cornell.mannlib.vitro.webapp.dynapi.components.steps;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public class NullStep implements Step {

    private static NullStep instance = new NullStep();

    public static NullStep getInstance() {
        return instance;
    }

    private NullStep() {
    }

    @Override
    public OperationResult run(DataStore input) {
        return OperationResult.ok();
    }

    @Override
    public void dereference() {
    }

    @Override
    public Set<StepInfo> getNextNodes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public Parameters getInputParams() {
        return new Parameters();
    }

    @Override
    public Parameters getOutputParams() {
        return new Parameters();
    }

    @Override
    public Map<String, ProcedureDescriptor> getDependencies() {
        return Collections.emptyMap();
    }

}
