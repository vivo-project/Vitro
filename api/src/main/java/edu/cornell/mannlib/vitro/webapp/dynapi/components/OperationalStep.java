package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.computation.StepInfo;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class OperationalStep implements Step {

    private static final Log log = LogFactory.getLog(OperationalStep.class);

    private Operation operation;
    private boolean optional;
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

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasNextStep", maxOccurs = 1)
    public void setNextStep(Step step) {
        this.nextStep = step;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasOperation", maxOccurs = 1)
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#isOptional", maxOccurs = 1)
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public OperationResult run(OperationData data) {
        OperationResult result = OperationResult.badRequest();
        log.debug("Processing in STEP");
        log.debug("Execution step is optional? " + optional);
        if (operation != null) {
            log.debug("Operation not null");
            result = operation.run(data);
            if (!optional && result.hasError()) {
                return result;
            }
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
    public Parameters getRequiredParams() {
        return operation.getRequiredParams();
    }

    @Override
    public Parameters getProvidedParams() {
        return operation.getProvidedParams();
    }

}
