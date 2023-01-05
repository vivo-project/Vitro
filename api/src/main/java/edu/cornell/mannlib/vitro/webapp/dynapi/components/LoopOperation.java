package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class LoopOperation extends Operation{
    
    private Parameters outputParams = new Parameters();
    private Parameters inputParams = new Parameters();
    private boolean inputCalculated = false;
    private Parameters internalParams = new Parameters();
    private List<ProcedureDescriptor> inputDescriptors = new LinkedList<>();
    private List<ProcedureDescriptor> outputDescriptors = new LinkedList<>();
    private List<ProcedureDescriptor> conditionDescriptors = new LinkedList<>();
    private List<ProcedureDescriptor> dependencies = new LinkedList<>();
    private ProcedureDescriptor executableDescriptor;


    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#inputDescriptor")
    public void addInputMapping(ProcedureDescriptor pd) {
        inputDescriptors.add(pd);
        dependencies.add(pd);
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#outputDescriptor")
    public void addOutputMapping(ProcedureDescriptor pd) {
        outputDescriptors.add(pd);
        dependencies.add(pd);
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#conditionDescriptor", minOccurs = 1)
    public void addCondition(ProcedureDescriptor pd) {
        conditionDescriptors.add(pd);
        dependencies.add(pd);
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#executableDescriptor", minOccurs = 1, maxOccurs = 1)
    public void setExecutable(ProcedureDescriptor pd) {
        executableDescriptor = pd;
        dependencies.add(pd);
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addOutputParameter(Parameter param) {
        outputParams.add(param);
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
    
    public ProcedureDescriptor getExecutableDescriptor() {
        return executableDescriptor;
    }
    
    @Override
    public OperationResult run(DataStore dataStore) {
        if (!isValid(dataStore)) {
            return OperationResult.internalServerError();
        }
        DataStore localStore = new DataStore();
        initializeInternalData(localStore);
        copyDataToLocalStore(dataStore, localStore);
        LoopOperationExecution execution = new LoopOperationExecution(localStore, this);
        OperationResult result = execution.execute();
        if (result.equals(OperationResult.ok())) {
            copyDataFromLocalStore(dataStore, localStore);
        }
        return result;
    }
    
    private void initializeInternalData(DataStore localStore) {
        for (String name : internalParams.getNames()) {
            Parameter param = internalParams.get(name);
            Data data = new Data(param);
            data.initializeDefault();
            localStore.addData(name, data);
        }        
    }

    private void copyDataFromLocalStore(DataStore dataStore, DataStore localStore) {
        for (String name : outputParams.getNames()) {
            Data data = localStore.getData(name);
            dataStore.addData(name, data);
        }
    }
    
    private void copyDataToLocalStore(DataStore dataStore, DataStore localStore) {
        for (String name : inputParams.getNames()) {
            Data data = dataStore.getData(name);
            localStore.addData(name, data);
        }
    }

    @Override
    public void dereference() {}

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
        for (ProcedureDescriptor condition: conditionDescriptors) {
            conditionsRequired.addAll(condition.getInputParams());
        }
        return conditionsRequired;     
    }

    private Parameters getOutputConvertersRequired() {
        Parameters outputConvertersRequired = new Parameters(); 
        for (ProcedureDescriptor outputConverter: outputDescriptors) {
            outputConvertersRequired.addAll(outputConverter.getInputParams());
        }
        return outputConvertersRequired;
    }
    
    private Parameters getInputConvertersRequired() {
        Parameters inputConvertersRequired = new Parameters(); 
        for (ProcedureDescriptor inputConverter: inputDescriptors) {
            inputConvertersRequired.addAll(inputConverter.getInputParams());
        }
        return inputConvertersRequired;
    }
    
    private Parameters getInputConvertersProvided() {
        Parameters inputConvertersProvided = new Parameters(); 
        for (ProcedureDescriptor inputConverter: inputDescriptors) {
            inputConvertersProvided.addAll(inputConverter.getOutputParams());
        }
        return inputConvertersProvided;
    }

    protected boolean isValid(DataStore dataStore) {
        if (!isValid()) {
            return false;
        }
        //TODO: implement checks
        return true;
    }

    public boolean isValid() {
        //TODO:implement checks
        if (conditionDescriptors.isEmpty()) {
            return false;
        }
        if (executableDescriptor == null) {
            return false;
        }
        return true;
    }


}
