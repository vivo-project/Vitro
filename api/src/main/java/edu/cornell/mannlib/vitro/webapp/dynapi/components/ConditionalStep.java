package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;

public class ConditionalStep implements Step {

    private Condition condition;
    
    Step nextStepIfFailed = NullStep.getInstance();
    Step nextStepIfSucceded = NullStep.getInstance();

    private Parameters parameters = new Parameters();


    @Override
    public OperationResult run(OperationData input) {
        return null;
    }

    @Override
    public Parameters getProvidedParams() {
        return new Parameters();
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public void dereference() {}

    @Override
    public Set<StepInfo> getNextNodes() {
        HashSet<StepInfo> nextNodes = new HashSet<StepInfo>();
        nextNodes.add(nextStepIfFailed);
        nextNodes.add(nextStepIfSucceded);
        return nextNodes;
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
        return parameters ;
    }

    public void setNextStepFalse(Step step) {
        nextStepIfFailed = step;
    }

    public void setNextStepTrue(Step step) {
        nextStepIfSucceded = step;
    }

}
