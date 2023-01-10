package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class LoopOperation extends Operation {

    private static final Log log = LogFactory.getLog(LoopOperation.class.getName());
    private Parameters outputParams = new Parameters();
    private Parameters inputParams = new Parameters();
    private boolean inputCalculated = false;
    private Parameters internalParams = new Parameters();
    private List<ProcedureDescriptor> inputDescriptors = new LinkedList<>();
    private List<ProcedureDescriptor> outputDescriptors = new LinkedList<>();
    private List<ProcedureDescriptor> conditionDescriptors = new LinkedList<>();
    private Map<String, ProcedureDescriptor> dependencies = new HashMap<>();
    private ProcedureDescriptor executableDescriptor;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#inputDescriptor")
    public void addInputMapping(ProcedureDescriptor pd) {
        inputDescriptors.add(pd);
        dependencies.put(pd.getUri(), pd);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#outputDescriptor")
    public void addOutputMapping(ProcedureDescriptor pd) {
        outputDescriptors.add(pd);
        dependencies.put(pd.getUri(), pd);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#conditionDescriptor", minOccurs = 1)
    public void addCondition(ProcedureDescriptor pd) {
        conditionDescriptors.add(pd);
        dependencies.put(pd.getUri(), pd);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#executableDescriptor", minOccurs = 1, maxOccurs = 1)
    public void setExecutable(ProcedureDescriptor pd) {
        executableDescriptor = pd;
        dependencies.put(pd.getUri(), pd);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addOutputParameter(Parameter param) {
        outputParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#internalParameter")
    public void addInternalParameter(Parameter param) {
        internalParams.add(param);
    }

    @Override
    public Map<String, ProcedureDescriptor> getDependencies() {
        return dependencies;
    }

    @Override
    public Parameters getOutputParams() {
        return outputParams;
    }

    public List<ProcedureDescriptor> getInputDescriptors() {
        return inputDescriptors;
    }

    public List<ProcedureDescriptor> getOutputDescriptors() {
        return outputDescriptors;
    }

    public List<ProcedureDescriptor> getConditionDescriptors() {
        return conditionDescriptors;
    }

    public Parameters getInternalParams() {
        return internalParams;
    }

    public ProcedureDescriptor getExecutableDescriptor() {
        return executableDescriptor;
    }

    @Override
    public OperationResult run(DataStore dataStore) {
        if (!isValid(dataStore)) {
            return OperationResult.internalServerError();
        }
        OperationResult result = OperationResult.ok();
        try {
            LoopOperationExecution execution = new LoopOperationExecution(dataStore, this);
            result = execution.executeLoop();
        } catch (ConversionException e) {
            log.error(e, e);
            return OperationResult.internalServerError();
        }
        return result;
    }

    @Override
    public void dereference() {
    }

    @Override
    public Parameters getInputParams() {
        if (!inputCalculated) {
            calculateInputParams();
        }
        return inputParams;
    }

    protected void calculateInputParams() {
        Parameters inputParams = new Parameters();
        Parameters inConvertersRequired = getInputConvertersRequired();
        Parameters inConvertersProvided = getInputConvertersProvided();
        Parameters outConvertersRequired = getOutputConvertersRequired();
        Parameters conditionsRequired = getConditionsRequired();

        inputParams.addAll(outConvertersRequired);
        inputParams.removeAll(executableDescriptor.getOutputParams());
        inputParams.addAll(executableDescriptor.getInputParams());
        inputParams.removeAll(inConvertersProvided);
        inputParams.addAll(inConvertersRequired);
        inputParams.addAll(conditionsRequired);
        inputParams.removeAll(internalParams);
        inputCalculated = true;
    }

    private Parameters getConditionsRequired() {
        Parameters conditionsRequired = new Parameters();
        for (ProcedureDescriptor condition : conditionDescriptors) {
            conditionsRequired.addAll(condition.getInputParams());
        }
        return conditionsRequired;
    }

    private Parameters getOutputConvertersRequired() {
        Parameters outputConvertersRequired = new Parameters();
        for (ProcedureDescriptor outputConverter : outputDescriptors) {
            outputConvertersRequired.addAll(outputConverter.getInputParams());
        }
        return outputConvertersRequired;
    }

    private Parameters getInputConvertersRequired() {
        Parameters inputConvertersRequired = new Parameters();
        for (ProcedureDescriptor inputConverter : inputDescriptors) {
            inputConvertersRequired.addAll(inputConverter.getInputParams());
        }
        return inputConvertersRequired;
    }

    private Parameters getInputConvertersProvided() {
        Parameters inputConvertersProvided = new Parameters();
        for (ProcedureDescriptor inputConverter : inputDescriptors) {
            inputConvertersProvided.addAll(inputConverter.getOutputParams());
        }
        return inputConvertersProvided;
    }

    protected boolean isValid(DataStore dataStore) {
        if (!isValid()) {
            return false;
        }
        if (!areDescriptorsValid(dataStore)) {
            return false;
        }
        return true;
    }

    public boolean isValid() {
        if (conditionDescriptors.isEmpty()) {
            return false;
        }
        if (executableDescriptor == null) {
            return false;
        }
        return true;
    }

    private boolean areDescriptorsValid(DataStore dataStore) {
        for (ProcedureDescriptor descriptor : dependencies.values()) {
            if (!isValidDescriptor(descriptor, dataStore)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidDescriptor(ProcedureDescriptor descriptor, DataStore dataStore) {
        String uri = descriptor.getUri();
        if (uri == null) {
            log.error("Uri not provided. Loop descriptor validation failed.");
            return false;
        }
        Map<String, Action> map = dataStore.getDependencies();
        if (!map.containsKey(uri)) {
            log.error(format("Dependency with uri: '%s' expected, but not provided. Loop validation failed.", uri));
            return false;
        }
        Action dependency = map.get(uri);
        if (dependency == null) {
            log.error(format("Dependency with uri: '%s' expected, but null provided. Loop validation failed.", uri));
            return false;
        }
        if (NullAction.getInstance().equals(dependency)) {
            log.error(format(
                    "Dependency with uri: '%s' expected, but default null object provided. Loop validation failed.",
                    uri));
            return false;
        }
        return true;
    }

}
