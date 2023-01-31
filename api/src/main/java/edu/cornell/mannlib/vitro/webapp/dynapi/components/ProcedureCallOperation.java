package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ProcedureCallOperation extends AbstractOperation {

    private static final Log log = LogFactory.getLog(ProcedureCallOperation.class.getName());
    private Parameters outputParams = new Parameters();
    private Parameters inputParams = new Parameters();
    private boolean inputCalculated = false;
    private Parameters internalParams = new Parameters();
    private Map<String, ProcedureDescriptor> dependencies = new HashMap<>();
    private ProcedureDescriptor callableDescriptor;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#executableDescriptor", minOccurs = 1, maxOccurs = 1)
    public void setExecutable(ProcedureDescriptor pd) {
        callableDescriptor = pd;
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

    public Parameters getInternalParams() {
        return internalParams;
    }

    public ProcedureDescriptor getExecutableDescriptor() {
        return callableDescriptor;
    }

    @Override
    public OperationResult run(DataStore dataStore) {
        if (!isValid(dataStore)) {
            return OperationResult.internalServerError();
        }
        DataStore localStore = new DataStore();
        try {
            ProcedureDescriptorCall.initilaizeLocalStore(dataStore, localStore, inputParams, internalParams);
            ProcedureDescriptorCall.execute(callableDescriptor, localStore);
        } catch (Exception e) {
            log.error(e, e);
            return OperationResult.internalServerError();
        }
        ProcedureDescriptorCall.copyData(localStore, dataStore, outputParams);
        return OperationResult.ok();
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
        inputParams.addAll(callableDescriptor.getInputParams());
        inputParams.removeAll(internalParams);
        inputCalculated = true;
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
        if (callableDescriptor == null) {
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
        for (String name : inputParams.getNames()) {
            if (!dataStore.contains(name)) {
                log.error(String.format("Input parameter '%s' is not provided in data store", name));
                return false;
            }
        }
        return true;
    }

    private boolean isValidDescriptor(ProcedureDescriptor descriptor, DataStore dataStore) {
        Parameter uriParam = descriptor.getUriParam();
        if (uriParam != null) {
            if (dataStore.contains(uriParam.getName())) {
                return true;
            } else {
                log.error("Callable procedure uri provided as uriParameter not found in data store.");
                return false;
            }
        }
        String uri = descriptor.getUri();
        if (uri == null) {
            log.error("Uri not provided. Loop descriptor validation failed.");
            return false;
        }
        Map<String, Procedure> map = dataStore.getDependencies();
        if (!map.containsKey(uri)) {
            log.error(format("Dependency with uri: '%s' expected, but not provided. Loop validation failed.", uri));
            return false;
        }
        Procedure dependency = map.get(uri);
        if (dependency == null) {
            log.error(format("Dependency with uri: '%s' expected, but null provided. Loop validation failed.", uri));
            return false;
        }
        if (NullProcedure.getInstance().equals(dependency)) {
            log.error(format(
                    "Dependency with uri: '%s' expected, but default null object provided. Loop validation failed.",
                    uri));
            return false;
        }
        return true;
    }

}
