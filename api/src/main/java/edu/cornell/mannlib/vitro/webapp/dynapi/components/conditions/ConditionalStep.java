package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.NullStep;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Step;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ConditionalStep implements Step {

    private HashSet<Condition> conditions = new HashSet<Condition>();

    private boolean allConditionsRequired = true;

    Step nextIfNotSatisfied = NullStep.getInstance();
    Step nextIfSatisfied = NullStep.getInstance();

    @Override
    public OperationResult run(DataStore data) {
        Step next;
        if (isSatisfied(data)) {
            next = nextIfSatisfied;
        } else {
            next = nextIfNotSatisfied;
        }
        return next.run(data);
    }

    private boolean isSatisfied(DataStore data) {
        if (allConditionsRequired) {
            return areAllSatisfied(data);
        } else {
            return isAtLeastOneSatisfied(data);
        }
    }

    private boolean isAtLeastOneSatisfied(DataStore data) {
        for (Condition condition : conditions) {
            if (condition.isSatisfied(data)) {
                return true;
            }
        }
        return false;
    }

    private boolean areAllSatisfied(DataStore data) {
        for (Condition condition : conditions) {
            if (!condition.isSatisfied(data)) {
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
    public Parameters getOutputParams() {
        return new Parameters();
    }

    @Override
    public Parameters getInputParams() {
        Parameters inputParams = new Parameters();
        for (Condition condition : conditions) {
            inputParams.addAll(condition.getInputParams());
        }
        return inputParams;
    }

    @Override
    public void dereference() {}

    @Override
    public Map<String,ProcedureDescriptor> getDependencies() {
        Map<String, ProcedureDescriptor> current = new HashMap<>();
        current.putAll(nextIfSatisfied.getDependencies());
        current.putAll(nextIfNotSatisfied.getDependencies());
        for (Condition condition : conditions) {
            current.putAll(condition.getDependencies());
        }
        return current;
    }

}
