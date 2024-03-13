/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.steps;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ParameterSubstitution;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ParameterSubstitutor;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.AbstractOperation;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OperationalStep implements Step {

    private static final Log log = LogFactory.getLog(OperationalStep.class);

    private AbstractOperation operation;
    private boolean optional;
    private Set<ParameterSubstitution> substitutions = Collections.emptySet();
    private Step nextStep = NullStep.getInstance();

    public OperationalStep() {
        optional = false;
        operation = null;
    }

    @Override
    public void dereference() {
        if (operation != null) {
            operation.dereference();
            operation = null;
        }
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasParameterSubstitution")
    public void addParameterSubstitution(ParameterSubstitution substitution) {
        if (substitutions.isEmpty()) {
            substitutions = new HashSet<>();
        }
        substitutions.add(substitution);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasNextStep", maxOccurs = 1)
    public void setNextStep(Step step) {
        this.nextStep = step;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasOperation", maxOccurs = 1)
    public void setOperation(AbstractOperation operation) {
        this.operation = operation;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#isOptional", maxOccurs = 1)
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public OperationResult run(DataStore data) {
        OperationResult result = OperationResult.badRequest();
        long start = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Started execution in step.");
            if (optional) {
                log.debug("Operation step is optional.");
            }
        }
        if (operation != null) {
            ParameterSubstitutor.forwardSubstitution(substitutions, data);
            result = operation.run(data);
            ParameterSubstitutor.backwardSubstitution(substitutions, data);
            if (!optional && result.hasError()) {
                return result;
            }
        }
        if (log.isDebugEnabled()) {
            long time = System.currentTimeMillis() - start;
            log.debug("Step execution time: " + time + "ms");
        }
        return nextStep.run(data);
    }

    @Override
    public Set<StepInfo> getNextNodes() {
        return Collections.singleton(nextStep);
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
        Parameters params = operation.getInputParams();
        if (substitutions.isEmpty()) {
            return params;
        } else {
            return ParameterSubstitutor.substituteDependencies(params, substitutions);
        }
    }

    @Override
    public Parameters getOutputParams() {
        Parameters params = operation.getOutputParams();
        if (substitutions.isEmpty()) {
            return params;
        } else {
            return ParameterSubstitutor.substituteDependencies(params, substitutions);
        }
    }

    @Override
    public Map<String, ProcedureDescriptor> getDependencies() {
        Map<String, ProcedureDescriptor> next = nextStep.getDependencies();
        Map<String, ProcedureDescriptor> current = operation.getDependencies();
        if (next.isEmpty() && current.isEmpty()) {
            return Collections.emptyMap();
        }
        if (!next.isEmpty() && current.isEmpty()) {
            return next;
        }
        if (next.isEmpty() && !current.isEmpty()) {
            return current;
        }
        Map<String, ProcedureDescriptor> unionDependencies = new HashMap<>();
        unionDependencies.putAll(current);
        unionDependencies.putAll(next);
        return unionDependencies;
    }

}
