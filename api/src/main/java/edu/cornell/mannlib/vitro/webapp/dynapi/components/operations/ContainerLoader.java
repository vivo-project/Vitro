/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.components.operations;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.OperationResult;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.JsonContainerView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.SimpleDataView;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JsonContainer;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.StringUtils;

public class ContainerLoader extends AbstractOperation {

    private static final Log log = LogFactory.getLog(ContainerLoader.class);

    private Parameter containerParam;
    private Parameter paramToLoad;
    private String key;
    private Parameter keyParameter;

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter", minOccurs = 1, maxOccurs = 1)
    public void addInputParameter(Parameter param) {
        inputParams.add(param);
        paramToLoad = param;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#targetContainer", minOccurs = 1, maxOccurs = 1)
    public void setContainer(Parameter param) throws InitializationException {
        if (!param.isJsonContainer()) {
            throw new InitializationException("only JsonContainer parameter is allowed as a target");
        }
        inputParams.add(param);
        containerParam = param;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#key", maxOccurs = 1)
    public void setKey(String key) {
        this.key = key;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#keyParameter", maxOccurs = 1)
    public void setKeyParameter(Parameter keyParameter) {
        inputParams.add(keyParameter);
        this.keyParameter = keyParameter;
    }

    @Override
    public OperationResult runOperation(DataStore dataStore) {
        JsonContainer container = JsonContainerView.getJsonContainer(dataStore, containerParam);
        Data input = dataStore.getData(getLoadParamName());
        if (StringUtils.isEmpty(key) && keyParameter == null) {
            container.addValue(input);
        } else {
            addKeyValue(container, input, dataStore);
        }
        return OperationResult.ok();
    }

    private void addKeyValue(JsonContainer container, Data input, DataStore dataStore) {
        if (!StringUtils.isEmpty(key)) {
            container.addKeyValue(key, input);
        } else {
            Data keyData = dataStore.getData(keyParameter.getName());
            String tmpKey = SimpleDataView.getStringRepresentation(keyData);
            container.addKeyValue(tmpKey, input);
        }
    }

    private String getLoadParamName() {
        return paramToLoad.getName();
    }

    public boolean isValid() {
        if (paramToLoad == null) {
            log.error("parameter to load into container is not set");
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
        if (dataStore.getData(getLoadParamName()) == null) {
            log.error("data to load is not provided in data store");
            return false;
        }
        return true;
    }
}
