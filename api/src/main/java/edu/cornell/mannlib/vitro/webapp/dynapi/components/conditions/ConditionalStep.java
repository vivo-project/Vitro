package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullStep;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Step;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ConditionalStep implements Step {

    private HashSet<Condition> conditions = new HashSet<Condition>();

    private boolean allConditionsRequired = true;  
    
    Step nextIfNotSatisfied = NullStep.getInstance();
    Step nextIfSatisfied = NullStep.getInstance();

    @Override
    public OperationResult run(OperationData data) {
        Step next;
        if (isSatisfied(data)) {
            next = nextIfSatisfied;
        } else {
            next = nextIfNotSatisfied;
        }
        return next.run(data);
    }

    private boolean isSatisfied(OperationData data) {
        if (allConditionsRequired) {
            return areAllSatisfied(data);
        } else {
            return isAtLeastOneSatisfied(data);     
        }
    }

    private boolean isAtLeastOneSatisfied(OperationData data) {
        for (Condition condition : conditions) {
            if (condition.isSatisfied(data)){
                return true;
            }
        }
        return false;
    }

    private boolean areAllSatisfied(OperationData data) {
        for (Condition condition : conditions) {
            if (!condition.isSatisfied(data)){
                return false;
            }
        }
        return true;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasCondition", minOccurs = 1, maxOccurs = 1)
    public void setCondition(Condition condition) {
        conditions.add(condition);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#nextIfNotSatisfied", minOccurs = 0, maxOccurs = 1)
    public void setNextIfNotSatisfied(Step step) {
        nextIfNotSatisfied = step;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#nextIfSatisfied", minOccurs = 0, maxOccurs = 1)
    public void setNextIfSatisfied(Step step) {
        nextIfSatisfied = step;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#allConditionsRequired", minOccurs = 0, maxOccurs = 1)
    public void setAllConditionsRequired(boolean allConditionsRequired) {
        this.allConditionsRequired = allConditionsRequired;
    }
    
    @Override
    public Set<StepInfo> getNextNodes() {
        HashSet<StepInfo> nextNodes = new HashSet<StepInfo>();
        nextNodes.add(nextIfNotSatisfied);
        nextNodes.add(nextIfSatisfied);
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
    public Parameters getProvidedParams() {
        return new Parameters();
    }
    
    @Override
    public Parameters getRequiredParams() {
        Parameters requiredParams = new Parameters();
        for (Condition condition: conditions) {
            requiredParams.addAll(condition.getRequiredParams());
        }
        return requiredParams;
    }

    @Override
    public void dereference() {}

}

