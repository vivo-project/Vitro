package edu.cornell.mannlib.vitro.webapp.dynapi.components.conditions;

import java.util.Collections;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.ProcedureDescriptor;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConditionContainerContains implements Condition {

    private static final Log log = LogFactory.getLog(ConditionContainerContains.class);

    private Parameters inputParams = new Parameters();
    private Parameters keys = new Parameters();
    private Parameter containerParam;

    @Override
    public Parameters getInputParams() {
        return inputParams;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#targetContainer", minOccurs = 1, maxOccurs = 1)
    public void setContainer(Parameter param) throws InitializationException {
        if (!param.isJsonContainer()) {
            throw new InitializationException("Only JsonContainer parameter is allowed as a target");
        }
        inputParams.add(param);
        containerParam = param;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter", minOccurs = 1, maxOccurs = 1)
    public void addInputParameter(Parameter param) {
        inputParams.add(param);
        keys.add(param);
    }

    @Override
    public Map<String, ProcedureDescriptor> getDependencies() {
        return Collections.emptyMap();
    }

    private Data firstKeyData(DataStore dataStore) {
        return dataStore.getData(firstKeyName());
    }

    private String firstKeyName() {
        return keys.getNames().iterator().next();
    }

    @Override
    public boolean isSatisfied(DataStore dataStore) {
        if (!isValid(dataStore)) {
            throw new RuntimeException("Validation failed.");
        }
        JsonContainer container = JsonContainerView.getJsonContainer(dataStore, containerParam);
        String key = SimpleDataView.getStringRepresentation(firstKeyData(dataStore));
        return container.contains(key);
    }

    public boolean isValid() {
        if (containerParam == null) {
            log.error("container param is not set");
            return false;
        }
        if (keys.getNames().isEmpty()) {
            log.error("target param key not set");
            return false;
        }
        return true;
    }

    public boolean isValid(DataStore dataStore) {
        if (!isValid()) {
            return false;
        }
        if (dataStore == null) {
            log.error("data store is null");
            return false;
        }
        Data container = dataStore.getData(containerParam.getName());
        if (container == null) {
            log.error("container data is not provided in data store");
            return false;
        }
        if (container.getParam() == null) {
            log.error("container data param is null");
            return false;
        }
        if (!container.getParam().isJsonContainer()) {
            log.error("container data is not json container");
            return false;
        }
        for (String name : inputParams.getNames()) {
            if (!dataStore.contains(name)) {
                log.error(String.format("Input parameter '%s' is not provided in data store", name));
                return false;
            }
        }
        return true;
    }
}
