package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.ParameterSubstitution.Direction;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public class ParameterSubstitutor {

    public static void forwardSubstitution(Set<ParameterSubstitution> substitutions, DataStore dataStore) {
        for (ParameterSubstitution substitution : substitutions) {
            if (substitution.containsDirection(Direction.FORWARD)) {
                String sourceName = substitution.getSource().getName();
                if (dataStore.contains(sourceName)) {
                    substitute(substitution.getSource(), substitution.getTarget(), dataStore);
                }
            }
        }
    }

    public static void backwardSubstitution(Set<ParameterSubstitution> substitutions, DataStore dataStore) {
        for (ParameterSubstitution substitution : substitutions) {
            if (substitution.containsDirection(Direction.BACKWARD)) {
                String targetName = substitution.getTarget().getName();
                if (dataStore.contains(targetName)) {
                    substitute(substitution.getTarget(), substitution.getSource(), dataStore);
                }
            }
        }
    }

    private static void substitute(Parameter source, Parameter target, DataStore dataStore) {
        Data data = dataStore.getData(source.getName());
        data.setParam(target);
        dataStore.remove(source.getName());
        dataStore.addData(target.getName(), data);
    }

    public static Parameters inverseSubstitution(Parameters params, Set<ParameterSubstitution> substitutions,
            Direction direction) {
        Parameters result = new Parameters();
        result.addAll(params);
        for (ParameterSubstitution substitution : substitutions) {
            if (substitution.containsDirection(direction) && result.contains(substitution.getTarget())) {
                result.remove(substitution.getTarget());
                result.add(substitution.getSource());
            }
        }
        return result;
    }
}
