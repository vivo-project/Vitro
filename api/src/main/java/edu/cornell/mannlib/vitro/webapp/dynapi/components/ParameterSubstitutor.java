package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;

public class ParameterSubstitutor {

    /**
     * Makes parameter substitutions on data store
     * @param substitutions - set of substitutions to make
     * @param dataStore - data storage
     */
    public static void forwardSubstitution(Set<ParameterSubstitution> substitutions, DataStore dataStore) {
        Map<String, Data> tmpStore = Collections.emptyMap();
        for (ParameterSubstitution substitution : substitutions) {
            final Parameter target = substitution.getTarget();
            final Parameter source = substitution.getSource();
            tmpStore = substitute(dataStore, tmpStore, target, source);
        }
        appendCreatedSubstitutions(dataStore, tmpStore);
    }

    /**
     * Makes parameter substitutions on data store
     * Note that source and target are swapped to reverse substitution
     * @param substitutions - set of substitutions to make
     * @param dataStore - data storage
     */
    public static void backwardSubstitution(Set<ParameterSubstitution> substitutions, DataStore dataStore) {
        Map<String, Data> tmpStore = Collections.emptyMap();
        for (ParameterSubstitution substitution : substitutions) {
            final Parameter source = substitution.getTarget();
            final Parameter target = substitution.getSource();
            tmpStore = substitute(dataStore, tmpStore, target, source);
        }
        appendCreatedSubstitutions(dataStore, tmpStore);
    }

    private static Map<String, Data> substitute(DataStore store, Map<String, Data> tmp, Parameter target, Parameter source) {
        if (store.contains(source.getName())) {
            if (tmp.isEmpty()) {
                tmp = new HashMap<>();
            }
            substitute(source, target, store, tmp);
        }
        return tmp;
    }
    
    private static void appendCreatedSubstitutions(DataStore dataStore, Map<String, Data> substitutedDataStore) {
        for (Entry<String, Data> substitutedData : substitutedDataStore.entrySet()) {
            dataStore.addData(substitutedData.getKey(), substitutedData.getValue());
        }
    }

    private static void substitute(Parameter source, Parameter target, DataStore dataStore, Map<String, Data> substitutedDataStore) {
        Data data = dataStore.getData(source.getName());
        data.setParam(target);
        dataStore.remove(source.getName());
        substitutedDataStore.put(target.getName(), data);
    }

    public static Parameters substituteDependencies(Parameters params, Set<ParameterSubstitution> substitutions) {
        Parameters result = new Parameters();
        Set<Parameter> tmp = Collections.emptySet();
        result.addAll(params);
        for (ParameterSubstitution substitution : substitutions) {
            if (result.contains(substitution.getTarget())) {
                if (tmp.isEmpty()) {
                    tmp = new HashSet<>();
                }
                result.remove(substitution.getTarget());
                tmp.add(substitution.getSource());
            }
        }
        for (Parameter param : tmp) {
            result.add(param);
        }
        return result;
    }
}
