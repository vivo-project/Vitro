/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonFactory;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContainerQuery extends AbstractOperation {

    private static final Log log = LogFactory.getLog(ContainerQuery.class);

    private Parameters keys = new Parameters();
    private Parameter containerParam;
    private Parameter outputParam;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter", minOccurs = 1, maxOccurs = 1)
    public void addOutputParameter(Parameter param) {
        outputParams.add(param);
        outputParam = param;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter", minOccurs = 1, maxOccurs = 1)
    public void addInputParameter(Parameter param) {
        inputParams.add(param);
        keys.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#targetContainer", minOccurs = 1, maxOccurs = 1)
    public void setContainer(Parameter param) throws InitializationException {
        if (!param.isJsonContainer()) {
            throw new InitializationException("only JsonContainer parameter is allowed as a target");
        }
        inputParams.add(param);
        containerParam = param;
    }

    @Override
    public OperationResult runOperation(DataStore dataStore) {
        JsonContainer container = JsonContainerView.getJsonContainer(dataStore, containerParam);
        String key = SimpleDataView.getStringRepresentation(firstKeyData(dataStore));
        Data item = container.getItem(key, outputParam);
        if (!item.isInitialized()) {
            log.error(String.format("Key '%s' not found in container %s", key, JsonFactory.serialize(container)));
            return OperationResult.internalServerError();
        }
        Parameter itemParam = item.getParam();
        if (!outputParam.equals(itemParam)) {
            log.error("returned result has different parameter");
            return OperationResult.internalServerError();
        }
        dataStore.addData(itemParam.getName(), item);
        return OperationResult.ok();
    }

    private Data firstKeyData(DataStore dataStore) {
        return dataStore.getData(firstKeyName());
    }

    private String firstKeyName() {
        return keys.getNames().iterator().next();
    }

    public boolean isValid() {
        if (outputParam == null) {
            log.error("output parameter is not set");
            return false;
        }
        if (containerParam == null) {
            log.error("container param is not set");
            return false;
        }
        if (!containerParam.isJsonContainer()) {
            log.error("container param is not JsonContainer");
            return false;
        }
        if (keys.getNames().isEmpty()) {
            log.error("target param key not set");
            return false;
        }
        return true;
    }

    public boolean isInputValid(DataStore dataStore) {
        if (!super.isInputValid(dataStore)) {
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
