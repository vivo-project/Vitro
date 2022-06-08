package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Collections;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;

public class NullStep implements Step {

    private static NullStep instance = new NullStep();

    public static NullStep getInstance() {
        return instance;
    }
    
    private NullStep() {}
    
    @Override
    public OperationResult run(OperationData input) {
        return new OperationResult(200);
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
    public Parameters getRequiredParams() {
        return new Parameters();
    }

    @Override
    public Parameters getProvidedParams() {
        return new Parameters();
    }

}
